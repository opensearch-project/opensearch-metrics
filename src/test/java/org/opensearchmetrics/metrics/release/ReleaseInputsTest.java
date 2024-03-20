package org.opensearchmetrics.metrics.release;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReleaseInputsTest {


    @Test
    public void testGetVersion() {
        assertEquals("3.0.0", ReleaseInputs.VERSION_3_0_0.getVersion());
        assertEquals("2.12.0", ReleaseInputs.VERSION_2_12_0.getVersion());
        assertEquals("2.13.0", ReleaseInputs.VERSION_2_13_0.getVersion());
        assertEquals("1.3.15", ReleaseInputs.VERSION_1_3_15.getVersion());
    }

    @Test
    public void testGetState() {
        assertEquals("open", ReleaseInputs.VERSION_3_0_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_2_12_0.getState());
        assertEquals("open", ReleaseInputs.VERSION_2_13_0.getState());
        assertEquals("closed", ReleaseInputs.VERSION_1_3_15.getState());
    }

    @Test
    public void testGetBranch() {
        assertEquals("main", ReleaseInputs.VERSION_3_0_0.getBranch());
        assertEquals("2.12", ReleaseInputs.VERSION_2_12_0.getBranch());
        assertEquals("2.x", ReleaseInputs.VERSION_2_13_0.getBranch());
        assertEquals("1.3", ReleaseInputs.VERSION_1_3_15.getBranch());
    }

    @Test
    public void testGetAllReleaseInputs() {
        ReleaseInputs[] releaseInputs = ReleaseInputs.getAllReleaseInputs();
        assertEquals(4, releaseInputs.length);
        assertEquals(ReleaseInputs.VERSION_3_0_0, releaseInputs[0]);
        assertEquals(ReleaseInputs.VERSION_2_12_0, releaseInputs[1]);
        assertEquals(ReleaseInputs.VERSION_2_13_0, releaseInputs[2]);
        assertEquals(ReleaseInputs.VERSION_1_3_15, releaseInputs[3]);
    }
}
