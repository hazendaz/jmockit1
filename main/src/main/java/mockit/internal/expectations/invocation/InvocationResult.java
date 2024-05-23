/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.util.Iterator;

import javax.annotation.Nullable;

import org.checkerframework.checker.nullness.qual.NonNull;

class InvocationResult {
    InvocationResult next;

    @Nullable
    Object produceResult(@NonNull Object[] args) throws Throwable {
        return null;
    }

    @Nullable
    Object produceResult(@Nullable Object invokedObject, @NonNull ExpectedInvocation invocation,
            @NonNull InvocationConstraints constraints, @NonNull Object[] args) throws Throwable {
        return produceResult(args);
    }

    static final class ReturnValueResult extends InvocationResult {
        @Nullable
        private final Object returnValue;

        ReturnValueResult(@Nullable Object returnValue) {
            this.returnValue = returnValue;
        }

        @Nullable
        @Override
        Object produceResult(@NonNull Object[] args) {
            return returnValue;
        }
    }

    static final class ThrowableResult extends InvocationResult {
        @NonNull
        private final Throwable throwable;

        ThrowableResult(@NonNull Throwable throwable) {
            this.throwable = throwable;
        }

        @NonNull
        @Override
        Object produceResult(@NonNull Object[] args) throws Throwable {
            throwable.fillInStackTrace();
            throw throwable;
        }
    }

    static final class DeferredResults extends InvocationResult {
        @NonNull
        private final Iterator<?> values;

        DeferredResults(@NonNull Iterator<?> values) {
            this.values = values;
        }

        @Nullable
        @Override
        Object produceResult(@NonNull Object[] args) throws Throwable {
            Object nextValue = values.hasNext() ? values.next() : null;

            if (nextValue instanceof Throwable) {
                Throwable t = (Throwable) nextValue;
                t.fillInStackTrace();
                throw t;
            }

            return nextValue;
        }
    }
}
