package ro.mta.sdk;

public class Main {
    public static void main(String[] args) {

        ToggleSystemConfig toggleSystemConfig = ToggleSystemConfig.builder()
                .toggleServerAPI("https://localhost:8443")
                .apiKey("Q4z23ZaK:Ml2JXk0j:YG5OqVxL:0.11874B303D0F7F9A451579BE23B60A0EFD7510D26D07B8897F1A9728A85B285C")
                .appName("concedii")
                .build();

        ToggleSystemClient systemClient = new ToggleSystemClient(toggleSystemConfig);

        ToggleSystemContext context = ToggleSystemContext.builder()
                .addContext("user_role", "25")
                .addContext("conf","14")
                .addContext("conf2","4")
                .build();

        boolean isEnabled = systemClient.isEnabled("background-color",context);
        String payload = systemClient.getPayload("background-color",context);
        System.out.println(isEnabled);
        System.out.println("payload:"+payload);

        boolean isEnabled2 = systemClient.isEnabled("background-color",context);
        System.out.println("isEn2: "+isEnabled2);

        boolean isEnabled4 = systemClient.isEnabled("feature_test");
        System.out.println("isEn4: "+isEnabled4);
    }
}
