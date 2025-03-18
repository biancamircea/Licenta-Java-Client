package ro.mta.sdk.evaluator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mta.sdk.*;
import ro.mta.sdk.repository.HttpToggleFetcher;
import ro.mta.sdk.repository.JsonToggleParser;
import ro.mta.sdk.repository.ToggleCollection;
import ro.mta.sdk.repository.ToggleResponse;
import ro.mta.sdk.util.DateTimeSerializer;
import ro.mta.sdk.util.ToggleSystemURL;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HttpEvaluatorSender implements EvaluatorSender{
    private static final Logger LOG = LoggerFactory.getLogger(HttpEvaluatorSender.class);
    private static final int CONNECT_TIMEOUT = 10000;
    private final Gson gson;
    private ToggleSystemConfig toggleSystemConfig;
    private final URL clientEvaluationURL;
    private final URL constraintsURL;

    public HttpEvaluatorSender(ToggleSystemConfig systemConfig){
        this.toggleSystemConfig = systemConfig;
        ToggleSystemURL urls = systemConfig.getToggleSystemURL();
        this.clientEvaluationURL = urls.getEvaluateToggleURL();
        this.gson = new GsonBuilder().create();
        this.constraintsURL = urls.getConstraintsURL();
    }



    @Override
    public FeatureEvaluationResponse evaluateToggle(FeatureEvaluationRequest featureEvaluationRequest) {
        try {
            return post(this.clientEvaluationURL, featureEvaluationRequest);
        } catch (ToggleSystemException ex) {
            return new FeatureEvaluationResponse(null, FeatureEvaluationResponse.Status.ERROR);
        }
    }

    public ConstraintResponse fetchConstraints(String apiToken, String toggleName) throws ToggleSystemException {
        HttpURLConnection connection = null;
        try {
            connection = openConnection(this.constraintsURL);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiToken);
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(toggleName);
                writer.flush();
            }

            return getConstraintsResponse(connection);
        } catch (IOException e) {
            throw new ToggleSystemException("Error fetching constraints", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
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

    private ConstraintResponse getConstraintsResponse(HttpURLConnection request) throws IOException {
        int responseCode = request.getResponseCode();
        if (responseCode < 300) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
                // Deserializare directă într-o listă de obiecte ConstraintDTO
                List<ConstraintDTO> constraints = gson.fromJson(reader, new TypeToken<List<ConstraintDTO>>(){}.getType());

                // Creare și setare răspunsul
                ConstraintResponse response = new ConstraintResponse();
                response.setConstraints(constraints);
                return response;
            }
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            LOG.warn("Constraints not found.");
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            LOG.warn("Invalid API token.");
        }
        return new ConstraintResponse();
    }



    private String readErrorStream(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
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
