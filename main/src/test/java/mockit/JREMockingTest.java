package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

/**
 * The Class JREMockingTest.
 */
final class JREMockingTest {
    // Mocking java.io.File
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Mocking of file.
     *
     * @param file
     *            the file
     */
    @Test
    void mockingOfFile(@Mocked final File file) {
        new Expectations() {
            {
                file.exists();
                result = true;
            }
        };

        File f = Path.of("...").toFile();
        assertTrue(f.exists());
    }

    /**
     * Mocking file and recording expectation to match on specific constructor call.
     *
     * @param anyFile
     *            the any file
     */
    @Test
    void mockingFileAndRecordingExpectationToMatchOnSpecificConstructorCall(@Mocked File anyFile) {
        new Expectations() {
            {
                Path.of("a.txt").toFile().exists();
                result = true;
            }
        };

        boolean aExists = Path.of("a.txt").toFile().exists();
        // noinspection TooBroadScope
        boolean bExists = Path.of("b.txt").toFile().exists();

        assertTrue(aExists);
        assertFalse(bExists);
    }

    // Faking java.util.Calendar
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Faking of calendar.
     */
    @Test
    void fakingOfCalendar() {
        final Calendar calCST = new GregorianCalendar(2010, Calendar.MAY, 15);
        final TimeZone tzCST = TimeZone.getTimeZone("CST");
        new MockUp<Calendar>() {
            @Mock
            Calendar getInstance(Invocation inv, TimeZone tz) {
                return tz == tzCST ? calCST : inv.<Calendar> proceed();
            }
        };

        Calendar cal1 = Calendar.getInstance(tzCST);
        assertSame(calCST, cal1);
        assertEquals(2010, cal1.get(Calendar.YEAR));

        TimeZone tzPST = TimeZone.getTimeZone("PST");
        Calendar cal2 = Calendar.getInstance(tzPST);
        assertNotSame(calCST, cal2);
    }

    // Mocking java.util.Date
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Regular mocking of annotated JRE method.
     *
     * @param d
     *            the d
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void regularMockingOfAnnotatedJREMethod(@Mocked Date d) throws Exception {
        assertTrue(d.getClass().getDeclaredMethod("parse", String.class).isAnnotationPresent(Deprecated.class));
    }

    /**
     * Dynamic mocking of annotated JRE method.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @SuppressWarnings("deprecation")
    void dynamicMockingOfAnnotatedJREMethod() throws Exception {
        final Date d = new Date();

        new Expectations(d) {
            {
                d.getMinutes();
                result = 5;
            }
        };

        assertEquals(5, d.getMinutes());
        assertTrue(Date.class.getDeclaredMethod("getMinutes").isAnnotationPresent(Deprecated.class));
    }

    // Mocking of IO classes
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** The stream. */
    @Injectable
    FileOutputStream stream;

    /** The writer. */
    @Injectable
    Writer writer;

    /**
     * Dynamic mocking of file output stream through mock field.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void dynamicMockingOfFileOutputStreamThroughMockField() throws Exception {
        new Expectations() {
            {
                // noinspection ConstantConditions
                stream.write((byte[]) any);
            }
        };

        stream.write("Hello world".getBytes());
        writer.append('x');

        new Verifications() {
            {
                writer.append('x');
            }
        };
    }

    // Un-mockable JRE classes
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked FileInputStream unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked FileOutputStream unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked Writer unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked FileWriter unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked PrintWriter unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked DataInputStream unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked StringBuffer unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked StringBuilder unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked ArrayList<?> unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked Throwable unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked Thread unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked ThreadLocal<?> unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked ClassLoader unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked Class<?> unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked Math unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked StrictMath unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked Object unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked Enum<?> unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked System unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked JarFile unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked JarEntry unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked Manifest unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@Mocked Attributes unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test
    void attemptToMockUnmockableJREClass(@SuppressWarnings("Since15") @Mocked Duration unmockable) {
        assertThrows(IllegalArgumentException.class, () -> {
            fail("Should never get here");
        });
    }

    // Mocking java.time
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The Interface DurationProvider.
     */
    public interface DurationProvider {
        /**
         * Gets the duration.
         *
         * @return the duration
         */
        @SuppressWarnings("Since15")
        Duration getDuration();
    }

    /**
     * Mock method which returns A duration.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockMethodWhichReturnsADuration(@Mocked DurationProvider mock) {
        Object d = mock.getDuration();

        assertNull(d);
    }

    // Mocking java.util.logging
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Mock log manager.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockLogManager(@Mocked LogManager mock) {
        LogManager logManager = LogManager.getLogManager();
        // noinspection MisorderedAssertEqualsArguments
        assertSame(mock, logManager);
    }

    /**
     * Mock logger.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockLogger(@Mocked Logger mock) {
        // TODO: this call causes Surefire to fail: assertNotNull(LogManager.getLogManager());
        // noinspection MisorderedAssertEqualsArguments
        assertSame(mock, Logger.getLogger("test"));
    }
}
