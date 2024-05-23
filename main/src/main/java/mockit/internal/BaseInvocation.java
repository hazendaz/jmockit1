/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import mockit.Invocation;
import mockit.internal.reflection.MethodReflection;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base class for encapsulating state and logic needed by both the Mocking and Faking APIs, but which should not go into
 * {@link Invocation} in order to keep the published API clean.
 */
public abstract class BaseInvocation extends Invocation {
    @Nullable
    private Member realMember;
    @Nullable
    private BaseInvocation previousInvocation;

    protected BaseInvocation(@Nullable Object invokedInstance, @NonNull Object[] invokedArguments,
            @NonNegative int invocationCount) {
        super(invokedInstance, invokedArguments, invocationCount);
    }

    @NonNull
    public final Member getRealMember() {
        if (realMember == null) {
            realMember = findRealMember();
        }

        return realMember;
    }

    @NonNull
    protected abstract Member findRealMember();

    @Nullable
    public final <T> T doProceed(@Nullable Object[] replacementArguments) {
        Member memberToInvoke = getRealMember();

        if (memberToInvoke instanceof Constructor) {
            prepareToProceed();
            return null;
        }

        prepareToProceed();

        Method realMethod = (Method) memberToInvoke;
        Object[] actualArgs = getInvokedArguments();

        if (replacementArguments != null && replacementArguments.length > 0) {
            actualArgs = replacementArguments;
        }

        try {
            return MethodReflection.invoke(getInvokedInstance(), realMethod, actualArgs);
        } finally {
            cleanUpAfterProceed();
        }
    }

    public abstract void prepareToProceed();

    protected abstract void cleanUpAfterProceed();

    @Nullable
    public final BaseInvocation getPrevious() {
        return previousInvocation;
    }

    public final void setPrevious(@NonNull BaseInvocation previous) {
        previousInvocation = previous;
    }

    public final boolean isMethodInSuperclass(@Nullable Object mock, @NonNull String classDesc) {
        if (mock != null && mock == getInvokedInstance() && getInvokedMember() instanceof Method) {
            Method methodToInvoke = getInvokedMember();
            String invokedClassDesc = methodToInvoke.getDeclaringClass().getName().replace('.', '/');
            return !invokedClassDesc.equals(classDesc);
        }

        return previousInvocation != null && previousInvocation.isMethodInSuperclass(mock, classDesc);
    }
}
