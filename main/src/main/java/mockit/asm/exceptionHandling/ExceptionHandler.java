package mockit.asm.exceptionHandling;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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
    @NonNull
    final Label start;

    /**
     * End of the exception handler's scope (exclusive).
     */
    @NonNull
    final Label end;

    /**
     * Beginning of the exception handler's code.
     */
    @NonNull
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

    ExceptionHandler(@NonNull Label start, @NonNull Label end, @NonNull Label handler, @Nullable String desc,
            @NonNegative int type) {
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.desc = desc;
        this.type = type;
    }

    @NonNull
    String getCatchTypeDesc() {
        return desc == null ? "java/lang/Throwable" : desc;
    }

    void put(@NonNull ByteVector out) {
        out.putShort(start.position).putShort(end.position).putShort(handler.position).putShort(type);
    }
}
