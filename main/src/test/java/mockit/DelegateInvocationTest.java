package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.Callable;

import org.junit.Test;

/**
 * The Class DelegateInvocationTest.
 */
public final class DelegateInvocationTest {

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

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
        Collaborator(@SuppressWarnings("unused") int i) {
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        int getValue() {
            return -1;
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
        String doSomething(boolean b, int[] i, String s) {
            return s + b + i[0];
        }

        /**
         * Static method.
         *
         * @return true, if successful
         */
        static boolean staticMethod() {
            return true;
        }

        /**
         * Static method.
         *
         * @param i
         *            the i
         *
         * @return true, if successful
         */
        static boolean staticMethod(int i) {
            return i > 0;
        }

        /**
         * Public method.
         *
         * @param b
         *            the b
         *
         * @return the long
         */
        public long publicMethod(boolean b) {
            return b ? 0L : -1L;
        }
    }

    /**
     * Delegate with context object.
     *
     * @param unused
     *            the unused
     */
    @Test
    public void delegateWithContextObject(@Mocked Collaborator unused) {
        new Expectations() {
            {
                Collaborator.staticMethod();
                result = new Delegate<Object>() {
                    @Mock
                    boolean staticMethod(Invocation context) {
                        assertNull(context.getInvokedInstance());
                        assertEquals(0, context.getInvokedArguments().length);
                        assertEquals(context.getInvocationCount() - 1, context.getInvocationIndex());
                        return context.getInvocationCount() > 0;
                    }
                };
            }
        };

        assertTrue(Collaborator.staticMethod());
        assertTrue(Collaborator.staticMethod());
    }

    /**
     * The Class ConstructorDelegate.
     */
    static class ConstructorDelegate implements Delegate<Void> {

        /** The captured argument. */
        int capturedArgument;

        /**
         * Inits the.
         *
         * @param context
         *            the context
         * @param i
         *            the i
         */
        @Mock
        void init(Invocation context, int i) {
            assertNotNull(context.getInvokedInstance());
            capturedArgument = i + context.getInvocationCount();
        }
    }

    /**
     * Delegate for constructor with context.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateForConstructorWithContext(@Mocked Collaborator mock) {
        final ConstructorDelegate delegate = new ConstructorDelegate();

        new Expectations() {
            {
                new Collaborator(anyInt);
                result = delegate;
            }
        };

        new Collaborator(4);

        assertEquals(5, delegate.capturedArgument);
    }

    /**
     * Delegate receiving null arguments.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateReceivingNullArguments(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.doSomething(true, null, null);
                result = new Delegate<Object>() {
                    @Mock
                    void doSomething(Invocation invocation, Boolean b, int[] i, String s) {
                        Collaborator instance = invocation.getInvokedInstance();
                        assertSame(mock, instance);
                        assertEquals(1, invocation.getInvocationCount());
                        assertTrue(b);
                        assertNull(i);
                        assertNull(s);
                        Object[] args = invocation.getInvokedArguments();
                        assertEquals(3, args.length);
                        assertTrue((Boolean) args[0]);
                        assertNull(args[1]);
                        assertNull(args[2]);
                    }
                };
            }
        };

        assertNull(mock.doSomething(true, null, null));
    }

    /**
     * Delegate with another method on the delegate class.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateWithAnotherMethodOnTheDelegateClass(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = new Delegate<Object>() {
                    @Mock
                    int getValue(Invocation context) {
                        return context.getInvocationCount();
                    }

                    @SuppressWarnings("unused")
                    private void otherMethod(Invocation context) {
                        fail();
                    }
                };
            }
        };

        assertEquals(1, new Collaborator().getValue());
        assertEquals(2, new Collaborator().getValue());
    }

    /**
     * Delegate class with multiple methods and inexact but valid match.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateClassWithMultipleMethodsAndInexactButValidMatch(@Mocked Collaborator mock) {
        new Expectations() {
            {
                Collaborator.staticMethod(1);
                result = new Delegate<Object>() {
                    @SuppressWarnings("unused")
                    private void otherMethod(int i) {
                        fail();
                    }

                    @Mock
                    boolean staticMethod(Invocation invocation, Number i) {
                        return i.intValue() > 0;
                    }
                };
            }
        };

        assertTrue(Collaborator.staticMethod(1));
    }

    /**
     * Delegate method with no parameters for expectation with parameters.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateMethodWithNoParametersForExpectationWithParameters(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.publicMethod(true);
                result = new Delegate<Object>() {
                    @Mock
                    long nonMatchingDelegate() {
                        return 123L;
                    }
                };
            }
        };

        assertEquals(123, mock.publicMethod(true));
    }

    /**
     * Delegate with different method name.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateWithDifferentMethodName(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.publicMethod(anyBoolean);
                result = new Delegate<Object>() {
                    @Mock
                    long differentName(Invocation invocation, boolean b) {
                        assertEquals(1, invocation.getInvocationCount());
                        assertTrue(b);
                        assertSame(Boolean.TRUE, invocation.getInvokedArguments()[0]);
                        return 3L;
                    }
                };
            }
        };

        assertEquals(3L, new Collaborator().publicMethod(true));
    }

    /**
     * Consecutive delegates for the same expectation.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void consecutiveDelegatesForTheSameExpectation(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                returns(new Delegate<Object>() {
                    @Mock
                    int delegate(Invocation invocation) {
                        assertSame(mock, invocation.getInvokedInstance());
                        return invocation.getInvocationCount();
                    }
                }, new Delegate<Object>() {
                    @Mock
                    int delegate(Invocation invocation) {
                        return invocation.getInvocationCount();
                    }
                }, new Delegate<Object>() {
                    @Mock
                    int delegate(Invocation invocation) {
                        assertEquals(3, invocation.getInvocationCount());
                        throw new SecurityException();
                    }
                });
            }
        };

        assertEquals(1, mock.getValue());
        assertEquals(2, mock.getValue());

        try {
            mock.getValue();
            fail();
        } catch (SecurityException ignore) {
            // OK
        }
    }

    /**
     * Delegate method with invocation for interface.
     *
     * @param mock
     *            the mock
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void delegateMethodWithInvocationForInterface(@Mocked final Callable<String> mock) throws Exception {
        new Expectations() {
            {
                mock.call();
                result = new Delegate<Object>() {
                    @Mock
                    String delegate(Invocation inv) {
                        return inv.getInvokedMember().getDeclaringClass().getName();
                    }
                };
            }
        };

        String s = mock.call();

        assertEquals(Callable.class.getName(), s);
    }

    /**
     * Use of context parameters for JRE methods.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void useOfContextParametersForJREMethods() throws Exception {
        final Runtime rt = Runtime.getRuntime();

        new Expectations(rt) {
            {
                rt.exec(anyString, null);
                maxTimes = 1;
                result = new Delegate<Object>() {
                    @Mock
                    void exec(Invocation inv, String command, String[] envp) {
                        assertSame(rt, inv.getInvokedInstance());
                        assertEquals(0, inv.getInvocationIndex());
                        assertNotNull(command);
                        assertNull(envp);
                    }
                };
            }
        };

        assertNull(rt.exec("test", null));
    }
}
