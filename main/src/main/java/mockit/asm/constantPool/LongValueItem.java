/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public class LongValueItem extends Item {
    /**
     * Value of this item, for a long item.
     */
    long longVal;

    LongValueItem(@NonNegative int index) {
        super(index);
    }

    LongValueItem(@NonNegative int index, @NonNull LongValueItem item) {
        super(index, item);
        longVal = item.longVal;
    }

    public final void setValue(long value) {
        longVal = value;
        setHashCode((int) value);
    }

    @Override
    final boolean isEqualTo(@NonNull Item item) {
        return ((LongValueItem) item).longVal == longVal;
    }
}
