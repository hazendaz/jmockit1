package mockit.asm.constantPool;

import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AttributeWriter {
    @NonNull
    protected final ConstantPoolGeneration cp;

    /**
     * The index of the constant pool item that contains the name of the associated attribute.
     */
    @NonNegative
    protected int attributeIndex;

    protected AttributeWriter(@NonNull ConstantPoolGeneration cp) {
        this.cp = cp;
    }

    protected AttributeWriter(@NonNull ConstantPoolGeneration cp, @NonNull String attributeName) {
        this.cp = cp;
        setAttribute(attributeName);
    }

    protected final void setAttribute(@NonNull String attributeName) {
        attributeIndex = cp.newUTF8(attributeName);
    }

    @NonNegative
    public abstract int getSize();

    public void put(@NonNull ByteVector out) {
        put(out, 2);
    }

    protected final void put(@NonNull ByteVector out, @NonNegative int size) {
        out.putShort(attributeIndex).putInt(size);
    }
}
