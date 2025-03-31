const fs = require("fs");
const snarkjs = require("snarkjs");
const path = require("path");

async function main() {
    console.log("[DEBUG] process.argv:", process.argv.slice(2));

    const args = process.argv.slice(2);
    if (args.length !== 4) {
        console.error("Usage: node generate_proof.js <wasm_file> <input_json> <output_dir> <zkey_file>");
        process.exit(1);
    }

    const wasmPath = args[0];
    const inputPath = args[1];
    const outputDir = args[2];
    const zkeyPath = args[3];

    try {
        if (!fs.existsSync(wasmPath)) throw new Error(`WASM file missing: ${wasmPath}`);
        if (!fs.existsSync(inputPath)) throw new Error(`Input file missing: ${inputPath}`);
        if (!fs.existsSync(zkeyPath)) throw new Error(`ZKey file missing: ${zkeyPath}`);

        const input = JSON.parse(fs.readFileSync(inputPath, "utf8"));

        console.log("[INFO] Generating proof...");
        const { proof, publicSignals } = await snarkjs.plonk.fullProve(input, wasmPath, zkeyPath);

        if (!fs.existsSync(outputDir)) {
            fs.mkdirSync(outputDir, { recursive: true });
        }

        fs.writeFileSync(path.join(outputDir, "proof.json"), JSON.stringify(proof, null, 2));
        fs.writeFileSync(path.join(outputDir, "public.json"), JSON.stringify(publicSignals, null, 2));

        console.log("[SUCCESS] Proof generat cu succes:");
        process.exit(0);
    } catch (error) {
        console.error("[ERROR]", error.message);
        process.exit(1);
    }
}

if (require.main === module) {
    main();
}
