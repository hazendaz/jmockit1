/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.faces.application.FacesMessage;

import java.lang.reflect.Method;

import javax.accessibility.AccessibleState;

import mockit.MoreFakingTest.ClassWithNested.Nested;

import org.junit.jupiter.api.Test;

/**
 * The Class MoreFakingTest.
 */
@SuppressWarnings("JUnitTestMethodWithNoAssertions")
final class MoreFakingTest {

    /** The code under test. */
    final CodeUnderTest codeUnderTest = new CodeUnderTest();

    /** The fake executed. */
    boolean fakeExecuted;

    /**
     * The Class CodeUnderTest.
     */
    static class CodeUnderTest {

        /** The dependency. */
        private final Collaborator dependency = new Collaborator();

        /**
         * Do something.
         */
        void doSomething() {
            dependency.provideSomeService();
        }

        /**
         * Do something else.
         *
         * @param i
         *            the i
         *
         * @return the long
         */
        long doSomethingElse(int i) {
            return dependency.getThreadSpecificValue(i);
        }
    }

    /**
     * The Class Collaborator.
     */
    public static class Collaborator {

        /** The xyz. */
        static Object xyz;

        /** The value. */
        protected int value;

        /**
         * Instantiates a new collaborator.
         */
        public Collaborator() {
        }

        /**
         * Instantiates a new collaborator.
         *
         * @param value
         *            the value
         */
        Collaborator(int value) {
            this.value = value;
        }

        /**
         * Do internal.
         *
         * @return the string
         */
        @SuppressWarnings("DeprecatedIsStillUsed")
        @Deprecated
        protected static String doInternal() {
            return "123";
        }

        /**
         * Provide some service.
         */
        public void provideSomeService() {
            throw new RuntimeException("Real provideSomeService() called");
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public int getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(int value) {
            this.value = value;
        }

        /**
         * Gets the thread specific value.
         *
         * @param i
         *            the i
         *
         * @return the thread specific value
         */
        protected long getThreadSpecificValue(int i) {
            return Thread.currentThread().getId() + i;
        }
    }

    /**
     * The Class FakeCollaborator1.
     */
    static class FakeCollaborator1 extends MockUp<Collaborator> {

        /**
         * Provide some service.
         */
        @Mock
        void provideSomeService() {
        }
    }

    /**
     * Fake doing nothing.
     */
    @Test
    void fakeDoingNothing() {
        new FakeCollaborator1();

        codeUnderTest.doSomething();
    }

    /**
     * Apply fakes from inner fake class with fake constructor.
     */
    @Test
    void applyFakesFromInnerFakeClassWithFakeConstructor() {
        new FakeCollaborator4();
        assertFalse(fakeExecuted);

        new CodeUnderTest().doSomething();

        assertTrue(fakeExecuted);
    }

    /**
     * The Class FakeCollaborator4.
     */
    class FakeCollaborator4 extends MockUp<Collaborator> {

        /**
         * $init.
         */
        @Mock
        void $init() {
            fakeExecuted = true;
        }

        /**
         * Provide some service.
         */
        @Mock
        void provideSomeService() {
        }
    }

    /**
     * Apply reentrant fake.
     */
    @Test
    public void applyReentrantFake() {
        new FakeCollaboratorWithReentrantFakeMethod();

        assertThrows(RuntimeException.class, () -> {
            codeUnderTest.doSomething();
        });
    }

    /**
     * The Class FakeCollaboratorWithReentrantFakeMethod.
     */
    static class FakeCollaboratorWithReentrantFakeMethod extends MockUp<Collaborator> {

        /**
         * Gets the value.
         *
         * @return the value
         */
        @Mock
        int getValue() {
            return 123;
        }

        /**
         * Provide some service.
         *
         * @param inv
         *            the inv
         */
        @Mock
        void provideSomeService(Invocation inv) {
            inv.proceed();
        }
    }

    /**
     * Apply fake for constructor.
     */
    @Test
    void applyFakeForConstructor() {
        new FakeCollaboratorWithConstructorFake();

        new FacesMessage("test");
    }

    /**
     * The Class FakeCollaboratorWithConstructorFake.
     */
    static class FakeCollaboratorWithConstructorFake extends MockUp<FacesMessage> {

        /**
         * $init.
         *
         * @param value
         *            the value
         */
        @Mock
        void $init(String value) {
            assertEquals("test", value);
        }
    }

    /**
     * The Class SubCollaborator.
     */
    public static class SubCollaborator extends Collaborator {

        /**
         * Instantiates a new sub collaborator.
         *
         * @param i
         *            the i
         */
        public SubCollaborator(int i) {
            throw new RuntimeException(String.valueOf(i));
        }

        @Override
        public void provideSomeService() {
            value = 123;
        }
    }

    /**
     * Apply fake for class hierarchy.
     */
    @Test
    void applyFakeForClassHierarchy() {
        new MockUp<SubCollaborator>() {
            @Mock
            void $init(Invocation inv, int i) {
                assertNotNull(inv.getInvokedInstance());
                assertTrue(i > 0);
            }

            @Mock
            void provideSomeService(Invocation inv) {
                SubCollaborator it = inv.getInvokedInstance();
                it.value = 45;
            }

            @Mock
            int getValue(Invocation inv) {
                SubCollaborator it = inv.getInvokedInstance();
                assertNotNull(it);
                return 123;
            }
        };

        SubCollaborator collaborator = new SubCollaborator(123);
        collaborator.provideSomeService();
        assertEquals(45, collaborator.value);
        assertEquals(123, collaborator.getValue());
    }

    /**
     * Apply fake for JRE class.
     */
    @Test
    void applyFakeForJREClass() {
        FakeThread fakeThread = new FakeThread();

        Thread.currentThread().interrupt();

        assertTrue(fakeThread.interrupted);
    }

    /**
     * The Class FakeThread.
     */
    public static class FakeThread extends MockUp<Thread> {

        /** The interrupted. */
        boolean interrupted;

        /**
         * Interrupt.
         */
        @Mock
        public void interrupt() {
            interrupted = true;
        }
    }

    /**
     * Fake static initializer.
     */
    @Test
    void fakeStaticInitializer() {
        new MockUp<AccessibleState>() {
            @Mock
            void $clinit() {
            }
        };

        assertNull(AccessibleState.ACTIVE);
    }

    /**
     * The Class AnAbstractClass.
     */
    abstract static class AnAbstractClass {
        /**
         * Do something.
         *
         * @return the int
         */
        protected abstract int doSomething();
    }

    /**
     * Fake abstract class with fake for abstract method having invocation parameter.
     *
     * @param <A>
     *            the generic type
     */
    @Test
    <A extends AnAbstractClass> void fakeAbstractClassWithFakeForAbstractMethodHavingInvocationParameter() {
        final AnAbstractClass obj = new AnAbstractClass() {
            @Override
            protected int doSomething() {
                return 0;
            }
        };

        new MockUp<A>() {
            @Mock
            int doSomething(Invocation inv) {
                assertSame(obj, inv.getInvokedInstance());
                Method invokedMethod = inv.getInvokedMember();
                assertTrue(AnAbstractClass.class.isAssignableFrom(invokedMethod.getDeclaringClass()));
                return 123;
            }
        };

        assertEquals(123, obj.doSomething());
    }

    /**
     * The Class GenericClass.
     *
     * @param <T>
     *            the generic type
     */
    static class GenericClass<T> {
        /**
         * Do something.
         *
         * @return the t
         */
        protected T doSomething() {
            return null;
        }
    }

    /**
     * Fake generic class with fake having invocation parameter.
     */
    @Test
    void fakeGenericClassWithFakeHavingInvocationParameter() {
        new MockUp<GenericClass<String>>() {
            @Mock
            String doSomething(Invocation inv) {
                return "faked";
            }
        };

        GenericClass<String> faked = new GenericClass<>();
        assertEquals("faked", faked.doSomething());
    }

    /**
     * Concurrent fake.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @SuppressWarnings("MethodWithMultipleLoops")
    void concurrentFake() throws Exception {
        new MockUp<Collaborator>() {
            @Mock
            long getThreadSpecificValue(int i) {
                return Thread.currentThread().getId() + 123;
            }
        };

        Thread[] threads = new Thread[5];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {
                @Override
                public void run() {
                    long threadSpecificValue = Thread.currentThread().getId() + 123;
                    long actualValue = new CodeUnderTest().doSomethingElse(0);
                    assertEquals(threadSpecificValue, actualValue);
                }
            };
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Fake affects instances of specified subclass and not of base class.
     */
    @Test
    void fakeAffectsInstancesOfSpecifiedSubclassAndNotOfBaseClass() {
        new FakeForSubclass();

        // Faking applies to instance methods executed on instances of the subclass:
        assertEquals(123, new SubCollaborator(5).getValue());

        // And to static methods from any class in the hierarchy:
        // noinspection deprecation
        assertEquals("faked", Collaborator.doInternal());

        // But not to instance methods executed on instances of the base class:
        assertEquals(62, new Collaborator(62).getValue());
    }

    /**
     * The Class FakeForSubclass.
     */
    static class FakeForSubclass extends MockUp<SubCollaborator> {

        /**
         * $init.
         *
         * @param i
         *            the i
         */
        @Mock
        void $init(int i) {
        }

        /**
         * Do internal.
         *
         * @return the string
         */
        @Mock
        String doInternal() {
            return "faked";
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        @Mock
        int getValue() {
            return 123;
        }
    }

    /**
     * The Class ClassWithNested.
     */
    public static final class ClassWithNested {

        /**
         * Do something.
         */
        public static void doSomething() {
        }

        /**
         * The Class Nested.
         */
        public static final class Nested {
        }
    }

    /**
     * Fake A class having A nested class.
     */
    @Test
    void fakeAClassHavingANestedClass() {
        new MockUp<ClassWithNested>() {
            @Mock
            void doSomething() {
            }
        };

        Class<?> outerClass = Nested.class.getDeclaringClass();

        assertSame(ClassWithNested.class, outerClass);
    }
}
