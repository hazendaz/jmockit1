package mockit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

/**
 * The Class TestedFieldWithFailedFullDITest.
 */
class TestedFieldWithFailedFullDITest {

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
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            // Access the tested field to ensure initialization happens (and the failure is thrown) during test
            // execution.
            // Depending on JMockit/JUnit interplay, initialization may occur earlier; this mirrors the original intent.
            ClassWithFieldOfClassHavingParameterizedConstructor t = tested;
            // reference to avoid unused warning
            if (t == null) {
                // no-op
            }
        });

        assertTrue(e.getMessage().contains("Missing @Tested or @Injectable"));
        assertTrue(e.getMessage()
                .contains("for parameter \"value\" in constructor ClassWithParameterizedConstructor(int value)"));
        assertTrue(e.getMessage().contains("when initializing field ") || e.getMessage().contains("when initializing"));
        assertTrue(e.getMessage().contains("dependency"));
        assertTrue(e.getMessage().contains("of @Tested object \""
                + ClassWithFieldOfClassHavingParameterizedConstructor.class.getSimpleName() + " tested"));
    }

    /**
     * Attempt to use tested object whose creation failed due to injectable with null value 2.
     */
    @Test
    void attemptToUseTestedObjectWhoseCreationFailedDueToInjectableWithNullValue2() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            ClassWithFieldOfClassHavingParameterizedConstructor t = tested;
            if (t == null) {
                // no-op
            }
        });

        assertTrue(e.getMessage().contains("Missing @Tested or @Injectable"));
        assertTrue(e.getMessage()
                .contains("for parameter \"value\" in constructor ClassWithParameterizedConstructor(int value)"));
        assertTrue(e.getMessage().contains("when initializing field ") || e.getMessage().contains("when initializing"));
        assertTrue(e.getMessage().contains("dependency"));
        assertTrue(e.getMessage().contains("of @Tested object \""
                + ClassWithFieldOfClassHavingParameterizedConstructor.class.getSimpleName() + " tested"));
    }
}
