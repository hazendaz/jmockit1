package mockit.asm;

import javax.annotation.Nonnull;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

public final class SignatureWriter extends AttributeWriter {
    @NonNegative
    private final int signatureIndex;

    public SignatureWriter(@Nonnull ConstantPoolGeneration cp, @Nonnull String signature) {
        super(cp, "Signature");
        signatureIndex = cp.newUTF8(signature);
    }

    @NonNegative
    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void put(@Nonnull ByteVector out) {
        super.put(out);
        out.putShort(signatureIndex);
    }
}
