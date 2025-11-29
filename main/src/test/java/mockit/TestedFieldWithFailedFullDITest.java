/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import jakarta.inject.Inject;

import mockit.integration.junit5.ExpectedException;
import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class TestedFieldWithFailedFullDITest.
 */
@ExtendWith(JMockitExtension.class)
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
    @ExpectedException(value = IllegalStateException.class, expectedMessages = { "Missing @Tested or @Injectable",
            "for parameter \"value\" in constructor ClassWithParameterizedConstructor(int value)",
            "when initializing field ", "dependency",
            "of @Tested object \"ClassWithFieldOfClassHavingParameterizedConstructor tested" })
    void attemptToUseTestedObjectWhoseCreationFailedDueToInjectableWithNullValue() {
    }

    /**
     * Attempt to use tested object whose creation failed due to injectable with null value 2.
     */
    @Test
    @ExpectedException(value = IllegalStateException.class, expectedMessages = { "Missing @Tested or @Injectable",
            "for parameter \"value\" in constructor ClassWithParameterizedConstructor(int value)",
            "when initializing field ", "dependency",
            "of @Tested object \"ClassWithFieldOfClassHavingParameterizedConstructor tested" })
    void attemptToUseTestedObjectWhoseCreationFailedDueToInjectableWithNullValue2() {
    }
}
