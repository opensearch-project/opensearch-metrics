/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.model.alarm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlarmDataTest {
    @Mock
    ObjectMapper objectMapper;

    private AlarmData alarmData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        alarmData = new AlarmData();
    }

    @Test
    public void testAlarmName() {
        alarmData.setAlarmName("testName");
        assertEquals("testName", alarmData.getAlarmName());
    }

    @Test
    public void testAlarmDescription() {
        alarmData.setAlarmDescription("testDescription");
        assertEquals("testDescription", alarmData.getAlarmDescription());
    }

    @Test
    public void testStateChangeTime() {
        alarmData.setStateChangeTime("testStateChangeTime");
        assertEquals("testStateChangeTime", alarmData.getStateChangeTime());
    }

    @Test
    public void testRegion() {
        alarmData.setRegion("testRegion");
        assertEquals("testRegion", alarmData.getRegion());
    }

    @Test
    public void testAlarmArn() {
        alarmData.setAlarmArn("testArn");
        assertEquals("testArn", alarmData.getAlarmArn());
    }
}
