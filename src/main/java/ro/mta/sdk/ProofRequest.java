package ro.mta.sdk;

public class ProofRequest {
    private Long age;
    private Long threshold;

    public ProofRequest(Long age, Long threshold) {
        this.age = age;
        this.threshold = threshold;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public Long getThreshold() {
        return threshold;
    }

    public void setThreshold(Long threshold) {
        this.threshold = threshold;
    }
}
