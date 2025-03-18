package ro.mta.sdk.evaluator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ro.mta.sdk.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
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
            payloadCache.put(payloadCacheKey, "NO_PAYLOAD_DEFINED");
        }
    }
    @Override
    public boolean remoteEvalution(String toggleName, ToggleSystemContext systemContext, boolean defaultSetting) {
        String cacheKey = toggleName + "_" + systemContext.getAllContextFieldsToString();
        Boolean cachedValue = featureFlagCache.getIfPresent(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        FeatureEvaluationRequest featureEvaluationRequest = new FeatureEvaluationRequest(toggleName,
                systemContext.getAllContextFields());
        FeatureEvaluationResponse featureEvaluationResponse = evaluatorSender.evaluateToggle(featureEvaluationRequest);
        if(featureEvaluationResponse.getStatus().equals(FeatureEvaluationResponse.Status.SUCCESS)){
            cacheResponse(toggleName, systemContext, featureEvaluationResponse);
            return featureEvaluationResponse.getEnabled();
        } else {
            return defaultSetting;
        }
    }

    @Override
    public boolean remoteEvalutionWithZKP(String toggleName, ToggleSystemContext systemContext) {
        ConstraintResponse response = evaluatorSender.fetchConstraints(toggleSystemConfig.getApiKey(), toggleName);
        if (response != null) {
            System.out.println("Received constraints: " + response.getConstraints());
        }

        Long age =Long.parseLong(systemContext.getPropertyByName("age").orElseThrow());
        Long threshold = Long.parseLong(response.getValuesForContext("age").get(0));

        try {
            ZKPGenerator zkpGenerator = new ZKPGenerator();
            String proofJson = zkpGenerator.generateProof(age.intValue(), threshold.intValue());

            System.out.println("Generated Proof: " + proofJson);

            //de trimis la server

            //de primit on/off de la server

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String remotePayload(String toggleName, Boolean enabled, ToggleSystemContext systemContext, String defaultPayload) {
        String cacheKey = toggleName + "_" + enabled.toString();
        String cachedValue = payloadCache.getIfPresent(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }
        FeatureEvaluationRequest featureEvaluationRequest = new FeatureEvaluationRequest(toggleName,
                systemContext.getAllContextFields());
        FeatureEvaluationResponse featureEvaluationResponse = evaluatorSender.evaluateToggle(featureEvaluationRequest);
        if(featureEvaluationResponse.getStatus().equals(FeatureEvaluationResponse.Status.SUCCESS)){
            cacheResponse(toggleName, systemContext, featureEvaluationResponse);
            if(featureEvaluationResponse.getPayload() == null){
                return defaultPayload;
            } else {
                return featureEvaluationResponse.getPayload();
            }
        } else {
            return defaultPayload;
        }
    }
}
