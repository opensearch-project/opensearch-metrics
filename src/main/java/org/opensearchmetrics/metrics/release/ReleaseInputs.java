package org.opensearchmetrics.metrics.release;

public enum ReleaseInputs {
    VERSION_3_0_0("3.0.0", "open", "main"),
    VERSION_2_12_0("2.12.0", "closed", "2.12"),
    VERSION_2_13_0("2.13.0", "closed", "2.13"),
    VERSION_2_14_0("2.14.0", "closed", "2.14"),
    VERSION_2_15_0("2.15.0", "closed", "2.15"),
    VERSION_2_16_0("2.16.0", "open", "2.x"),
    VERSION_1_3_15("1.3.15", "closed", "1.3"),
    VERSION_1_3_16("1.3.16", "closed", "1.3"),
    VERSION_1_3_17("1.3.17", "closed", "1.3");

    private final String version;
    private final String state;
    private final String branch;

    ReleaseInputs(String version, String state, String branch) {
        this.version = version;
        this.state = state;
        this.branch = branch;
    }

    public String getVersion() {
        return version;
    }

    public String getState() {
        return state;
    }

    public String getBranch() {
        return branch;
    }
    public static ReleaseInputs[] getAllReleaseInputs() {
        return values();
    }
}
