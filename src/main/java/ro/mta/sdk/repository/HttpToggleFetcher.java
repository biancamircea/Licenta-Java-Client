package ro.mta.sdk.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mta.sdk.ToggleSystemConfig;
import ro.mta.sdk.ToggleSystemException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpToggleFetcher implements ToggleFetcher{
    private static final Logger LOG = LoggerFactory.getLogger(HttpToggleFetcher.class);
    private static final int CONNECT_TIMEOUT = 10000;
    private final ToggleSystemConfig config;
    private final URL toggleUrl;

    public HttpToggleFetcher(ToggleSystemConfig config) {
        this.config = config;
        this.toggleUrl =
                config.getToggleSystemURL().getFetchTogglesURL();
    }

    @Override
    public ToggleResponse fetchToggles() throws ToggleSystemException {
        HttpURLConnection connection = null;
        try {
            connection = openConnection(this.toggleUrl);
            connection.connect();

            return getToggleResponse(connection);
        } catch (IOException e) {
            throw new ToggleSystemException("Could not fetch toggles", e);
        } catch (IllegalStateException e) {
            throw new ToggleSystemException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private ToggleResponse getToggleResponse(HttpURLConnection request) throws IOException {
        int responseCode = request.getResponseCode();
        if (responseCode < 300) {

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(
                                         (InputStream) request.getContent(), StandardCharsets.UTF_8))) {

                ToggleCollection toggles = JsonToggleParser.fromJson(reader);
                return new ToggleResponse(
                        ToggleResponse.Status.CHANGED,
                        toggles);
            }
        }  else if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
            return new ToggleResponse(
                    ToggleResponse.Status.NOT_CHANGED, responseCode);
        } else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            LOG.warn("Api Token not valid.");
        }
        return new ToggleResponse(
                ToggleResponse.Status.UNAVAILABLE,
                responseCode,
                getLocationHeader(request));
    }



    private String getLocationHeader(HttpURLConnection connection) {
        return connection.getHeaderField("Location");
    }

    private HttpURLConnection openConnection(URL url) throws IOException {
        HttpURLConnection connection;
        connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(CONNECT_TIMEOUT);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");

        ToggleSystemConfig.setRequestProperties(connection, this.config);

        connection.setUseCaches(true);
        return connection;
    }
}
