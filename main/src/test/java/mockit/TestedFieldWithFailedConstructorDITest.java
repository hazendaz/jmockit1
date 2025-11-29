/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import mockit.integration.junit5.ExpectedException;
import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class TestedFieldWithFailedConstructorDITest.
 */
@ExtendWith(JMockitExtension.class)
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
    @ExpectedException(value = IllegalArgumentException.class, expectedMessages = "No injectable value available for parameter \"value\" in constructor ClassWithOneParameter(Integer value)")
    void attemptToUseTestedObjectWhoseCreationFailedDueToInjectableWithoutAValue(@Injectable String s) {
        // This will not run as constructor failed above
        assertEquals("", s);
    }
}
