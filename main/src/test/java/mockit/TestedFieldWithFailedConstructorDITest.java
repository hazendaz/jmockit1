package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class TestedFieldWithFailedConstructorDITest.
 */
public final class TestedFieldWithFailedConstructorDITest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /**
     * Configure expected exception.
     */
    @Before
    public void configureExpectedException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No injectable value available for parameter \"value\" in constructor ");
        thrown.expectMessage("ClassWithOneParameter(Integer value)");
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
    public void attemptToUseTestedObjectWhoseCreationFailedDueToInjectableWithoutAValue(@Injectable String s) {
        assertEquals("", s);
    }
}
