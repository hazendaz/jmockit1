/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The Class DynamicMockingInBeforeMethodTest.
 */
final class DynamicMockingInBeforeMethodTest {

    /**
     * The Class MockedClass.
     */
    static final class MockedClass {
        /**
         * Do something.
         *
         * @param i
         *            the i
         *
         * @return true, if successful
         */
        boolean doSomething(int i) {
            return i > 0;
        }
    }

    /** The an instance. */
    final MockedClass anInstance = new MockedClass();

    /**
     * Record expectations on dynamically mocked class.
     */
    @BeforeEach
    void recordExpectationsOnDynamicallyMockedClass() {
        assertTrue(anInstance.doSomething(56));

        new Expectations(anInstance) {
            {
                anInstance.doSomething(anyInt);
                result = true;
                minTimes = 0;
            }
        };

        assertTrue(anInstance.doSomething(-56));
    }

    /**
     * Verify that dynamically mocked class is still mocked.
     */
    @AfterEach
    void verifyThatDynamicallyMockedClassIsStillMocked() {
        new FullVerifications() {
            {
                anInstance.doSomething(anyInt);
                times = 2;
            }
        };
    }

    /**
     * Test something.
     */
    @Test
    void testSomething() {
        assertTrue(anInstance.doSomething(56));
    }

    /**
     * Test something else.
     */
    @Test
    void testSomethingElse() {
        assertTrue(anInstance.doSomething(-129));
    }
}
