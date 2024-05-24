package mockit.asm;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class SignatureWriter extends AttributeWriter {
    @NonNegative
    private final int signatureIndex;

    public SignatureWriter(@NonNull ConstantPoolGeneration cp, @NonNull String signature) {
        super(cp, "Signature");
        signatureIndex = cp.newUTF8(signature);
    }

    @NonNegative
    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void put(@NonNull ByteVector out) {
        super.put(out);
        out.putShort(signatureIndex);
    }
}
