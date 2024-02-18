package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

/**
 * The Class FakeInvocationTest.
 */
public final class FakeInvocationTest {

    /**
     * The Class Collaborator.
     */
    public static class Collaborator {

        /** The value. */
        int value;

        /**
         * Instantiates a new collaborator.
         */
        Collaborator() {
        }

        /**
         * Instantiates a new collaborator.
         *
         * @param i
         *            the i
         */
        public Collaborator(int i) {
            value = i;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public int getValue() {
            return -1;
        }

        /**
         * Sets the value.
         *
         * @param i
         *            the new value
         */
        public void setValue(int i) {
            value = i;
        }

        /**
         * Do something.
         *
         * @param b
         *            the b
         * @param i
         *            the i
         * @param s
         *            the s
         *
         * @return the string
         */
        public String doSomething(boolean b, int[] i, String s) {
            return s + b + i[0];
        }

        /**
         * Static method.
         *
         * @return true, if successful
         */
        public static boolean staticMethod() {
            return true;
        }
    }

    /**
     * The Class FakeMethods.
     */
    static final class FakeMethods extends MockUp<Collaborator> {

        /**
         * Static method.
         *
         * @param context
         *            the context
         *
         * @return true, if successful
         */
        @Mock
        static boolean staticMethod(Invocation context) {
            assertNotNull(context);
            assertNull(context.getInvokedInstance());
            assertEquals(1, context.getInvocationCount());
            return false;
        }

        /**
         * Gets the value.
         *
         * @param context
         *            the context
         *
         * @return the value
         */
        @Mock
        int getValue(Invocation context) {
            assertTrue(context.getInvokedInstance() instanceof Collaborator);
            assertEquals(0, context.getInvocationIndex());
            return 123;
        }
    }

    /**
     * Fake methods for methods without parameters.
     */
    @Test
    public void fakeMethodsForMethodsWithoutParameters() {
        new FakeMethods();
        assertFalse(Collaborator.staticMethod());
        assertEquals(123, new Collaborator().getValue());
    }

    /**
     * Instance fake method for static method.
     */
    @Test
    public void instanceFakeMethodForStaticMethod() {
        new MockUp<Collaborator>() {
            @Mock
            boolean staticMethod(Invocation context) {
                assertNull(context.getInvokedInstance());
                assertEquals(context.getInvocationCount() - 1, context.getInvocationIndex());
                return context.getInvocationCount() <= 0;
            }
        };

        assertFalse(Collaborator.staticMethod());
        assertFalse(Collaborator.staticMethod());
    }

    /**
     * Fake methods with invocation parameter.
     */
    @Test
    public void fakeMethodsWithInvocationParameter() {
        new MockUp<Collaborator>() {
            Collaborator instantiated;

            @Mock
            void $init(Invocation inv, int i) {
                assertNotNull(inv.getInvokedInstance());
                assertTrue(i > 0);
                instantiated = inv.getInvokedInstance();
            }

            @Mock
            String doSomething(Invocation inv, boolean b, int[] array, String s) {
                assertNotNull(inv);
                assertSame(instantiated, inv.getInvokedInstance());
                assertEquals(1, inv.getInvocationCount());
                assertTrue(b);
                assertNull(array);
                assertEquals("test", s);
                return "mock";
            }
        };

        String s = new Collaborator(123).doSomething(true, null, "test");
        assertEquals("mock", s);
    }

    /**
     * The Class FakeMethodsWithParameters.
     */
    static class FakeMethodsWithParameters extends MockUp<Collaborator> {

        /** The captured argument. */
        int capturedArgument;

        /** The faked instance. */
        Collaborator fakedInstance;

        /**
         * $init.
         *
         * @param context
         *            the context
         * @param i
         *            the i
         */
        @Mock
        void $init(Invocation context, int i) {
            capturedArgument = i + context.getInvocationCount();
            assertNull(fakedInstance);
            assertTrue(context.getInvokedInstance() instanceof Collaborator);
            assertEquals(1, context.getInvokedArguments().length);
        }

        /**
         * Sets the value.
         *
         * @param context
         *            the context
         * @param i
         *            the i
         */
        @Mock
        void setValue(Invocation context, int i) {
            assertEquals(i, context.getInvocationIndex());
            assertSame(fakedInstance, context.getInvokedInstance());
            assertEquals(1, context.getInvokedArguments().length);
        }
    }

    /**
     * Fake methods with parameters.
     */
    @Test
    public void fakeMethodsWithParameters() {
        FakeMethodsWithParameters mock = new FakeMethodsWithParameters();

        Collaborator col = new Collaborator(4);
        mock.fakedInstance = col;

        assertEquals(5, mock.capturedArgument);
        col.setValue(0);
        col.setValue(1);
    }

    /**
     * Use of context parameters for JRE methods.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void useOfContextParametersForJREMethods() throws Exception {
        new MockUp<Runtime>() {
            @Mock
            Process exec(Invocation inv, String command, String[] envp) {
                assertSame(Runtime.getRuntime(), inv.getInvokedInstance());
                assertEquals(0, inv.getInvocationIndex());
                assertNotNull(command);
                assertNull(envp);
                return null;
            }
        };

        assertNull(Runtime.getRuntime().exec("test", null));
    }

    /**
     * The Class FakeByMethodNameOnly.
     */
    public static final class FakeByMethodNameOnly extends MockUp<Collaborator> {

        /**
         * Do something.
         *
         * @param inv
         *            the inv
         *
         * @return the string
         */
        @Mock
        public static String doSomething(Invocation inv) {
            Object[] args = inv.getInvokedArguments();
            assertEquals(3, args.length);
            return "fake";
        }
    }

    /**
     * Fake method by name only using public fake.
     */
    @Test
    public void fakeMethodByNameOnly_usingPublicFake() {
        new FakeByMethodNameOnly();

        String result = new Collaborator().doSomething(true, new int[] { 1, 2 }, "test");

        assertEquals("fake", result);
    }

    /**
     * Fake method by name only using anonymous fake.
     */
    @Test
    public void fakeMethodByNameOnly_usingAnonymousFake() {
        new MockUp<Collaborator>() {
            @Mock
            String doSomething(Invocation inv) {
                Object[] args = inv.getInvokedArguments();
                assertEquals(3, args.length);
                return "fake";
            }
        };

        String result = new Collaborator().doSomething(true, new int[] { 1, 2 }, "test");

        assertEquals("fake", result);
    }

    /**
     * The Class PublicFakeForConstructorUsingInvocation.
     */
    public static final class PublicFakeForConstructorUsingInvocation extends MockUp<Collaborator> {

        /**
         * $init.
         *
         * @param inv
         *            the inv
         */
        @Mock
        public void $init(Invocation inv) {
        }
    }

    /**
     * Fake constructor using public fake class with public fake method having invocation parameter.
     */
    @Test
    public void fakeConstructorUsingPublicFakeClassWithPublicFakeMethodHavingInvocationParameter() {
        new PublicFakeForConstructorUsingInvocation();

        new Collaborator();
    }
}
