package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.LONG;

import javax.annotation.Nonnull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class LongItem extends LongValueItem {
    public LongItem(@NonNegative int index) {
        super(index);
        type = LONG;
    }

    LongItem(@NonNegative int index, @Nonnull LongItem item) {
        super(index, item);
    }
}
