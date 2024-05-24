/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import static mockit.internal.reflection.FieldReflection.getFieldValue;

import java.lang.instrument.ClassDefinition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.asm.classes.ClassReader;
import mockit.asm.types.JavaType;
import mockit.internal.BaseClassModifier;
import mockit.internal.capturing.CaptureOfImplementations;
import mockit.internal.startup.Startup;
import mockit.internal.state.MockFixture;
import mockit.internal.state.TestRun;
import mockit.internal.util.Utilities;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class CaptureOfNewInstances extends CaptureOfImplementations<MockedType> {
    protected static final class Capture {
        @NonNull
        final MockedType typeMetadata;
        @Nullable
        private Object originalMockInstance;
        @NonNull
        private final List<Object> instancesCaptured;

        private Capture(@NonNull MockedType typeMetadata, @Nullable Object originalMockInstance) {
            this.typeMetadata = typeMetadata;
            this.originalMockInstance = originalMockInstance;
            instancesCaptured = new ArrayList<>(4);
        }

        private boolean isInstanceAlreadyCaptured(@NonNull Object mock) {
            return Utilities.containsReference(instancesCaptured, mock);
        }

        private boolean captureInstance(@Nullable Object fieldOwner, @NonNull Object instance) {
            if (instancesCaptured.size() < typeMetadata.getMaxInstancesToCapture()) {
                if (fieldOwner != null && typeMetadata.field != null && originalMockInstance == null) {
                    originalMockInstance = getFieldValue(typeMetadata.field, fieldOwner);
                }

                instancesCaptured.add(instance);
                return true;
            }

            return false;
        }

        void reset() {
            originalMockInstance = null;
            instancesCaptured.clear();
        }
    }

    @NonNull
    private final Map<Class<?>, List<Capture>> baseTypeToCaptures;

    CaptureOfNewInstances() {
        baseTypeToCaptures = new HashMap<>();
    }

    @NonNull
    protected final Collection<List<Capture>> getCapturesForAllBaseTypes() {
        return baseTypeToCaptures.values();
    }

    @NonNull
    @Override
    protected BaseClassModifier createModifier(@Nullable ClassLoader cl, @NonNull ClassReader cr,
            @NonNull Class<?> baseType, @Nullable MockedType typeMetadata) {
        MockedClassModifier modifier = new MockedClassModifier(cl, cr, typeMetadata);
        String baseTypeDesc = JavaType.getInternalName(baseType);
        modifier.setClassNameForCapturedInstanceMethods(baseTypeDesc);
        return modifier;
    }

    @Override
    protected void redefineClass(@NonNull Class<?> realClass, @NonNull byte[] modifiedClass) {
        ClassDefinition newClassDefinition = new ClassDefinition(realClass, modifiedClass);
        Startup.redefineMethods(newClassDefinition);

        MockFixture mockFixture = TestRun.mockFixture();
        mockFixture.addRedefinedClass(newClassDefinition);
        mockFixture.registerMockedClass(realClass);
    }

    void registerCaptureOfNewInstances(@NonNull MockedType typeMetadata, @Nullable Object mockInstance) {
        Class<?> baseType = typeMetadata.getClassType();

        if (!typeMetadata.isFinalFieldOrParameter()) {
            makeSureAllSubtypesAreModified(typeMetadata);
        }

        List<Capture> captures = baseTypeToCaptures.get(baseType);

        if (captures == null) {
            captures = new ArrayList<>();
            baseTypeToCaptures.put(baseType, captures);
        }

        captures.add(new Capture(typeMetadata, mockInstance));
    }

    void makeSureAllSubtypesAreModified(@NonNull MockedType typeMetadata) {
        Class<?> baseType = typeMetadata.getClassType();
        makeSureAllSubtypesAreModified(baseType, typeMetadata.fieldFromTestClass, typeMetadata);
    }

    public boolean captureNewInstance(@Nullable Object fieldOwner, @NonNull Object mock) {
        Class<?> mockedClass = mock.getClass();
        List<Capture> captures = baseTypeToCaptures.get(mockedClass);
        boolean constructorModifiedForCaptureOnly = captures == null;

        if (constructorModifiedForCaptureOnly) {
            captures = findCaptures(mockedClass);

            if (captures == null) {
                return false;
            }
        }

        Capture captureFound = findCapture(fieldOwner, mock, captures);

        if (captureFound != null) {
            if (captureFound.typeMetadata.injectable) {
                TestRun.getExecutingTest().addCapturedInstanceForInjectableMock(captureFound.originalMockInstance,
                        mock);
                constructorModifiedForCaptureOnly = true;
            } else {
                TestRun.getExecutingTest().addCapturedInstance(captureFound.originalMockInstance, mock);
            }
        }

        return constructorModifiedForCaptureOnly;
    }

    @Nullable
    private List<Capture> findCaptures(@NonNull Class<?> mockedClass) {
        Class<?>[] interfaces = mockedClass.getInterfaces();

        for (Class<?> anInterface : interfaces) {
            List<Capture> found = baseTypeToCaptures.get(anInterface);

            if (found != null) {
                return found;
            }
        }

        Class<?> superclass = mockedClass.getSuperclass();

        if (superclass == Object.class) {
            return null;
        }

        List<Capture> found = baseTypeToCaptures.get(superclass);

        return found != null ? found : findCaptures(superclass);
    }

    @Nullable
    private static Capture findCapture(@Nullable Object fieldOwner, @NonNull Object mock,
            @NonNull List<Capture> captures) {
        for (Capture capture : captures) {
            if (capture.isInstanceAlreadyCaptured(mock)) {
                break;
            } else if (capture.captureInstance(fieldOwner, mock)) {
                return capture;
            }
        }

        return null;
    }

    public void cleanUp() {
        baseTypeToCaptures.clear();
    }
}
