package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.internal.expectations.invocation.MissingInvocation;

import org.junit.Test;

/**
 * The Class TestedAndMockedTest.
 */
public final class TestedAndMockedTest {

    /**
     * The Class ClassToBeTested.
     */
    public static class ClassToBeTested {

        /** The some data. */
        private final String someData;

        /** The output data. */
        String outputData;

        /** The collaborator. */
        AnotherClassToBeTested collaborator;

        /**
         * Instantiates a new class to be tested.
         *
         * @param someData
         *            the some data
         */
        public ClassToBeTested(String someData) {
            this.someData = someData;
        }

        /**
         * Do some operation.
         *
         * @param i
         *            the i
         * @param s
         *            the s
         *
         * @return true, if successful
         */
        public boolean doSomeOperation(int i, String s) {
            validateInput(i, s);
            int j = i + doSomething();
            doSomethingElse(s);
            return j > 0;
        }

        /**
         * Validate input.
         *
         * @param i
         *            the i
         * @param s
         *            the s
         */
        static void validateInput(int i, String s) {
            if (i <= 0 || s == null) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething() {
            return -1;
        }

        /**
         * Do something else.
         *
         * @param s
         *            the s
         */
        void doSomethingElse(String s) {
            outputData = "output data: " + s;
        }

        /**
         * Do another operation.
         *
         * @return the int
         */
        int doAnotherOperation() {
            return collaborator.doSomething() - 23;
        }
    }

    /**
     * The Class AnotherClassToBeTested.
     */
    static final class AnotherClassToBeTested {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething() {
            return 123;
        }
    }

    /** The tested and injected. */
    @Tested
    AnotherClassToBeTested testedAndInjected;

    /** The tested. */
    @Tested(fullyInitialized = true)
    @Mocked
    ClassToBeTested tested;

    /** The test data. */
    @Injectable
    final String testData = "test data";

    /**
     * Exercise public method while having helper methods mocked.
     */
    @Test
    public void exercisePublicMethodWhileHavingHelperMethodsMocked() {
        assertEquals(testData, tested.someData);

        new Expectations() {
            {
                tested.doSomething();
                result = 123;
            }
        };
        new Expectations() {
            {
                ClassToBeTested.validateInput(anyInt, anyString);
            }
        };

        boolean result = tested.doSomeOperation(0, "testing");

        assertTrue(result);
        assertEquals("output data: testing", tested.outputData);

        new Verifications() {
            {
                tested.doSomethingElse(anyString);
                times = 1;
            }
        };
    }

    /**
     * Exercise top level tested object together with injected second level tested object.
     */
    @Test
    public void exerciseTopLevelTestedObjectTogetherWithInjectedSecondLevelTestedObject() {
        assertEquals(123, testedAndInjected.doSomething());
        assertEquals(100, tested.doAnotherOperation());
    }

    /**
     * Mock tested class.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = MissingInvocation.class)
    public void mockTestedClass(@Mocked final ClassToBeTested mock) {
        new Expectations() {
            {
                mock.doSomethingElse("");
            }
        };
    }
}
