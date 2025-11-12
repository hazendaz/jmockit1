/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package java8testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import mockit.Mock;
import mockit.MockUp;

import org.junit.jupiter.api.Test;

/**
 * The Class TimeAPIFakingTest.
 */
final class TimeAPIFakingTest {

    /**
     * Inject clock object.
     */
    @Test
    void injectClockObject() {
        // Create a test clock with a fixed instant.
        LocalDateTime testDateTime = LocalDateTime.parse("2014-05-10T10:15:30");
        ZoneId zoneId = ZoneId.systemDefault();
        Instant testInstant = testDateTime.toInstant(zoneId.getRules().getOffset(testDateTime));
        Clock testClock = Clock.fixed(testInstant, zoneId);

        // In production code, obtain local date & time from the test clock.
        LocalDateTime now = LocalDateTime.now(testClock);

        assertEquals(testDateTime, now);
    }

    /**
     * Fake local date time.
     */
    @Test
    void fakeLocalDateTime() {
        LocalDateTime testDateTime = LocalDateTime.parse("2014-05-10T09:35:12");
        new MockUp<LocalDateTime>() {
            @Mock
            LocalDateTime now() {
                return testDateTime;
            }
        };

        LocalDateTime now = LocalDateTime.now();

        assertSame(testDateTime, now);
    }

    /**
     * Fake instant.
     */
    @Test
    void fakeInstant() {
        Instant testInstant = Instant.parse("2014-05-10T09:35:12Z");
        new MockUp<Instant>() {
            @Mock
            Instant now() {
                return testInstant;
            }
        };

        Instant now = Instant.now();

        assertSame(testInstant, now);
    }
}
