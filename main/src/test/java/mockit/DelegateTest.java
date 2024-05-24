package mockit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class DelegateTest.
 */
@SuppressWarnings("unused")
public final class DelegateTest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

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
        Collaborator(int i) {
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
         * Native method.
         *
         * @param b
         *            the b
         *
         * @return the long
         */
        protected native long nativeMethod(boolean b);

        /**
         * Native method returning an array of A primitive type.
         *
         * @return the int[]
         */
        public native int[] nativeMethodReturningAnArrayOfAPrimitiveType();

        /**
         * Native method returning an array of A reference type.
         *
         * @return the foo[]
         */
        public native Foo[] nativeMethodReturningAnArrayOfAReferenceType();

        /**
         * Native method returning A two dimensional array.
         *
         * @return the string[][]
         */
        public native String[][] nativeMethodReturningATwoDimensionalArray();

        /**
         * Final method.
         *
         * @return the char
         */
        final char finalMethod() {
            return 's';
        }

        /**
         * Adds the elements.
         *
         * @param elements
         *            the elements
         */
        void addElements(List<String> elements) {
            elements.add("one element");
        }

        /**
         * Gets the foo.
         *
         * @return the foo
         */
        Foo getFoo() {
            return null;
        }

        /**
         * Gets the array.
         *
         * @return the array
         */
        byte[] getArray() {
            return null;
        }
    }

    /**
     * The Class Foo.
     */
    static final class Foo {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething() {
            return 1;
        }
    }

    /**
     * Result from delegate.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void resultFromDelegate(@Mocked final Collaborator collaborator) {
        final boolean bExpected = true;
        final int[] iExpected = {};
        final String sExpected = "test";

        new Expectations() {
            {
                collaborator.getValue();
                result = new Delegate<Object>() {
                    int getValue() {
                        return 2;
                    }
                };

                collaborator.doSomething(bExpected, iExpected, sExpected);
                result = new Delegate<Object>() {
                    String doSomething(boolean b, int[] i, String s) {
                        assertEquals(bExpected, b);
                        assertArrayEquals(iExpected, i);
                        assertEquals(sExpected, s);
                        return "";
                    }
                };
            }
        };

        assertEquals(2, collaborator.getValue());
        assertEquals("", collaborator.doSomething(bExpected, iExpected, sExpected));
    }

    /**
     * Consecutive results through delegates having different values.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void consecutiveResultsThroughDelegatesHavingDifferentValues(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = new Delegate<Object>() {
                    int getValue() {
                        return 1;
                    }
                };
                result = new Delegate<Object>() {
                    int getValue() {
                        return 2;
                    }
                };
            }
        };

        Collaborator collaborator = new Collaborator();
        assertEquals(1, collaborator.getValue());
        assertEquals(2, collaborator.getValue());
    }

    /**
     * Consecutive return values through delegates using single returns with varargs.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void consecutiveReturnValuesThroughDelegatesUsingSingleReturnsWithVarargs(
            @Mocked final Collaborator collaborator) {
        final int[] array = { 1, 2 };

        new Expectations() {
            {
                collaborator.doSomething(true, array, "");
                returns(new Delegate<Object>() {
                    String execute(boolean b, int[] i, String s) {
                        assertEquals(1, i[0]);
                        return "a";
                    }
                }, new Delegate<Object>() {
                    String execute(boolean b, int[] i, String s) {
                        assertEquals(2, i[0]);
                        return "b";
                    }
                });
            }
        };

        assertEquals("a", collaborator.doSomething(true, array, ""));

        array[0] = 2;
        assertEquals("b", collaborator.doSomething(true, array, ""));
    }

    /**
     * Result with multiple return values through single delegate.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void resultWithMultipleReturnValuesThroughSingleDelegate(@Mocked final Collaborator collaborator) {
        new Expectations() {
            {
                collaborator.getValue();
                result = new Delegate<Object>() {
                    int i = 1;

                    int getValue() {
                        return i++;
                    }
                };
            }
        };

        assertEquals(1, collaborator.getValue());
        assertEquals(2, collaborator.getValue());
        assertEquals(3, collaborator.getValue());
    }

    /**
     * Constructor delegate with single method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void constructorDelegateWithSingleMethod(@Mocked Collaborator mock) {
        final ConstructorDelegate delegate = new ConstructorDelegate();

        new Expectations() {
            {
                new Collaborator(anyInt);
                result = delegate;
            }
        };

        new Collaborator(4);

        assertTrue(delegate.capturedArgument > 0);
    }

    /**
     * The Class ConstructorDelegate.
     */
    static class ConstructorDelegate implements Delegate<Void> {

        /** The captured argument. */
        int capturedArgument;

        /**
         * Delegate.
         *
         * @param i
         *            the i
         */
        void delegate(int i) {
            capturedArgument = i;
        }
    }

    /**
     * Constructor delegate with multiple methods.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void constructorDelegateWithMultipleMethods(@Mocked Collaborator mock) {
        new Expectations() {
            {
                new Collaborator(anyInt);
                result = new Delegate<Object>() {
                    void init(int i) {
                        if (i < 0)
                            throw new IllegalArgumentException();
                    }

                    private void anotherMethod() {
                    }
                };
            }
        };

        new Collaborator(123);

        try {
            new Collaborator(-123);
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }

    /**
     * Attempt to use constructor delegate with private methods only.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void attemptToUseConstructorDelegateWithPrivateMethodsOnly(@Mocked Collaborator mock) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No non-private instance method found");

        new Expectations() {
            {
                new Collaborator();
                result = new Delegate<Object>() {
                    private void delegate() {
                    }

                    private void anotherMethod() {
                    }
                };
            }
        };
    }

    /**
     * Delegate for static method.
     *
     * @param unused
     *            the unused
     */
    @Test
    public void delegateForStaticMethod(@Mocked Collaborator unused) {
        new Expectations() {
            {
                Collaborator.staticMethod();
                result = new Delegate<Object>() {
                    boolean staticMethod() {
                        return false;
                    }
                };
            }
        };

        assertFalse(Collaborator.staticMethod());
    }

    /**
     * Delegate with static method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateWithStaticMethod(@Mocked Collaborator mock) {
        new Expectations() {
            {
                Collaborator.staticMethod(anyInt);
                result = StaticDelegate.create();
            }
        };

        assertTrue(Collaborator.staticMethod(34));
    }

    /**
     * The Class StaticDelegate.
     */
    static final class StaticDelegate implements Delegate<Object> {

        /**
         * Creates the.
         *
         * @return the static delegate
         */
        static StaticDelegate create() {
            return new StaticDelegate();
        }

        /**
         * Delegate method.
         *
         * @param i
         *            the i
         *
         * @return true, if successful
         */
        boolean delegateMethod(int i) {
            assertEquals(34, i);
            return true;
        }
    }

    /**
     * Delegate for native method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateForNativeMethod(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.nativeMethod(anyBoolean);
                result = new Delegate<Object>() {
                    Long nativeMethod(boolean b) {
                        assertTrue(b);
                        return 0L;
                    }
                };
            }
        };

        assertEquals(0L, new Collaborator().nativeMethod(true));
    }

    /**
     * Delegate for final method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateForFinalMethod(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.finalMethod();
                result = new Delegate<Object>() {
                    char finalMethod() {
                        return 'M';
                    }
                };
            }
        };

        assertEquals('M', new Collaborator().finalMethod());
    }

    /**
     * Delegate for method with compatible but distinct parameter type.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void delegateForMethodWithCompatibleButDistinctParameterType(@Mocked final Collaborator collaborator) {
        new Expectations() {
            {
                collaborator.addElements(this.<List<String>>withNotNull());
                result = new Delegate<Object>() {
                    void delegate(Collection<String> elements) {
                        elements.add("test");
                    }
                };
            }
        };

        List<String> elements = new ArrayList<>();
        new Collaborator().addElements(elements);

        assertTrue(elements.contains("test"));
    }

    /**
     * Delegate receiving null arguments.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void delegateReceivingNullArguments(@Mocked final Collaborator collaborator) {
        new Expectations() {
            {
                collaborator.doSomething(true, null, null);
                result = new Delegate<Object>() {
                    String delegate(boolean b, int[] i, String s) {
                        // noinspection ImplicitArrayToString
                        return b + " " + i + " " + s;
                    }
                };
            }
        };

        String s = new Collaborator().doSomething(true, null, null);
        assertEquals("true null null", s);
    }

    /**
     * Delegate with two methods.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void delegateWithTwoMethods(@Mocked final Collaborator collaborator) {
        new Expectations() {
            {
                collaborator.doSomething(true, null, "str");
                result = new Delegate<Object>() {
                    private String someOther() {
                        return "";
                    }

                    void doSomething(boolean b, int[] i, String s) {
                    }
                };
            }
        };

        assertNull(collaborator.doSomething(true, null, "str"));
    }

    /**
     * Delegate with single method having A different name.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void delegateWithSingleMethodHavingADifferentName(@Mocked final Collaborator collaborator) {
        new Expectations() {
            {
                collaborator.doSomething(true, null, "str");
                result = new Delegate<Object>() {
                    void onReplay(boolean b, int[] i, String s) {
                        assertTrue(b);
                        assertNull(i);
                        assertEquals("str", s);
                    }
                };
            }
        };

        assertNull(new Collaborator().doSomething(true, null, "str"));
    }

    /**
     * Delegate with single method having no parameters.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void delegateWithSingleMethodHavingNoParameters(@Mocked final Collaborator collaborator) {
        new Expectations() {
            {
                collaborator.doSomething(anyBoolean, null, null);
                result = new Delegate<Object>() {
                    String onReplay() {
                        return "action";
                    }
                };
            }
        };

        String result = new Collaborator().doSomething(true, null, null);

        assertEquals("action", result);
    }

    /**
     * Delegate with single method having no parameters except for invocation context.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void delegateWithSingleMethodHavingNoParametersExceptForInvocationContext(
            @Mocked final Collaborator collaborator) {
        new Expectations() {
            {
                collaborator.doSomething(anyBoolean, null, null);
                result = new Delegate<Object>() {
                    void doSomething(Invocation inv) {
                        assertEquals(1, inv.getInvocationCount());
                    }
                };
            }
        };

        assertNull(new Collaborator().doSomething(false, new int[] { 1, 2 }, "test"));
    }

    /**
     * Delegate with one method having different parameters.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void delegateWithOneMethodHavingDifferentParameters(@Mocked final Collaborator collaborator) {
        new Expectations() {
            {
                collaborator.doSomething(true, null, "str");
                result = new Delegate<Object>() {
                    void delegate(boolean b, String s) {
                    }
                };
            }
        };

        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            collaborator.doSomething(true, null, "str");
        });
        assertEquals("Failure to invoke method: void mockit.DelegateTest$18$1.delegate(boolean,java.lang.String)",
                throwable.getMessage());
    }

    /**
     * Delegate with two non private methods.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void delegateWithTwoNonPrivateMethods(@Mocked final Collaborator collaborator) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("More than one candidate delegate method found: ");
        thrown.expectMessage("someOther()");
        thrown.expectMessage("doSomethingElse(boolean,int[],String)");

        new Expectations() {
            {
                collaborator.doSomething(true, null, "str");
                result = new Delegate<Object>() {
                    String someOther() {
                        return "";
                    }

                    void doSomethingElse(boolean b, int[] i, String s) {
                    }
                };
            }
        };
    }

    /**
     * Delegate causing concurrent mock invocation.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateCausingConcurrentMockInvocation(@Mocked final Collaborator mock) {
        final Collaborator collaborator = new Collaborator();
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                collaborator.doSomething(false, null, "");
            }
        });

        new Expectations() {
            {
                mock.getValue();
                times = 1;
                result = new Delegate<Object>() {
                    int executeInAnotherThread() throws Exception {
                        t.start();
                        t.join();
                        return 1;
                    }
                };
            }
        };

        assertEquals(1, collaborator.getValue());
    }

    /**
     * Delegate which calls the same mocked method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateWhichCallsTheSameMockedMethod(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = new Delegate<Object>() {
                    int count;

                    // Would result in a StackOverflowError without a termination condition.
                    int delegate() {
                        return count++ > 1 ? 123 : 1 + mock.getValue();
                    }
                };
            }
        };

        assertEquals(125, mock.getValue());
    }

    /**
     * Delegate which calls another mocked method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateWhichCallsAnotherMockedMethod(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = new Delegate<Object>() {
                    int delegate() {
                        return mock.finalMethod();
                    }
                };

                mock.finalMethod();
                result = 'A';
            }
        };

        assertEquals('A', mock.getValue());
    }

    /**
     * Delegate which calls another mocked method partial mocking of instance.
     */
    @Test
    public void delegateWhichCallsAnotherMockedMethod_partialMockingOfInstance() {
        final Collaborator collaborator = new Collaborator();

        new Expectations(collaborator) {
            {
                collaborator.getValue();
                result = new Delegate<Object>() {
                    int delegate() {
                        return collaborator.finalMethod();
                    }
                };

                collaborator.finalMethod();
                result = 'A';
            }
        };

        assertEquals('A', collaborator.getValue());
    }

    /**
     * Delegate which calls another mocked method injectable mocking.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateWhichCallsAnotherMockedMethod_injectableMocking(@Injectable final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = new Delegate<Object>() {
                    int delegate() {
                        return mock.finalMethod();
                    }
                };

                mock.finalMethod();
                result = 'A';
            }
        };

        assertEquals('A', mock.getValue());
    }

    /**
     * Delegate which calls another mocked method producing A cascaded instance.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void delegateWhichCallsAnotherMockedMethodProducingACascadedInstance(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getFoo().doSomething();
                result = 123;

                mock.getValue();
                result = new Delegate<Object>() {
                    int delegate() {
                        return mock.getFoo().doSomething();
                    }
                };
            }
        };

        assertEquals(123, mock.getFoo().doSomething());
        assertEquals(123, mock.getValue());
    }

    /**
     * Delegate calling mocked method later verified.
     *
     * @param collaborator
     *            the collaborator
     * @param action
     *            the action
     */
    @Test
    public void delegateCallingMockedMethodLaterVerified(@Mocked final Collaborator collaborator,
            @Mocked final Runnable action) {
        new Expectations() {
            {
                collaborator.getFoo();
                result = new Delegate<Object>() {
                    void delegate() {
                        action.run();
                    }
                };
            }
        };

        collaborator.getFoo();

        new Verifications() {
            {
                action.run();
            }
        };
    }

    /**
     * Convert value returned from delegate when returns types differ.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void convertValueReturnedFromDelegateWhenReturnsTypesDiffer(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = new Delegate<Object>() {
                    byte delegate() {
                        return (byte) 123;
                    }
                };
            }
        };

        int value = mock.getValue();

        assertEquals(123, value);
    }

    /**
     * Return inconvertible value from delegate when returns types differ.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnInconvertibleValueFromDelegateWhenReturnsTypesDiffer(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = new Delegate<Object>() {
                    String delegate() {
                        return "abc";
                    }
                };
            }
        };

        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            mock.getValue();
        });
        assertEquals(
                "Value of type String incompatible with return type int of mockit.DelegateTest$Collaborator#getValue()",
                throwable.getMessage());
    }

    /**
     * Return void from delegate method for recorded method having primitive return type.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnVoidFromDelegateMethodForRecordedMethodHavingPrimitiveReturnType(
            @Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = new Delegate<Object>() {
                    void delegate() {
                    }
                };
            }
        };

        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> {
            mock.getValue();
        });
        assertEquals(
                "void return type incompatible with return type int of mockit.DelegateTest$Collaborator#getValue()",
                throwable.getMessage());
    }

    /**
     * Return byte array from delegate method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnByteArrayFromDelegateMethod(@Mocked final Collaborator mock) {
        final byte[] bytes = "test".getBytes();
        new Expectations() {
            {
                mock.getArray();
                result = new Delegate<Object>() {
                    byte[] delegate() {
                        return bytes;
                    }
                };
            }
        };

        assertSame(bytes, mock.getArray());
    }

    /**
     * Return value of correct type from delegate method returning A supertype.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnValueOfCorrectTypeFromDelegateMethodReturningASupertype(@Mocked final Collaborator mock) {
        final Foo expected = new Foo();

        new Expectations() {
            {
                mock.getFoo();
                result = new Delegate<Object>() {
                    Object delegate() {
                        return expected;
                    }
                };
            }
        };

        Foo actual = mock.getFoo();

        assertSame(expected, actual);
    }
}
