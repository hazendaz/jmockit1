package mockit.asm.constantPool;

import javax.annotation.Nonnull;

import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

public abstract class AttributeWriter {
    @Nonnull
    protected final ConstantPoolGeneration cp;

    /**
     * The index of the constant pool item that contains the name of the associated attribute.
     */
    @NonNegative
    protected int attributeIndex;

    protected AttributeWriter(@Nonnull ConstantPoolGeneration cp) {
        this.cp = cp;
    }

    protected AttributeWriter(@Nonnull ConstantPoolGeneration cp, @Nonnull String attributeName) {
        this.cp = cp;
        setAttribute(attributeName);
    }

    protected final void setAttribute(@Nonnull String attributeName) {
        attributeIndex = cp.newUTF8(attributeName);
    }

    @NonNegative
    public abstract int getSize();

    public void put(@Nonnull ByteVector out) {
        put(out, 2);
    }

    protected final void put(@Nonnull ByteVector out, @NonNegative int size) {
        out.putShort(attributeIndex).putInt(size);
    }
}
