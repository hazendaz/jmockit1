package mockit;

import java.util.Date;
import java.util.List;

import mockit.internal.expectations.invocation.MissingInvocation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The Class VerificationsWithSomeArgumentMatchersTest.
 */
final class VerificationsWithSomeArgumentMatchersTest {

    /**
     * The Class Collaborator.
     */
    @SuppressWarnings("unused")
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
        void setValues(long value1, Byte value2, double value3, Short value4) {
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
        static long staticLongValues(long value1, byte value2, double value3, char value4) {
            return -2;
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
        long anotherOperation(byte b, long l) {
            return -1;
        }

        /**
         * Static void method.
         *
         * @param l
         *            the l
         * @param c
         *            the c
         * @param f
         *            the f
         */
        static void staticVoidMethod(long l, char c, float f) {
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
    }

    /** The mock. */
    @Mocked
    Collaborator mock;

    /**
     * Use matcher only for one argument.
     */
    @Test
    void useMatcherOnlyForOneArgument() {
        mock.simpleOperation(1, "", null);
        mock.simpleOperation(2, "str", null);
        mock.simpleOperation(1, "", null);
        mock.simpleOperation(12, "arg", new Date());

        mock.anotherOperation((byte) 0, 5);
        mock.anotherOperation((byte) 3, 5);

        Collaborator.staticVoidMethod(34L, '8', 5.0F);
        Collaborator.staticBooleanMethod(true, "start-end", null);

        new VerificationsInOrder() {
            {
                mock.simpleOperation(withEqual(1), "", null);
                mock.simpleOperation(withNotEqual(1), null, (Date) withNull());
                mock.simpleOperation(1, withNotEqual("arg"), null);
                mock.simpleOperation(12, "arg", (Date) withNotNull());

                mock.anotherOperation((byte) 0, anyLong);
                mock.anotherOperation(anyByte, 5);

                Collaborator.staticVoidMethod(34L, anyChar, 5.0F);
                Collaborator.staticBooleanMethod(true, withSuffix("end"), null);
            }
        };
    }

    /**
     * Use matcher only for first argument with unexpected replay value.
     */
    @Test
    void useMatcherOnlyForFirstArgumentWithUnexpectedReplayValue() {
        Assertions.assertThrows(MissingInvocation.class, () -> {

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
        Assertions.assertThrows(MissingInvocation.class, () -> {

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
        Assertions.assertThrows(MissingInvocation.class, () -> {

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
        mock.setValues(123L, (byte) 5, 6.4, (short) 41);
        mock.booleanValues(12L, (byte) 4, 6.1, (short) 14);
        Collaborator.staticSetValues(2L, (byte) 4, 6.1, (short) 3);
        Collaborator.staticLongValues(12L, (byte) -7, 6.1, 'F');

        new Verifications() {
            {
                mock.setValues(123L, anyByte, 6.4, anyShort);
                mock.booleanValues(12L, (byte) 4, withEqual(6.0, 0.1), withEqual((short) 14));
                Collaborator.staticSetValues(withNotEqual(1L), (byte) 4, 6.1, withEqual((short) 3));
                Collaborator.staticLongValues(12L, anyByte, withEqual(6.1), 'F');
            }
        };
    }

    /**
     * Use any int field.
     */
    @Test
    void useAnyIntField() {
        mock.setValue(1);

        new FullVerifications() {
            {
                mock.setValue(anyInt);
            }
        };
    }

    /**
     * Use several any fields.
     */
    @Test
    void useSeveralAnyFields() {
        final Date now = new Date();
        mock.simpleOperation(2, "abc", now);
        mock.simpleOperation(5, "test", null);
        mock.simpleOperation(3, "test2", null);
        mock.simpleOperation(-1, "Xyz", now);
        mock.simpleOperation(1, "", now);

        Collaborator.staticSetValues(2, (byte) 1, 0, (short) 2);
        Collaborator.staticLongValues(23L, (byte) 1, 1.34, 'S');
        Collaborator.staticVoidMethod(45L, 'S', 56.4F);

        new FullVerifications() {
            {
                mock.simpleOperation(anyInt, null, null);
                mock.simpleOperation(anyInt, "test", null);
                mock.simpleOperation(3, "test2", null);
                mock.simpleOperation(-1, null, (Date) any);
                mock.simpleOperation(1, anyString, now);

                Collaborator.staticSetValues(2L, anyByte, 0.0, anyShort);
                Collaborator.staticLongValues(anyLong, (byte) 1, anyDouble, anyChar);
                Collaborator.staticVoidMethod(45L, 'S', anyFloat);
            }
        };
    }

    /**
     * Use with methods mixed with any fields.
     */
    @Test
    void useWithMethodsMixedWithAnyFields() {
        Date now = new Date();
        mock.simpleOperation(2, "abc", now);
        mock.simpleOperation(5, "test", null);
        mock.simpleOperation(3, "test2", null);
        mock.simpleOperation(-1, "Xyz", now);
        mock.simpleOperation(1, "", now);

        new Verifications() {
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
        scheduler.getAlerts("123", 1, true);
        scheduler.getAlerts(null, 1, false);

        new FullVerifications() {
            {
                scheduler.getAlerts(any, 1, anyBoolean);
                times = 2;
            }
        };

        new Verifications() {
            {
                scheduler.getAlerts(null, anyInt, anyBoolean);
                times = 2;
            }
        };
    }
}
