package ro.mta.sdk.evaluator;

import com.google.gson.JsonObject;

public class ZKPProof {
    private String name;
    private JsonObject proof;

    public ZKPProof(String name, JsonObject proof) {
        this.name = name;
        this.proof = proof;
    }

    public String getName() {
        return name;
    }

    public JsonObject getProof() {
        return proof;
    }

}

