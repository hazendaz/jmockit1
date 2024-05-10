package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The Class TestedFieldWithFailedConstructorDITest.
 */
final class TestedFieldWithFailedConstructorDITest {

    /**
     * Configure expected exception.
     */
    @BeforeEach
    void configureExpectedException() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
        });
        assertTrue(exception.getMessage().contains("ClassWithOneParameter(Integer value)"));
    }

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
        assertEquals("", s);
    }
}
