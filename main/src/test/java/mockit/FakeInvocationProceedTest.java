package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * The Class FakeInvocationProceedTest.
 */
public final class FakeInvocationProceedTest {

    /**
     * The Class BaseClassToBeFaked.
     */
    public static class BaseClassToBeFaked {

        /** The name. */
        protected String name;

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
         * Method to be faked.
         *
         * @param i
         *            the i
         *
         * @return the int
         */
        protected int methodToBeFaked(int i) {
            return i;
        }
    }

    /**
     * The Class ClassToBeFaked.
     */
    public static class ClassToBeFaked extends BaseClassToBeFaked {

        /**
         * Instantiates a new class to be faked.
         */
        public ClassToBeFaked() {
            name = "";
        }

        /**
         * Instantiates a new class to be faked.
         *
         * @param name
         *            the name
         */
        public ClassToBeFaked(String name) {
            this.name = name;
        }

        /**
         * Method to be faked.
         *
         * @return true, if successful
         */
        public boolean methodToBeFaked() {
            return true;
        }

        /**
         * Method to be faked.
         *
         * @param i
         *            the i
         * @param args
         *            the args
         *
         * @return the int
         */
        protected int methodToBeFaked(int i, Object... args) {
            int result = i;

            for (Object arg : args) {
                if (arg != null) {
                    result++;
                }
            }

            return result;
        }

        /**
         * Another method to be faked.
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
        public String anotherMethodToBeFaked(String s, boolean b, List<Integer> ints) {
            return (b ? s.toUpperCase() : s.toLowerCase()) + ints;
        }

        /**
         * Static method to be faked.
         *
         * @return true, if successful
         *
         * @throws FileNotFoundException
         *             the file not found exception
         */
        public static boolean staticMethodToBeFaked() throws FileNotFoundException {
            throw new FileNotFoundException();
        }

        /**
         * Native method.
         */
        protected static native void nativeMethod();
    }

    /**
     * Proceed from fake method without parameters.
     */
    @Test
    public void proceedFromFakeMethodWithoutParameters() {
        new MockUp<ClassToBeFaked>() {
            @Mock
            boolean methodToBeMocked(Invocation inv) {
                return inv.proceed();
            }
        };

        assertTrue(new ClassToBeFaked().methodToBeFaked());
    }

    /**
     * Proceed from fake method with parameters.
     */
    @Test
    public void proceedFromFakeMethodWithParameters() {
        new MockUp<ClassToBeFaked>() {
            @Mock
            int methodToBeFaked(Invocation inv, int i) {
                Integer j = inv.proceed();
                return j + 1;
            }

            @Mock
            private int methodToBeFaked(Invocation inv, int i, Object... args) {
                args[2] = "mock";
                return inv.<Integer>proceed();
            }
        };

        ClassToBeFaked faked = new ClassToBeFaked();

        assertEquals(124, faked.methodToBeFaked(123));
        assertEquals(-8, faked.methodToBeFaked(-9));
        assertEquals(7, faked.methodToBeFaked(3, "Test", new Object(), null, 45));
    }

    /**
     * Proceed conditionally from fake method.
     */
    @Test
    public void proceedConditionallyFromFakeMethod() {
        new MockUp<ClassToBeFaked>() {
            @Mock
            String anotherMethodToBeFaked(Invocation inv, String s, boolean b, List<Number> ints) {
                if (!b) {
                    return s;
                }

                ints.add(45);
                return inv.proceed();
            }
        };

        ClassToBeFaked mocked = new ClassToBeFaked();

        // Do not proceed:
        assertNull(mocked.anotherMethodToBeFaked(null, false, null));

        // Do proceed:
        List<Integer> values = new ArrayList<>();
        assertEquals("TEST[45]", mocked.anotherMethodToBeFaked("test", true, values));

        // Do not proceed again:
        assertEquals("No proceed", mocked.anotherMethodToBeFaked("No proceed", false, null));
    }

    /**
     * Proceed from fake method which throws checked exception.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void proceedFromFakeMethodWhichThrowsCheckedException() throws Exception {
        new MockUp<ClassToBeFaked>() {
            @Mock
            boolean staticMethodToBeFaked(Invocation inv) throws Exception {
                if (inv.getInvocationIndex() == 0) {
                    return inv.<Boolean>proceed();
                }

                throw new InterruptedException("fake");
            }
        };

        try {
            ClassToBeFaked.staticMethodToBeFaked();
            fail();
        } catch (FileNotFoundException ignored) {
        }

        assertThrows(InterruptedException.class, () -> {
            ClassToBeFaked.staticMethodToBeFaked();
        });
    }

    /**
     * Proceed from fake method into real method with modified arguments.
     */
    @Test
    public void proceedFromFakeMethodIntoRealMethodWithModifiedArguments() {
        class FakeWhichModifiesArguments extends MockUp<ClassToBeFaked> {
            @Mock
            final int methodToBeFaked(Invocation invocation, int i) {
                return invocation.<Integer>proceed(i + 2);
            }
        }

        new FakeWhichModifiesArguments() {
            @Mock
            synchronized int methodToBeFaked(Invocation inv, int i, Object... args) {
                Object[] newArgs = { 2, "3" };
                return inv.<Integer>proceed(1, newArgs);
            }
        };

        ClassToBeFaked faked = new ClassToBeFaked();
        assertEquals(3, faked.methodToBeFaked(1));
        assertEquals(3, faked.methodToBeFaked(-2, null, "Abc", true, 'a'));
    }

    /**
     * Cannot proceed from fake method into native method.
     */
    @Test
    public void cannotProceedFromFakeMethodIntoNativeMethod() {
        new MockUp<ClassToBeFaked>() {
            @Mock
            void nativeMethod(Invocation inv) {
                inv.proceed();
                fail("Should not get here");
            }
        };

        Throwable throwable = assertThrows(UnsupportedOperationException.class, () -> {
            ClassToBeFaked.nativeMethod();
        });
        assertEquals("Cannot proceed into real implementation of native method", throwable.getMessage());
    }

    /**
     * Proceed from fake method into constructor.
     */
    @Test
    public void proceedFromFakeMethodIntoConstructor() {
        new MockUp<ClassToBeFaked>() {
            @Mock
            void $init(Invocation inv) {
                assertNotNull(inv.<ClassToBeFaked>getInvokedInstance());
                inv.proceed();
            }
        };

        ClassToBeFaked obj = new ClassToBeFaked();
        assertEquals("", obj.name);
    }

    /**
     * Proceed conditionally from fake method into constructor.
     */
    @Test
    public void proceedConditionallyFromFakeMethodIntoConstructor() {
        new MockUp<ClassToBeFaked>() {
            @Mock
            void $init(Invocation inv, String name) {
                assertNotNull(inv.getInvokedInstance());

                if ("proceed".equals(name)) {
                    inv.proceed();
                }
            }
        };

        assertEquals("proceed", new ClassToBeFaked("proceed").name);
        assertNull(new ClassToBeFaked("do not proceed").name);
    }

    /**
     * Proceed conditionally from fake method into JRE constructor.
     */
    @Test
    public void proceedConditionallyFromFakeMethodIntoJREConstructor() {
        new MockUp<File>() {
            @Mock
            void $init(Invocation inv, String name) {
                if ("proceed".equals(name)) {
                    inv.proceed();
                }
            }
        };

        assertEquals("proceed", new File("proceed").getPath());
        assertNull(new File("do not proceed").getPath());
    }

    /**
     * Proceed from fake method into method inherited from base class.
     */
    @Test
    public void proceedFromFakeMethodIntoMethodInheritedFromBaseClass() {
        new MockUp<ClassToBeFaked>() {
            @Mock
            int baseMethod(Invocation inv, int i) {
                return inv.proceed(i + 1);
            }
        };

        assertEquals(3, new ClassToBeFaked().baseMethod(1));
    }
}
