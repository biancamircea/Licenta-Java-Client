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

            if (constraint.getIsConfidential()==1 && contextValueOpt.isPresent()) {
                try {
                    Integer value = Integer.parseInt(contextValueOpt.get());
                    Integer threshold = Integer.parseInt(constraint.getValues().get(0));
                    Integer operation = getOperationCode(constraint.getOperator());

                    ZKPGenerator zkpGenerator = new ZKPGenerator();

                    JsonObject proofJson = zkpGenerator.generateProof(value, threshold, operation);

                    zkProofs.add(new ZKPProof(contextKey, proofJson));


                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (contextValueOpt.isPresent()) {
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
