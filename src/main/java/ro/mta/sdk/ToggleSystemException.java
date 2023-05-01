package ro.mta.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.mta.sdk.repository.HttpToggleFetcher;

import javax.annotation.Nullable;

public class ToggleSystemException extends RuntimeException{
    private static final Logger LOG = LoggerFactory.getLogger(ToggleSystemException.class);
    public ToggleSystemException(String message, @Nullable Throwable cause) {
        super(message, cause);
        LOG.warn(message);
    }
}
