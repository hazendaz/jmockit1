/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import static mockit.internal.reflection.MethodReflection.JAVA_LANG;
import static mockit.internal.reflection.MethodReflection.findNonPrivateHandlerMethod;
import static mockit.internal.reflection.MethodReflection.invoke;

import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

import mockit.Delegate;
import mockit.Invocation;
import mockit.asm.types.JavaType;
import mockit.internal.expectations.RecordAndReplayExecution;
import mockit.internal.reflection.ParameterReflection;
import mockit.internal.state.TestRun;
import mockit.internal.util.MethodFormatter;
import mockit.internal.util.TypeDescriptor;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

final class DelegatedResult extends InvocationResult {
    private static final Object[] NO_ARGS = {};

    @NonNull
    private final ExpectedInvocation recordedInvocation;
    @NonNull
    private final Object targetObject;
    @NonNull
    private final Method methodToInvoke;
    @NonNull
    private final Class<?> targetReturnType;
    private final boolean hasInvocationParameter;
    private final int numberOfRegularParameters;

    DelegatedResult(@NonNull ExpectedInvocation recordedInvocation, @NonNull Delegate<?> delegate) {
        this.recordedInvocation = recordedInvocation;
        targetObject = delegate;
        methodToInvoke = findNonPrivateHandlerMethod(delegate);

        JavaType returnType = JavaType.getReturnType(recordedInvocation.getMethodNameAndDescription());
        targetReturnType = TypeDescriptor.getClassForType(returnType);

        Class<?>[] parameters = methodToInvoke.getParameterTypes();
        int n = parameters.length;

        hasInvocationParameter = n > 0 && parameters[0] == Invocation.class;
        numberOfRegularParameters = hasInvocationParameter ? n - 1 : n;
    }

    @Nullable
    @Override
    Object produceResult(@Nullable Object invokedObject, @NonNull ExpectedInvocation invocation,
            @NonNull InvocationConstraints constraints, @NonNull Object[] args) {
        Object[] delegateArgs = numberOfRegularParameters == 0 ? NO_ARGS : args;
        return hasInvocationParameter
                ? invokeMethodWithContext(invokedObject, invocation, constraints, args, delegateArgs)
                : executeMethodToInvoke(delegateArgs);
    }

    @Nullable
    private Object invokeMethodWithContext(@Nullable Object mockOrRealObject,
            @NonNull ExpectedInvocation expectedInvocation, @NonNull InvocationConstraints constraints,
            @NonNull Object[] invokedArgs, @NonNull Object[] delegateArgs) {
        Invocation delegateInvocation = new DelegateInvocation(mockOrRealObject, invokedArgs, expectedInvocation,
                constraints);
        Object[] delegateArgsWithInvocation = ParameterReflection.argumentsWithExtraFirstValue(delegateArgs,
                delegateInvocation);
        Object result = executeMethodToInvoke(delegateArgsWithInvocation);

        return expectedInvocation.isConstructor() && TestRun.getExecutingTest().isProceedingIntoRealImplementation()
                ? Void.class : result;
    }

    @Nullable
    private Object executeMethodToInvoke(@NonNull Object[] args) {
        ReentrantLock reentrantLock = RecordAndReplayExecution.RECORD_OR_REPLAY_LOCK;

        if (!reentrantLock.isHeldByCurrentThread()) {
            return executeTargetMethod(args);
        }

        reentrantLock.unlock();

        try {
            return executeTargetMethod(args);
        } finally {
            // noinspection LockAcquiredButNotSafelyReleased
            reentrantLock.lock();
        }
    }

    @Nullable
    private Object executeTargetMethod(@NonNull Object[] args) {
        Object returnValue = invoke(targetObject, methodToInvoke, args);
        Class<?> fromReturnType = methodToInvoke.getReturnType();

        if (returnValue == null || targetReturnType.isInstance(returnValue)) {
            if (fromReturnType == void.class && fromReturnType != targetReturnType && targetReturnType.isPrimitive()) {
                String returnTypeName = JAVA_LANG.matcher(targetReturnType.getName()).replaceAll("");
                MethodFormatter methodDesc = new MethodFormatter(recordedInvocation.getClassDesc(),
                        recordedInvocation.getMethodNameAndDescription());
                String msg = "void return type incompatible with return type " + returnTypeName + " of " + methodDesc;
                throw new IllegalArgumentException(msg);
            }

            return returnValue;
        }

        ReturnTypeConversion typeConversion = new ReturnTypeConversion(recordedInvocation, targetReturnType,
                returnValue);
        return typeConversion.getConvertedValue();
    }
}
