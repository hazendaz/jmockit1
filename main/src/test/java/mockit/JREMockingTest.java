package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
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

import org.junit.Test;

/**
 * The Class JREMockingTest.
 */
public final class JREMockingTest {
    // Mocking java.io.File
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Mocking of file.
     *
     * @param file
     *            the file
     */
    @Test
    public void mockingOfFile(@Mocked final File file) {
        new Expectations() {
            {
                file.exists();
                result = true;
            }
        };

        File f = new File("...");
        assertTrue(f.exists());
    }

    /**
     * Mocking file and recording expectation to match on specific constructor call.
     *
     * @param anyFile
     *            the any file
     */
    @Test
    public void mockingFileAndRecordingExpectationToMatchOnSpecificConstructorCall(@Mocked File anyFile) {
        new Expectations() {
            {
                new File("a.txt").exists();
                result = true;
            }
        };

        boolean aExists = new File("a.txt").exists();
        // noinspection TooBroadScope
        boolean bExists = new File("b.txt").exists();

        assertTrue(aExists);
        assertFalse(bExists);
    }

    // Faking java.util.Calendar
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Faking of calendar.
     */
    @Test
    public void fakingOfCalendar() {
        final Calendar calCST = new GregorianCalendar(2010, Calendar.MAY, 15);
        final TimeZone tzCST = TimeZone.getTimeZone("CST");
        new MockUp<Calendar>() {
            @Mock
            Calendar getInstance(Invocation inv, TimeZone tz) {
                return tz == tzCST ? calCST : inv.<Calendar>proceed();
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
    public void regularMockingOfAnnotatedJREMethod(@Mocked Date d) throws Exception {
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
    public void dynamicMockingOfAnnotatedJREMethod() throws Exception {
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
    public void dynamicMockingOfFileOutputStreamThroughMockField() throws Exception {
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
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked FileInputStream unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked FileOutputStream unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked Writer unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked FileWriter unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked PrintWriter unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked DataInputStream unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked StringBuffer unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked StringBuilder unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked Exception unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked Throwable unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked Thread unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked ThreadLocal<?> unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked ClassLoader unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked Class<?> unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked Math unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked StrictMath unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked Object unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked Enum<?> unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked System unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked JarFile unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked JarEntry unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked Manifest unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@Mocked Attributes unmockable) {
        fail("Should never get here");
    }

    /**
     * Attempt to mock unmockable JRE class.
     *
     * @param unmockable
     *            the unmockable
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToMockUnmockableJREClass(@SuppressWarnings("Since15") @Mocked Duration unmockable) {
        fail("Should never get here");
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
    public void mockMethodWhichReturnsADuration(@Mocked DurationProvider mock) {
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
    public void mockLogManager(@Mocked LogManager mock) {
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
    public void mockLogger(@Mocked Logger mock) {
        // TODO: this call causes Surefire to fail: assertNotNull(LogManager.getLogManager());
        // noinspection MisorderedAssertEqualsArguments
        assertSame(mock, Logger.getLogger("test"));
    }
}
