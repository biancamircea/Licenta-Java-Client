const fs = require("fs");
const snarkjs = require("snarkjs");
const path = require("path");

async function main() {
    // Loghează toate argumentele primite
    console.log("[DEBUG] process.argv:", process.argv.slice(2));

    const args = process.argv.slice(2);
    if (args.length !== 4) {
        console.error("Usage: node generate_witness.js <wasm_file> <input_json> <output_dir> <key_file>");
        process.exit(1);
    }

    const wasmPath = args[0];
    const inputPath = args[1];
    const outputDir = args[2];
    const keyPath = args[3];

    console.log("[DEBUG] wasmPath:", wasmPath);
    console.log("[DEBUG] inputPath:", inputPath);
    console.log("[DEBUG] outputDir:", outputDir);
    console.log("[DEBUG] keyPath:", keyPath);

    try {
        // Verifică existența fișierelor
        if (!fs.existsSync(wasmPath)) throw new Error(`WASM file missing: ${wasmPath}`);
        if (!fs.existsSync(inputPath)) throw new Error(`Input file missing: ${inputPath}`);
        if (!fs.existsSync(keyPath)) throw new Error(`ZKey file missing: ${keyPath}`);

        // Încarcă input-ul
        const input = JSON.parse(fs.readFileSync(inputPath, "utf8"));

        // Generează proof-ul
        const { proof, publicSignals } = await snarkjs.groth16.fullProve(
            input,
            wasmPath,
            keyPath
        );

        // Creează directorul de output (dacă nu există)
        if (!fs.existsSync(outputDir)) {
            fs.mkdirSync(outputDir, { recursive: true });
        }

        // Salvează rezultatele
        fs.writeFileSync(path.join(outputDir, "proof.json"), JSON.stringify(proof, null, 2));
        fs.writeFileSync(path.join(outputDir, "public.json"), JSON.stringify(publicSignals, null, 2));

        console.log("Proof generat cu succes în:", outputDir);
        process.exit(0); // Ieși explicit
    } catch (error) {
        console.error("[ERROR]", error.message);
        process.exit(1);
    }
}

// Rulează doar dacă scriptul este executat direct (nu importat ca modul)
if (require.main === module) {
    main();
}