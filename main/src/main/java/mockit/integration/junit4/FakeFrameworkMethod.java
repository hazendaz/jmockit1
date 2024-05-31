/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

import mockit.Expectations;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import mockit.internal.faking.FakeInvocation;
import mockit.internal.util.StackTrace;

import org.junit.runners.model.FrameworkMethod;

/**
 * Startup fake that modifies the JUnit 4.5+ test runner so that it calls back to JMockit immediately after every test
 * executes. When that happens, JMockit will assert any expectations recorded during the test in {@link Expectations}
 * subclasses.
 * <p>
 * This class is not supposed to be accessed from user code. JMockit will automatically load it at startup.
 */
public final class FakeFrameworkMethod extends MockUp<FrameworkMethod> {
    @NonNull
    private final JUnit4TestRunnerDecorator decorator = new JUnit4TestRunnerDecorator();

    @Nullable
    @Mock
    public Object invokeExplosively(@NonNull Invocation invocation, Object target, Object... params) throws Throwable {
        return decorator.invokeExplosively((FakeInvocation) invocation, target, params);
    }

    @Mock
    public static void validatePublicVoidNoArg(@NonNull Invocation invocation, boolean isStatic,
            List<Throwable> errors) {
        FrameworkMethod it = invocation.getInvokedInstance();
        assert it != null;

        int previousErrorCount = errors.size();

        if (!isStatic && eachParameterContainsAKnownAnnotation(it.getMethod().getParameterAnnotations())) {
            it.validatePublicVoid(false, errors);
        } else {
            ((FakeInvocation) invocation).prepareToProceedFromNonRecursiveMock();
            it.validatePublicVoidNoArg(isStatic, errors);
        }

        int errorCount = errors.size();

        for (int i = previousErrorCount; i < errorCount; i++) {
            Throwable errorAdded = errors.get(i);
            StackTrace.filterStackTrace(errorAdded);
        }
    }

    private static boolean eachParameterContainsAKnownAnnotation(
            @NonNull Annotation[][] parametersAndTheirAnnotations) {
        if (parametersAndTheirAnnotations.length == 0) {
            return false;
        }

        for (Annotation[] parameterAnnotations : parametersAndTheirAnnotations) {
            if (!containsAKnownAnnotation(parameterAnnotations)) {
                return false;
            }
        }

        return true;
    }

    private static boolean containsAKnownAnnotation(@NonNull Annotation[] parameterAnnotations) {
        if (parameterAnnotations.length == 0) {
            return false;
        }

        for (Annotation parameterAnnotation : parameterAnnotations) {
            Class<? extends Annotation> annotationType = parameterAnnotation.annotationType();
            String annotationTypeName = annotationType.getName();

            if ("mockit.Tested mockit.Mocked mockit.Injectable mockit.Capturing".contains(annotationTypeName)
                    || annotationType.isAnnotationPresent(Tested.class)) {
                return true;
            }
        }

        return false;
    }
}
