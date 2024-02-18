package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The Class FakeClassInstantiationPerSetupTest.
 */
public final class FakeClassInstantiationPerSetupTest {

    /**
     * The Class RealClass1.
     */
    public static final class RealClass1 {

        /**
         * Do something.
         */
        public static void doSomething() {
            throw new RuntimeException();
        }

        /**
         * Perform computation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         *
         * @return the int
         */
        public int performComputation(int a, boolean b) {
            return b ? a : -a;
        }
    }

    /**
     * The Class RealClass2.
     */
    public static final class RealClass2 {

        /**
         * Do something.
         */
        public static void doSomething() {
            throw new RuntimeException();
        }

        /**
         * Perform computation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         *
         * @return the int
         */
        public int performComputation(int a, boolean b) {
            return b ? a : -a;
        }
    }

    /**
     * The Class RealClass3.
     */
    public static final class RealClass3 {

        /**
         * Do something.
         */
        public static void doSomething() {
            throw new RuntimeException();
        }

        /**
         * Perform computation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         *
         * @return the int
         */
        public int performComputation(int a, boolean b) {
            return b ? a : -a;
        }
    }

    /**
     * The Class RealClass4.
     */
    public static final class RealClass4 {

        /**
         * Do something.
         */
        public static void doSomething() {
            throw new RuntimeException();
        }

        /**
         * Perform computation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         *
         * @return the int
         */
        public int performComputation(int a, boolean b) {
            return b ? a : -a;
        }
    }

    /**
     * The Class FakeClass1.
     */
    static final class FakeClass1 extends MockUp<RealClass1> {

        /** The single instance created. */
        static Object singleInstanceCreated;

        /**
         * Instantiates a new fake class 1.
         */
        FakeClass1() {
            assertNull(singleInstanceCreated);
            singleInstanceCreated = this;
        }

        /**
         * Do something.
         */
        @Mock
        void doSomething() {
            assertSame(singleInstanceCreated, this);
        }

        /**
         * Perform computation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         *
         * @return the int
         */
        @Mock
        int performComputation(int a, boolean b) {
            assertSame(singleInstanceCreated, this);
            assertTrue(a > 0);
            assertTrue(b);
            return 2;
        }
    }

    /**
     * The Class FakeClass2.
     */
    static final class FakeClass2 extends MockUp<RealClass2> {

        /** The single instance created. */
        static Object singleInstanceCreated;

        /**
         * Instantiates a new fake class 2.
         */
        FakeClass2() {
            assertNull(singleInstanceCreated);
            singleInstanceCreated = this;
        }

        /**
         * Do something.
         */
        @Mock
        void doSomething() {
            assertSame(singleInstanceCreated, this);
        }

        /**
         * Perform computation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         *
         * @return the int
         */
        @Mock
        int performComputation(int a, boolean b) {
            assertSame(singleInstanceCreated, this);
            assertTrue(a > 0);
            assertTrue(b);
            return 2;
        }
    }

    /**
     * The Class FakeClass3.
     */
    static final class FakeClass3 extends MockUp<RealClass3> {

        /** The single instance created. */
        static Object singleInstanceCreated;

        /**
         * Instantiates a new fake class 3.
         */
        FakeClass3() {
            assertNull(singleInstanceCreated);
            singleInstanceCreated = this;
        }

        /**
         * Do something.
         */
        @Mock
        void doSomething() {
            assertSame(singleInstanceCreated, this);
        }

        /**
         * Perform computation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         *
         * @return the int
         */
        @Mock
        int performComputation(int a, boolean b) {
            assertSame(singleInstanceCreated, this);
            assertTrue(a > 0);
            assertTrue(b);
            return 2;
        }
    }

    /**
     * The Class FakeClass4.
     */
    static final class FakeClass4 extends MockUp<RealClass4> {

        /** The single instance created. */
        static Object singleInstanceCreated;

        /**
         * Instantiates a new fake class 4.
         */
        FakeClass4() {
            assertNull(singleInstanceCreated);
            singleInstanceCreated = this;
        }

        /**
         * Do something.
         */
        @Mock
        void doSomething() {
            assertSame(singleInstanceCreated, this);
        }

        /**
         * Perform computation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         *
         * @return the int
         */
        @Mock
        int performComputation(int a, boolean b) {
            assertSame(singleInstanceCreated, this);
            assertTrue(a > 0);
            assertTrue(b);
            return 2;
        }
    }

    /**
     * Sets the up class level fakes.
     */
    @BeforeClass
    public static void setUpClassLevelFakes() {
        new FakeClass1();
    }

    /**
     * Sets the up additional class level fakes.
     */
    @BeforeClass
    public static void setUpAdditionalClassLevelFakes() {
        new FakeClass2();
    }

    /**
     * Sets the up method level fakes.
     */
    @Before
    public void setUpMethodLevelFakes() {
        FakeClass3.singleInstanceCreated = null;
        new FakeClass3();
    }

    /**
     * Fake instance per setup in class and fixture scopes.
     */
    @Test
    public void fakeInstancePerSetupInClassAndFixtureScopes() {
        assertFakeClass1();
        assertFakeClass2();
        assertFakeClass3();
        assertEquals(1, new RealClass4().performComputation(1, true));
    }

    /**
     * Assert fake class 1.
     */
    void assertFakeClass1() {
        RealClass1.doSomething();
        assertEquals(2, new RealClass1().performComputation(1, true));
    }

    /**
     * Assert fake class 2.
     */
    void assertFakeClass2() {
        RealClass2.doSomething();
        assertEquals(2, new RealClass2().performComputation(1, true));
    }

    /**
     * Assert fake class 3.
     */
    void assertFakeClass3() {
        RealClass3.doSomething();
        assertEquals(2, new RealClass3().performComputation(1, true));
    }

    /**
     * Assert fake class 4.
     */
    void assertFakeClass4() {
        RealClass4.doSomething();
        assertEquals(2, new RealClass4().performComputation(1, true));
    }

    /**
     * Fake instance per setup in all scopes.
     */
    @Test
    public void fakeInstancePerSetupInAllScopes() {
        new FakeClass4();

        assertFakeClass1();
        assertFakeClass2();
        assertFakeClass3();
        assertFakeClass4();
    }

    /**
     * The Class FakeURL.
     */
    public static final class FakeURL extends MockUp<URL> {

        /**
         * Open stream.
         *
         * @param inv
         *            the inv
         *
         * @return the input stream
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Mock
        public InputStream openStream(Invocation inv) throws IOException {
            URL it = inv.getInvokedInstance();

            if ("test".equals(it.getHost())) {
                return new ByteArrayInputStream("response".getBytes());
            }

            return it.openStream();
        }
    }

    /**
     * Reentrant fake for JRE class.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void reentrantFakeForJREClass() throws Exception {
        new FakeURL();

        InputStream response = new URL("http://test").openStream();

        assertEquals("response", new Scanner(response).nextLine());
    }
}
