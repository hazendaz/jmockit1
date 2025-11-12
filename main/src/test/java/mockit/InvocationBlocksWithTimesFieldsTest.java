/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import java.util.Date;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class InvocationBlocksWithTimesFieldsTest.
 */
public final class InvocationBlocksWithTimesFieldsTest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /** The code under test. */
    private final CodeUnderTest codeUnderTest = new CodeUnderTest();

    /**
     * The Class CodeUnderTest.
     */
    static class CodeUnderTest {

        /** The dependency. */
        private final Collaborator dependency = new Collaborator();

        /**
         * Do something.
         */
        void doSomething() {
            dependency.provideSomeService();
        }

        /**
         * Do something else.
         */
        void doSomethingElse() {
            dependency.simpleOperation(1, "b", null);
        }
    }

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /**
         * Instantiates a new collaborator.
         */
        Collaborator() {
        }

        /**
         * Instantiates a new collaborator.
         *
         * @param value
         *            the value
         */
        Collaborator(@SuppressWarnings("unused") int value) {
        }

        /**
         * Provide some service.
         */
        void provideSomeService() {
        }

        /**
         * Simple operation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         * @param c
         *            the c
         */
        @SuppressWarnings("UnusedDeclaration")
        final void simpleOperation(int a, String b, Date c) {
        }
    }

    // Tests with recorded expectations ////////////////////////////////////////////////////////////////////////////////

    /**
     * Expect twice by using invocation count.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectTwiceByUsingInvocationCount(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                times = 2;
                mock.simpleOperation(1, "b", null);
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();
    }

    /**
     * Expect twice by using invocation count but replay only once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectTwiceByUsingInvocationCountButReplayOnlyOnce(@Mocked final Collaborator mock) {
        thrown.expect(MissingInvocation.class);

        new Expectations() {
            {
                mock.simpleOperation(1, "b", null);
                mock.provideSomeService();
                times = 2;
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
    }

    /**
     * Expect at least once and replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectAtLeastOnceAndReplayTwice(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                mock.simpleOperation(1, "b", null);
            }
        };

        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
    }

    /**
     * Min times and max times out of order.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void minTimesAndMaxTimesOutOfOrder(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                maxTimes = 2;
                minTimes = 1;
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
    }

    /**
     * Expect at most twice and replay once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectAtMostTwiceAndReplayOnce(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                maxTimes = 2;
                mock.simpleOperation(1, "b", null);
            }
        };

        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();
    }

    /**
     * Expect at most once but replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectAtMostOnceButReplayTwice(@Mocked final Collaborator mock) {
        thrown.expect(UnexpectedInvocation.class);

        new Expectations() {
            {
                mock.simpleOperation(1, "b", null);
                mock.provideSomeService();
                maxTimes = 1;
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
    }

    /**
     * Expect at most zero.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectAtMostZero(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                maxTimes = 0;
            }
        };
    }

    /**
     * Expect at most zero but replay once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectAtMostZeroButReplayOnce(@Mocked final Collaborator mock) {
        thrown.expect(UnexpectedInvocation.class);

        new Expectations() {
            {
                mock.provideSomeService();
                maxTimes = 0;
            }
        };

        codeUnderTest.doSomething();
    }

    /**
     * Max times does not overwrite min times.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void maxTimesDoesNotOverwriteMinTimes(@Mocked final Collaborator mock) {
        thrown.expect(MissingInvocation.class);

        new Expectations() {
            {
                mock.provideSomeService();
                minTimes = 2;
                maxTimes = 3;
            }
        };

        codeUnderTest.doSomething();
    }

    /**
     * Expect same method once or twice then once but replay each expectation only once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectSameMethodOnceOrTwiceThenOnceButReplayEachExpectationOnlyOnce(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.simpleOperation(1, "", null);
                maxTimes = 2;
                mock.simpleOperation(2, "", null);
            }
        };

        mock.simpleOperation(2, "", null);
        mock.simpleOperation(1, "", null);
    }

    /**
     * Expect two or three times.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectTwoOrThreeTimes(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                minTimes = 2;
                maxTimes = 3;
                mock.simpleOperation(1, "b", null);
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();
    }

    // Tests with ordered verifications ////////////////////////////////////////////////////////////////////////////////

    /**
     * Ordered verify twice by using invocation count.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_verifyTwiceByUsingInvocationCount(@Mocked final Collaborator mock) {
        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();

        new VerificationsInOrder() {
            {
                mock.provideSomeService();
                times = 2;
                mock.simpleOperation(1, "b", null);
            }
        };
    }

    /**
     * Ordered verify twice by using invocation count but replay only once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_verifyTwiceByUsingInvocationCountButReplayOnlyOnce(@Mocked final Collaborator mock) {
        thrown.expect(MissingInvocation.class);

        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();

        new VerificationsInOrder() {
            {
                mock.simpleOperation(1, "b", null);
                mock.provideSomeService();
                times = 2;
            }
        };
    }

    /**
     * Ordered verify at least once and replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_verifyAtLeastOnceAndReplayTwice(@Mocked final Collaborator mock) {
        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();

        new VerificationsInOrder() {
            {
                mock.provideSomeService();
                mock.simpleOperation(1, "b", null);
            }
        };
    }

    /**
     * Ordered min times and max times out of order.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_minTimesAndMaxTimesOutOfOrder(@Mocked final Collaborator mock) {
        thrown.expect(UnexpectedInvocation.class);

        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomething();

        new VerificationsInOrder() {
            {
                mock.provideSomeService();
                maxTimes = 2;
                minTimes = 1;
            }
        };
    }

    /**
     * Ordered verify at most twice and replay once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_verifyAtMostTwiceAndReplayOnce(@Mocked final Collaborator mock) {
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();

        new VerificationsInOrder() {
            {
                mock.provideSomeService();
                maxTimes = 2;
                mock.simpleOperation(1, "b", null);
            }
        };
    }

    /**
     * Ordered verify at most once but replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_verifyAtMostOnceButReplayTwice(@Mocked final Collaborator mock) {
        thrown.expect(UnexpectedInvocation.class);

        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();

        new VerificationsInOrder() {
            {
                mock.provideSomeService();
                maxTimes = 1;
                mock.simpleOperation(1, "b", null);
            }
        };
    }

    /**
     * Ordered verify at most zero.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_verifyAtMostZero(@Mocked final Collaborator mock) {
        new VerificationsInOrder() {
            {
                mock.provideSomeService();
                maxTimes = 0;
            }
        };
    }

    /**
     * Ordered verify at most zero but replay once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_verifyAtMostZeroButReplayOnce(@Mocked final Collaborator mock) {
        thrown.expect(UnexpectedInvocation.class);

        codeUnderTest.doSomething();

        new VerificationsInOrder() {
            {
                mock.provideSomeService();
                maxTimes = 0;
            }
        };
    }

    /**
     * Ordered max times does not overwrite min times.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_maxTimesDoesNotOverwriteMinTimes(@Mocked final Collaborator mock) {
        thrown.expect(MissingInvocation.class);

        codeUnderTest.doSomething();

        new VerificationsInOrder() {
            {
                mock.provideSomeService();
                minTimes = 2;
                maxTimes = 3;
            }
        };
    }

    /**
     * Ordered verify same method once or twice then once but replay each expectation only once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_verifySameMethodOnceOrTwiceThenOnceButReplayEachExpectationOnlyOnce(
            @Mocked final Collaborator mock) {
        mock.simpleOperation(1, "", null);
        mock.simpleOperation(2, "", null);

        new VerificationsInOrder() {
            {
                mock.simpleOperation(1, "", null);
                maxTimes = 2;
                mock.simpleOperation(2, "", null);
            }
        };
    }

    /**
     * Ordered verify two or three times.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_verifyTwoOrThreeTimes(@Mocked final Collaborator mock) {
        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();

        new VerificationsInOrder() {
            {
                mock.provideSomeService();
                minTimes = 2;
                maxTimes = 3;
                mock.simpleOperation(1, "b", null);
            }
        };
    }

    /**
     * Ordered verify zero or more times and replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test
    @SuppressWarnings("UnusedDeclaration")
    public void ordered_verifyZeroOrMoreTimesAndReplayTwice(@Mocked Collaborator mock) {
        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();
        codeUnderTest.doSomething();

        final Collaborator collaborator = new Collaborator();

        new VerificationsInOrder() {
            {
                collaborator.simpleOperation(1, "b", null);
                collaborator.provideSomeService();
            }
        };
    }

    /**
     * Ordered verify zero or more times and replay none.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void ordered_verifyZeroOrMoreTimesAndReplayNone(@Mocked final Collaborator mock) {
        codeUnderTest.doSomethingElse();

        new VerificationsInOrder() {
            {
                mock.provideSomeService();
                minTimes = 0;
                maxTimes = -1;
                mock.simpleOperation(1, "b", null);
            }
        };
    }

    // Tests with unordered verifications //////////////////////////////////////////////////////////////////////////////

    /**
     * Unordered verify twice by using invocation count.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_verifyTwiceByUsingInvocationCount(@Mocked final Collaborator mock) {
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();

        new Verifications() {
            {
                mock.provideSomeService();
                times = 2;
                mock.simpleOperation(1, "b", null);
            }
        };
    }

    /**
     * Unordered verify twice by using invocation count but replay only once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_verifyTwiceByUsingInvocationCountButReplayOnlyOnce(@Mocked final Collaborator mock) {
        thrown.expect(MissingInvocation.class);

        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();

        new FullVerifications() {
            {
                mock.provideSomeService();
                times = 2;
                mock.simpleOperation(1, "b", null);
            }
        };
    }

    /**
     * Unordered verify at least once and replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_verifyAtLeastOnceAndReplayTwice(@Mocked final Collaborator mock) {
        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();
        codeUnderTest.doSomething();

        new Verifications() {
            {
                mock.provideSomeService();
                mock.simpleOperation(1, "b", null);
            }
        };
    }

    /**
     * Unordered min times and max times out of order.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_minTimesAndMaxTimesOutOfOrder(@Mocked final Collaborator mock) {
        thrown.expect(UnexpectedInvocation.class);

        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomething();

        new Verifications() {
            {
                mock.provideSomeService();
                maxTimes = 2;
                minTimes = 1;
            }
        };
    }

    /**
     * Unordered verify at most twice and replay once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_verifyAtMostTwiceAndReplayOnce(@Mocked final Collaborator mock) {
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();

        new FullVerifications() {
            {
                mock.simpleOperation(1, "b", null);
                mock.provideSomeService();
                maxTimes = 2;
            }
        };
    }

    /**
     * Unordered verify at most once but replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_verifyAtMostOnceButReplayTwice(@Mocked final Collaborator mock) {
        thrown.expect(UnexpectedInvocation.class);

        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();

        new Verifications() {
            {
                mock.provideSomeService();
                maxTimes = 1;
                mock.simpleOperation(1, "b", null);
            }
        };
    }

    /**
     * Unordered verify at most zero.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_verifyAtMostZero(@Mocked final Collaborator mock) {
        new Verifications() {
            {
                mock.provideSomeService();
                maxTimes = 0;
            }
        };
    }

    /**
     * Unordered verify at most zero but replay once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_verifyAtMostZeroButReplayOnce(@Mocked final Collaborator mock) {
        thrown.expect(UnexpectedInvocation.class);

        codeUnderTest.doSomething();

        new Verifications() {
            {
                mock.provideSomeService();
                maxTimes = 0;
            }
        };
    }

    /**
     * Unordered max times does not overwrite min times.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_maxTimesDoesNotOverwriteMinTimes(@Mocked final Collaborator mock) {
        thrown.expect(MissingInvocation.class);

        codeUnderTest.doSomething();

        new FullVerifications() {
            {
                mock.provideSomeService();
                minTimes = 2;
                maxTimes = 3;
            }
        };
    }

    /**
     * Unordered verify same method once or twice then once but replay each expectation only once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_verifySameMethodOnceOrTwiceThenOnceButReplayEachExpectationOnlyOnce(
            @Mocked final Collaborator mock) {
        mock.simpleOperation(2, "", null);
        mock.simpleOperation(1, "", null);

        new Verifications() {
            {
                mock.simpleOperation(1, "", null);
                maxTimes = 2;
                mock.simpleOperation(2, "", null);
            }
        };
    }

    /**
     * Unordered verify two or three times.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_verifyTwoOrThreeTimes(@Mocked final Collaborator mock) {
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();

        new FullVerifications() {
            {
                mock.simpleOperation(1, "b", null);
                mock.provideSomeService();
                minTimes = 2;
                maxTimes = 3;
            }
        };
    }

    /**
     * Unordered verify one or more times and replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void unordered_verifyOneOrMoreTimesAndReplayTwice(@Mocked Collaborator mock) {
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
        codeUnderTest.doSomething();

        final Collaborator collaborator = new Collaborator();

        new Verifications() {
            {
                collaborator.simpleOperation(1, "b", null);
                collaborator.provideSomeService();
            }
        };
    }
}
