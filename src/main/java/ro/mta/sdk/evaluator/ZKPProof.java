package ro.mta.sdk.evaluator;

import com.google.gson.JsonObject;

public class ZKPProof {
    private String name;
    private JsonObject proof;
    private String type; //normal, location

    public ZKPProof(String name, JsonObject proof, String type) {
        this.name = name;
        this.proof = proof;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public JsonObject getProof() {
        return proof;
    }

    public String getType() {
        return type;
    }

}

