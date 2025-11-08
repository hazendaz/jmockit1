package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.Observable;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class TestedClassWithConstructorDI1Test.
 */
@ExtendWith(JMockitExtension.class)
class TestedClassWithConstructorDI1Test {

    /**
     * The Class BaseTestedClass.
     */
    public static class BaseTestedClass {

        /** The base counter. */
        static int baseCounter;

        /**
         * Initialize base.
         */
        @PostConstruct
        void initializeBase() {
            baseCounter++;
        }

        /**
         * Destroy base.
         */
        @PreDestroy
        void destroyBase() {
            baseCounter++;
        }
    }

    /**
     * The Class TestedClass.
     */
    public static final class TestedClass extends BaseTestedClass {

        /** The counter. */
        static int counter;

        /** The dependency. */
        private final Dependency dependency;

        /** The runnable. */
        private final Runnable runnable;

        /** The observable. */
        private final Observable observable;

        /**
         * Instantiates a new tested class.
         *
         * @param dependency
         *            the dependency
         */
        public TestedClass(Dependency dependency) {
            this(dependency, null, null);
        }

        /**
         * Instantiates a new tested class.
         *
         * @param dependency
         *            the dependency
         * @param runnable
         *            the runnable
         */
        public TestedClass(Dependency dependency, Runnable runnable) {
            this(dependency, runnable, null);
        }

        /**
         * Instantiates a new tested class.
         *
         * @param dependency
         *            the dependency
         * @param observable
         *            the observable
         */
        public TestedClass(Dependency dependency, Observable observable) {
            this(dependency, null, observable);
        }

        /**
         * Instantiates a new tested class.
         *
         * @param dependency
         *            the dependency
         * @param runnable
         *            the runnable
         * @param observable
         *            the observable
         */
        public TestedClass(Dependency dependency, Runnable runnable, Observable observable) {
            this.dependency = dependency;
            this.runnable = runnable;
            this.observable = observable;
        }

        /**
         * Do some operation.
         *
         * @return true, if successful
         */
        public boolean doSomeOperation() {
            if (runnable != null) {
                runnable.run();
            }

            boolean b = dependency.doSomething() > 0;

            if (b && observable != null) {
                observable.notifyObservers();
            }

            return b;
        }

        /**
         * Initialize.
         */
        @PostConstruct
        void initialize() {
            counter++;
        }

        /**
         * Destroy.
         */
        @PreDestroy
        void destroy() {
            counter++;
        }
    }

    /**
     * The Class Dependency.
     */
    static class Dependency {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething() {
            return -1;
        }
    }

    /** The tested. */
    @Tested
    TestedClass tested;

    /** The mock. */
    @Injectable
    Dependency mock;

    /**
     * Exercise tested object with single dependency injected through constructor.
     */
    @Test
    void exerciseTestedObjectWithSingleDependencyInjectedThroughConstructor() {
        assertTestedObjectWasInitialized();

        new Expectations() {
            {
                mock.doSomething();
                result = 23;
            }
        };

        assertTrue(tested.doSomeOperation());
    }

    /**
     * Exercise tested object with two dependencies injected through constructor.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void exerciseTestedObjectWithTwoDependenciesInjectedThroughConstructor(@Injectable final Runnable mock2) {
        assertTestedObjectWasInitialized();

        new Expectations() {
            {
                mock.doSomething();
                result = 23;
            }
        };

        assertTrue(tested.doSomeOperation());

        new Verifications() {
            {
                mock2.run();
            }
        };
    }

    /**
     * Exercise tested object with two other dependencies injected through constructor.
     *
     * @param obs
     *            the obs
     */
    @Test
    void exerciseTestedObjectWithTwoOtherDependenciesInjectedThroughConstructor(@Injectable final Observable obs) {
        assertTestedObjectWasInitialized();

        new Expectations() {
            {
                mock.doSomething();
                result = 123;
            }
        };

        assertTrue(tested.doSomeOperation());

        new FullVerifications() {
            {
                obs.notifyObservers();
            }
        };
    }

    /**
     * Exercise tested object with all dependencies injected through constructor.
     *
     * @param mock2
     *            the mock 2
     * @param mock3
     *            the mock 3
     */
    @Test
    void exerciseTestedObjectWithAllDependenciesInjectedThroughConstructor(@Injectable final Runnable mock2,
            @Injectable final Observable mock3) {
        assertTestedObjectWasInitialized();

        new Expectations() {
            {
                mock.doSomething();
                result = 123;
            }
        };

        assertTrue(tested.doSomeOperation());

        new VerificationsInOrder() {
            {
                mock2.run();
                mock3.notifyObservers();
            }
        };
    }

    /**
     * Reset counter.
     */
    @BeforeEach
    void resetCounter() {
        BaseTestedClass.baseCounter = 0;
        TestedClass.counter = 0;
    }

    /**
     * Assert tested object was initialized.
     */
    void assertTestedObjectWasInitialized() {
        assertEquals(1, BaseTestedClass.baseCounter);
        assertEquals(1, TestedClass.counter);
    }

    /**
     * Verify tested object after every test.
     */
    @AfterEach
    void verifyTestedObjectAfterEveryTest() {
        assertEquals(2, BaseTestedClass.baseCounter);
        assertEquals(2, TestedClass.counter);
    }
}
