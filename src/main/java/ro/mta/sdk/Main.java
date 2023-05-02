package ro.mta.sdk;

public class Main {
    public static void main(String[] args) {
        ToggleSystemConfig toggleSystemConfig = ToggleSystemConfig.builder()
                .toggleServerAPI("http://localhost:8080")
                .instanceId("instance1")
                .apiKey("2:1.44B72BDC152F3D1214B84CA5D923CA917AD14E1B48713EE2CEC2F3F0B339E3A3")
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
            if(systemClient.isEnabled("nume5")) {
                System.out.println("DA");
            } else {
                System.out.println("NU");
            }

        }
//        Long a = 3L;
    }
}
