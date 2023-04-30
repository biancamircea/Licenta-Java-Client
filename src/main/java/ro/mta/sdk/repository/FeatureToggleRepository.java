package ro.mta.sdk.repository;

import ro.mta.sdk.FeatureToggle;
import ro.mta.sdk.ToggleSystemConfig;
import ro.mta.sdk.ToggleSystemException;
import ro.mta.sdk.util.ToggleSystemExecutor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureToggleRepository implements ToggleRepository{

    private final ToggleFetcher toggleFetcher;
    private ToggleCollection toggleCollection;
    private boolean ready;

    public FeatureToggleRepository(ToggleSystemConfig systemConfig){
        this(systemConfig, new HttpToggleFetcher(systemConfig));
    }

    public FeatureToggleRepository(
            ToggleSystemConfig systemConfig,
            ToggleFetcher toggleFetcher) {
        this(
                systemConfig,
                systemConfig.getToggleSystemExecutor(),
                toggleFetcher);
    }

    public FeatureToggleRepository(
            ToggleSystemConfig systemConfig,
            ToggleSystemExecutor executor,
            ToggleFetcher toggleFetcher) {
        this.toggleFetcher = toggleFetcher;
        if (systemConfig.isSynchronousFetchOnInitialisation()) {
            updateToggles().run();
        }

        executor.setInterval(updateToggles(), 0, systemConfig.getPollingInterval());
    }


    private Runnable updateToggles() {
        return () -> {
            try {
                ToggleResponse response = toggleFetcher.fetchToggles();
                if (response.getStatus() == ToggleResponse.Status.CHANGED) {
                    toggleCollection = response.getToggleCollection();
//                    toggleBackupHandler.write(response.getToggleCollection());
                }
                if (!ready) {
                    ready = true;
                }
            } catch (ToggleSystemException e) {
            }
        };
    }

    @Nullable
    @Override
    public FeatureToggle getToggle(String name) {
        return toggleCollection.getToggle(name);
    }

    @Override
    public List<String> getFeatureNames() {
        return toggleCollection.getFeatureToggles().stream()
                .map(FeatureToggle::getName)
                .collect(Collectors.toList());
    }
}
