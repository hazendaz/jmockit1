package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.INTEGER;

import javax.annotation.Nonnull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class IntItem extends IntValueItem {
    public IntItem(@NonNegative int index) {
        super(index);
        type = INTEGER;
    }

    IntItem(@NonNegative int index, @Nonnull IntItem item) {
        super(index, item);
    }
}
