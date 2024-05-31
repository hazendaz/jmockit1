/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import static mockit.internal.util.Utilities.getClassType;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class MultiValuedProvider extends InjectionProvider {
    @NonNull
    private final List<InjectionProvider> individualProviders;

    MultiValuedProvider(@NonNull Type elementType) {
        super(elementType, "");
        individualProviders = new ArrayList<>();
    }

    void addInjectable(@NonNull InjectionProvider provider) {
        individualProviders.add(provider);
    }

    @NonNull
    @Override
    public Class<?> getClassOfDeclaredType() {
        return getClassType(declaredType);
    }

    @NonNull
    @Override
    public Object getValue(@Nullable Object owner) {
        List<Object> values = new ArrayList<>(individualProviders.size());

        for (InjectionProvider provider : individualProviders) {
            Object value = provider.getValue(owner);
            values.add(value);
        }

        return values;
    }
}
