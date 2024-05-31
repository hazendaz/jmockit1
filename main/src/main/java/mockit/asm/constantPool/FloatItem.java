package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.FLOAT;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class FloatItem extends IntValueItem {
    public FloatItem(@NonNegative int index) {
        super(index);
        type = FLOAT;
    }

    FloatItem(@NonNegative int index, @NonNull FloatItem item) {
        super(index, item);
    }

    /**
     * Sets the value of this item.
     */
    public void set(float value) {
        int intValue = Float.floatToRawIntBits(value);
        setValue(intValue);
    }
}
