package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The Class TestedFieldWithFailedConstructorDITest.
 */
class TestedFieldWithFailedConstructorDITest {

    /**
     * The Class ClassWithOneParameter.
     */
    static class ClassWithOneParameter {

        /** The value. */
        Integer value;

        /**
         * Instantiates a new class with one parameter.
         *
         * @param value
         *            the value
         */
        ClassWithOneParameter(Integer value) {
            this.value = value;
        }
    }

    /** The tested. */
    @Tested
    ClassWithOneParameter tested;

    /** The foo. */
    @Injectable
    Integer foo;

    /**
     * Attempt to use tested object whose creation failed due to injectable without A value.
     *
     * @param s
     *            the s
     */
    @Test
    void attemptToUseTestedObjectWhoseCreationFailedDueToInjectableWithoutAValue(@Injectable String s) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            // Access the @Tested field to trigger initialization (and the expected failure)
            ClassWithOneParameter t = tested;
            if (t == null) {
                // no-op to avoid unused warning
            }
        });

        // Verify the exception message is similar to what the original test expected
        assertTrue(e.getMessage().contains("No injectable value available for parameter \"value\" in constructor "));
        assertTrue(e.getMessage().contains("ClassWithOneParameter(Integer value)"));

        // The injectable method parameter should still be provided by JMockit
        assertEquals("", s);
    }
}
