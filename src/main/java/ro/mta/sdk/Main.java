package ro.mta.sdk;

public class Main {
    public static void main(String[] args) {

        ToggleSystemConfig toggleSystemConfig = ToggleSystemConfig.builder()
                .toggleServerAPI("https://localhost:8443")
                .apiKey("Q4z23ZaK:Ml2JXk0j:YG5OqVxL:1.E78F856F45392D1B5995B5110E7549945F6BD273046FBC679C38F7C6AB35FC1F")
                .appName("concedii")
                .build();

        ToggleSystemClient systemClient = new ToggleSystemClient(toggleSystemConfig);

        ToggleSystemContext context = ToggleSystemContext.builder()
                .addLocation(178,-89)
                .build();

        boolean isEnabled = systemClient.isEnabled("feature_back",context);
        String payload = systemClient.getPayload("feature_back",context);
        System.out.println(isEnabled);
        System.out.println("payload:"+payload);

//        boolean isEnabled2 = systemClient.isEnabled("background-color",context);
//        System.out.println("isEn2: "+isEnabled2);
//
//        boolean isEnabled4 = systemClient.isEnabled("feature_test");
//        System.out.println("isEn4: "+isEnabled4);
    }
}
