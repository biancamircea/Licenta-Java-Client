package ro.mta.sdk.metric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ro.mta.sdk.ToggleSystemConfig;
import ro.mta.sdk.ToggleSystemException;
import ro.mta.sdk.util.DateTimeSerializer;
import ro.mta.sdk.util.ToggleSystemURL;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

public class HttpMetricSender implements MetricSender{

    private static final int CONNECT_TIMEOUT = 10000;

    private final Gson gson;
    private ToggleSystemConfig toggleSystemConfig;
    private final URL clientRegistrationURL;

    public HttpMetricSender(ToggleSystemConfig systemConfig) {
        this.toggleSystemConfig = systemConfig;
        ToggleSystemURL urls = systemConfig.getToggleSystemURL();
        this.clientRegistrationURL = urls.getClientRegisterURL();

        this.gson =
                new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new DateTimeSerializer())
                        .create();
    }

    @Override
    public void registerClient(ClientRegistration registration) {
        try {
            int response = post(clientRegistrationURL, registration);
        } catch (ToggleSystemException ex) {
        }
    }

    private int post(URL url, Object o) throws ToggleSystemException {

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(CONNECT_TIMEOUT);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            toggleSystemConfig.setRequestProperties(connection, this.toggleSystemConfig);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            gson.toJson(o, wr);
            wr.flush();
            wr.close();

            connection.connect();

            // TODO should probably check response code to detect errors?
            return connection.getResponseCode();
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
}
