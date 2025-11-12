/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.Test;

/**
 * The Class ExpectationsWithInvocationCountsTest.
 */
public final class ExpectationsWithInvocationCountsTest {

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

    /**
     * Expect once.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectOnce(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
            }
        };

        codeUnderTest.doSomething();
    }

    /**
     * Expect once but replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = UnexpectedInvocation.class)
    public void expectOnceButReplayTwice(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                times = 1;
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomething();

        fail("Should not get here");
    }

    /**
     * Expect once but replay more times.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = UnexpectedInvocation.class)
    public void expectOnceButReplayMoreTimes(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                times = 1;
            }
        };

        codeUnderTest.doSomething();

        try {
            codeUnderTest.doSomething();
        } finally {
            codeUnderTest.doSomethingElse();
        }

        fail("Should not get here");
    }

    /**
     * Catch unexpected invocation and continue.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void catchUnexpectedInvocationAndContinue(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                maxTimes = 0;
            }
        };

        try {
            mock.provideSomeService();
        } catch (UnexpectedInvocation ignore) {
        }

        mock.simpleOperation(1, "", null);
    }

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
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
    }

    /**
     * Expect twice by using invocation count but replay only once.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = MissingInvocation.class)
    public void expectTwiceByUsingInvocationCountButReplayOnlyOnce(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                times = 2;
                mock.simpleOperation(1, "b", null);
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
    }

    /**
     * Expect exactly twice but replay more times.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = UnexpectedInvocation.class)
    public void expectExactlyTwiceButReplayMoreTimes(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                times = 2;
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
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
                minTimes = 1;
                mock.simpleOperation(1, "b", null);
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
    }

    /**
     * Expect at least twice but replay once with single expectation.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = MissingInvocation.class)
    public void expectAtLeastTwiceButReplayOnceWithSingleExpectation(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                minTimes = 2;
            }
        };

        codeUnderTest.doSomething();
    }

    /**
     * Expect at least twice but replay once with two consecutive expectations.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = MissingInvocation.class)
    public void expectAtLeastTwiceButReplayOnceWithTwoConsecutiveExpectations(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                minTimes = 2;
                mock.simpleOperation(1, "b", null);
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
    }

    /**
     * Repeats at least overwriting upper limit.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void repeatsAtLeastOverwritingUpperLimit(@Mocked final Collaborator mock) {
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

        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
    }

    /**
     * Expect at most once but replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = UnexpectedInvocation.class)
    public void expectAtMostOnceButReplayTwice(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                maxTimes = 1;
                mock.simpleOperation(1, "b", null);
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
    }

    /**
     * Repeats at most does not overwrite lower limit.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = MissingInvocation.class)
    public void repeatsAtMostDoesNotOverwriteLowerLimit(@Mocked final Collaborator mock) {
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
                minTimes = 1;
                maxTimes = 2;
                mock.simpleOperation(2, "", null);
            }
        };

        mock.simpleOperation(1, "", null);
        mock.simpleOperation(2, "", null);
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
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
    }

    /**
     * Expect zero or more times and replay twice.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void expectZeroOrMoreTimesAndReplayTwice(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                minTimes = 0;
                maxTimes = -1;
                mock.simpleOperation(1, "b", null);
            }
        };

        codeUnderTest.doSomething();
        codeUnderTest.doSomething();
        codeUnderTest.doSomethingElse();
    }

    /**
     * Expect at least one invocation matching strict expectation but invoke none.
     *
     * @param a
     *            the a
     */
    @Test(expected = MissingInvocation.class)
    public void expectAtLeastOneInvocationMatchingStrictExpectationButInvokeNone(@Mocked final Collaborator a) {
        new Expectations() {
            {
                a.provideSomeService();
                maxTimes = -1;
            }
        };

        // Do nothing at replay time.
    }

    /**
     * Expect one or more invocations followed by another which wont occur max times.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = MissingInvocation.class)
    public void expectOneOrMoreInvocationsFollowedByAnotherWhichWontOccur_maxTimes(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                maxTimes = -1;
                mock.simpleOperation(1, null, null);
            }
        };

        codeUnderTest.doSomething();
    }

    /**
     * Expect one or more invocations followed by another which wont occur min times.
     *
     * @param mock
     *            the mock
     */
    @Test(expected = MissingInvocation.class)
    public void expectOneOrMoreInvocationsFollowedByAnotherWhichWontOccur_minTimes(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.simpleOperation(1, anyString, null);
                minTimes = 1;
                mock.provideSomeService();
            }
        };

        codeUnderTest.doSomethingElse();
    }
}
