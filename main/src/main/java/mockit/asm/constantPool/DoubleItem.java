package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.DOUBLE;

import javax.annotation.Nonnull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class DoubleItem extends LongValueItem {
    public DoubleItem(@NonNegative int index) {
        super(index);
        type = DOUBLE;
    }

    DoubleItem(@NonNegative int index, @Nonnull DoubleItem item) {
        super(index, item);
    }

    /**
     * Sets the value of this item.
     */
    public void set(double value) {
        long longValue = Double.doubleToRawLongBits(value);
        setValue(longValue);
    }
}
