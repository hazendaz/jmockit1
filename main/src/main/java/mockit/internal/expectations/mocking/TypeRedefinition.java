/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Type;

import mockit.internal.expectations.MockingFilters;

class TypeRedefinition extends BaseTypeRedefinition {
    TypeRedefinition(@NonNull MockedType typeMetadata) {
        super(typeMetadata);
    }

    @Nullable
    final InstanceFactory redefineType() {
        // noinspection ConstantConditions
        Class<?> classToMock = typeMetadata.getClassType();

        if (MockingFilters.isSubclassOfUnmockable(classToMock)) {
            String mockSource = typeMetadata.field == null ? "mock parameter" : "mock field";
            throw new IllegalArgumentException(
                    classToMock + " is not mockable (" + mockSource + " \"" + typeMetadata.getName() + "\")");
        }

        Type declaredType = typeMetadata.getDeclaredType();
        return redefineType(declaredType);
    }
}
