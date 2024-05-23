/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4;

import static mockit.internal.util.StackTrace.filterStackTrace;

import java.lang.reflect.Method;

import mockit.integration.TestRunnerDecorator;
import mockit.internal.expectations.RecordAndReplayExecution;
import mockit.internal.faking.FakeInvocation;
import mockit.internal.state.SavePoint;
import mockit.internal.state.TestRun;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;

final class JUnit4TestRunnerDecorator extends TestRunnerDecorator {
    @Nullable
    Object invokeExplosively(@NonNull FakeInvocation invocation, @Nullable Object target, Object... params)
            throws Throwable {
        FrameworkMethod it = invocation.getInvokedInstance();
        assert it != null;

        // A @BeforeClass/@AfterClass method:
        if (target == null) {
            try {
                return executeClassMethod(invocation, params);
            } catch (Throwable t) {
                filterStackTrace(t);
                throw t;
            }
        }

        handleMockingOutsideTestMethods(target);

        // A @Before/@After method:
        if (it.getAnnotation(Test.class) == null) {
            if (shouldPrepareForNextTest && it.getAnnotation(Before.class) != null) {
                prepareToExecuteSetupMethod(target);
            }

            TestRun.setRunningIndividualTest(target);

            try {
                invocation.prepareToProceedFromNonRecursiveMock();
                return it.invokeExplosively(target, params);
            } catch (Throwable t) {
                // noinspection ThrowableNotThrown
                RecordAndReplayExecution.endCurrentReplayIfAny();
                filterStackTrace(t);
                throw t;
            } finally {
                if (it.getAnnotation(After.class) != null) {
                    shouldPrepareForNextTest = true;
                }
            }
        }

        if (shouldPrepareForNextTest) {
            prepareForNextTest();
        }

        shouldPrepareForNextTest = true;

        try {
            executeTestMethod(invocation, target, params);
            return null; // it's a test method, therefore has void return type
        } catch (Throwable t) {
            filterStackTrace(t);
            throw t;
        } finally {
            TestRun.finishCurrentTestExecution();
        }
    }

    @Nullable
    private static Object executeClassMethod(@NonNull FakeInvocation inv, @NonNull Object[] params) throws Throwable {
        FrameworkMethod method = inv.getInvokedInstance();
        assert method != null;
        handleMockingOutsideTests(method);

        TestRun.clearCurrentTestInstance();
        inv.prepareToProceedFromNonRecursiveMock();

        return method.invokeExplosively(null, params);
    }

    private void prepareToExecuteSetupMethod(@NonNull Object target) {
        discardTestLevelMockedTypes();
        prepareForNextTest();
        shouldPrepareForNextTest = false;
        createInstancesForTestedFieldsBeforeSetup(target);
    }

    private static void handleMockingOutsideTests(@NonNull FrameworkMethod it) {
        Class<?> testClass = it.getMethod().getDeclaringClass();

        TestRun.enterNoMockingZone();

        try {
            Class<?> currentTestClass = TestRun.getCurrentTestClass();

            if (currentTestClass != null && testClass.isAssignableFrom(currentTestClass)
                    && it.getAnnotation(AfterClass.class) != null) {
                cleanUpMocksFromPreviousTest();
            }

            if (it.getAnnotation(BeforeClass.class) != null) {
                updateTestClassState(null, testClass);
            }
        } finally {
            TestRun.exitNoMockingZone();
        }
    }

    private static void handleMockingOutsideTestMethods(@NonNull Object target) {
        Class<?> testClass = target.getClass();

        TestRun.enterNoMockingZone();

        try {
            updateTestClassState(target, testClass);
        } finally {
            TestRun.exitNoMockingZone();
        }
    }

    private static void executeTestMethod(@NonNull FakeInvocation invocation, @NonNull Object testInstance,
            @Nullable Object... parameters) throws Throwable {
        SavePoint savePoint = new SavePoint();

        TestRun.setRunningIndividualTest(testInstance);

        FrameworkMethod it = invocation.getInvokedInstance();
        assert it != null;
        Method testMethod = it.getMethod();
        Throwable testFailure = null;
        boolean testFailureExpected = false;

        try {
            createInstancesForTestedFieldsFromBaseClasses(testInstance);
            Object[] annotatedParameters = createInstancesForAnnotatedParameters(testInstance, testMethod, parameters);
            createInstancesForTestedFields(testInstance);

            invocation.prepareToProceedFromNonRecursiveMock();

            Object[] params = annotatedParameters == null ? parameters : annotatedParameters;
            it.invokeExplosively(testInstance, params);
        } catch (Throwable thrownByTest) {
            testFailure = thrownByTest;
            Class<?> expectedType = testMethod.getAnnotation(Test.class).expected();
            testFailureExpected = expectedType.isAssignableFrom(thrownByTest.getClass());
        } finally {
            concludeTestMethodExecution(savePoint, testFailure, testFailureExpected);
        }
    }
}
