/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.fail;

import jakarta.inject.Inject;

import mockit.integration.junit5.ExpectedException;
import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class StandardDI2Test.
 */
@ExtendWith(JMockitExtension.class)
class StandardDI2Test {

    /**
     * The Class TestedClass.
     */
    static class TestedClass {

        /**
         * Instantiates a new tested class.
         */
        TestedClass() {
            throw new RuntimeException("Must not occur");
        }

        /**
         * Instantiates a new tested class.
         *
         * @param action
         *            the action
         */
        @Inject
        TestedClass(Runnable action) {
        }
    }

    /** The tested. */
    @Tested
    TestedClass tested;

    /**
     * Attempt to create tested object through annotated constructor with missing injectables.
     */
    @Test
    @ExpectedException(IllegalArgumentException.class)
    public void attemptToCreateTestedObjectThroughAnnotatedConstructorWithMissingInjectables() {
        fail();
    }
}
