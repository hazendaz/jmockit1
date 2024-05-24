/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4;

import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.coverage.testRedundancy.JUnitListener;
import mockit.coverage.testRedundancy.TestCoverage;
import mockit.integration.TestRunnerDecorator;
import mockit.internal.faking.FakeInvocation;
import mockit.internal.state.TestRun;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Startup fake which works in conjunction with {@link JUnit4TestRunnerDecorator} to provide JUnit 4.5+ integration.
 * <p>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at startup.
 */
public final class FakeRunNotifier extends MockUp<RunNotifier> {
    @Mock
    public static void fireTestRunStarted(Invocation invocation, Description description) {
        RunNotifier it = invocation.getInvokedInstance();
        assert it != null;

        TestCoverage testCoverage = TestCoverage.INSTANCE;

        if (testCoverage != null) {
            it.addListener(new JUnitListener(testCoverage));
        }

        prepareToProceed(invocation);
        it.fireTestRunStarted(description);
    }

    private static void prepareToProceed(@NonNull Invocation invocation) {
        ((FakeInvocation) invocation).prepareToProceedFromNonRecursiveMock();
    }

    @Mock
    public static void fireTestStarted(Invocation invocation, Description description) {
        Class<?> currentTestClass = TestRun.getCurrentTestClass();

        if (currentTestClass != null) {
            Class<?> newTestClass = description.getTestClass();

            if (newTestClass == null || !currentTestClass.isAssignableFrom(newTestClass)) {
                TestRunnerDecorator.cleanUpMocksFromPreviousTestClass();
            }
        }

        prepareToProceed(invocation);

        RunNotifier it = invocation.getInvokedInstance();
        assert it != null;
        it.fireTestStarted(description);
    }

    @Mock
    public static void fireTestRunFinished(Invocation invocation, Result result) {
        TestRun.enterNoMockingZone();

        try {
            TestRunnerDecorator.cleanUpAllMocks();

            prepareToProceed(invocation);

            RunNotifier it = invocation.getInvokedInstance();
            assert it != null;
            it.fireTestRunFinished(result);
        } finally {
            TestRun.exitNoMockingZone();
        }
    }
}
