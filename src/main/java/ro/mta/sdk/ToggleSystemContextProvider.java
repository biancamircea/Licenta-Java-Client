package ro.mta.sdk;

public interface ToggleSystemContextProvider {
    ToggleSystemContext getContext();

    static ToggleSystemContextProvider getDefaultProvider(){
        return () -> ToggleSystemContext.builder().build();
    }
}
