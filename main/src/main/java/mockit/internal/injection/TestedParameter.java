/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mockit.Tested;
import mockit.internal.state.ParameterNames;
import mockit.internal.util.TestMethod;
import mockit.internal.util.TypeConversion;

import org.checkerframework.checker.index.qual.NonNegative;

final class TestedParameter extends TestedObject {
    @Nonnull
    private final TestMethod testMethod;
    @NonNegative
    private final int parameterIndex;

    TestedParameter(@Nonnull InjectionState injectionState, @Nonnull TestMethod testMethod,
            @NonNegative int parameterIndex, @Nonnull Tested metadata) {
        super(injectionState, metadata, testMethod.testClass, ParameterNames.getName(testMethod, parameterIndex),
                testMethod.getParameterType(parameterIndex), testMethod.getParameterClass(parameterIndex));
        this.testMethod = testMethod;
        this.parameterIndex = parameterIndex;
    }

    @Nullable
    @Override
    Object getExistingTestedInstanceIfApplicable(@Nonnull Object testClassInstance) {
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
    void setInstance(@Nonnull Object testClassInstance, @Nullable Object testedInstance) {
        testMethod.setParameterValue(parameterIndex, testedInstance);
    }
}
