package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The Class TestedAbstractClassTest.
 */
final class TestedAbstractClassTest {

    /**
     * The Class AbstractClass.
     */
    public abstract static class AbstractClass implements Runnable {

        /** The value. */
        private final int value;

        /** The name. */
        protected String name;

        /**
         * Instantiates a new abstract class.
         *
         * @param value
         *            the value
         */
        protected AbstractClass(int value) {
            this.value = value;
        }

        /**
         * Do some operation.
         *
         * @return true, if successful
         */
        public final boolean doSomeOperation() {
            run();
            return doSomething() > 0;
        }

        /**
         * Do something.
         *
         * @return the int
         */
        protected abstract int doSomething();

        /**
         * Gets the value.
         *
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    // A subclass is generated with the *same* constructors as the tested class, and with *mocked* implementations
    /** The tested. */
    // for all abstract methods in the tested base class, its super-classes and its implemented interfaces.
    @Tested
    AbstractClass tested;

    /** The value. */
    @Injectable("123")
    int value;

    /**
     * Exercise tested object.
     *
     * @param name
     *            the name
     */
    @Test
    void exerciseTestedObject(@Injectable("Test") String name) {
        assertThatGeneratedSubclassIsAlwaysTheSame();
        assertEquals(123, tested.getValue());
        assertEquals("Test", tested.name);

        new Expectations() {
            {
                tested.doSomething();
                result = 23;
                times = 1;
            }
        };

        assertTrue(tested.doSomeOperation());

        new Verifications() {
            {
                tested.run();
            }
        };
    }

    /**
     * Exercise dynamically mocked tested object.
     */
    @Test
    void exerciseDynamicallyMockedTestedObject() {
        assertThatGeneratedSubclassIsAlwaysTheSame();
        assertEquals(123, tested.getValue());

        new Expectations(tested) {
            {
                tested.getValue();
                result = 45;
                tested.doSomething();
                result = 7;
            }
        };

        assertEquals(45, tested.getValue());
        assertTrue(tested.doSomeOperation());

        new Verifications() {
            {
                tested.run();
                times = 1;
            }
        };
    }

    /**
     * Exercise tested object again.
     *
     * @param text
     *            the text
     */
    @Test
    void exerciseTestedObjectAgain(@Injectable("Another test") String text) {
        assertThatGeneratedSubclassIsAlwaysTheSame();
        assertEquals(123, tested.getValue());
        assertEquals("Another test", tested.name);

        assertFalse(tested.doSomeOperation());

        new VerificationsInOrder() {
            {
                tested.run();
                tested.doSomething();
            }
        };
    }

    /** The generated subclass. */
    Class<?> generatedSubclass;

    /**
     * Assert that generated subclass is always the same.
     */
    void assertThatGeneratedSubclassIsAlwaysTheSame() {
        Class<?> testedClass = tested.getClass();

        if (generatedSubclass == null) {
            generatedSubclass = testedClass;
        } else {
            assertSame(generatedSubclass, testedClass);
        }
    }
}
