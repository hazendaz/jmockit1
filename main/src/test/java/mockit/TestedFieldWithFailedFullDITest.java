package mockit;

import jakarta.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class TestedFieldWithFailedFullDITest.
 */
public final class TestedFieldWithFailedFullDITest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /**
     * Configure expected exception.
     */
    @Before
    public void configureExpectedException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Missing @Tested or @Injectable");
        thrown.expectMessage("for parameter \"value\" in constructor ClassWithParameterizedConstructor(int value)");
        thrown.expectMessage("when initializing field ");
        thrown.expectMessage("dependency");
        thrown.expectMessage("of @Tested object \""
                + ClassWithFieldOfClassHavingParameterizedConstructor.class.getSimpleName() + " tested");
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
    public void attemptToUseTestedObjectWhoseCreationFailedDueToInjectableWithNullValue() {
    }

    /**
     * Attempt to use tested object whose creation failed due to injectable with null value 2.
     */
    @Test
    public void attemptToUseTestedObjectWhoseCreationFailedDueToInjectableWithNullValue2() {
    }
}
