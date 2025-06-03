/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

public enum ReleaseInputs {
    VERSION_3_1_0("3.1.0", "open", "main", true),
    VERSION_3_0_0("3.0.0", "closed", "3.0", false),
    VERSION_2_12_0("2.12.0", "closed", "2.12", false),
    VERSION_2_13_0("2.13.0", "closed", "2.13", false),
    VERSION_2_14_0("2.14.0", "closed", "2.14", false),
    VERSION_2_15_0("2.15.0", "closed", "2.15", false),
    VERSION_2_16_0("2.16.0", "closed", "2.16", false),
    VERSION_2_17_0("2.17.0", "closed", "2.17", false),
    VERSION_2_18_0("2.18.0", "closed", "2.18", false),
    VERSION_2_19_0("2.19.0", "closed", "2.19", false),
    VERSION_2_19_1("2.19.1", "closed", "2.19", false),
    VERSION_2_19_2("2.19.2", "closed", "2.19", false),
    VERSION_2_19_3("2.19.3", "open", "2.19", true),
    VERSION_1_3_15("1.3.15", "closed", "1.3", false),
    VERSION_1_3_16("1.3.16", "closed", "1.3", false),
    VERSION_1_3_17("1.3.17", "closed", "1.3", false),
    VERSION_1_3_18("1.3.18", "closed", "1.3", false),
    VERSION_1_3_19("1.3.19", "closed", "1.3", false),
    VERSION_1_3_20("1.3.20", "closed", "1.3", false);

    private final String version;
    private final String state;
    private final String branch;

    private final boolean track;

    ReleaseInputs(String version, String state, String branch, boolean track) {
        this.version = version;
        this.state = state;
        this.branch = branch;
        this.track = track;
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

    public boolean getTrack() {
        return track;
    }

    public static ReleaseInputs[] getAllReleaseInputs() {
        return values();
    }
}
