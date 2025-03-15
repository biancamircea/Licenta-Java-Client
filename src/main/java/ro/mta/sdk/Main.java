package ro.mta.sdk;

public class Main {
    public static void main(String[] args) {
        ToggleSystemConfig toggleSystemConfig = ToggleSystemConfig.builder()
                .toggleServerAPI("http://localhost:8080")
                .instanceId("1")
                .apiKey("1:1:1.C76EFF5D24B48E506758D87CC688E6C439467404E5F9F91593B6B3B43DD66976")
                .remoteEvaluation(true)
                .cacheTimeout(10)
                .appName("demo")
                .environment("production")
                .build();

        ToggleSystemContextProvider toggleSystemContextProvider = ToggleSystemContextProvider.getDefaultProvider();
        ToggleSystemContext toggleSystemContext = toggleSystemConfig.getToggleSystemContextProvider().getContext();

        ToggleSystemContext context = ToggleSystemContext.builder()
                .userId("user@mail.com").build();

        ToggleSystemClient systemClient = new ToggleSystemClient(toggleSystemConfig);

        while (true){
            if(systemClient.isEnabled("feat1")) {
                System.out.println("DA");
            } else {
                System.out.println("NU");
            }

        }
//        Long a = 3L;
    }
}
