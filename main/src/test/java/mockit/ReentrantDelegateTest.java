package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

/**
 * The Class ReentrantDelegateTest.
 */
public final class ReentrantDelegateTest {

    /**
     * The Class RealClass.
     */
    public static class RealClass {

        /**
         * Non recursive static method.
         *
         * @param i
         *            the i
         *
         * @return the int
         */
        protected static int nonRecursiveStaticMethod(int i) {
            return -i;
        }

        /**
         * Non recursive method.
         *
         * @param i
         *            the i
         *
         * @return the int
         */
        public int nonRecursiveMethod(int i) {
            return -i;
        }
    }

    /**
     * Recursive delegate method without invocation parameter.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recursiveDelegateMethodWithoutInvocationParameter(@Mocked RealClass mock) {
        new Expectations() {
            {
                RealClass.nonRecursiveStaticMethod(anyInt);
                result = new Delegate() {
                    @Mock
                    int delegate(int i) {
                        if (i > 1) {
                            return i;
                        }
                        return RealClass.nonRecursiveStaticMethod(i + 1);
                    }
                };
            }
        };

        int result = RealClass.nonRecursiveStaticMethod(1);
        assertEquals(2, result);
    }

    /**
     * Recursive delegate method with invocation parameter not used for proceeding.
     *
     * @param rc
     *            the rc
     */
    @Test
    public void recursiveDelegateMethodWithInvocationParameterNotUsedForProceeding(@Injectable final RealClass rc) {
        new Expectations() {
            {
                rc.nonRecursiveMethod(anyInt);
                result = new Delegate() {
                    @Mock
                    int delegate(Invocation inv, int i) {
                        if (i > 1) {
                            return i;
                        }
                        RealClass it = inv.getInvokedInstance();
                        return it.nonRecursiveMethod(i + 1);
                    }
                };
            }
        };

        int result = rc.nonRecursiveMethod(1);
        assertEquals(2, result);
    }

    /**
     * Non recursive delegate method with invocation parameter used for proceeding.
     *
     * @param rc
     *            the rc
     */
    @Test
    public void nonRecursiveDelegateMethodWithInvocationParameterUsedForProceeding(@Injectable final RealClass rc) {
        new Expectations() {
            {
                rc.nonRecursiveMethod(anyInt);
                result = new Delegate() {
                    @Mock
                    int nonRecursiveMethod(Invocation inv, int i) {
                        if (i > 1) {
                            return i;
                        }
                        return inv.proceed(i + 1);
                    }
                };
            }
        };

        int result = rc.nonRecursiveMethod(1);
        assertEquals(-2, result);
    }
}
