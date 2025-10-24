/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ReleaseInputsTest {

    @Test
    public void testGetVersion() {
        assertEquals("3.3.2", ReleaseInputs.VERSION_3_3_2.getVersion());
        assertEquals("3.3.1", ReleaseInputs.VERSION_3_3_1.getVersion());
        assertEquals("3.3.0", ReleaseInputs.VERSION_3_3_0.getVersion());
        assertEquals("3.2.0", ReleaseInputs.VERSION_3_2_0.getVersion());
        assertEquals("3.1.0", ReleaseInputs.VERSION_3_1_0.getVersion());
        assertEquals("3.0.0", ReleaseInputs.VERSION_3_0_0.getVersion());
        assertEquals("2.12.0", ReleaseInputs.VERSION_2_12_0.getVersion());
        assertEquals("2.13.0", ReleaseInputs.VERSION_2_13_0.getVersion());
        assertEquals("2.14.0", ReleaseInputs.VERSION_2_14_0.getVersion());
        assertEquals("2.15.0", ReleaseInputs.VERSION_2_15_0.getVersion());
        assertEquals("2.16.0", ReleaseInputs.VERSION_2_16_0.getVersion());
        assertEquals("2.17.0", ReleaseInputs.VERSION_2_17_0.getVersion());
        assertEquals("2.18.0", ReleaseInputs.VERSION_2_18_0.getVersion());
        assertEquals("2.19.0", ReleaseInputs.VERSION_2_19_0.getVersion());
        assertEquals("2.19.1", ReleaseInputs.VERSION_2_19_1.getVersion());
        assertEquals("2.19.2", ReleaseInputs.VERSION_2_19_2.getVersion());
        assertEquals("2.19.3", ReleaseInputs.VERSION_2_19_3.getVersion());
        assertEquals("2.19.4", ReleaseInputs.VERSION_2_19_4.getVersion());
        assertEquals("1.3.15", ReleaseInputs.VERSION_1_3_15.getVersion());
        assertEquals("1.3.16", ReleaseInputs.VERSION_1_3_16.getVersion());
        assertEquals("1.3.17", ReleaseInputs.VERSION_1_3_17.getVersion());
        assertEquals("1.3.18", ReleaseInputs.VERSION_1_3_18.getVersion());
        assertEquals("1.3.19", ReleaseInputs.VERSION_1_3_19.getVersion());
        assertEquals("1.3.20", ReleaseInputs.VERSION_1_3_20.getVersion());
    }

    @Test
    public void testGetState() {
        assertEquals("open", ReleaseInputs.VERSION_3_3_2.getState());
        assertEquals("closed", ReleaseInputs.VERSION_3_3_1.getState());
        assertEquals("closed", ReleaseInputs.VERSION_3_3_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_3_2_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_3_1_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_3_0_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_12_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_13_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_14_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_15_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_16_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_17_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_18_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_19_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_19_1.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_19_2.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_19_3.getState());
        assertEquals("open", ReleaseInputs.VERSION_2_19_4.getState());
        assertEquals("closed", ReleaseInputs.VERSION_1_3_15.getState());
        assertEquals("closed", ReleaseInputs.VERSION_1_3_16.getState());
        assertEquals("closed", ReleaseInputs.VERSION_1_3_17.getState());
        assertEquals("closed", ReleaseInputs.VERSION_1_3_18.getState());
        assertEquals("closed", ReleaseInputs.VERSION_1_3_19.getState());
        assertEquals("closed", ReleaseInputs.VERSION_1_3_20.getState());
    }

    @Test
    public void testGetBranch() {
        assertEquals("3.3", ReleaseInputs.VERSION_3_3_1.getBranch());
        assertEquals("3.3", ReleaseInputs.VERSION_3_3_1.getBranch());
        assertEquals("3.3", ReleaseInputs.VERSION_3_3_0.getBranch());
        assertEquals("3.2", ReleaseInputs.VERSION_3_2_0.getBranch());
        assertEquals("3.1", ReleaseInputs.VERSION_3_1_0.getBranch());
        assertEquals("3.0", ReleaseInputs.VERSION_3_0_0.getBranch());
        assertEquals("2.12", ReleaseInputs.VERSION_2_12_0.getBranch());
        assertEquals("2.13", ReleaseInputs.VERSION_2_13_0.getBranch());
        assertEquals("2.14", ReleaseInputs.VERSION_2_14_0.getBranch());
        assertEquals("2.15", ReleaseInputs.VERSION_2_15_0.getBranch());
        assertEquals("2.16", ReleaseInputs.VERSION_2_16_0.getBranch());
        assertEquals("2.17", ReleaseInputs.VERSION_2_17_0.getBranch());
        assertEquals("2.18", ReleaseInputs.VERSION_2_18_0.getBranch());
        assertEquals("2.19", ReleaseInputs.VERSION_2_19_0.getBranch());
        assertEquals("2.19", ReleaseInputs.VERSION_2_19_1.getBranch());
        assertEquals("2.19", ReleaseInputs.VERSION_2_19_2.getBranch());
        assertEquals("2.19", ReleaseInputs.VERSION_2_19_3.getBranch());
        assertEquals("2.19", ReleaseInputs.VERSION_2_19_4.getBranch());
        assertEquals("1.3", ReleaseInputs.VERSION_1_3_15.getBranch());
        assertEquals("1.3", ReleaseInputs.VERSION_1_3_16.getBranch());
        assertEquals("1.3", ReleaseInputs.VERSION_1_3_17.getBranch());
        assertEquals("1.3", ReleaseInputs.VERSION_1_3_18.getBranch());
        assertEquals("1.3", ReleaseInputs.VERSION_1_3_19.getBranch());
        assertEquals("1.3", ReleaseInputs.VERSION_1_3_20.getBranch());
    }

    @Test
    public void testGetTrack() {
        assertEquals(true, ReleaseInputs.VERSION_3_3_2.getTrack());
        assertEquals(true, ReleaseInputs.VERSION_3_3_1.getTrack());
        assertEquals(true, ReleaseInputs.VERSION_3_3_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_3_2_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_3_1_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_3_0_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_12_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_13_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_14_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_15_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_16_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_17_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_18_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_19_0.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_19_1.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_19_2.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_2_19_3.getTrack());
        assertEquals(true, ReleaseInputs.VERSION_2_19_4.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_1_3_15.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_1_3_16.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_1_3_17.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_1_3_18.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_1_3_19.getTrack());
        assertEquals(false, ReleaseInputs.VERSION_1_3_20.getTrack());
    }

    @Test
    public void testGetAllReleaseInputs() {
        ReleaseInputs[] releaseInputs = ReleaseInputs.getAllReleaseInputs();
        assertEquals(24, releaseInputs.length);
        assertEquals(ReleaseInputs.VERSION_3_3_2, releaseInputs[0]);
        assertEquals(ReleaseInputs.VERSION_3_3_1, releaseInputs[1]);
        assertEquals(ReleaseInputs.VERSION_3_3_0, releaseInputs[2]);
        assertEquals(ReleaseInputs.VERSION_3_2_0, releaseInputs[3]);
        assertEquals(ReleaseInputs.VERSION_3_1_0, releaseInputs[4]);
        assertEquals(ReleaseInputs.VERSION_3_0_0, releaseInputs[5]);
        assertEquals(ReleaseInputs.VERSION_2_12_0, releaseInputs[6]);
        assertEquals(ReleaseInputs.VERSION_2_13_0, releaseInputs[7]);
        assertEquals(ReleaseInputs.VERSION_2_14_0, releaseInputs[8]);
        assertEquals(ReleaseInputs.VERSION_2_15_0, releaseInputs[9]);
        assertEquals(ReleaseInputs.VERSION_2_16_0, releaseInputs[10]);
        assertEquals(ReleaseInputs.VERSION_2_17_0, releaseInputs[11]);
        assertEquals(ReleaseInputs.VERSION_2_18_0, releaseInputs[12]);
        assertEquals(ReleaseInputs.VERSION_2_19_0, releaseInputs[13]);
        assertEquals(ReleaseInputs.VERSION_2_19_1, releaseInputs[14]);
        assertEquals(ReleaseInputs.VERSION_2_19_2, releaseInputs[15]);
        assertEquals(ReleaseInputs.VERSION_2_19_3, releaseInputs[16]);
        assertEquals(ReleaseInputs.VERSION_2_19_4, releaseInputs[17]);
        assertEquals(ReleaseInputs.VERSION_1_3_15, releaseInputs[18]);
        assertEquals(ReleaseInputs.VERSION_1_3_16, releaseInputs[19]);
        assertEquals(ReleaseInputs.VERSION_1_3_17, releaseInputs[20]);
        assertEquals(ReleaseInputs.VERSION_1_3_18, releaseInputs[21]);
        assertEquals(ReleaseInputs.VERSION_1_3_19, releaseInputs[22]);
        assertEquals(ReleaseInputs.VERSION_1_3_20, releaseInputs[23]);
    }

}
