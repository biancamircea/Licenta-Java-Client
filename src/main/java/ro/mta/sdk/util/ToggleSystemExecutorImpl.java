package ro.mta.sdk.util;

import javax.annotation.Nullable;
import java.util.concurrent.*;

public class ToggleSystemExecutorImpl implements ToggleSystemExecutor{
    @Nullable private static ToggleSystemExecutorImpl INSTANCE;

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final ExecutorService executorService;

    public ToggleSystemExecutorImpl() {
        ThreadFactory threadFactory =
                runnable -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setName("toggle-system-executor");
                    thread.setDaemon(true);
                    return thread;
                };

        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, threadFactory);
        this.scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);

        this.executorService = Executors.newSingleThreadExecutor(threadFactory);
    }

    public static synchronized ToggleSystemExecutorImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ToggleSystemExecutorImpl();
        }
        return INSTANCE;
    }
    @Nullable
    @Override
    public ScheduledFuture setInterval(Runnable command, long initialDelaySec, long periodSec) throws RejectedExecutionException {
        try {
            return scheduledThreadPoolExecutor.scheduleAtFixedRate(
                    command, initialDelaySec, periodSec, TimeUnit.SECONDS);
        } catch (RejectedExecutionException ex) {
//            LOG.error("Unleash background task crashed", ex);
            return null;
        }
    }

    @Override
    public Future<Void> scheduleOnce(Runnable runnable) {
        return (Future<Void>) executorService.submit(runnable);
    }

    @Override
    public void shutdown() {
        this.scheduledThreadPoolExecutor.shutdown();
    }
}
