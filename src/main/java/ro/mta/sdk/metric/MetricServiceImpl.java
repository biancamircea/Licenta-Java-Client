package ro.mta.sdk.metric;

import ro.mta.sdk.ToggleSystemConfig;
import ro.mta.sdk.util.ToggleSystemExecutor;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class MetricServiceImpl implements MetricService{
    private final LocalDateTime started;
    private final ToggleSystemConfig toggleSystemConfig;
    private final MetricSender metricSender;

    public MetricServiceImpl(
            ToggleSystemConfig systemConfig) {
        this(systemConfig, new HttpMetricSender(systemConfig));
    }

    public MetricServiceImpl(
            ToggleSystemConfig systemConfig,
            MetricSender metricSender) {
        this.started = LocalDateTime.now();
        this.toggleSystemConfig = systemConfig;
        this.metricSender = metricSender;
    }

    @Override
    public void register() {
        ClientRegistration registration =
                new ClientRegistration(toggleSystemConfig, started);
        metricSender.registerClient(registration);
    }
}
