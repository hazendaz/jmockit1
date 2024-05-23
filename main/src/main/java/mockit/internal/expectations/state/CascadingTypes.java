/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.state;

import static mockit.internal.util.Utilities.getClassType;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import mockit.asm.types.JavaType;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class CascadingTypes {
    @NonNull
    private final Map<Type, MockedTypeCascade> mockedTypesToCascades;

    CascadingTypes() {
        mockedTypesToCascades = new ConcurrentHashMap<>(4);
    }

    public void add(boolean fromMockField, @NonNull Type mockedType) {
        Class<?> mockedClass = getClassType(mockedType);
        String mockedTypeDesc = JavaType.getInternalName(mockedClass);
        add(mockedTypeDesc, fromMockField, mockedType);
    }

    @NonNull
    MockedTypeCascade add(@NonNull String mockedTypeDesc, boolean fromMockField, @NonNull Type mockedType) {
        MockedTypeCascade cascade = mockedTypesToCascades.get(mockedType);

        if (cascade == null) {
            cascade = new MockedTypeCascade(fromMockField, mockedType, mockedTypeDesc);
            mockedTypesToCascades.put(mockedType, cascade);
        }

        return cascade;
    }

    @NonNull
    MockedTypeCascade getCascade(@NonNull Type mockedType) {
        return mockedTypesToCascades.get(mockedType);
    }

    @Nullable
    MockedTypeCascade getCascade(@NonNull String mockedTypeDesc, @Nullable Object mockInstance) {
        if (mockedTypesToCascades.isEmpty()) {
            return null;
        }

        if (mockInstance != null) {
            MockedTypeCascade cascade = findCascadeForInstance(mockInstance);

            if (cascade != null) {
                return cascade;
            }
        }

        for (MockedTypeCascade cascade : mockedTypesToCascades.values()) {
            if (cascade.mockedTypeDesc.equals(mockedTypeDesc)) {
                return cascade;
            }
        }

        return null;
    }

    @Nullable
    private MockedTypeCascade findCascadeForInstance(@NonNull Object mockInstance) {
        for (MockedTypeCascade cascade : mockedTypesToCascades.values()) {
            if (cascade.hasInstance(mockInstance)) {
                return cascade;
            }
        }

        return null;
    }

    void clearNonSharedCascadingTypes() {
        if (!mockedTypesToCascades.isEmpty()) {
            Iterator<MockedTypeCascade> itr = mockedTypesToCascades.values().iterator();

            while (itr.hasNext()) {
                MockedTypeCascade cascade = itr.next();

                if (cascade.fromMockField) {
                    cascade.discardCascadedMocks();
                } else {
                    itr.remove();
                }
            }
        }
    }

    public void clear() {
        mockedTypesToCascades.clear();
    }

    void addInstance(@NonNull Type mockedType, @NonNull Object cascadingInstance) {
        MockedTypeCascade cascade = mockedTypesToCascades.get(mockedType);

        if (cascade != null) {
            cascade.addInstance(cascadingInstance);
        }
    }
}
