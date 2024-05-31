package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.LONG;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class LongItem extends LongValueItem {
    public LongItem(@NonNegative int index) {
        super(index);
        type = LONG;
    }

    LongItem(@NonNegative int index, @NonNull LongItem item) {
        super(index, item);
    }
}
