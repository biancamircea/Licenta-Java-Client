package ro.mta.sdk;

public class Main {
    public static void main(String[] args) {

        ToggleSystemConfig toggleSystemConfig = ToggleSystemConfig.builder()
                .toggleServerAPI("https://api.zkflag.ro")
                .apiKey("Q4z23ZaK:Ml2JXk0j:nmVMBZ0e:1.BD704B9C627DAC66BB4617874335B5E22E419EF3695FEBA0E9BE79DF4D023FF7")
                .appName("concedii")
                .build();

        ToggleSystemClient systemClient = new ToggleSystemClient(toggleSystemConfig);

        ToggleSystemContext context = ToggleSystemContext.builder()
                .addContext("background", "14")
                .addLocation( 44.0, 33.0)
                .build();


        boolean isEnabled = systemClient.isEnabled("flag_age",context);

        String payload = systemClient.getPayload("flag_age",context);
        System.out.println(payload);


        System.out.println(isEnabled);
    }
}
