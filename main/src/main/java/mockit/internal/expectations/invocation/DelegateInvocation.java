/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;

import mockit.internal.BaseInvocation;
import mockit.internal.expectations.state.ExecutingTest;
import mockit.internal.state.TestRun;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

final class DelegateInvocation extends BaseInvocation {
    @NonNull
    private final InvocationArguments invocationArguments;

    DelegateInvocation(@Nullable Object invokedInstance, @NonNull Object[] invokedArguments,
            @NonNull ExpectedInvocation expectedInvocation, @NonNull InvocationConstraints constraints) {
        super(invokedInstance, invokedArguments, constraints.invocationCount);
        invocationArguments = expectedInvocation.arguments;
    }

    @NonNull
    @Override
    protected Member findRealMember() {
        return invocationArguments.getRealMethodOrConstructor();
    }

    @Override
    public void prepareToProceed() {
        ExecutingTest executingTest = TestRun.getExecutingTest();

        if (getInvokedMember() instanceof Constructor) {
            executingTest.markAsProceedingIntoRealImplementation();
        } else {
            executingTest.markAsProceedingIntoRealImplementation(this);
        }
    }

    @Override
    public void cleanUpAfterProceed() {
        TestRun.getExecutingTest().clearProceedingState();
    }
}
