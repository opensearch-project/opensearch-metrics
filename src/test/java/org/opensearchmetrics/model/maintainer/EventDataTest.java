package org.opensearchmetrics.model.maintainer;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventDataTest {
    private EventData eventData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventData = new EventData();
    }

    @Test
    public void testEventAction() {
        eventData.setEventAction("testAction");
        assertEquals("testAction", eventData.getEventAction());
    }

    @Test
    public void testTimeLastEngaged() {
        Instant testInstant = Instant.parse("2021-02-09T11:19:42.12Z");
        eventData.setTimeLastEngaged(testInstant);
        assertEquals(testInstant, eventData.getTimeLastEngaged());
    }

    @Test
    public void testInactive() {
        eventData.setInactive(true);
        assertEquals(true, eventData.isInactive());
    }
}
