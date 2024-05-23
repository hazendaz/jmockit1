/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.lang.reflect.Array;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mockit.Delegate;
import mockit.internal.expectations.invocation.InvocationResult.DeferredResults;
import mockit.internal.expectations.invocation.InvocationResult.ReturnValueResult;
import mockit.internal.expectations.invocation.InvocationResult.ThrowableResult;

import org.checkerframework.checker.index.qual.NonNegative;

public final class InvocationResults {
    @Nonnull
    private final ExpectedInvocation invocation;
    @Nonnull
    private final InvocationConstraints constraints;
    @Nullable
    private InvocationResult currentResult;
    @Nullable
    private InvocationResult lastResult;
    @NonNegative
    private int resultCount;

    public InvocationResults(@Nonnull ExpectedInvocation invocation, @Nonnull InvocationConstraints constraints) {
        this.invocation = invocation;
        this.constraints = constraints;
    }

    public void addReturnValue(@Nullable Object value) {
        if (value instanceof Delegate) {
            addDelegatedResult((Delegate<?>) value);
        } else {
            addNewReturnValueResult(value);
        }
    }

    public void addDelegatedResult(@Nonnull Delegate<?> delegate) {
        InvocationResult result = new DelegatedResult(invocation, delegate);
        addResult(result);
    }

    private void addNewReturnValueResult(@Nullable Object value) {
        InvocationResult result = new ReturnValueResult(value);
        addResult(result);
    }

    public void addReturnValueResult(@Nullable Object value) {
        addNewReturnValueResult(value);
    }

    public void addReturnValues(@Nonnull Object... values) {
        for (Object value : values) {
            addReturnValue(value);
        }
    }

    void addResults(@Nonnull Object array) {
        int n = Array.getLength(array);

        for (int i = 0; i < n; i++) {
            Object value = Array.get(array, i);
            addConsecutiveResult(value);
        }
    }

    private void addConsecutiveResult(@Nullable Object result) {
        if (result instanceof Throwable) {
            addThrowable((Throwable) result);
        } else {
            addReturnValue(result);
        }
    }

    void addResults(@Nonnull Iterable<?> values) {
        for (Object value : values) {
            addConsecutiveResult(value);
        }
    }

    void addDeferredResults(@Nonnull Iterator<?> values) {
        InvocationResult result = new DeferredResults(values);
        addResult(result);
        constraints.setUnlimitedMaxInvocations();
    }

    public void addThrowable(@Nonnull Throwable t) {
        addResult(new ThrowableResult(t));
    }

    private void addResult(@Nonnull InvocationResult result) {
        resultCount++;
        constraints.adjustMaxInvocations(resultCount);

        if (currentResult == null) {
            currentResult = result;
        } else {
            assert lastResult != null;
            lastResult.next = result;
        }

        lastResult = result;
    }

    @Nullable
    public Object produceResult(@Nullable Object invokedObject, @Nonnull Object[] invocationArgs) throws Throwable {
        InvocationResult resultToBeProduced = currentResult;

        if (resultToBeProduced == null) {
            return null;
        }

        InvocationResult nextResult = resultToBeProduced.next;

        if (nextResult != null) {
            currentResult = nextResult;
        }

        return resultToBeProduced.produceResult(invokedObject, invocation, constraints, invocationArgs);
    }
}
