package mockit;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;

import org.junit.Test;

/**
 * The Class DelegateInvocationProceedTest.
 */
public final class DelegateInvocationProceedTest {

    /**
     * The Class BaseClassToBeMocked.
     */
    public static class BaseClassToBeMocked {

        /** The name. */
        protected String name;

        /**
         * Gets the name.
         *
         * @return the name
         */
        public final String getName() {
            return name;
        }

        /**
         * Base method.
         *
         * @param i
         *            the i
         *
         * @return the int
         */
        public final int baseMethod(int i) {
            return i + 1;
        }

        /**
         * Method to be mocked.
         *
         * @param i
         *            the i
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        protected int methodToBeMocked(int i) throws IOException {
            return i;
        }
    }

    /**
     * The Class ClassToBeMocked.
     */
    public static class ClassToBeMocked extends BaseClassToBeMocked {

        /**
         * Instantiates a new class to be mocked.
         */
        public ClassToBeMocked() {
            name = "";
        }

        /**
         * Instantiates a new class to be mocked.
         *
         * @param name
         *            the name
         */
        public ClassToBeMocked(String name) {
            this.name = name;
        }

        /**
         * Method to be mocked.
         *
         * @return true, if successful
         */
        public boolean methodToBeMocked() {
            return true;
        }

        @Override
        protected int methodToBeMocked(int i) throws IOException {
            return super.methodToBeMocked(i);
        }

        /**
         * Method to be mocked.
         *
         * @param i
         *            the i
         * @param args
         *            the args
         *
         * @return the int
         */
        int methodToBeMocked(int i, Object... args) {
            int result = i;

            for (Object arg : args) {
                if (arg != null) {
                    result++;
                }
            }

            return result;
        }

        /**
         * Another method to be mocked.
         *
         * @param s
         *            the s
         * @param b
         *            the b
         * @param ints
         *            the ints
         *
         * @return the string
         */
        String anotherMethodToBeMocked(String s, boolean b, List<Integer> ints) {
            return (b ? s.toUpperCase() : s.toLowerCase()) + ints;
        }
    }

    /**
     * Proceed from delegate method on regular mocked class.
     *
     * @param mocked
     *            the mocked
     */
    @Test
    public void proceedFromDelegateMethodOnRegularMockedClass(@Mocked final ClassToBeMocked mocked) {
        new Expectations() {
            {
                mocked.methodToBeMocked();
                result = new Delegate<Object>() {
                    @Mock
                    boolean delegate(Invocation inv) {
                        return inv.proceed();
                    }
                };
            }
        };

        assertTrue(mocked.methodToBeMocked());
    }

    /**
     * Proceed from delegate method on injectable mocked class.
     *
     * @param mocked
     *            the mocked
     */
    @Test
    public void proceedFromDelegateMethodOnInjectableMockedClass(@Injectable final ClassToBeMocked mocked) {
        new Expectations() {
            {
                mocked.methodToBeMocked();
                result = new Delegate<Object>() {
                    @Mock
                    boolean delegate(Invocation inv) {
                        return inv.proceed();
                    }
                };
            }
        };

        assertTrue(mocked.methodToBeMocked());
    }

    /**
     * Proceed from delegate method with parameters.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void proceedFromDelegateMethodWithParameters() throws Exception {
        final ClassToBeMocked mocked = new ClassToBeMocked();

        new Expectations(mocked) {
            {
                mocked.methodToBeMocked(anyInt);
                result = new Delegate<Object>() {
                    @Mock
                    int delegate(Invocation inv, int i) {
                        Integer j = inv.proceed();
                        return j + 1;
                    }
                };

                mocked.methodToBeMocked(anyInt, (Object[]) any);
                maxTimes = 1;
                result = new Delegate<Object>() {
                    @Mock
                    Integer delegate(Invocation inv, int i, Object... args) {
                        args[2] = "mock";
                        return inv.proceed();
                    }
                };
            }
        };

        assertEquals(124, mocked.methodToBeMocked(123));
        assertEquals(-8, mocked.methodToBeMocked(-9));
        assertEquals(7, mocked.methodToBeMocked(3, "Test", new Object(), null, 45));
    }

    /**
     * Proceed conditionally from delegate method.
     */
    @Test
    public void proceedConditionallyFromDelegateMethod() {
        final ClassToBeMocked mocked = new ClassToBeMocked();

        new Expectations(mocked) {
            {
                mocked.anotherMethodToBeMocked(anyString, anyBoolean, null);
                result = new Delegate<Object>() {
                    @Mock
                    String delegate(Invocation inv, String s, boolean b, List<Number> ints) {
                        if (!b) {
                            return s;
                        }

                        ints.add(45);
                        return inv.proceed();
                    }
                };
            }
        };

        // Do not proceed:
        assertNull(mocked.anotherMethodToBeMocked(null, false, null));

        // Do proceed:
        List<Integer> values = new ArrayList<>();
        assertEquals("TEST[45]", mocked.anotherMethodToBeMocked("test", true, values));

        // Do not proceed again:
        assertEquals("No proceed", mocked.anotherMethodToBeMocked("No proceed", false, null));
    }

    /**
     * Proceed from delegate method into real method with modified arguments.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void proceedFromDelegateMethodIntoRealMethodWithModifiedArguments() throws Exception {
        final ClassToBeMocked mocked = new ClassToBeMocked();

        new Expectations(mocked) {
            {
                mocked.methodToBeMocked(anyInt);
                result = new Delegate<Object>() {
                    @Mock
                    Integer delegate1(Invocation invocation, int i) {
                        return invocation.proceed(i + 2);
                    }
                };

                mocked.methodToBeMocked(anyInt, (Object[]) any);
                result = new Delegate<Object>() {
                    @Mock
                    Integer delegate2(Invocation inv, int i, Object... args) {
                        Object[] newArgs = { 2, "3" };
                        return inv.proceed(1, newArgs);
                    }
                };
            }
        };

        assertEquals(3, mocked.methodToBeMocked(1));
        assertEquals(3, mocked.methodToBeMocked(-2, null, "Abc", true, 'a'));
    }

    /**
     * Proceed from delegate method into constructor.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void proceedFromDelegateMethodIntoConstructor(@Mocked ClassToBeMocked mock) {
        new Expectations() {
            {
                new ClassToBeMocked();
                result = new Delegate<Object>() {
                    @Mock
                    void init(Invocation inv) {
                        assertNotNull(inv.getInvokedInstance());
                        inv.proceed();
                    }
                };
            }
        };

        ClassToBeMocked obj = new ClassToBeMocked();
        assertEquals("", obj.name);
    }

    /**
     * Proceed conditionally from delegate method into constructor.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void proceedConditionallyFromDelegateMethodIntoConstructor(@Mocked ClassToBeMocked mock) {
        new Expectations() {
            {
                new ClassToBeMocked(anyString);
                result = new Delegate<Object>() {
                    @Mock
                    void init(Invocation inv, String name) {
                        assertNotNull(inv.getInvokedInstance());

                        if ("proceed".equals(name)) {
                            inv.proceed();
                        }
                    }
                };
            }
        };

        assertEquals("proceed", new ClassToBeMocked("proceed").name);
        assertNull(new ClassToBeMocked("do not proceed").name);
    }

    /**
     * Proceed from delegate method into JRE constructor.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void proceedFromDelegateMethodIntoJREConstructor(@Mocked ProcessBuilder mock) {
        new Expectations() {
            {
                ProcessBuilder pb = new ProcessBuilder((String[]) any);
                result = new Delegate<Object>() {
                    @Mock
                    void init(Invocation inv) {
                        inv.proceed();
                    }
                };

                pb.command();
                result = new Delegate<Object>() {
                    @Mock
                    List<String> delegate(Invocation inv) {
                        return inv.proceed();
                    }
                };
            }
        };

        ProcessBuilder pb1 = new ProcessBuilder("proceed");
        assertEquals("proceed", pb1.command().get(0));

        ProcessBuilder pb2 = new ProcessBuilder("proceed", "again");
        assertEquals(asList("proceed", "again"), pb2.command());
    }

    /**
     * Proceed from delegate method into method inherited from base class.
     */
    @Test
    public void proceedFromDelegateMethodIntoMethodInheritedFromBaseClass() {
        final ClassToBeMocked obj = new ClassToBeMocked();

        new Expectations(obj) {
            {
                obj.baseMethod(anyInt);
                result = new Delegate<Object>() {
                    @Mock
                    int baseMethod(Invocation inv, int i) {
                        return inv.proceed(i + 1);
                    }
                };
            }
        };

        assertEquals(3, obj.baseMethod(1));
    }

    /**
     * Proceed from delegate method into overriding method which calls super.
     *
     * @param mocked
     *            the mocked
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void proceedFromDelegateMethodIntoOverridingMethodWhichCallsSuper(@Mocked final ClassToBeMocked mocked)
            throws Exception {
        new Expectations() {
            {
                mocked.methodToBeMocked(1);
                result = new Delegate<Object>() {
                    @Mock
                    int delegate(Invocation inv) {
                        return inv.proceed();
                    }
                };
            }
        };

        assertEquals(1, mocked.methodToBeMocked(1));
    }

    /**
     * Proceed from delegate method into overriding method that calls super which also has A proceeding delegate.
     *
     * @param mockedBase
     *            the mocked base
     * @param mocked
     *            the mocked
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void proceedFromDelegateMethodIntoOverridingMethodThatCallsSuperWhichAlsoHasAProceedingDelegate(
            @Mocked final BaseClassToBeMocked mockedBase, @Mocked final ClassToBeMocked mocked) throws Exception {
        new Expectations() {
            {
                mockedBase.methodToBeMocked(1);
                result = new Delegate<Object>() {
                    // Will not execute when calling on subclass instance.
                    @Mock
                    int delegate(Invocation inv) {
                        int i = inv.proceed();
                        return i + 1;
                    }
                };

                mocked.methodToBeMocked(1);
                result = new Delegate<Object>() {
                    @Mock
                    int delegate(Invocation inv) {
                        return inv.proceed();
                    }
                };
            }
        };

        assertEquals(2, mockedBase.methodToBeMocked(1));
        assertEquals(1, mocked.methodToBeMocked(1));
    }

    /**
     * Throw exception from proceed into JRE method.
     *
     * @param c1
     *            the c 1
     * @param c2
     *            the c 2
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void throwExceptionFromProceedIntoJREMethod(@Injectable final AbstractExecutorService c1,
            @Mocked final ClassToBeMocked c2) throws Exception {
        new Expectations() {
            {
                c1.submit((Runnable) any);
                result = new Delegate<Object>() {
                    @Mock
                    void delegate(Invocation inv) {
                        inv.proceed();
                    }
                };
            }
        };

        try {
            c1.submit((Runnable) null);
            fail();
        } catch (NullPointerException ignored) {
            new Expectations() {
                {
                    c2.methodToBeMocked(anyInt);
                    result = 123;
                }
            };

            assertEquals(123, c2.methodToBeMocked(1));
        }
    }
}
