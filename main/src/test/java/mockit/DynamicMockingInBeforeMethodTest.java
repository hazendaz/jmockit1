package mockit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class DynamicMockingInBeforeMethodTest.
 */
public final class DynamicMockingInBeforeMethodTest {

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
    @Before
    public void recordExpectationsOnDynamicallyMockedClass() {
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
    @After
    public void verifyThatDynamicallyMockedClassIsStillMocked() {
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
    public void testSomething() {
        assertTrue(anInstance.doSomething(56));
    }

    /**
     * Test something else.
     */
    @Test
    public void testSomethingElse() {
        assertTrue(anInstance.doSomething(-129));
    }
}
