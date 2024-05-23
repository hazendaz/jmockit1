/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import mockit.Tested;
import mockit.internal.state.ParameterNames;
import mockit.internal.util.TestMethod;
import mockit.internal.util.TypeConversion;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class TestedParameter extends TestedObject {
    @NonNull
    private final TestMethod testMethod;
    @NonNegative
    private final int parameterIndex;

    TestedParameter(@NonNull InjectionState injectionState, @NonNull TestMethod testMethod,
            @NonNegative int parameterIndex, @NonNull Tested metadata) {
        super(injectionState, metadata, testMethod.testClass, ParameterNames.getName(testMethod, parameterIndex),
                testMethod.getParameterType(parameterIndex), testMethod.getParameterClass(parameterIndex));
        this.testMethod = testMethod;
        this.parameterIndex = parameterIndex;
    }

    @Nullable
    @Override
    Object getExistingTestedInstanceIfApplicable(@NonNull Object testClassInstance) {
        Object testedObject = null;

        if (!createAutomatically) {
            String providedValue = metadata.value();

            if (!providedValue.isEmpty()) {
                Class<?> parameterClass = testMethod.getParameterClass(parameterIndex);
                testedObject = TypeConversion.convertFromString(parameterClass, providedValue);

                if (testedObject != null) {
                    testMethod.setParameterValue(parameterIndex, testedObject);
                }
            }

            createAutomatically = testedObject == null;
        }

        return testedObject;
    }

    @Override
    void setInstance(@NonNull Object testClassInstance, @Nullable Object testedInstance) {
        testMethod.setParameterValue(parameterIndex, testedInstance);
    }
}
