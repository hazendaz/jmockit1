/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.injection.field;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.internal.injection.InjectionState;
import mockit.internal.injection.Injector;
import mockit.internal.injection.full.FullInjection;

public final class FieldInjection extends Injector {
    public FieldInjection(@NonNull InjectionState injectionState, @Nullable FullInjection fullInjection) {
        super(injectionState, fullInjection);
    }
}
