package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.INTEGER;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class IntItem extends IntValueItem {
    public IntItem(@NonNegative int index) {
        super(index);
        type = INTEGER;
    }

    IntItem(@NonNegative int index, @NonNull IntItem item) {
        super(index, item);
    }
}
