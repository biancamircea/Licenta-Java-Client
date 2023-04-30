package ro.mta.sdk.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class ToggleSystemURL {
    private final URL fetchTogglesURL;

    public ToggleSystemURL(URI toggleSystemAPI) {
        try {
            String toggleSystemAPIstr = toggleSystemAPI.toString();
            fetchTogglesURL = URI.create(toggleSystemAPIstr + "/client/features").normalize().toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Unleash API is not a valid URL: " + toggleSystemAPI);
        }
    }
    public URL getFetchTogglesURL() {
        return fetchTogglesURL;
    }
}
