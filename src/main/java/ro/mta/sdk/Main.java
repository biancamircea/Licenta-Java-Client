package ro.mta.sdk;

public class Main {
    public static void main(String[] args) {
        ToggleSystemConfig toggleSystemConfig = ToggleSystemConfig.builder()
                .toggleServerAPI("http://localhost:8080")
                .apiKey("Q4z23ZaK:Ml2JXk0j:nmVMBZ0e:0.F6BE9E47853417FA60E83E73B4B4416555CE65AD610B589402A4270E15D709D3")
                .appName("concedii")
                .remoteEvaluation(true)
                .build();

        ToggleSystemClient systemClient = new ToggleSystemClient(toggleSystemConfig);

        ToggleSystemContext context = ToggleSystemContext.builder()
                .addProperty("age", "20")
                .build();

        boolean isEnabled = systemClient.isEnabledZKP("togge1",context);
        System.out.println(isEnabled);
    }
}
