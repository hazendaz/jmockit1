/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class MisusedMockingAPITest.
 */
public final class MisusedMockingAPITest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /**
     * The Class Blah.
     */
    static class Blah {

        /** The name. */
        @SuppressWarnings("RedundantStringConstructorCall")
        final String name = "Blah";

        /**
         * Value.
         *
         * @return the int
         */
        int value() {
            return -1;
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(@SuppressWarnings("unused") int value) {
        }

        /**
         * Do something.
         *
         * @param b
         *            the b
         *
         * @return the string
         */
        String doSomething(@SuppressWarnings("unused") boolean b) {
            return "";
        }

        /**
         * Gets the something else.
         *
         * @return the something else
         */
        Runnable getSomethingElse() {
            return null;
        }
    }

    /** The mock. */
    @Mocked
    Blah mock;

    // Arrange-Act-Assert non-conformance
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Record expectation after invoking same method in replay phase.
     */
    @Test
    public void recordExpectationAfterInvokingSameMethodInReplayPhase() {
        assertEquals(0, mock.value());

        new Expectations() {
            {
                mock.value();
                result = 1;
            }
        };

        assertEquals(1, mock.value());
    }

    // Duplicate/pointless recordings
    // //////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Record duplicate invocation with no arguments.
     */
    @Test
    public void recordDuplicateInvocationWithNoArguments() {
        new Expectations() {
            {
                mock.value();
                result = 1;
                mock.value();
                result = 2; // second recording overrides the first
            }
        };

        assertEquals(2, mock.value());
        assertEquals(2, mock.value());
    }

    /**
     * Record duplicate invocation with argument matcher.
     */
    @Test
    public void recordDuplicateInvocationWithArgumentMatcher() {
        new Expectations() {
            {
                mock.setValue(anyInt);
                result = new UnknownError();
                mock.setValue(anyInt); // overrides the previous one
            }
        };

        mock.setValue(3);
    }

    /**
     * Record duplicate invocation in separate expectation blocks.
     */
    @Test
    public void recordDuplicateInvocationInSeparateExpectationBlocks() {
        new Expectations() {
            {
                mock.value();
                result = 1;
            }
        };
        new Expectations() {
            {
                mock.value();
                result = 2;
            }
        }; // overrides the previous expectation

        assertEquals(2, mock.value());
    }

    /**
     * Record mock instance for constructor expectation.
     */
    @Test
    public void recordMockInstanceForConstructorExpectation() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid assignment");
        thrown.expectMessage("result ");
        thrown.expectMessage(" constructor");

        new Expectations() {
            {
                new Blah();
                result = mock;
            }
        };

        new Blah();
    }

    // Order-related recordings
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Record unordered instantiation of class mocked twice.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    public void recordUnorderedInstantiationOfClassMockedTwice(@Mocked final Blah mock2) {
        new Expectations() {
            {
                new Blah();
                times = 1;
                mock.value();
                result = 123;
                mock2.value();
                result = 45;
            }
        };

        assertEquals(45, mock2.value());
        assertEquals(123, mock.value());
        new Blah();
    }

    /**
     * Verify ordered instantiation of class mocked twice.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    public void verifyOrderedInstantiationOfClassMockedTwice(@Mocked final Blah mock2) {
        new Blah();
        mock2.doSomething(true);

        new VerificationsInOrder() {
            {
                new Blah();
                mock2.doSomething(anyBoolean);
            }
        };
    }

    /**
     * Verify unordered instantiation of class mocked twice.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    public void verifyUnorderedInstantiationOfClassMockedTwice(@Mocked final Blah mock2) {
        mock.doSomething(false);
        mock2.doSomething(true);
        new Blah();

        new Verifications() {
            {
                mock2.doSomething(true);
                new Blah();
                mock.doSomething(false);
            }
        };
    }

    // Cascading
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Ambiguous cascading when multiple valid candidates are available.
     *
     * @param r1
     *            the r 1
     * @param r2
     *            the r 2
     */
    @Test
    public void ambiguousCascadingWhenMultipleValidCandidatesAreAvailable(@Injectable Runnable r1,
            @Injectable Runnable r2) {
        Runnable cascaded = mock.getSomethingElse(); // which one to return: r1 or r2?

        assertSame(r2, cascaded); // currently, last mock to be declared wins
    }

    // @Tested/@Injectable usage
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The Class TestedClass.
     */
    static class TestedClass {
        /** The action. */
        Runnable action;
    }

    /** The mock action. */
    @Injectable
    static Runnable mockAction = new Runnable() {
        @Override
        public void run() {
        }
    };

    /** The tested. */
    @Tested
    TestedClass tested;

    /**
     * Check static injectable is not used.
     */
    @Test
    public void checkStaticInjectableIsNotUsed() {
        assertNotSame(mockAction, tested.action);
    }

    // Other cases
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The Class CustomException.
     */
    static final class CustomException extends Exception {

        private static final long serialVersionUID = 1L;
    }

    /**
     * Attempting to mock all instances of exception subclass.
     *
     * @param anyCustomException
     *            the any custom exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptingToMockAllInstancesOfExceptionSubclass(@Mocked CustomException anyCustomException) {
        fail("Shouldn't get here");
    }
}
