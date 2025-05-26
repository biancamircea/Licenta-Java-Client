package ro.mta.sdk.evaluator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import ro.mta.sdk.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EvaluatorServiceImpl implements EvaluatorService {
    private final ToggleSystemConfig toggleSystemConfig;
    private final Cache<String, Boolean> featureFlagCache;
    private final Cache<String, String> payloadCache;
    private final EvaluatorSender evaluatorSender;

    public EvaluatorServiceImpl(ToggleSystemConfig toggleSystemConfig) {
        this(toggleSystemConfig, new HttpEvaluatorSender(toggleSystemConfig));
    }

    public EvaluatorServiceImpl(ToggleSystemConfig toggleSystemConfig,
                                EvaluatorSender evaluatorSender) {
        this.toggleSystemConfig = toggleSystemConfig;
        this.evaluatorSender = evaluatorSender;
        this.featureFlagCache = Caffeine.newBuilder()
                .expireAfterWrite(toggleSystemConfig.getCacheTimeout(), TimeUnit.SECONDS)
                .maximumSize(1000)
                .build();
        this.payloadCache = Caffeine.newBuilder()
                .expireAfterWrite(toggleSystemConfig.getCacheTimeout(), TimeUnit.SECONDS)
                .maximumSize(1000)
                .build();
    }

    private void cacheResponse(String toggleName, ToggleSystemContext systemContext, FeatureEvaluationResponse featureEvaluationResponse){
        String featureFlagCacheKey = toggleName + "_" + systemContext.getAllContextFieldsToString();
        String payloadCacheKey = toggleName + "_" + featureEvaluationResponse.getEnabled().toString();
        featureFlagCache.put(featureFlagCacheKey, featureEvaluationResponse.getEnabled());
        if(featureEvaluationResponse.getPayload() != null){
            payloadCache.put(payloadCacheKey, featureEvaluationResponse.getPayload());
        } else {
            payloadCache.put(payloadCacheKey, "default");
        }
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private FeatureEvaluationRequest getFeatureEvaluationRequest(String toggleName, ToggleSystemContext systemContext){
        ConstraintResponse response = evaluatorSender.fetchConstraints(toggleSystemConfig.getApiKey(), toggleName);
        if (response == null) {
            return null;
        }
        if(response.getConstraints() == null){
            response.setConstraints(new ArrayList<>());
        }

        List<ZKPProof> zkProofs = new ArrayList<>();
        List<ContextField> nonConfidentialContext = new ArrayList<>();

        for (ConstraintDTO constraint : response.getConstraints()) {
            String contextKey = constraint.getContextName();
            Optional<String> contextValueOpt = systemContext.getPropertyByName(contextKey);
            String proofName = contextKey + constraint.getConstrGroupId();


            if (constraint.getIsConfidential() == 1 && contextValueOpt.isPresent()) {
                String valueStr = contextValueOpt.get();
                String thresholdStr = constraint.getValues().get(0);

                if (!isInteger(valueStr) || !isInteger(thresholdStr)) {
                    continue;
                }

                Integer value = Integer.parseInt(valueStr);
                Integer threshold = Integer.parseInt(thresholdStr);
                Integer operation = getOperationCode(constraint.getOperator());

                try {
                    ZKPGenerator zkpGenerator = new ZKPGenerator();
                    JsonObject proofJson = zkpGenerator.generateProof(value, threshold, operation);
                    zkProofs.add(new ZKPProof(proofName, proofJson, "normal"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(constraint.getIsConfidential()==2 && systemContext.hasLocation()) {
                try {
                    Double lat = systemContext.getLocationY().orElse(0.0);
                    Double lng = systemContext.getLocationX().orElse(0.0);

                    String rawConstraint = constraint.getValues().get(0); // "22:11:2"
                    String[] parts = rawConstraint.split(":");

                    Double lngAdmin = Double.parseDouble(parts[0]); // 22
                    Double latAdmin = Double.parseDouble(parts[1]); // 11
                    Integer marginCode = Integer.parseInt(parts[2]);

                    ZKPGenerator zkpGenerator = new ZKPGenerator();

                    JsonObject proofJson = zkpGenerator.generateProof(lng,lat, lngAdmin, latAdmin, marginCode);

                    zkProofs.add(new ZKPProof(proofName, proofJson, "location"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (contextValueOpt.isPresent()) {
                boolean exists = nonConfidentialContext.stream()
                        .anyMatch(cf -> cf.getName().equals(contextKey));
                if (!exists) {
                    ContextField cf = new ContextField(contextKey, contextValueOpt.get());
                    nonConfidentialContext.add(cf);
                }
            }

        }

        return new FeatureEvaluationRequest(nonConfidentialContext, zkProofs);
    }

    @Override
    public boolean remoteEvalutionWithZKP(String toggleName, ToggleSystemContext systemContext) {
        String cacheKey = toggleName + "_" + systemContext.getAllContextFieldsToString();
        Boolean cachedValue = featureFlagCache.getIfPresent(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        FeatureEvaluationRequest request = getFeatureEvaluationRequest(toggleName, systemContext);
        if(request==null){
            return false;
        }

        List<ContextField> contextFields = request.getContextFields() != null
                ? request.getContextFields()
                : Collections.emptyList();

        FeatureEvaluationResponse evaluationResponse=evaluatorSender.sendZKPVerificationRequest(toggleName, toggleSystemConfig.getApiKey(),
                contextFields, request.getZkpProofs());

        if(evaluationResponse==null){
            return false;
        }
        if(evaluationResponse.getEnabled()!=null){
            cacheResponse(toggleName, systemContext, evaluationResponse);
            return evaluationResponse.getEnabled();
        } else {
            return false;
        }
    }

    private int getOperationCode(String operation) {
        return switch (operation) {
            case "GREATER_THAN" -> 1;
            case "LESS_THAN" -> 0;
            case "IN" -> 2;
            case "NOT_IN" -> 3;
            default -> -1;
        };
    }

    @Override
    public String remotePayload(String toggleName, Boolean enabled, ToggleSystemContext systemContext, String defaultPayload) {
        String cacheKey = toggleName + "_" + enabled.toString();
        String cachedValue = payloadCache.getIfPresent(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        FeatureEvaluationRequest featureEvaluationRequest = getFeatureEvaluationRequest(toggleName, systemContext);
        if(featureEvaluationRequest==null){
            return defaultPayload;
        }

        List<ContextField> contextFields = featureEvaluationRequest.getContextFields() != null
                ? featureEvaluationRequest.getContextFields()
                : Collections.emptyList();
        FeatureEvaluationResponse evaluationResponse = evaluatorSender.sendZKPVerificationRequest(toggleName, toggleSystemConfig.getApiKey(), contextFields, featureEvaluationRequest.getZkpProofs());
       if(evaluationResponse == null){
            return defaultPayload;
        }

        if (evaluationResponse.getEnabled() != null) {
            cacheResponse(toggleName, systemContext, evaluationResponse);
            if (evaluationResponse.getPayload() != null) {
                return evaluationResponse.getPayload();
            }
        } else {
            return defaultPayload;
        }

        return defaultPayload;
    }
}
