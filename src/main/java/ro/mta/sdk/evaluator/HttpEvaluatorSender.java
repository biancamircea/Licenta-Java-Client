package ro.mta.sdk.evaluator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mta.sdk.ToggleSystemConfig;
import ro.mta.sdk.ToggleSystemException;
import ro.mta.sdk.repository.HttpToggleFetcher;
import ro.mta.sdk.repository.JsonToggleParser;
import ro.mta.sdk.repository.ToggleCollection;
import ro.mta.sdk.repository.ToggleResponse;
import ro.mta.sdk.util.DateTimeSerializer;
import ro.mta.sdk.util.ToggleSystemURL;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class HttpEvaluatorSender implements EvaluatorSender{
    private static final Logger LOG = LoggerFactory.getLogger(HttpEvaluatorSender.class);
    private static final int CONNECT_TIMEOUT = 10000;
    private final Gson gson;
    private ToggleSystemConfig toggleSystemConfig;
    private final URL clientEvaluationURL;
    public HttpEvaluatorSender(ToggleSystemConfig systemConfig){
        this.toggleSystemConfig = systemConfig;
        ToggleSystemURL urls = systemConfig.getToggleSystemURL();
        this.clientEvaluationURL = urls.getEvaluateToggleURL();
        this.gson = new GsonBuilder().create();
    }



    @Override
    public FeatureEvaluationResponse evaluateToggle(FeatureEvaluationRequest featureEvaluationRequest) {
        try {
            return post(this.clientEvaluationURL, featureEvaluationRequest);
        } catch (ToggleSystemException ex) {
            return new FeatureEvaluationResponse(null, FeatureEvaluationResponse.Status.ERROR);
        }
    }

    private FeatureEvaluationResponse post(URL url, Object o) throws ToggleSystemException {
        HttpURLConnection connection = null;
        try {
            connection = openConnection(url);

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            gson.toJson(o, wr);
            wr.flush();
            wr.close();
            connection.connect();

            return getEvaluateResponse(connection);
        } catch (IOException e) {
            throw new ToggleSystemException("Could not post to Unleash API", e);
        } catch (IllegalStateException e) {
            throw new ToggleSystemException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    private FeatureEvaluationResponse getEvaluateResponse(HttpURLConnection request) throws IOException {
        int responseCode = request.getResponseCode();
        if (responseCode < 300) {
            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         (InputStream) request.getContent(), StandardCharsets.UTF_8))) {

//                TODO: get reponse
                FeatureEvaluationResponse featureEvaluationResponse = gson.fromJson(reader, FeatureEvaluationResponse.class);
                featureEvaluationResponse.setStatus(FeatureEvaluationResponse.Status.SUCCESS);
                return featureEvaluationResponse;
            }
        } else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            LOG.warn("Feature toggle not found.");
        }
        else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            LOG.warn("Api Token not valid.");
        }
        return new FeatureEvaluationResponse(null, FeatureEvaluationResponse.Status.ERROR);
    }

    private HttpURLConnection openConnection(URL url) throws IOException {
        HttpURLConnection connection;
        connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(CONNECT_TIMEOUT);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");

        ToggleSystemConfig.setRequestProperties(connection, this.toggleSystemConfig);

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }
}
