package ro.mta.sdk.metric;

import ro.mta.sdk.ToggleSystemConfig;

import java.time.LocalDateTime;

public class ClientRegistration {
    private final String appName;
    private final String instanceId;
//    private final String sdkVersion;
    private final LocalDateTime started;
    private final String environment;

    ClientRegistration(ToggleSystemConfig config, LocalDateTime started) {
        this.environment = config.getEnvironment();
        this.appName = config.getAppName();
        this.instanceId = config.getInstanceId();
//        this.sdkVersion = config.getSdkVersion();
        this.started = started;
    }

    public String getAppName() {
        return appName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public LocalDateTime getStarted() {
        return started;
    }

    public String getEnvironment() {
        return environment;
    }

    @Override
    public String toString() {
        return "Client Registration:" +
                "appName='" + appName + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", started=" + started +
                ", environment='" + environment + '\'' +
                '}';
    }
}
