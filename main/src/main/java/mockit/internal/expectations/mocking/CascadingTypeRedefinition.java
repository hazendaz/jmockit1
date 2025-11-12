/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.mocking;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Type;

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
