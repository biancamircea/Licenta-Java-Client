package ro.mta.sdk.util;

import javax.annotation.Nullable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;

public interface ToggleSystemExecutor {
    @Nullable
    ScheduledFuture setInterval(Runnable command, long initialDelaySec, long periodSec)
            throws RejectedExecutionException;

    Future<Void> scheduleOnce(Runnable runnable);

    public default void shutdown() {}
}
