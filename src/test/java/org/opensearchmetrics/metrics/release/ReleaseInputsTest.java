package org.opensearchmetrics.metrics.release;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReleaseInputsTest {


    @Test
    public void testGetVersion() {
        assertEquals("3.0.0", ReleaseInputs.VERSION_3_0_0.getVersion());
        assertEquals("2.12.0", ReleaseInputs.VERSION_2_12_0.getVersion());
        assertEquals("2.13.0", ReleaseInputs.VERSION_2_13_0.getVersion());
        assertEquals("2.14.0", ReleaseInputs.VERSION_2_14_0.getVersion());
        assertEquals("2.15.0", ReleaseInputs.VERSION_2_15_0.getVersion());
        assertEquals("2.16.0", ReleaseInputs.VERSION_2_16_0.getVersion());
        assertEquals("1.3.15", ReleaseInputs.VERSION_1_3_15.getVersion());
        assertEquals("1.3.16", ReleaseInputs.VERSION_1_3_16.getVersion());
        assertEquals("1.3.17", ReleaseInputs.VERSION_1_3_17.getVersion());
    }

    @Test
    public void testGetState() {
        assertEquals("open", ReleaseInputs.VERSION_3_0_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_12_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_13_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_14_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_15_0.getState());
        assertEquals("open", ReleaseInputs.VERSION_2_16_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_1_3_15.getState());
        assertEquals("closed", ReleaseInputs.VERSION_1_3_16.getState());
        assertEquals("closed", ReleaseInputs.VERSION_1_3_17.getState());
    }

    @Test
    public void testGetBranch() {
        assertEquals("main", ReleaseInputs.VERSION_3_0_0.getBranch());
        assertEquals("2.12", ReleaseInputs.VERSION_2_12_0.getBranch());
        assertEquals("2.13", ReleaseInputs.VERSION_2_13_0.getBranch());
        assertEquals("2.14", ReleaseInputs.VERSION_2_14_0.getBranch());
        assertEquals("2.15", ReleaseInputs.VERSION_2_15_0.getBranch());
        assertEquals("2.x", ReleaseInputs.VERSION_2_16_0.getBranch());
        assertEquals("1.3", ReleaseInputs.VERSION_1_3_15.getBranch());
        assertEquals("1.3", ReleaseInputs.VERSION_1_3_16.getBranch());
        assertEquals("1.3", ReleaseInputs.VERSION_1_3_17.getBranch());
    }

    @Test
    public void testGetAllReleaseInputs() {
        ReleaseInputs[] releaseInputs = ReleaseInputs.getAllReleaseInputs();
        assertEquals(9, releaseInputs.length);
        assertEquals(ReleaseInputs.VERSION_3_0_0, releaseInputs[0]);
        assertEquals(ReleaseInputs.VERSION_2_12_0, releaseInputs[1]);
        assertEquals(ReleaseInputs.VERSION_2_13_0, releaseInputs[2]);
        assertEquals(ReleaseInputs.VERSION_2_14_0, releaseInputs[3]);
        assertEquals(ReleaseInputs.VERSION_2_15_0, releaseInputs[4]);
        assertEquals(ReleaseInputs.VERSION_2_16_0, releaseInputs[5]);
        assertEquals(ReleaseInputs.VERSION_1_3_15, releaseInputs[6]);
        assertEquals(ReleaseInputs.VERSION_1_3_16, releaseInputs[7]);
        assertEquals(ReleaseInputs.VERSION_1_3_17, releaseInputs[8]);
    }
}
