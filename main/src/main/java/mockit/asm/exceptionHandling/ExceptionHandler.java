package mockit.asm.exceptionHandling;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mockit.asm.controlFlow.Label;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Information about an exception handler block.
 */
final class ExceptionHandler {
    /**
     * Beginning of the exception handler's scope (inclusive).
     */
    @Nonnull
    final Label start;

    /**
     * End of the exception handler's scope (exclusive).
     */
    @Nonnull
    final Label end;

    /**
     * Beginning of the exception handler's code.
     */
    @Nonnull
    final Label handler;

    /**
     * Internal name of the type of exceptions handled by this handler, or <code>null</code> to catch any exceptions.
     */
    @Nullable
    private final String desc;

    /**
     * Constant pool index of the internal name of the type of exceptions handled by this handler, or <code>0</code> to
     * catch any exceptions.
     */
    @NonNegative
    private final int type;

    ExceptionHandler(@Nonnull Label start, @Nonnull Label end, @Nonnull Label handler, @Nullable String desc,
            @NonNegative int type) {
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.desc = desc;
        this.type = type;
    }

    @Nonnull
    String getCatchTypeDesc() {
        return desc == null ? "java/lang/Throwable" : desc;
    }

    void put(@Nonnull ByteVector out) {
        out.putShort(start.position).putShort(end.position).putShort(handler.position).putShort(type);
    }
}
