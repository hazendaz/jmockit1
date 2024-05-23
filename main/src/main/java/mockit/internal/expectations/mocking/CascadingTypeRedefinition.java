/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.reflect.Type;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class CascadingTypeRedefinition extends BaseTypeRedefinition {
    @NonNull
    private final Type mockedType;

    public CascadingTypeRedefinition(@NonNull String cascadingMethodName, @NonNull Type mockedType) {
        super(new MockedType(cascadingMethodName, mockedType));
        this.mockedType = mockedType;
    }

    @Nullable
    public InstanceFactory redefineType() {
        return redefineType(mockedType);
    }
}
