const fs = require("fs");
const snarkjs = require("snarkjs");
const path = require("path");

async function main() {
    const args = process.argv.slice(2);
    if (args.length !== 4) {
        console.error("Usage: node generate_witness.js <wasm_file> <input_json> <output_dir> <key_file>");
        process.exit(1);
    }

    const wasmPath = args[0];
    const inputPath = args[1];
    const outputDir = args[2];
    const keyPath = args[3];

    try {
        const input = JSON.parse(fs.readFileSync(inputPath, "utf8"));
        const { proof, publicSignals } = await snarkjs.groth16.fullProve(
            input,
            wasmPath,
            keyPath
        );

        fs.writeFileSync(path.join(outputDir, "proof.json"), JSON.stringify(proof));
        fs.writeFileSync(path.join(outputDir, "public.json"), JSON.stringify(publicSignals));

        console.log("Fișiere generate în:", outputDir);
        process.exit(0);
    } catch (error) {
        console.error("Eroare:", error.message);
        process.exit(1);
    }
}

main();