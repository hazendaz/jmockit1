package mockit;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;

/**
 * The Class AssertionErrorMessagesTest.
 */
public final class AssertionErrorMessagesTest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /**
         * Do something.
         */
        void doSomething() {
            // do nothing
        }

        /**
         * Do something.
         *
         * @param i
         *            the i
         * @param s
         *            the s
         */
        void doSomething(@SuppressWarnings("unused") int i, @SuppressWarnings("unused") String s) {
            // do nothing
        }

        /**
         * Do something else.
         *
         * @param s
         *            the s
         */
        void doSomethingElse(@SuppressWarnings("unused") String s) {
            // do nothing
        }
    }

    /** The mock. */
    @Mocked
    Collaborator mock;

    /**
     * Unexpected invocation for recorded expectation.
     */
    @Test
    public void unexpectedInvocationForRecordedExpectation() {
        new Expectations() {
            {
                mock.doSomething(anyInt, anyString);
                times = 1;
            }
        };

        mock.doSomething(1, "Abc");
        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> mock.doSomething(2, "xyz"));
        Assertions.assertTrue(exception.getMessage().contains("Unexpected invocation to"));
        Assertions.assertTrue(exception.getMessage().contains("doSomething(2, \"xyz\""));
    }

    /**
     * Unexpected invocation where expecting another for recorded expectations.
     */
    @Test
    public void unexpectedInvocationWhereExpectingAnotherForRecordedExpectations() {
        mock.doSomething(1, "Abc");
        mock.doSomething(2, "xyz");
        mock.doSomethingElse("test");

        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> {
            new VerificationsInOrder() {
                {
                    mock.doSomething(anyInt, anyString);
                    times = 1;
                    mock.doSomethingElse(anyString);
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("doSomething(2, \"xyz\""));
    }

    /**
     * Unexpected invocation for recorded expectation with maximum invocation count of zero.
     */
    @Test
    public void unexpectedInvocationForRecordedExpectationWithMaximumInvocationCountOfZero() {
        new Expectations() {
            {
                mock.doSomething(anyInt, anyString);
                times = 0;
            }
        };

        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> mock.doSomething(1, "Abc"));
        Assertions.assertTrue(exception.getMessage().contains("1, \"Abc\""));
    }

    /**
     * Unexpected invocation for verified expectation.
     */
    @Test
    public void unexpectedInvocationForVerifiedExpectation() {
        mock.doSomething(123, "Test");
        mock.doSomethingElse("abc");

        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> {
            new Verifications() {
                {
                    mock.doSomething(123, anyString);
                    times = 0;
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("123, \"Test\""));
    }

    /**
     * Unexpected invocation for expectations verified in order.
     */
    @Test
    public void unexpectedInvocationForExpectationsVerifiedInOrder() {
        mock.doSomethingElse("test");
        mock.doSomething(123, "Test");

        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> {
            new VerificationsInOrder() {
                {
                    mock.doSomethingElse(anyString);
                    mock.doSomething(anyInt, anyString);
                    times = 0;
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("123, \"Test\""));
    }

    /**
     * Unexpected invocation on method with no parameters.
     */
    @Test
    public void unexpectedInvocationOnMethodWithNoParameters() {
        new Expectations() {
            {
                mock.doSomethingElse(anyString);
            }
        };

        mock.doSomething();

        thrown.expect(UnexpectedInvocation.class);
        thrown.expectMessage("doSomething()\n   on mock instance");
        new FullVerifications(mock) {
        };
    }

    /**
     * Missing invocation for recorded expectation.
     */
    @Test
    public void missingInvocationForRecordedExpectation() {
        new Expectations() {
            {
                mock.doSomething(anyInt, anyString);
                times = 2;
            }
        };

        thrown.expect(MissingInvocation.class);
        thrown.expectMessage("any int, any String");

        mock.doSomething(123, "Abc");
    }

    /**
     * Missing invocation for recorded expectation which gets non matching invocations at replay time.
     */
    @Test
    public void missingInvocationForRecordedExpectationWhichGetsNonMatchingInvocationsAtReplayTime() {
        new Expectations() {
            {
                mock.doSomethingElse("test");
            }
        };

        thrown.expect(MissingInvocation.class);
        thrown.expectMessage("doSomethingElse(\"test\")");
        thrown.expectMessage("instead got:");
        thrown.expectMessage("doSomethingElse(\"Abc\")");
        thrown.expectMessage("doSomethingElse(\"\")");

        mock.doSomethingElse("Abc");
        mock.doSomething(1, "xy");
        mock.doSomethingElse("");
    }

    /**
     * Missing invocation for verified expectation.
     */
    @Test
    public void missingInvocationForVerifiedExpectation() {
        Throwable exception = Assertions.assertThrows(MissingInvocation.class, () -> {
            new Verifications() {
                {
                    mock.doSomething(123, anyString);
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("123, any String"));
    }

    /**
     * Missing invocation for verified expectation which gets non matching invocations at replay time.
     */
    @Test
    public void missingInvocationForVerifiedExpectationWhichGetsNonMatchingInvocationsAtReplayTime() {
        mock.doSomethingElse("Abc");
        mock.doSomething(1, "xy");
        mock.doSomethingElse("");

        Throwable exception = Assertions.assertThrows(MissingInvocation.class, () -> {
            new Verifications() {
                {
                    mock.doSomethingElse("test");
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("doSomethingElse(\"test\")"));
        Assertions.assertTrue(exception.getMessage().contains("instead got:"));
        Assertions.assertTrue(exception.getMessage().contains("doSomethingElse(\"Abc\")"));
        Assertions.assertTrue(exception.getMessage().contains("doSomethingElse(\"\")"));
    }

    /**
     * Missing invocation for expectation verified in order.
     */
    @Test
    public void missingInvocationForExpectationVerifiedInOrder() {
        mock.doSomething(123, "Test");

        Throwable exception = Assertions.assertThrows(MissingInvocation.class, () -> {
            new VerificationsInOrder() {
                {
                    mock.doSomething(anyInt, anyString);
                    minTimes = 3;
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("any int, any String"));
    }

    /**
     * Missing invocation for fully verified expectations.
     */
    @Test
    public void missingInvocationForFullyVerifiedExpectations() {
        mock.doSomething(123, "Abc");

        Throwable exception = Assertions.assertThrows(MissingInvocation.class, () -> {
            new FullVerifications() {
                {
                    mock.doSomething(anyInt, anyString);
                    times = 2;
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("any int, any String"));
    }

    /**
     * Missing invocation for expectation using matcher for different parameter type.
     */
    @Test
    public void missingInvocationForExpectationUsingMatcherForDifferentParameterType() {
        mock.doSomething(5, "");

        Throwable exception = Assertions.assertThrows(MissingInvocation.class, () -> {
            new Verifications() {
                {
                    mock.doSomething(anyChar, "");
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("any char"));
    }
}
