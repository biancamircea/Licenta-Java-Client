package ro.mta.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.stream.Collectors;

public class ZKPGenerator {
    private static final String WASM_PATH = "zkp/age_check.wasm";
    private static final String ZKEY_PATH = "zkp/age_check_0001.zkey";
    private static final String GENERATE_SCRIPT = "zkp/generate_witness.js";
    private static final String NODE_MODULES_PATH = "src/main/resources/zkp/node_modules";

    public String generateProof(int age, int threshold) throws Exception {
        Path tempDir = Files.createTempDirectory("zkp");

        Path inputFile = tempDir.resolve("input.json");
        Files.write(inputFile,
                String.format("{\"age\":%d,\"threshold\":%d}", age, threshold).getBytes()
        );

        ProcessBuilder pb = new ProcessBuilder(
                "node",
                getResourcePath(GENERATE_SCRIPT),
                getResourcePath(WASM_PATH),
                inputFile.toString(),
                tempDir.toString(),
                getResourcePath(ZKEY_PATH)
        );

        executeProcess(pb);

        String proof = Files.readString(tempDir.resolve("proof.json"));
        String publicSignals = Files.readString(tempDir.resolve("public.json"));

        return String.format("{\"proof\":%s,\"publicSignals\":%s}", proof, publicSignals);
    }

    private String getResourcePath(String resource) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
        Path tempFile = Files.createTempFile("zkp_", "_temp");
        Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile.toAbsolutePath().toString();
    }

    private void executeProcess(ProcessBuilder pb) throws IOException, InterruptedException {
        Map<String, String> env = pb.environment();
        env.put("NODE_PATH", NODE_MODULES_PATH);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[ZKP] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("ZKP process failed with exit code: " + exitCode);
        }
    }
}