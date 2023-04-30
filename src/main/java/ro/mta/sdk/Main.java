package ro.mta.sdk;

public class Main {
    public static void main(String[] args) {
        ToggleSystemConfig toggleSystemConfig = ToggleSystemConfig.builder()
                .toggleServerAPI("http://localhost:8080/api")
                .apiKey("dadada")
                .pollingInterval(2)
                .appName("app")
                .build();

        ToggleSystemContextProvider toggleSystemContextProvider = ToggleSystemContextProvider.getDefaultProvider();
        ToggleSystemContext toggleSystemContext = toggleSystemConfig.getToggleSystemContextProvider().getContext();


        ToggleSystemContext context = ToggleSystemContext.builder()
                .userId("user@mail.com").build();

        ToggleSystemClient systemClient = new ToggleSystemClient(toggleSystemConfig);
        while (true){

        }
//        Long a = 3L;
    }
}
