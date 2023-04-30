package ro.mta.sdk.repository;

import ro.mta.sdk.ToggleSystemException;

public interface ToggleFetcher {
    ToggleResponse fetchToggles() throws ToggleSystemException;
}
