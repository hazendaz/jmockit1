package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * The Class TestedClassWithConstructorDI3Test.
 */
final class TestedClassWithConstructorDI3Test {

    /**
     * The Class TestedClass.
     */
    public static final class TestedClass {

        /** The dependencies. */
        private final Dependency[] dependencies;

        /**
         * Instantiates a new tested class.
         *
         * @param r
         *            the r
         * @param dependencies
         *            the dependencies
         */
        public TestedClass(Runnable r, Dependency... dependencies) {
            this.dependencies = dependencies;
            r.run();
        }

        /**
         * Do some operation.
         *
         * @return the int
         */
        public int doSomeOperation() {
            int sum = 0;

            for (Dependency dependency : dependencies) {
                sum += dependency.doSomething();
            }

            return sum;
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

    /** The support. */
    @Tested(availableDuringSetup = true)
    TestedClass support;

    /** The tested. */
    @Tested
    TestedClass tested;

    /** The mock 1. */
    @Injectable
    Dependency mock1;

    /** The task. */
    @Injectable
    Runnable task;

    /** The mock 2. */
    @Injectable
    Dependency mock2;

    /**
     * Exercise tested object with dependencies of same type injected through varargs constructor parameter.
     */
    @Test
    void exerciseTestedObjectWithDependenciesOfSameTypeInjectedThroughVarargsConstructorParameter() {
        assertNotNull(support);

        new Expectations() {
            {
                mock1.doSomething();
                result = 23;
                mock2.doSomething();
                result = 5;
            }
        };

        assertEquals(28, tested.doSomeOperation());
    }

    /**
     * Exercise tested object with dependencies provided by mock fields and mock parameter.
     *
     * @param mock3
     *            the mock 3
     */
    @Test
    void exerciseTestedObjectWithDependenciesProvidedByMockFieldsAndMockParameter(@Injectable final Dependency mock3) {
        assertNotNull(support);

        new Expectations() {
            {
                mock1.doSomething();
                result = 2;
                mock2.doSomething();
                result = 3;
                mock3.doSomething();
                result = 5;
            }
        };

        assertEquals(10, tested.doSomeOperation());
    }

    /**
     * The Class ClassWithStringParameter.
     */
    static class ClassWithStringParameter {

        /** The name. */
        final String name;

        /**
         * Instantiates a new class with string parameter.
         *
         * @param name
         *            the name
         */
        ClassWithStringParameter(String name) {
            this.name = name;
        }
    }

    /** The tested 2. */
    @Tested
    ClassWithStringParameter tested2;

    /** The name. */
    @Injectable
    String name;

    /**
     * Initialize tested object with empty string parameter.
     */
    @Test
    void initializeTestedObjectWithEmptyStringParameter() {
        assertEquals("", tested2.name);
    }
}
