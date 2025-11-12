/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.INTEGER;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class IntItem extends IntValueItem {
    public IntItem(@NonNegative int index) {
        super(index);
        type = INTEGER;
    }

    IntItem(@NonNegative int index, @NonNull IntItem item) {
        super(index, item);
    }
}
