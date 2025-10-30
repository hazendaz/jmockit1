package mockit;

import static java.util.Arrays.asList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import mockit.internal.expectations.invocation.MissingInvocation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * The Class ExpectationsWithVarArgsMatchersTest.
 */
final class ExpectationsWithVarArgsMatchersTest {

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /**
         * Complex operation.
         *
         * @param input1
         *            the input 1
         * @param otherInputs
         *            the other inputs
         *
         * @return the list
         */
        List<?> complexOperation(Object input1, Object... otherInputs) {
            return input1 == null ? Collections.emptyList() : asList(otherInputs);
        }

        /**
         * Another operation.
         *
         * @param i
         *            the i
         * @param b
         *            the b
         * @param s
         *            the s
         * @param otherStrings
         *            the other strings
         *
         * @return the int
         */
        @SuppressWarnings("unused")
        int anotherOperation(int i, boolean b, String s, String... otherStrings) {
            return -1;
        }

        /**
         * Do something.
         *
         * @param i
         *            the i
         * @param values
         *            the values
         *
         * @return true, if successful
         */
        static boolean doSomething(int i, Object... values) {
            return i + values.length > 0;
        }
    }

    /**
     * The Interface Dependency.
     */
    public interface Dependency {
        /**
         * Do something.
         *
         * @param args
         *            the args
         */
        void doSomething(String... args);
    }

    /** The mock. */
    @Mocked
    Collaborator mock;

    /** The mock 2. */
    @Mocked
    Dependency mock2;

    /**
     * Replay varargs method with different than expected non varargs argument.
     */
    @Test
    void replayVarargsMethodWithDifferentThanExpectedNonVarargsArgument() {
        assertThrows(MissingInvocation.class, () -> {

            mock.complexOperation(2, 2, 3);

            new Verifications() {
                {
                    mock.complexOperation(1, 2, 3);
                }
            };
        });
    }

    /**
     * Replay varargs method with different than expected number of varargs arguments.
     */
    @Test
    void replayVarargsMethodWithDifferentThanExpectedNumberOfVarargsArguments() {
        assertThrows(MissingInvocation.class, () -> {
            new Expectations() {
                {
                    mock2.doSomething("1", "2", "3");
                    times = 1;
                }
            };

            mock2.doSomething("1", "2");
        });
    }

    /**
     * Replay varargs method with different than expected varargs argument.
     */
    @Test
    void replayVarargsMethodWithDifferentThanExpectedVarargsArgument() {
        assertThrows(MissingInvocation.class, () -> {
            new Expectations() {
                {
                    mock2.doSomething("1", "2", "3");
                }
            };

            mock2.doSomething("1", "2", "4");
        });
    }

    /**
     * Expect invocation on method with varargs argument using argument matchers.
     */
    @Test
    void expectInvocationOnMethodWithVarargsArgumentUsingArgumentMatchers() {
        new Expectations() {
            {
                mock.complexOperation(withEqual(1), withNotEqual(2), withNull());
                mock2.doSomething(withPrefix("C"), withSuffix("."));
            }
        };

        mock.complexOperation(1, 3, null);
        mock2.doSomething("Cab", "123.");
    }

    /**
     * Expect invocation with any number of variable arguments.
     */
    @Test
    void expectInvocationWithAnyNumberOfVariableArguments() {
        new Expectations() {
            {
                mock.complexOperation(any, (Object[]) null);
                times = 3;
                mock2.doSomething((String[]) any);
                minTimes = 2;
            }
        };

        mock.complexOperation("test");
        mock.complexOperation(null, 'X');
        mock2.doSomething();
        mock2.doSomething("test", "abc");
        mock.complexOperation(123, true, "test", 3);
    }

    /**
     * Expect invocations with matcher for varargs parameter only.
     */
    @Test
    void expectInvocationsWithMatcherForVarargsParameterOnly() {
        final List<Integer> values = asList(1, 2, 3);

        new Expectations() {
            {
                mock.complexOperation("test", (Object[]) any);
                result = values;
                mock.anotherOperation(1, true, null, (String[]) any);
                result = 123;
                Collaborator.doSomething(anyInt, (Object[]) any);
                result = true;
            }
        };

        assertSame(values, mock.complexOperation("test", true, 'a', 2.5));
        assertSame(values, mock.complexOperation("test", 123));
        assertSame(values, mock.complexOperation("test"));

        assertEquals(123, mock.anotherOperation(1, true, null));
        assertEquals(123, mock.anotherOperation(1, true, null, "A", null, "b"));
        assertEquals(123, mock.anotherOperation(1, true, "test", "a", "b"));

        assertTrue(Collaborator.doSomething(-1));
        assertTrue(Collaborator.doSomething(-2, "test"));
    }

    /**
     * Expect invocation on varargs method with matcher only for regular first parameter.
     */
    @Test
    void expectInvocationOnVarargsMethodWithMatcherOnlyForRegularFirstParameter() {
        new Expectations() {
            {
                mock.complexOperation(any, 1, 2);
            }
        };

        mock.complexOperation("test", 1, 2);
    }

    /**
     * Expect invocation with matchers for regular parameters and all varargs values.
     */
    @Test
    void expectInvocationWithMatchersForRegularParametersAndAllVarargsValues() {
        new Expectations() {
            {
                mock.complexOperation(anyBoolean, anyInt, withEqual(2));
                mock.complexOperation(anyString, withEqual(1), any, withEqual(3), anyBoolean);
            }
        };

        mock.complexOperation(true, 1, 2);
        mock.complexOperation("abc", 1, 2, 3, true);
    }

    /**
     * Record expectations with matchers for some regular parameters and none for varargs.
     */
    @Test
    void recordExpectationsWithMatchersForSomeRegularParametersAndNoneForVarargs() {
        new Expectations() {
            {
                mock.anotherOperation(1, anyBoolean, "test", "a");
                result = 1;
                mock.anotherOperation(anyInt, true, withSubstring("X"), "a", "b");
                result = 2;
            }
        };

        // Invocations that match a recorded expectation:
        assertEquals(1, mock.anotherOperation(1, true, "test", "a"));
        assertEquals(1, mock.anotherOperation(1, true, "test", "a"));
        assertEquals(1, mock.anotherOperation(1, false, "test", "a"));

        assertEquals(2, mock.anotherOperation(2, true, "aXb", "a", "b"));
        assertEquals(2, mock.anotherOperation(-1, true, "  X", "a", "b"));
        assertEquals(2, mock.anotherOperation(0, true, "XXX", "a", "b"));
        assertEquals(2, mock.anotherOperation(1, true, "X", "a", "b"));

        // Invocations that don't match any expectation:
        assertEquals(0, mock.anotherOperation(1, false, "test", null, "a"));
        assertEquals(0, mock.anotherOperation(1, false, "tst", "a"));
        assertEquals(0, mock.anotherOperation(0, false, "test", "a"));
        assertEquals(0, mock.anotherOperation(1, true, "test", "b"));
        assertEquals(0, mock.anotherOperation(1, true, "test"));

        assertEquals(0, mock.anotherOperation(2, false, "aXb", "a", "b"));
        assertEquals(0, mock.anotherOperation(1, true, "  X", "A", "b"));
        assertEquals(0, mock.anotherOperation(0, true, "XXX", "a"));
        assertEquals(0, mock.anotherOperation(0, true, "XXX", "b"));
        assertEquals(0, mock.anotherOperation(32, true, "-Xx", "a", null));
    }

    /**
     * Expect invocations with non null regular argument and any varargs.
     */
    @Test
    void expectInvocationsWithNonNullRegularArgumentAndAnyVarargs() {
        new Expectations() {
            {
                mock.complexOperation(withNotNull(), (Object[]) any);
                times = 3;
            }
        };

        mock.complexOperation(new Object(), 1, "2");
        mock.complexOperation("", true, 'a', 2.5);
        mock.complexOperation(123);
    }

    /**
     * Expect invocation with non null regular argument and any varargs but replay with null.
     */
    @Test
    void expectInvocationWithNonNullRegularArgumentAndAnyVarargsButReplayWithNull() {
        assertThrows(MissingInvocation.class, () -> {

            mock.complexOperation(null, 1, "2");

            new Verifications() {
                {
                    mock.complexOperation(withNotNull(), (Object[]) any);
                }
            };
        });
    }

    /**
     * Expect invocation with matchers for some regular parameters and all for varargs.
     */
    @Test
    void expectInvocationWithMatchersForSomeRegularParametersAndAllForVarargs() {
        new Expectations() {
            {
                mock.anotherOperation(anyInt, true, withEqual("abc"), anyString, withEqual("test"));
                result = 1;
                mock.anotherOperation(0, anyBoolean, withEqual("Abc"), anyString, anyString, anyString);
                result = 2;
            }
        };

        assertEquals(0, mock.anotherOperation(1, false, "test", null, "a"));

        assertEquals(1, mock.anotherOperation(2, true, "abc", "xyz", "test"));
        assertEquals(1, mock.anotherOperation(-1, true, "abc", null, "test"));
        assertEquals(0, mock.anotherOperation(-1, true, "abc", null, "test", null));

        assertEquals(2, mock.anotherOperation(0, false, "Abc", "", "Abc", "test"));
        assertEquals(0, mock.anotherOperation(0, false, "Abc", "", "Abc", "test", ""));
    }

    /**
     * The Class VarArgs.
     */
    @SuppressWarnings("unused")
    static class VarArgs {

        /**
         * Vars only.
         *
         * @param ints
         *            the ints
         */
        public void varsOnly(int... ints) {
        }

        /**
         * Mixed.
         *
         * @param arg0
         *            the arg 0
         * @param ints
         *            the ints
         */
        public void mixed(String arg0, int... ints) {
        }
    }

    /**
     * Expect invocation with no var args.
     *
     * @param varargs
     *            the varargs
     */
    @SuppressWarnings("NullArgumentToVariableArgMethod")
    @Test
    void expectInvocationWithNoVarArgs(@Mocked final VarArgs varargs) {
        new Expectations() {
            {
                varargs.varsOnly();
                times = 2;
                varargs.mixed("arg");
                times = 2;
            }
        };

        varargs.varsOnly();
        varargs.varsOnly(null);
        varargs.mixed("arg");
        varargs.mixed("arg", null);
    }

    /**
     * The Class ReferenceVarArgs.
     */
    static class ReferenceVarArgs {

        /**
         * Mixed.
         *
         * @param strings
         *            the strings
         * @param ints
         *            the ints
         */
        @SuppressWarnings("unused")
        public void mixed(String[] strings, Integer... ints) {
        }
    }

    /**
     * Expect invocation with non primitive var args.
     *
     * @param varargs
     *            the varargs
     */
    @Test
    void expectInvocationWithNonPrimitiveVarArgs(@Mocked final ReferenceVarArgs varargs) {
        final String[] strings1 = {};
        final String[] strings2 = { "first", "second" };

        new Expectations() {
            {
                varargs.mixed(null, 4, 5, 6);
                varargs.mixed(strings1, 4, 5, 6);
                varargs.mixed(strings2, 4, 5, 6);
                varargs.mixed(null);
                varargs.mixed(strings1);
                varargs.mixed(strings2);
            }
        };

        varargs.mixed(null, 4, 5, 6);
        varargs.mixed(strings1, 4, 5, 6);
        varargs.mixed(strings2, 4, 5, 6);
        varargs.mixed(null);
        varargs.mixed(strings1);
        varargs.mixed(strings2);
    }

    /**
     * The Class PrimitiveVarArgs.
     */
    @SuppressWarnings("unused")
    static class PrimitiveVarArgs {

        /**
         * Vars only.
         *
         * @param ints
         *            the ints
         */
        public void varsOnly(int... ints) {
        }

        /**
         * Mixed.
         *
         * @param arg0
         *            the arg 0
         * @param strings
         *            the strings
         * @param ints
         *            the ints
         */
        public void mixed(String arg0, String[] strings, int... ints) {
        }
    }

    /**
     * Expect invocation with primitive var args.
     *
     * @param varargs
     *            the varargs
     */
    @SuppressWarnings("NullArgumentToVariableArgMethod")
    @Test
    void expectInvocationWithPrimitiveVarArgs(@Mocked final PrimitiveVarArgs varargs) {
        final String[] strings1 = {};
        final String[] strings2 = { "first", "second" };

        new Expectations() {
            {
                varargs.varsOnly(1, 2, 3);
                varargs.varsOnly(null);
                varargs.mixed("arg", null, 4, 5, 6);
                varargs.mixed("arg", strings1, 4, 5, 6);
                varargs.mixed("arg", strings2, 4, 5, 6);
                varargs.mixed("arg", null);
                varargs.mixed("arg", strings1);
                varargs.mixed("arg", strings2);
                varargs.mixed("arg", null, null);
                varargs.mixed(null, null, null);
            }
        };

        varargs.varsOnly(1, 2, 3);
        varargs.varsOnly(null);
        varargs.mixed("arg", null, 4, 5, 6);
        varargs.mixed("arg", strings1, 4, 5, 6);
        varargs.mixed("arg", strings2, 4, 5, 6);
        varargs.mixed("arg", null);
        varargs.mixed("arg", strings1);
        varargs.mixed("arg", strings2);
        varargs.mixed("arg", null, null);
        varargs.mixed(null, null, null);
    }

    /**
     * The Class MixedVarArgs.
     */
    static class MixedVarArgs {
        /**
         * Mixed.
         *
         * @param strings
         *            the strings
         * @param ints
         *            the ints
         */
        @SuppressWarnings("unused")
        public void mixed(String[] strings, int... ints) {
        }
    }

    /**
     * Expect invocation with primitive var args using matchers.
     *
     * @param varargs
     *            the varargs
     */
    @Test
    void expectInvocationWithPrimitiveVarArgsUsingMatchers(@Mocked final MixedVarArgs varargs) {
        final String[] strings1 = {};
        final String[] strings2 = { "first", "second" };

        new Expectations() {
            {
                varargs.mixed((String[]) withNull(), withEqual(4), withEqual(5), withEqual(6));
                varargs.mixed(withEqual(strings1), withEqual(4), withEqual(5), withEqual(6));
                varargs.mixed(withEqual(strings2), withEqual(4), withEqual(5), withEqual(6));
                varargs.mixed((String[]) withNull());
                varargs.mixed(withEqual(strings1));
                varargs.mixed(withEqual(strings2));
            }
        };

        varargs.mixed(null, 4, 5, 6);
        varargs.mixed(strings1, 4, 5, 6);
        varargs.mixed(strings2, 4, 5, 6);
        varargs.mixed(null);
        varargs.mixed(strings1);
        varargs.mixed(strings2);
    }

    /**
     * Expect invocation with matchers for all parameters and varargs values but replay with different vararg value.
     */
    @Test
    void expectInvocationWithMatchersForAllParametersAndVarargsValuesButReplayWithDifferentVarargValue() {
        assertThrows(MissingInvocation.class, () -> {

            mock.complexOperation("abc", true, 1L);

            new Verifications() {
                {
                    mock.complexOperation(anyString, anyBoolean, withEqual(123L));
                }
            };
        });
    }

    /**
     * Expectation recorded with not null matcher for varargs parameter.
     */
    @Test
    void expectationRecordedWithNotNullMatcherForVarargsParameter() {
        new Expectations() {
            {
                Collaborator.doSomething(0, (Object[]) withNotNull());
                result = true;
            }
        };

        assertTrue(Collaborator.doSomething(0, "test"));
        // noinspection NullArgumentToVariableArgMethod
        assertFalse(Collaborator.doSomething(0, (Object[]) null));
    }

    /**
     * Record varargs method with regular parameter using matcher for varargs only.
     */
    @Test
    @Disabled("issue #292")
    void recordVarargsMethodWithRegularParameterUsingMatcherForVarargsOnly() {
        new Expectations() {
            {
                Collaborator.doSomething(123, anyString);
            }
        };

        Collaborator.doSomething(123, "test");
    }
}
