package ro.mta.sdk;

import ro.mta.sdk.util.ToggleSystemExecutor;
import ro.mta.sdk.util.ToggleSystemExecutorImpl;
import ro.mta.sdk.util.ToggleSystemURL;

import javax.annotation.Nullable;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class ToggleSystemConfig {
    public static final String TOGGLE_SYSTEM_APP_NAME_HEADER = "TOGGLE-SYSTEM-APPNAME";
    public static final String TOGGLE_SYSTEM_INSTANCE_ID_HEADER = "TOGGLE-SYSTEM-INSTANCEID";

    private final URI toggleServerAPI;
    private final ToggleSystemURL toggleSystemURL;
    private final String apiKey;
    private final String appName;
    private final String environment;
    private final String instanceId;
    private final int pollingInterval; // Default polling interval in seconds
    private final long cacheTimeout;
    private String backupFilePath;
    private final boolean remoteEvaluation;
    private final boolean synchronousFetchOnInitialisation;
    private final ToggleSystemContextProvider toggleSystemContextProvider;
    private final ToggleSystemExecutor toggleSystemExecutor;

    private ToggleSystemConfig(
            URI toggleServerAPI,
            String apiKey,
            String appName,
            String environment,
            String instanceId,
            boolean remoteEvaluation,
            boolean synchronousFetchOnInitialisation,
            int pollingInterval,
            long cacheTimeout,
            String backupFilePath,
            ToggleSystemContextProvider toggleSystemContextProvider,
            ToggleSystemExecutor toggleSystemExecutor
    ) {
        if (appName == null) {
            throw new IllegalStateException("You are required to specify the unleash appName");
        }
        if (instanceId == null) {
            throw new IllegalStateException("You are required to specify the unleash instanceId");
        }
        if (toggleServerAPI == null) {
            throw new IllegalStateException("You are required to specify the unleashAPI url");
        }
        if (toggleSystemExecutor == null) {
            throw new IllegalStateException("You are required to specify a scheduler");
        }

        this.toggleServerAPI = toggleServerAPI;
        this.toggleSystemURL = new ToggleSystemURL(toggleServerAPI);
        this.apiKey = apiKey;
        this.appName = appName;
        this.environment = environment;
        this.instanceId = instanceId;
        this.remoteEvaluation = remoteEvaluation;
        this.synchronousFetchOnInitialisation = synchronousFetchOnInitialisation;
        this.pollingInterval = pollingInterval;
        this.cacheTimeout = cacheTimeout;
        this.backupFilePath = backupFilePath;
        this.toggleSystemContextProvider = toggleSystemContextProvider;
        this.toggleSystemExecutor = toggleSystemExecutor;

    }

    public URI getToggleServerAPI() {
        return toggleServerAPI;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAppName() {
        return appName;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public long getCacheTimeout() {
        return cacheTimeout;
    }

    public String getBackupFilePath() {
        return backupFilePath;
    }

    public ToggleSystemContextProvider getToggleSystemContextProvider() {
        return toggleSystemContextProvider;
    }

    public ToggleSystemExecutor getToggleSystemExecutor() {
        return toggleSystemExecutor;
    }

    public ToggleSystemURL getToggleSystemURL() {
        return toggleSystemURL;
    }

    public boolean isRemoteEvaluation() {
        return remoteEvaluation;
    }

    public boolean isSynchronousFetchOnInitialisation() {
        return synchronousFetchOnInitialisation;
    }

    public static Builder builder(){
        return new Builder();
    }

    public static void setRequestProperties(HttpURLConnection connection, ToggleSystemConfig config) {
        connection.setRequestProperty(TOGGLE_SYSTEM_APP_NAME_HEADER, config.getAppName());
        connection.setRequestProperty(TOGGLE_SYSTEM_INSTANCE_ID_HEADER, config.getInstanceId());

        connection.setRequestProperty("User-Agent", config.getAppName());
        connection.setRequestProperty("Authorization", config.getApiKey());
    }
    public static class Builder {
        private URI toggleServerAPI;
        private String apiKey;
        private @Nullable String appName = "default";
        private String environment = "default";
        private String instanceId = getDefaultInstanceId();
        private int pollingInterval = 60; // Default polling interval in seconds
        private @Nullable long cacheTimeout = 60;
        private @Nullable String backupFilePath; // Default backup file path
        private boolean remoteEvaluation = false;
        private boolean synchronousFetchOnInitialisation = false;
        private ToggleSystemContextProvider toggleSystemContextProvider = ToggleSystemContextProvider.getDefaultProvider();
        private ToggleSystemExecutor toggleSystemExecutor;

//        SETTER
        public Builder toggleServerAPI(URI toggleServerAPI) {
            this.toggleServerAPI = toggleServerAPI;
            return this;
        }
        public Builder toggleServerAPI(String toggleServerAPI) {
            this.toggleServerAPI = URI.create(toggleServerAPI);
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder appName(@Nullable String appName) {
            this.appName = appName;
            return this;
        }

        public Builder remoteEvaluation(boolean remoteEvaluation) {
            this.remoteEvaluation = remoteEvaluation;
            return this;
        }
        public Builder synchronousFetchOnInitialisation(boolean enable) {
            this.synchronousFetchOnInitialisation = enable;
            return this;
        }

        public Builder pollingInterval(int pollingInterval) {
            this.pollingInterval = pollingInterval;
            return this;
        }

        public Builder cacheTimeout(long cacheTimeout) {
            this.cacheTimeout = cacheTimeout;
            return this;
        }

        public Builder backupFilePath(@Nullable String backupFilePath) {
            this.backupFilePath = backupFilePath;
            return this;
        }

        public Builder toggleSystemContextProvider(ToggleSystemContextProvider toggleSystemContextProvider) {
            this.toggleSystemContextProvider = toggleSystemContextProvider;
            return this;
        }

        public Builder environment(String environment){
            this.environment = environment;
            return this;
        }

        public Builder instanceId(String instanceId){
            this.instanceId = instanceId;
            return this;
        }



//      AUXILIARY METHODS
        private static String getHostname() {
            String hostName = System.getProperty("hostname");
            if (hostName == null || hostName.length() == 0) {
                try {
                    hostName = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                }
            }
            return hostName + "-";
        }

        static String getDefaultInstanceId() {
            return getHostname() + "generated-" + Math.round(Math.random() * 1000000.0D);
        }

        private String getBackupFile() {
            if (backupFilePath != null) {
                return backupFilePath;
            } else {
                String fileName = "toggle-system-" + sanitizedAppName(appName) + "-repo.json";
                String tmpDir = System.getProperty("java.io.tmpdir");
                if (tmpDir == null) {
                    throw new IllegalStateException(
                            "'java.io.tmpdir' must not be empty, cause we write backup files into it.");
                }
                tmpDir = !tmpDir.endsWith(File.separator) ? tmpDir + File.separatorChar : tmpDir;
                return tmpDir + fileName;
            }
        }

        private String sanitizedAppName(String appName) {
            if (null == appName) {
                return "default";
            } else if (appName.contains("/") || appName.contains("\\")) {
                return appName.replace("/", "-").replace("\\", "-");
            } else {
                return appName;
            }
        }

//      BUILD METHOD
        public ToggleSystemConfig build() {
            return new ToggleSystemConfig(
                    toggleServerAPI,
                    apiKey,
                    appName,
                    environment,
                    instanceId,
                    remoteEvaluation,
                    synchronousFetchOnInitialisation,
                    pollingInterval,
                    cacheTimeout,
                    getBackupFile(),
                    toggleSystemContextProvider,
                    ToggleSystemExecutorImpl.getInstance()
            );
        }
    }
}
