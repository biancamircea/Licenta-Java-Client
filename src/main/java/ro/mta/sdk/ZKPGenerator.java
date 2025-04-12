package ro.mta.sdk;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

public class ZKPGenerator {
    private static final String ZKP_DIR = "zkp";
    private static final String WASM_FILE_NORMAL = "age_check_plonk.wasm";
    private static final String ZKEY_FILE_NORMAL = "age_check_plonk.zkey";
    private static final String WASM_FILE_LOCATION = "circuit_location.wasm";
    private static final String ZKEY_FILE_LOCATION = "circuit_location.zkey";
    private static final String GENERATE_SCRIPT = "generate_witness.js";

    public JsonObject generateProof(double loc_x_user, double loc_y_user, double loc_x,double loc_y, int marginCode ) throws Exception {
        Path tempDir = extractZKPResourcesToTemp();

        Path wasmPath = tempDir.resolve(WASM_FILE_LOCATION);
        Path zkeyPath = tempDir.resolve(ZKEY_FILE_LOCATION);
        Path scriptPath = tempDir.resolve(GENERATE_SCRIPT);
        Path inputPath = tempDir.resolve("input.json");
        Path outputDir = Files.createTempDirectory("zkp_output");

        int latInt = (int) Math.round(loc_y_user * 10000);
        int lngInt = (int) Math.round(loc_x_user * 10000);
        int latAdmin = (int) Math.round(loc_y * 10000);
        int lngAdmin = (int) Math.round(loc_x * 10000);
        long margin;

        if (marginCode == 0) {
            margin = 1200;
        } else if (marginCode == 1) {
            margin = 25000;
        } else {
            margin = 225000;
        }

        String jsonInput = String.format(
                "{\"lat\":%d, \"lng\":%d, \"latAdmin\":%d, \"lngAdmin\":%d, \"margin\":%d}",
                latInt, lngInt, latAdmin, lngAdmin, margin
        );

        Files.writeString(inputPath, jsonInput);

        return runScript(tempDir,scriptPath, wasmPath, inputPath, outputDir, zkeyPath);

    }

    public JsonObject generateProof(int age, int threshold, int operation) throws Exception {
        Path tempDir = extractZKPResourcesToTemp();

        Path wasmPath = tempDir.resolve(WASM_FILE_NORMAL);
        Path zkeyPath = tempDir.resolve(ZKEY_FILE_NORMAL);
        Path scriptPath = tempDir.resolve(GENERATE_SCRIPT);
        Path inputPath = tempDir.resolve("input.json");
        Path outputDir = Files.createTempDirectory("zkp_output");

        Files.writeString(inputPath,
                String.format("{\"val\":%d,\"threshold\":%d,\"operation\":%d}", age, threshold,operation)
        );

        return runScript(tempDir,scriptPath, wasmPath, inputPath, outputDir, zkeyPath);
    }

    public JsonObject runScript(Path tempDir,Path scriptPath, Path wasmPath, Path inputPath, Path outputDir, Path zkeyPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "node",
                scriptPath.toString(),
                wasmPath.toString(),
                inputPath.toString(),
                outputDir.toString(),
                zkeyPath.toString()
        );


        Map<String, String> env = pb.environment();
        env.put("NODE_PATH", tempDir.resolve(ZKP_DIR).resolve("node_modules").toString());
        System.out.println("NODE_PATH: " + env.get("NODE_PATH"));

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
            throw new RuntimeException("Procesul ZKP a eșuat cu codul: " + exitCode);
        }

        String proof = Files.readString(outputDir.resolve("proof.json"));
        String publicSignals = Files.readString(outputDir.resolve("public.json"));

        JsonObject proofJson = JsonParser.parseString(proof).getAsJsonObject();
        JsonArray publicSignalsJson = JsonParser.parseString(publicSignals).getAsJsonArray();

        JsonObject proofContainer = new JsonObject();
        proofContainer.add("proof", proofJson);
        proofContainer.add("publicSignals", publicSignalsJson);

        return proofContainer;
    }

    private Path extractZKPResourcesToTemp() throws IOException {
        Path tempDir = Files.createTempDirectory("zkp_resources");
        ClassLoader loader = getClass().getClassLoader();

        try (InputStream in = loader.getResourceAsStream(ZKP_DIR)) {
            if (in == null) {
                throw new IOException("Resursele ZKP nu au fost găsite în JAR!");
            }
        }

        URL resourceUrl = loader.getResource(ZKP_DIR);
        if (resourceUrl == null) {
            throw new IOException("Resursele ZKP nu au fost găsite în JAR!");
        }

        try (FileSystem fileSystem = FileSystems.newFileSystem(resourceUrl.toURI(), Collections.emptyMap())) {
            Path zkpPathInJar = fileSystem.getPath(ZKP_DIR);

            Files.walk(zkpPathInJar)
                    .forEach(source -> {
                        try {
                            Path dest = tempDir.resolve(ZKP_DIR).resolve(zkpPathInJar.relativize(source).toString());

                            if (Files.isDirectory(source)) {
                                Files.createDirectories(dest);
                            } else {
                                Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Eroare la copierea resurselor!", e);
                        }
                    });
        } catch (Exception e) {
            throw new IOException("Eroare la copierea resurselor!", e);
        }

        return tempDir.resolve(ZKP_DIR);
    }
}