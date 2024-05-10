package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * The Class ReentrantFakeTest.
 */
final class ReentrantFakeTest {

    /**
     * The Class RealClass.
     */
    public static class RealClass {

        /**
         * Foo.
         *
         * @return the string
         */
        public String foo() {
            return "real value";
        }

        /**
         * Static recursive method.
         *
         * @param i
         *            the i
         *
         * @return the int
         */
        protected static int staticRecursiveMethod(int i) {
            return i <= 0 ? 0 : 2 + staticRecursiveMethod(i - 1);
        }

        /**
         * Recursive method.
         *
         * @param i
         *            the i
         *
         * @return the int
         */
        public int recursiveMethod(int i) {
            return i <= 0 ? 0 : 2 + recursiveMethod(i - 1);
        }

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
     * The Class AnnotatedFakeClass.
     */
    public static class AnnotatedFakeClass extends MockUp<RealClass> {

        /** The fake it. */
        private static Boolean fakeIt;

        /**
         * Foo.
         *
         * @param inv
         *            the inv
         *
         * @return the string
         */
        @Mock
        public String foo(Invocation inv) {
            if (fakeIt == null) {
                throw new IllegalStateException("null fakeIt");
            } else if (fakeIt) {
                return "fake value";
            } else {
                return inv.proceed();
            }
        }
    }

    /**
     * Call fake method.
     */
    @Test
    void callFakeMethod() {
        new AnnotatedFakeClass();
        AnnotatedFakeClass.fakeIt = true;

        String foo = new RealClass().foo();

        assertEquals("fake value", foo);
    }

    /**
     * Call original method.
     */
    @Test
    void callOriginalMethod() {
        new AnnotatedFakeClass();
        AnnotatedFakeClass.fakeIt = false;

        String foo = new RealClass().foo();

        assertEquals("real value", foo);
    }

    /**
     * Called fake throws exception.
     */
    @Test
    void calledFakeThrowsException() {
        assertThrows(IllegalStateException.class, () -> {
            new AnnotatedFakeClass();
            AnnotatedFakeClass.fakeIt = null;

            new RealClass().foo();
        });
    }

    /**
     * The Class FakeRuntime.
     */
    public static class FakeRuntime extends MockUp<Runtime> {

        /** The run finalization count. */
        private int runFinalizationCount;

        /**
         * Run finalization.
         *
         * @param inv
         *            the inv
         */
        @Mock
        public void runFinalization(Invocation inv) {
            if (runFinalizationCount < 2) {
                inv.proceed();
            }

            runFinalizationCount++;
        }

        /**
         * Removes the shutdown hook.
         *
         * @param inv
         *            the inv
         * @param hook
         *            the hook
         *
         * @return true, if successful
         */
        @Mock
        public boolean removeShutdownHook(Invocation inv, Thread hook) {
            if (hook == null) {
                // noinspection AssignmentToMethodParameter
                hook = Thread.currentThread();
            }

            return inv.proceed(hook);
        }
    }

    /**
     * Call fake method for JRE class.
     */
    @Test
    void callFakeMethodForJREClass() {
        Runtime runtime = Runtime.getRuntime();
        new FakeRuntime();

        runtime.runFinalization();
        runtime.runFinalization();
        runtime.runFinalization();

        assertFalse(runtime.removeShutdownHook(null));
    }

    /**
     * The Class ReentrantFakeForNativeMethod.
     */
    public static class ReentrantFakeForNativeMethod extends MockUp<Runtime> {

        /**
         * Available processors.
         *
         * @param inv
         *            the inv
         *
         * @return the int
         */
        @Mock
        public int availableProcessors(Invocation inv) {
            assertNotNull(inv.getInvokedInstance());
            return 5;
        }
    }

    /**
     * Apply reentrant fake for native JRE method.
     */
    @Test
    void applyReentrantFakeForNativeJREMethod() {
        new ReentrantFakeForNativeMethod();

        assertEquals(5, Runtime.getRuntime().availableProcessors());
    }

    /**
     * The Class MultiThreadedFake.
     */
    @SuppressWarnings("SynchronizeOnThis")
    static class MultiThreadedFake extends MockUp<RealClass> {

        /** The nobody entered. */
        private static boolean nobodyEntered = true;

        /**
         * Foo.
         *
         * @param inv
         *            the inv
         *
         * @return the string
         *
         * @throws InterruptedException
         *             the interrupted exception
         */
        @Mock
        public String foo(Invocation inv) throws InterruptedException {
            String value = inv.proceed();

            synchronized (MultiThreadedFake.class) {
                if (nobodyEntered) {
                    nobodyEntered = false;
                    // noinspection WaitNotInLoop
                    MultiThreadedFake.class.wait(5000);
                } else {
                    MultiThreadedFake.class.notifyAll();
                }
            }

            return value.replace("real", "fake");
        }
    }

    /**
     * Two concurrent threads calling the same reentrant fake.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @Timeout(1000)
    void twoConcurrentThreadsCallingTheSameReentrantFake() throws Exception {
        new MultiThreadedFake();

        final StringBuilder first = new StringBuilder();
        final StringBuilder second = new StringBuilder();

        Thread thread1 = new Thread(() -> first.append(new RealClass().foo()));
        thread1.start();

        Thread thread2 = new Thread(() -> second.append(new RealClass().foo()));
        thread2.start();

        thread1.join();
        thread2.join();

        assertEquals("fake value", first.toString());
        assertEquals("fake value", second.toString());
    }

    /**
     * The Class RealClass2.
     */
    public static final class RealClass2 {

        /**
         * First method.
         *
         * @return the int
         */
        public int firstMethod() {
            return 1;
        }

        /**
         * Second method.
         *
         * @return the int
         */
        public int secondMethod() {
            return 2;
        }
    }

    /**
     * Reentrant fake for non JRE class which calls another from A different thread.
     */
    @Test
    void reentrantFakeForNonJREClassWhichCallsAnotherFromADifferentThread() {
        new MockUp<RealClass2>() {
            int value;

            @Mock
            int firstMethod(Invocation inv) {
                return inv.proceed();
            }

            @Mock
            int secondMethod(Invocation inv) throws InterruptedException {
                final RealClass2 it = inv.getInvokedInstance();

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        value = it.firstMethod();
                    }
                };
                t.start();
                t.join();
                return value;
            }
        };

        RealClass2 r = new RealClass2();
        assertEquals(1, r.firstMethod());
        assertEquals(1, r.secondMethod());
    }

    /**
     * Reentrant fake for JRE class which calls another from A different thread.
     */
    @Test
    void reentrantFakeForJREClassWhichCallsAnotherFromADifferentThread() {
        System.setProperty("a", "1");
        System.setProperty("b", "2");

        new MockUp<System>() {
            String property;

            @Mock
            String getProperty(Invocation inv, String key) {
                return inv.proceed();
            }

            @Mock
            String clearProperty(final String key) throws InterruptedException {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        property = System.getProperty(key);
                    }
                };
                t.start();
                t.join();
                return property;
            }
        };

        assertEquals("1", System.getProperty("a"));
        assertEquals("2", System.clearProperty("b"));
    }

    /**
     * Fake file and force JRE to call reentrant faked method.
     */
    @Test
    void fakeFileAndForceJREToCallReentrantFakedMethod() {
        new MockUp<File>() {
            @Mock
            boolean exists(Invocation inv) {
                boolean exists = inv.proceed();
                return !exists;
            }
        };

        // Cause the JVM/JRE to load a new class, calling the faked File#exists() method in the process:
        new Runnable() {
            @Override
            public void run() {
            }
        };

        assertTrue(new File("noFile").exists());
    }

    /**
     * The Class RealClass3.
     */
    public static final class RealClass3 {

        /**
         * New instance.
         *
         * @return the real class 3
         */
        public RealClass3 newInstance() {
            return new RealClass3();
        }
    }

    /**
     * Reentrant fake for method which instantiates and returns new instance of the faked class.
     */
    @Test
    void reentrantFakeForMethodWhichInstantiatesAndReturnsNewInstanceOfTheFakedClass() {
        new MockUp<RealClass3>() {
            @Mock
            RealClass3 newInstance(Invocation inv) {
                return null;
            }
        };

        assertNull(new RealClass3().newInstance());
    }

    /**
     * The Class FakeClassWithReentrantFakeForRecursiveMethod.
     */
    public static final class FakeClassWithReentrantFakeForRecursiveMethod extends MockUp<RealClass> {

        /**
         * Recursive method.
         *
         * @param inv
         *            the inv
         * @param i
         *            the i
         *
         * @return the int
         */
        @Mock
        int recursiveMethod(Invocation inv, int i) {
            int j = inv.proceed();
            return 1 + j;
        }

        /**
         * Static recursive method.
         *
         * @param inv
         *            the inv
         * @param i
         *            the i
         *
         * @return the int
         */
        @Mock
        static int staticRecursiveMethod(Invocation inv, int i) {
            int j = inv.proceed();
            return 1 + j;
        }
    }

    /**
     * Reentrant fake method for recursive methods.
     */
    @Test
    void reentrantFakeMethodForRecursiveMethods() {
        assertEquals(0, RealClass.staticRecursiveMethod(0));
        assertEquals(2, RealClass.staticRecursiveMethod(1));

        RealClass r = new RealClass();
        assertEquals(0, r.recursiveMethod(0));
        assertEquals(2, r.recursiveMethod(1));

        new FakeClassWithReentrantFakeForRecursiveMethod();

        assertEquals(1, RealClass.staticRecursiveMethod(0));
        assertEquals(1 + 2 + 1, RealClass.staticRecursiveMethod(1));
        assertEquals(1, r.recursiveMethod(0));
        assertEquals(4, r.recursiveMethod(1));
    }

    /**
     * Fake that proceeds into recursive method.
     */
    @Test
    void fakeThatProceedsIntoRecursiveMethod() {
        RealClass r = new RealClass();
        assertEquals(0, r.recursiveMethod(0));
        assertEquals(2, r.recursiveMethod(1));

        new MockUp<RealClass>() {
            @Mock
            int recursiveMethod(Invocation inv, int i) {
                int ret = inv.proceed();
                return 1 + ret;
            }
        };

        assertEquals(1, r.recursiveMethod(0));
        assertEquals(4, r.recursiveMethod(1));
    }

    /**
     * Recursive fake method without invocation parameter.
     */
    @Test
    void recursiveFakeMethodWithoutInvocationParameter() {
        new MockUp<RealClass>() {
            @Mock
            int nonRecursiveStaticMethod(int i) {
                if (i > 1) {
                    return i;
                }
                return RealClass.nonRecursiveStaticMethod(i + 1);
            }
        };

        int result = RealClass.nonRecursiveStaticMethod(1);
        assertEquals(2, result);
    }

    /**
     * Recursive fake method with invocation parameter not used for proceeding.
     */
    @Test
    void recursiveFakeMethodWithInvocationParameterNotUsedForProceeding() {
        new MockUp<RealClass>() {
            @Mock
            int nonRecursiveMethod(Invocation inv, int i) {
                if (i > 1) {
                    return i;
                }
                RealClass it = inv.getInvokedInstance();
                return it.nonRecursiveMethod(i + 1);
            }
        };

        int result = new RealClass().nonRecursiveMethod(1);
        assertEquals(2, result);
    }

    /**
     * Non recursive fake method with invocation parameter used for proceeding.
     */
    @Test
    void nonRecursiveFakeMethodWithInvocationParameterUsedForProceeding() {
        new MockUp<RealClass>() {
            @Mock
            int nonRecursiveMethod(Invocation inv, int i) {
                if (i > 1) {
                    return i;
                }
                return inv.proceed(i + 1);
            }
        };

        int result = new RealClass().nonRecursiveMethod(1);
        assertEquals(-2, result);
    }
}
