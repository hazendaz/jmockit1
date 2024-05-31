package mockit.asm.constantPool;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

class IntValueItem extends Item {
    /**
     * Value of this item, for an integer item.
     */
    int intVal;

    IntValueItem(@NonNegative int index) {
        super(index);
    }

    IntValueItem(@NonNegative int index, @NonNull IntValueItem item) {
        super(index, item);
        intVal = item.intVal;
    }

    public final void setValue(int value) {
        intVal = value;
        setHashCode(value);
    }

    @Override
    final boolean isEqualTo(@NonNull Item item) {
        return ((IntValueItem) item).intVal == intVal;
    }
}
