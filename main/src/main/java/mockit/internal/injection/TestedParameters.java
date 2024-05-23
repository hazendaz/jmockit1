/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import java.lang.annotation.Annotation;
import java.util.List;

import mockit.Tested;
import mockit.internal.state.TestRun;
import mockit.internal.util.TestMethod;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class TestedParameters {
    @NonNull
    private final TestMethod testMethod;
    @NonNull
    private final InjectionState injectionState;

    public TestedParameters(@NonNull TestMethod testMethod) {
        this.testMethod = testMethod;

        TestedClassInstantiations testedClasses = TestRun.getTestedClassInstantiations();
        injectionState = testedClasses == null ? new InjectionState() : testedClasses.injectionState;
    }

    public void createTestedParameters(@NonNull Object testClassInstance,
            @NonNull List<? extends InjectionProvider> injectables) {
        injectionState.addInjectables(testClassInstance, injectables);

        for (int n = testMethod.getParameterCount(), i = 0; i < n; i++) {
            TestedParameter testedParameter = createTestedParameterIfApplicable(i);

            if (testedParameter != null) {
                instantiateTestedObject(testClassInstance, testedParameter);
            }
        }
    }

    @Nullable
    private TestedParameter createTestedParameterIfApplicable(@NonNegative int parameterIndex) {
        Annotation[] parameterAnnotations = testMethod.getParameterAnnotations(parameterIndex);

        for (Annotation parameterAnnotation : parameterAnnotations) {
            Tested testedMetadata = TestedObject.getTestedAnnotationIfPresent(parameterAnnotation);

            if (testedMetadata != null) {
                return new TestedParameter(injectionState, testMethod, parameterIndex, testedMetadata);
            }
        }

        return null;
    }

    private void instantiateTestedObject(@NonNull Object testClassInstance, @NonNull TestedParameter testedObject) {
        try {
            testedObject.instantiateWithInjectableValues(testClassInstance);
        } finally {
            injectionState.injectionProviders.resetConsumedInjectionProviders();
        }
    }
}
