package mockit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * The Class InstanceSpecificMockingTest.
 */
public final class InstanceSpecificMockingTest {

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /** The value. */
        protected final int value;

        /**
         * Instantiates a new collaborator.
         */
        Collaborator() {
            value = -1;
        }

        /**
         * Instantiates a new collaborator.
         *
         * @param value
         *            the value
         */
        Collaborator(int value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        int getValue() {
            return value;
        }

        /**
         * Simple operation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         * @param c
         *            the c
         *
         * @return true, if successful
         */
        @SuppressWarnings("unused")
        final boolean simpleOperation(int a, String b, Date c) {
            return true;
        }

        /**
         * Do something.
         *
         * @param b
         *            the b
         * @param s
         *            the s
         */
        @SuppressWarnings("unused")
        static void doSomething(boolean b, String s) {
            throw new IllegalStateException();
        }
    }

    /** The previous instance. */
    final Collaborator previousInstance = new Collaborator();

    /** The mock. */
    @Injectable
    Collaborator mock;

    /**
     * Exercise injected instance during replay only.
     */
    @Test
    public void exerciseInjectedInstanceDuringReplayOnly() {
        assertThatPreviouslyCreatedInstanceIsNotMocked();

        assertEquals(0, mock.value);
        assertEquals(0, mock.getValue());
        assertFalse(mock.simpleOperation(1, "test", null));

        assertThatNewlyCreatedInstanceIsNotMocked();
    }

    /**
     * Assert that previously created instance is not mocked.
     */
    void assertThatPreviouslyCreatedInstanceIsNotMocked() {
        assertEquals(-1, previousInstance.value);
        assertEquals(-1, previousInstance.getValue());
        assertTrue(previousInstance.simpleOperation(1, "test", null));
    }

    /**
     * Assert that newly created instance is not mocked.
     */
    void assertThatNewlyCreatedInstanceIsNotMocked() {
        Collaborator newInstance = new Collaborator();
        assertEquals(-1, newInstance.value);
        assertEquals(-1, newInstance.getValue());
        assertTrue(newInstance.simpleOperation(1, "test", null));
    }

    /**
     * Mock specific instance.
     */
    @Test
    public void mockSpecificInstance() {
        new Expectations() {
            {
                mock.simpleOperation(1, "", null);
                result = false;
                mock.getValue();
                result = 123;
                times = 1;
            }
        };

        assertFalse(mock.simpleOperation(1, "", null));
        assertEquals(123, mock.getValue());
        assertThatPreviouslyCreatedInstanceIsNotMocked();
        assertThatNewlyCreatedInstanceIsNotMocked();

        try {
            Collaborator.doSomething(false, null);
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    /**
     * Use A second mock instance of the same type.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    public void useASecondMockInstanceOfTheSameType(@Injectable final Collaborator mock2) {
        assertThatPreviouslyCreatedInstanceIsNotMocked();

        new Expectations() {
            {
                mock2.getValue();
                result = 2;
                mock.getValue();
                returns(1, 3);
            }
        };

        assertEquals(1, mock.getValue());
        assertEquals(2, mock2.getValue());
        assertEquals(3, mock.getValue());
        assertEquals(2, mock2.getValue());
        assertEquals(3, mock.getValue());

        assertThatPreviouslyCreatedInstanceIsNotMocked();
        assertThatNewlyCreatedInstanceIsNotMocked();
    }

    // Injectable mocks of unusual types ///////////////////////////////////////////////////////////////////////////////

    /**
     * Allow injectable mock of interface type.
     *
     * @param runnable
     *            the runnable
     */
    @Test
    public void allowInjectableMockOfInterfaceType(@Injectable final Runnable runnable) {
        runnable.run();
        runnable.run();

        new Verifications() {
            {
                runnable.run();
                minTimes = 1;
                maxTimes = 2;
            }
        };
    }

    /**
     * Allow injectable mock of annotation type.
     *
     * @param runWith
     *            the run with
     */
    @Test
    public void allowInjectableMockOfAnnotationType(@Injectable final RunWith runWith) {
        new Expectations() {
            {
                runWith.value();
                result = BlockJUnit4ClassRunner.class;
            }
        };

        assertSame(BlockJUnit4ClassRunner.class, runWith.value());
    }

    // Mocking java.nio.ByteBuffer /////////////////////////////////////////////////////////////////////////////////////

    /**
     * Mock byte buffer as injectable.
     *
     * @param buf
     *            the buf
     */
    @Test
    public void mockByteBufferAsInjectable(@Injectable final ByteBuffer buf) {
        ByteBuffer realBuf = ByteBuffer.allocateDirect(10);
        assertNotNull(realBuf);
        assertEquals(10, realBuf.capacity());

        new Expectations() {
            {
                buf.isDirect();
                result = true;

                // Calling "getBytes()" here indirectly creates a new ByteBuffer, requiring use of @Injectable.
                buf.put("Test".getBytes());
                times = 1;
            }
        };

        assertTrue(buf.isDirect());
        buf.put("Test".getBytes());
    }

    /**
     * Mock byte buffer regularly.
     *
     * @param mockBuffer
     *            the mock buffer
     */
    // TODO JWL 10/30/2022 Test is very flaky, ignore it
    @Ignore
    @Test
    public void mockByteBufferRegularly(@Mocked ByteBuffer mockBuffer) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(10);
        // noinspection MisorderedAssertEqualsArguments
        assertSame(mockBuffer, buffer);

        new Verifications() {
            {
                ByteBuffer.allocateDirect(anyInt);
            }
        };
    }

    /**
     * Mock byte buffer as cascading.
     *
     * @param unused
     *            the unused
     */
    // TODO JWL 10/30/2022 Test is very flaky, ignore it
    @Ignore
    @Test
    public void mockByteBufferAsCascading(@Mocked ByteBuffer unused) {
        ByteBuffer cascadedBuf = ByteBuffer.allocateDirect(10);
        assertNotNull(cascadedBuf);
        assertEquals(0, cascadedBuf.capacity());
    }

    /**
     * A factory for creating Buffer objects.
     */
    static class BufferFactory {
        /**
         * Creates a new Buffer object.
         *
         * @return the byte buffer
         */
        ByteBuffer createBuffer() {
            return null;
        }
    }

    /**
     * Mock byte buffer as cascaded mock.
     *
     * @param cascadingMock
     *            the cascading mock
     */
    @Test
    public void mockByteBufferAsCascadedMock(@Mocked BufferFactory cascadingMock) {
        ByteBuffer realBuf1 = ByteBuffer.allocateDirect(10);
        assertEquals(10, realBuf1.capacity());

        ByteBuffer cascadedBuf = cascadingMock.createBuffer();
        assertEquals(0, cascadedBuf.capacity());

        ByteBuffer realBuf2 = ByteBuffer.allocateDirect(20);
        assertEquals(20, realBuf2.capacity());
    }

    // Mocking java.io.InputStream /////////////////////////////////////////////////////////////////////////////////////

    /**
     * The Class ConcatenatingInputStream.
     */
    public static final class ConcatenatingInputStream extends InputStream {

        /** The sequential inputs. */
        private final Queue<InputStream> sequentialInputs;

        /** The current input. */
        private InputStream currentInput;

        /**
         * Instantiates a new concatenating input stream.
         *
         * @param sequentialInputs
         *            the sequential inputs
         */
        public ConcatenatingInputStream(InputStream... sequentialInputs) {
            this.sequentialInputs = new LinkedList<>(Arrays.asList(sequentialInputs));
            currentInput = this.sequentialInputs.poll();
        }

        @Override
        public int read() throws IOException {
            if (currentInput == null) {
                return -1;
            }

            int nextByte = currentInput.read();

            if (nextByte >= 0) {
                return nextByte;
            }

            currentInput = sequentialInputs.poll();
            // noinspection TailRecursion
            return read();
        }
    }

    /**
     * Concatenate input streams.
     *
     * @param input1
     *            the input 1
     * @param input2
     *            the input 2
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void concatenateInputStreams(@Injectable final InputStream input1, @Injectable final InputStream input2)
            throws Exception {
        new Expectations() {
            {
                input1.read();
                returns(1, 2, -1);
                input2.read();
                returns(3, -1);
            }
        };

        InputStream concatenatedInput = new ConcatenatingInputStream(input1, input2);
        byte[] buf = new byte[3];
        concatenatedInput.read(buf);
        concatenatedInput.close();

        byte[] expectedBytes = { 1, 2, 3 };
        assertArrayEquals(expectedBytes, buf);
    }
}
