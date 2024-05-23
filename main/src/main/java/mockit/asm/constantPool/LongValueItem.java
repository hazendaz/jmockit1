package mockit.asm.constantPool;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

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
