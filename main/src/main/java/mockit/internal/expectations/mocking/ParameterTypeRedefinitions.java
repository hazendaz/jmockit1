/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.mocking;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import mockit.internal.util.TestMethod;

import org.checkerframework.checker.index.qual.NonNegative;

public final class ParameterTypeRedefinitions extends TypeRedefinitions {
    @NonNull
    private final TestMethod testMethod;
    @NonNull
    private final MockedType[] mockParameters;
    @NonNull
    private final List<MockedType> injectableParameters;

    public ParameterTypeRedefinitions(@NonNull TestMethod testMethod, @NonNull Object[] parameterValues) {
        this.testMethod = testMethod;
        int n = testMethod.getParameterCount();
        mockParameters = new MockedType[n];
        injectableParameters = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            Object mock = parameterValues[i];
            createMockedTypeFromMockParameterDeclaration(i, mock);
        }

        InstanceFactory[] instanceFactories = redefineMockedTypes();
        instantiateMockedTypes(instanceFactories);
    }

    private void createMockedTypeFromMockParameterDeclaration(@NonNegative int parameterIndex, @Nullable Object mock) {
        Type parameterType = testMethod.getParameterType(parameterIndex);
        Annotation[] annotationsOnParameter = testMethod.getParameterAnnotations(parameterIndex);
        Class<?> parameterImplementationClass = mock == null ? null : mock.getClass();
        MockedType mockedType = new MockedType(testMethod, parameterIndex, parameterType, annotationsOnParameter,
                parameterImplementationClass);

        if (mockedType.isMockableType()) {
            mockParameters[parameterIndex] = mockedType;
        }

        if (mockedType.injectable) {
            injectableParameters.add(mockedType);
            testMethod.setParameterValue(parameterIndex, mockedType.providedValue);
        }
    }

    @NonNull
    private InstanceFactory[] redefineMockedTypes() {
        int n = mockParameters.length;
        InstanceFactory[] instanceFactories = new InstanceFactory[n];

        for (int i = 0; i < n; i++) {
            MockedType mockedType = mockParameters[i];

            if (mockedType != null) {
                instanceFactories[i] = redefineMockedType(mockedType);
            }
        }

        return instanceFactories;
    }

    @Nullable
    private InstanceFactory redefineMockedType(@NonNull MockedType mockedType) {
        TypeRedefinition typeRedefinition = new TypeRedefinition(mockedType);
        InstanceFactory instanceFactory = typeRedefinition.redefineType();

        if (instanceFactory != null) {
            addTargetClass(mockedType);
        }

        return instanceFactory;
    }

    private void registerCaptureOfNewInstances(@NonNull MockedType mockedType, @NonNull Object originalInstance) {
        if (captureOfNewInstances == null) {
            captureOfNewInstances = new CaptureOfNewInstances();
        }

        captureOfNewInstances.registerCaptureOfNewInstances(mockedType, originalInstance);
        captureOfNewInstances.makeSureAllSubtypesAreModified(mockedType);
    }

    private void instantiateMockedTypes(@NonNull InstanceFactory[] instanceFactories) {
        for (int paramIndex = 0; paramIndex < instanceFactories.length; paramIndex++) {
            InstanceFactory instanceFactory = instanceFactories[paramIndex];

            if (instanceFactory != null) {
                MockedType mockedType = mockParameters[paramIndex];
                @NonNull
                Object mockedInstance = instantiateMockedType(mockedType, instanceFactory, paramIndex);
                testMethod.setParameterValue(paramIndex, mockedInstance);
                mockedType.providedValue = mockedInstance;
            }
        }
    }

    @NonNull
    private Object instantiateMockedType(@NonNull MockedType mockedType, @NonNull InstanceFactory instanceFactory,
            @NonNegative int paramIndex) {
        Object mock = testMethod.getParameterValue(paramIndex);

        if (mock == null) {
            mock = instanceFactory.create();
        }

        registerMock(mockedType, mock);

        if (mockedType.withInstancesToCapture()) {
            registerCaptureOfNewInstances(mockedType, mock);
        }

        return mock;
    }

    @NonNull
    public List<MockedType> getInjectableParameters() {
        return injectableParameters;
    }
}
