package mockit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The Class TestedFieldWithFailedFullDITest.
 */
final class TestedFieldWithFailedFullDITest {

    /**
     * Configure expected exception.
     */
    @BeforeEach
    void configureExpectedException() {
        Throwable exception = assertThrows(IllegalStateException.class, () -> {
        });
        assertTrue(exception.getMessage().contains("of @Tested object \""
                + ClassWithFieldOfClassHavingParameterizedConstructor.class.getSimpleName() + " tested"));
    }

    /**
     * The Class ClassWithFieldOfClassHavingParameterizedConstructor.
     */
    static class ClassWithFieldOfClassHavingParameterizedConstructor {
        /** The dependency. */
        @Inject
        ClassWithParameterizedConstructor dependency;
    }

    /**
     * The Class ClassWithParameterizedConstructor.
     */
    static class ClassWithParameterizedConstructor {
        /**
         * Instantiates a new class with parameterized constructor.
         *
         * @param value
         *            the value
         */
        ClassWithParameterizedConstructor(@SuppressWarnings("unused") int value) {
        }
    }

    /** The tested. */
    @Tested(fullyInitialized = true)
    ClassWithFieldOfClassHavingParameterizedConstructor tested;

    /**
     * Attempt to use tested object whose creation failed due to injectable with null value.
     */
    @Test
    void attemptToUseTestedObjectWhoseCreationFailedDueToInjectableWithNullValue() {
    }

    /**
     * Attempt to use tested object whose creation failed due to injectable with null value 2.
     */
    @Test
    void attemptToUseTestedObjectWhoseCreationFailedDueToInjectableWithNullValue2() {
    }
}
