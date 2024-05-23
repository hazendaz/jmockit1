package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.HANDLE_BASE;

import javax.annotation.Nonnull;

import mockit.asm.util.MethodHandle;

import org.checkerframework.checker.index.qual.NonNegative;

public final class MethodHandleItem extends Item {
    private MethodHandle methodHandle;

    public MethodHandleItem(@NonNegative int index) {
        super(index);
        type = HANDLE_BASE;
    }

    MethodHandleItem(@NonNegative int index, @Nonnull MethodHandleItem item) {
        super(index, item);
        methodHandle = item.methodHandle;
    }

    /**
     * Sets the type and hash code of this method handle item.
     */
    public void set(@Nonnull MethodHandle methodHandle) {
        this.methodHandle = methodHandle;
        type = HANDLE_BASE;
        setHashCode(methodHandle.hashCode());
        type = HANDLE_BASE + methodHandle.tag;
    }

    @Override
    boolean isEqualTo(@Nonnull Item item) {
        return ((MethodHandleItem) item).methodHandle.equals(methodHandle);
    }
}
