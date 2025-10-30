package mockit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The Class AssertionErrorMessagesTest.
 */
final class AssertionErrorMessagesTest {

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /**
         * Do something.
         */
        void doSomething() {
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
        }

        /**
         * Do something else.
         *
         * @param s
         *            the s
         */
        void doSomethingElse(@SuppressWarnings("unused") String s) {
        }
    }

    /** The mock. */
    @Mocked
    Collaborator mock;

    /**
     * Unexpected invocation for recorded expectation.
     */
    @Test
    void unexpectedInvocationForRecordedExpectation() {
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
    void unexpectedInvocationWhereExpectingAnotherForRecordedExpectations() {
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
    void unexpectedInvocationForRecordedExpectationWithMaximumInvocationCountOfZero() {
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
    void unexpectedInvocationForVerifiedExpectation() {
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
    void unexpectedInvocationForExpectationsVerifiedInOrder() {
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
    void unexpectedInvocationOnMethodWithNoParameters() {
        new Expectations() {
            {
                mock.doSomethingElse(anyString);
            }
        };

        Throwable exception = assertThrows(UnexpectedInvocation.class, () -> {
            mock.doSomething();
            new FullVerifications(mock) {
            };
        });
        assertTrue(exception.getMessage().contains("doSomething()\n   on mock instance"));
    }

    /**
     * Missing invocation for recorded expectation.
     */
    @Test
    void missingInvocationForRecordedExpectation() {
        new Expectations() {
            {
                mock.doSomething(anyInt, anyString);
                times = 2;
            }
        };

        Throwable exception = assertThrows(MissingInvocation.class, () -> {
            mock.doSomething(123, "Abc");
        });
        assertTrue(exception.getMessage().contains("any int, any String"));
    }

    /**
     * Missing invocation for recorded expectation which gets non matching invocations at replay time.
     */
    @Test
    void missingInvocationForRecordedExpectationWhichGetsNonMatchingInvocationsAtReplayTime() {
        new Expectations() {
            {
                mock.doSomethingElse("test");
            }
        };

        Throwable exception = assertThrows(MissingInvocation.class, () -> {
            mock.doSomethingElse("Abc");
            mock.doSomething(1, "xy");
            mock.doSomethingElse("");
        });
        assertTrue(exception.getMessage().contains("doSomethingElse(\"\")"));
        assertTrue(exception.getMessage().contains("instead got:"));
        assertTrue(exception.getMessage().contains("doSomethingElse(\"Abc\")"));
        assertTrue(exception.getMessage().contains("doSomethingElse(\"\")"));
    }

    /**
     * Missing invocation for verified expectation.
     */
    @Test
    void missingInvocationForVerifiedExpectation() {
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
    void missingInvocationForVerifiedExpectationWhichGetsNonMatchingInvocationsAtReplayTime() {
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
    void missingInvocationForExpectationVerifiedInOrder() {
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
    void missingInvocationForFullyVerifiedExpectations() {
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
    void missingInvocationForExpectationUsingMatcherForDifferentParameterType() {
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
