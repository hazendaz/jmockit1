package mockit;

import static java.util.Arrays.asList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mockit.integration.junit5.JMockitExtension;
import mockit.internal.expectations.invocation.MissingInvocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class ExpectationsWithSomeArgMatchersRecordedTest.
 */
@ExtendWith(JMockitExtension.class)
class ExpectationsWithSomeArgMatchersRecordedTest {

    /**
     * The Class Dependency.
     */
    static class Dependency {

        /** The value. */
        private final int value;

        /**
         * Instantiates a new dependency.
         */
        Dependency() {
            value = 0;
        }

        /**
         * Instantiates a new dependency.
         *
         * @param value
         *            the value
         */
        Dependency(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Dependency && value == ((Dependency) obj).value;
        }

        @Override
        public int hashCode() {
            return value;
        }
    }

    /**
     * The Class Collaborator.
     */
    @SuppressWarnings({ "SameParameterValue", "unused" })
    static class Collaborator {

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(int value) {
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(double value) {
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(float value) {
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the value
         *
         * @return the string
         */
        String setValue(String value) {
            return "";
        }

        /**
         * Sets the values.
         *
         * @param value1
         *            the value 1
         * @param value2
         *            the value 2
         * @param value3
         *            the value 3
         * @param value4
         *            the value 4
         */
        void setValues(long value1, byte value2, double value3, short value4) {
        }

        /**
         * Boolean values.
         *
         * @param value1
         *            the value 1
         * @param value2
         *            the value 2
         * @param value3
         *            the value 3
         * @param value4
         *            the value 4
         *
         * @return true, if successful
         */
        boolean booleanValues(long value1, byte value2, double value3, short value4) {
            return true;
        }

        /**
         * Static set values.
         *
         * @param value1
         *            the value 1
         * @param value2
         *            the value 2
         * @param value3
         *            the value 3
         * @param value4
         *            the value 4
         */
        static void staticSetValues(long value1, byte value2, double value3, short value4) {
        }

        /**
         * Static long values.
         *
         * @param value1
         *            the value 1
         * @param value2
         *            the value 2
         * @param value3
         *            the value 3
         * @param value4
         *            the value 4
         *
         * @return the long
         */
        static long staticLongValues(long value1, byte value2, double value3, short value4) {
            return -2;
        }

        /**
         * Do something.
         *
         * @param src
         *            the src
         *
         * @return the int
         */
        int doSomething(Dependency src) {
            return -1;
        }

        /**
         * Do something.
         *
         * @param src
         *            the src
         * @param s
         *            the s
         *
         * @return the int
         */
        int doSomething(Dependency src, String s) {
            return -1;
        }

        /**
         * Do something.
         *
         * @param src
         *            the src
         * @param s
         *            the s
         *
         * @return the int
         */
        int doSomething(Dependency src, String... s) {
            return -1;
        }

        /**
         * Simple operation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         */
        final void simpleOperation(int a, String b) {
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
         */
        final void simpleOperation(int a, String b, Date c) {
        }

        /**
         * Another operation.
         *
         * @param b
         *            the b
         * @param l
         *            the l
         *
         * @return the long
         */
        long anotherOperation(byte b, Long l) {
            return -1;
        }

        /**
         * Static void method.
         *
         * @param l
         *            the l
         * @param c
         *            the c
         * @param d
         *            the d
         */
        static void staticVoidMethod(long l, char c, double d) {
        }

        /**
         * Static boolean method.
         *
         * @param b
         *            the b
         * @param s
         *            the s
         * @param array
         *            the array
         *
         * @return true, if successful
         */
        static boolean staticBooleanMethod(boolean b, String s, int[] array) {
            return false;
        }

        /**
         * Method with array parameters.
         *
         * @param c
         *            the c
         * @param s
         *            the s
         * @param matrix
         *            the matrix
         */
        void methodWithArrayParameters(char[][] c, String[] s, Object[][][] matrix) {
        }

        /**
         * Method with many parameters.
         *
         * @param b1
         *            the b 1
         * @param s1
         *            the s 1
         * @param i1
         *            the i 1
         * @param l1
         *            the l 1
         * @param str1
         *            the str 1
         * @param bo1
         *            the bo 1
         * @param f1
         *            the f 1
         * @param d1
         *            the d 1
         * @param ii1
         *            the ii 1
         * @param ss1
         *            the ss 1
         * @param b2
         *            the b 2
         * @param s2
         *            the s 2
         * @param i2
         *            the i 2
         * @param l2
         *            the l 2
         * @param str2
         *            the str 2
         * @param bo2
         *            the bo 2
         * @param f2
         *            the f 2
         * @param d2
         *            the d 2
         * @param ii2
         *            the ii 2
         * @param ss2
         *            the ss 2
         * @param c
         *            the c
         */
        void methodWithManyParameters(byte b1, short s1, int i1, long l1, String str1, boolean bo1, float f1, double d1,
                int[] ii1, String[] ss1, byte b2, short s2, int i2, long l2, String str2, boolean bo2, float f2,
                double d2, int[] ii2, String[] ss2, char c) {
        }
    }

    /** The mock. */
    @Mocked
    Collaborator mock;

    /**
     * Use matcher only for one argument.
     */
    @Test
    void useMatcherOnlyForOneArgument() {
        final Object o = new Object();

        new Expectations() {
            {
                mock.simpleOperation(withEqual(1), "", null);
                mock.simpleOperation(withNotEqual(1), null, (Date) withNull());
                mock.simpleOperation(12, "arg", (Date) withNotNull());

                mock.anotherOperation((byte) 0, anyLong);
                result = 123L;
                mock.anotherOperation(anyByte, 5L);
                result = -123L;

                Collaborator.staticVoidMethod(34L, anyChar, 5.0);
                Collaborator.staticBooleanMethod(true, withSuffix("end"), null);
                result = true;
                Collaborator.staticBooleanMethod(true, "", new int[] { 1, 2, 3 });
                result = true;

                char[][] chars = { { 'a', 'b' }, { 'X', 'Y', 'Z' } };
                Object[][][] matrix = { null, { { 1, 'X', "test" } }, { { o } } };
                mock.methodWithArrayParameters(chars, (String[]) any, matrix);
            }
        };

        mock.simpleOperation(1, "", null);
        mock.simpleOperation(2, "str", null);
        mock.simpleOperation(1, "", null);
        mock.simpleOperation(12, "arg", new Date());

        assertEquals(123L, mock.anotherOperation((byte) 0, 5L));
        assertEquals(-123L, mock.anotherOperation((byte) 3, 5L));

        Collaborator.staticVoidMethod(34L, '8', 5.0);
        assertTrue(Collaborator.staticBooleanMethod(true, "start-end", null));
        assertTrue(Collaborator.staticBooleanMethod(true, "", new int[] { 1, 2, 3 }));

        mock.methodWithArrayParameters(new char[][] { { 'a', 'b' }, { 'X', 'Y', 'Z' } }, null,
                new Object[][][] { null, { { 1, 'X', "test" } }, { { o } } });
    }

    /**
     * Use matcher only for first argument with unexpected replay value.
     */
    @Test
    void useMatcherOnlyForFirstArgumentWithUnexpectedReplayValue() {
        assertThrows(MissingInvocation.class, () -> {

            mock.simpleOperation(2, "", null);

            new Verifications() {
                {
                    mock.simpleOperation(withEqual(1), "", null);
                }
            };
        });
    }

    /**
     * Use matcher only for second argument with unexpected replay value.
     */
    @Test
    void useMatcherOnlyForSecondArgumentWithUnexpectedReplayValue() {
        assertThrows(MissingInvocation.class, () -> {

            mock.simpleOperation(1, "Xyz", null);

            new Verifications() {
                {
                    mock.simpleOperation(1, withPrefix("arg"), null);
                }
            };
        });
    }

    /**
     * Use matcher only for last argument with unexpected replay value.
     */
    @Test
    void useMatcherOnlyForLastArgumentWithUnexpectedReplayValue() {
        assertThrows(MissingInvocation.class, () -> {

            mock.simpleOperation(12, "arg", null);

            new Verifications() {
                {
                    mock.simpleOperation(12, "arg", (Date) withNotNull());
                }
            };
        });
    }

    /**
     * Use matchers for parameters of all sizes.
     */
    @Test
    void useMatchersForParametersOfAllSizes() {
        new Expectations() {
            {
                mock.setValues(123L, withEqual((byte) 5), 6.4, withNotEqual((short) 14));
                mock.booleanValues(12L, (byte) 4, withEqual(6.0, 0.1), withEqual((short) 14));
                Collaborator.staticSetValues(withNotEqual(1L), (byte) 4, 6.1, withEqual((short) 3));
                Collaborator.staticLongValues(12L, anyByte, withEqual(6.1), (short) 4);
            }
        };

        mock.setValues(123L, (byte) 5, 6.4, (short) 41);
        assertFalse(mock.booleanValues(12L, (byte) 4, 6.1, (short) 14));
        Collaborator.staticSetValues(2L, (byte) 4, 6.1, (short) 3);
        assertEquals(0L, Collaborator.staticLongValues(12L, (byte) -7, 6.1, (short) 4));
    }

    /**
     * Use any int field.
     */
    @Test
    void useAnyIntField() {
        new Expectations() {
            {
                mock.setValue(anyInt);
            }
        };

        mock.setValue(1);
    }

    /**
     * Use any string field.
     */
    @Test
    void useAnyStringField() {
        new Expectations() {
            {
                mock.setValue(anyString);
                returns("one", "two");
            }
        };

        assertEquals("one", mock.setValue("test"));
        assertEquals("two", mock.setValue(""));
        assertEquals("two", mock.setValue(null));
    }

    /**
     * Use several any fields.
     */
    @Test
    void useSeveralAnyFields() {
        final Date now = new Date();

        new Expectations() {
            {
                mock.simpleOperation(anyInt, null, null);
                mock.simpleOperation(anyInt, "test", null);
                mock.simpleOperation(3, "test2", null);
                mock.simpleOperation(-1, null, (Date) any);
                mock.simpleOperation(1, anyString, now);

                Collaborator.staticSetValues(2L, anyByte, 0.0, anyShort);

                // noinspection ConstantConditions
                mock.methodWithManyParameters(anyByte, anyShort, anyInt, anyLong, anyString, anyBoolean, anyFloat,
                        anyDouble, (int[]) any, (String[]) any, anyByte, anyShort, anyInt, anyLong, anyString,
                        anyBoolean, anyFloat, anyDouble, (int[]) any, (String[]) any, anyChar);
            }
        };

        mock.simpleOperation(2, "abc", now);
        mock.simpleOperation(5, "test", null);
        mock.simpleOperation(3, "test2", null);
        mock.simpleOperation(-1, "Xyz", now);
        mock.simpleOperation(1, "", now);

        Collaborator.staticSetValues(2, (byte) 1, 0, (short) 2);

        mock.methodWithManyParameters((byte) 1, (short) 2, 3, 4L, "5", false, 7.0F, 8.0, null, null, (byte) 10,
                (short) 20, 30, 40L, "50", true, 70.0F, 80.0, null, null, 'x');
    }

    /**
     * Use with methods mixed with any fields.
     */
    @Test
    void useWithMethodsMixedWithAnyFields() {
        mock.simpleOperation(2, "abc", new Date());
        mock.simpleOperation(5, "test", null);
        mock.simpleOperation(3, "test2", null);
        mock.simpleOperation(-1, "Xyz", new Date());
        mock.simpleOperation(1, "", new Date());

        new FullVerifications() {
            {
                mock.simpleOperation(anyInt, null, (Date) any);
                mock.simpleOperation(anyInt, withEqual("test"), null);
                mock.simpleOperation(3, withPrefix("test"), (Date) any);
                mock.simpleOperation(-1, anyString, (Date) any);
                mock.simpleOperation(1, anyString, (Date) withNotNull());
            }
        };
    }

    /**
     * The Interface Scheduler.
     */
    public interface Scheduler {
        /**
         * Gets the alerts.
         *
         * @param o
         *            the o
         * @param i
         *            the i
         * @param b
         *            the b
         *
         * @return the alerts
         */
        List<String> getAlerts(Object o, int i, boolean b);
    }

    /**
     * Use matchers in invocations to interface methods.
     *
     * @param scheduler
     *            the scheduler
     */
    @Test
    void useMatchersInInvocationsToInterfaceMethods(@Mocked final Scheduler scheduler) {
        new Expectations() {
            {
                scheduler.getAlerts(any, 1, anyBoolean);
                result = asList("A", "b");
            }
        };

        assertEquals(2, scheduler.getAlerts("123", 1, true).size());
    }

    // Tests for the matching of expectations to instances created during replay
    // ///////////////////////////////////////////////////////////

    /**
     * Record expectations for mocked objects instantiated inside SUT.
     *
     * @param dep
     *            the dep
     */
    @Test
    void recordExpectationsForMockedObjectsInstantiatedInsideSUT(@Mocked Dependency dep) {
        new Expectations() {
            {
                Dependency src1 = new Dependency(1);
                Dependency src2 = new Dependency(2);
                mock.doSomething(src1);
                result = 1;
                times = 2;
                mock.doSomething(src2);
                result = 2;
                times = 2;
            }
        };

        Dependency src1 = new Dependency(1);
        Dependency src2 = new Dependency(2);
        Dependency src3 = new Dependency(1);
        Dependency src4 = new Dependency(2);

        assertEquals(1, mock.doSomething(src1));
        assertEquals(2, mock.doSomething(src2));
        assertEquals(1, mock.doSomething(src3));
        assertEquals(2, mock.doSomething(src4));
    }

    /**
     * Verify unordered expectations for mocked objects instantiated inside SUT.
     */
    @Test
    void verifyUnorderedExpectationsForMockedObjectsInstantiatedInsideSUT() {
        Dependency src1 = new Dependency(1);
        Dependency src2 = new Dependency(2);
        Dependency src3 = new Dependency(1);
        Dependency src4 = new Dependency(2);

        mock.doSomething(src1);
        mock.doSomething(src2);
        mock.doSomething(src3);
        mock.doSomething(src4);

        final List<Dependency> dependencies = new ArrayList<>();
        new Verifications() {
            {
                mock.doSomething(withCapture(dependencies));
                times = 4;
            }
        };

        int i1 = dependencies.indexOf(new Dependency(1));
        int l1 = dependencies.lastIndexOf(new Dependency(1));
        assertTrue(i1 >= 0);
        assertTrue(l1 >= 0 && l1 != i1);

        int i2 = dependencies.indexOf(new Dependency(2));
        int l2 = dependencies.lastIndexOf(new Dependency(2));
        assertTrue(i2 >= 0);
        assertTrue(l2 >= 0 && l2 != i2);
    }

    /**
     * Verify ordered expectations for mocked objects instantiated inside SUT.
     */
    @Test
    void verifyOrderedExpectationsForMockedObjectsInstantiatedInsideSUT() {
        Dependency src1 = new Dependency(1);
        Dependency src2 = new Dependency(2);
        Dependency src3 = new Dependency(2);
        Dependency src4 = new Dependency(1);

        mock.doSomething(src1);
        mock.doSomething(src2);
        mock.doSomething(src3);
        mock.doSomething(src4);

        new VerificationsInOrder() {
            {
                Dependency dep1 = new Dependency(1);
                Dependency dep2 = new Dependency(2);
                mock.doSomething(dep1);
                mock.doSomething(dep2);
                times = 2;
                mock.doSomething(dep1);
            }
        };
    }

    /**
     * Record expectation with matcher and regular argument matching mocked object instantiated inside SUT.
     *
     * @param dep
     *            the dep
     */
    @Test
    void recordExpectationWithMatcherAndRegularArgumentMatchingMockedObjectInstantiatedInsideSUT(
            @Mocked Dependency dep) {
        final List<Dependency> dependencies = new ArrayList<>();

        new Expectations() {
            {
                Dependency src = new Dependency();
                dependencies.add(src);
            }
        };

        new Expectations() {
            {
                Dependency firstDep = dependencies.get(0);
                mock.doSomething(firstDep, anyString);
                result = 123;
            }
        };

        Dependency src = new Dependency();
        int i = mock.doSomething(src, "test");

        assertEquals(123, i);
    }

    /**
     * Record varargs expectation with matcher and regular argument matching mocked object instantiated inside SUT.
     *
     * @param dep
     *            the dep
     */
    @Test
    void recordVarargsExpectationWithMatcherAndRegularArgumentMatchingMockedObjectInstantiatedInsideSUT(
            @Mocked Dependency dep) {
        final List<Dependency> dependencies = new ArrayList<>();

        new Expectations() {
            {
                Dependency src = new Dependency();
                dependencies.add(src);
            }
        };

        new Expectations() {
            {
                Dependency firstDep = dependencies.get(0);
                mock.doSomething(firstDep, (String[]) any);
                result = 123;
            }
        };

        Dependency src = new Dependency();
        int i = mock.doSomething(src, "a", "b");

        assertEquals(123, i);
    }

    /**
     * Record instantiation expectations for mocked objects instantiated inside SUT.
     *
     * @param anyDep
     *            the any dep
     */
    @Test
    void recordInstantiationExpectationsForMockedObjectsInstantiatedInsideSUT(@Mocked Dependency anyDep) {
        new Expectations() {
            {
                Dependency dep1 = new Dependency(1);
                Dependency dep2 = new Dependency(2);
                mock.doSomething(dep1);
                result = 1;
                times = 2;
                mock.doSomething(dep2);
                result = 2;
                times = 2;
            }
        };

        Dependency src1 = new Dependency(1);
        Dependency src2 = new Dependency(2);
        Dependency src3 = new Dependency(1);
        Dependency src4 = new Dependency(2);

        assertEquals(1, mock.doSomething(src1));
        assertEquals(2, mock.doSomething(src2));
        assertEquals(1, mock.doSomething(src3));
        assertEquals(2, mock.doSomething(src4));
    }

    // The following tests failed only when compiled with the Eclipse compiler
    // /////////////////////////////////////////////////////////////

    /**
     * Expectation with matchers spanning multiple lines.
     */
    @Test
    void expectationWithMatchersSpanningMultipleLines() {
        new Expectations() {
            {
                mock.simpleOperation(1, (String) withNull());
            }
        };

        mock.simpleOperation(1, null);
    }

    /**
     * Expectation with matcher in second line and constant argument in third line.
     */
    @Test
    void expectationWithMatcherInSecondLineAndConstantArgumentInThirdLine() {
        new Expectations() {
            {
                mock.simpleOperation(anyInt, "test");
            }
        };

        mock.simpleOperation(123, "test");
    }

    /**
     * Expectations with partial matchers in every combination for method with three parameters.
     */
    @Test
    void expectationsWithPartialMatchersInEveryCombinationForMethodWithThreeParameters() {
        final Date now = new Date();

        mock.simpleOperation(123, "test", null);
        mock.simpleOperation(-2, "", now);
        mock.simpleOperation(0, "test", now);
        mock.simpleOperation(1, "test", null);
        mock.simpleOperation(0, "test", null);
        mock.simpleOperation(-3, "xyz", now);
        mock.simpleOperation(123, null, now);
        mock.simpleOperation(123, "", null);

        new VerificationsInOrder() {
            {
                // Expectations with one matcher:
                mock.simpleOperation(anyInt, "test", null);
                mock.simpleOperation(-2, anyString, null);
                mock.simpleOperation(0, "test", (Date) withNotNull());
                mock.simpleOperation(1, null, (Date) withNull());
                mock.simpleOperation(0, "test", (Date) any);

                // Expectations with two matchers:
                mock.simpleOperation(-3, anyString, (Date) any);
                mock.simpleOperation(withNotEqual(0), anyString, now);
                mock.simpleOperation(anyInt, "", (Date) any);
            }
        };
    }
}
