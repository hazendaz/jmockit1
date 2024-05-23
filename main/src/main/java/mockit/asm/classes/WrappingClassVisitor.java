package mockit.asm.classes;

import javax.annotation.Nullable;

import mockit.asm.annotations.AnnotationVisitor;
import mockit.asm.fields.FieldVisitor;
import mockit.asm.methods.MethodVisitor;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Same as {@link ClassVisitor}, except it always wraps a {@link ClassWriter}.
 */
public class WrappingClassVisitor extends ClassVisitor {
    /**
     * The class visitor to which this visitor must delegate method calls.
     */
    @NonNull
    protected final ClassWriter cw;

    /**
     * Constructs a new WrappingClassVisitor.
     *
     * @param cw
     *            the class writer to which this visitor must delegate method calls.
     */
    protected WrappingClassVisitor(@NonNull ClassWriter cw) {
        this.cw = cw;
    }

    @Override
    public void visit(int version, int access, @NonNull String name, @NonNull ClassInfo additionalInfo) {
        cw.visit(version, access, name, additionalInfo);
    }

    @Nullable
    @Override
    public AnnotationVisitor visitAnnotation(@NonNull String desc) {
        return cw.visitAnnotation(desc);
    }

    @Override
    public void visitInnerClass(@NonNull String name, @Nullable String outerName, @Nullable String innerName,
            int access) {
        cw.visitInnerClass(name, outerName, innerName, access);
    }

    @Nullable
    @Override
    public FieldVisitor visitField(int access, @NonNull String name, @NonNull String desc, @Nullable String signature,
            @Nullable Object value) {
        return cw.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, @NonNull String name, @NonNull String desc, @Nullable String signature,
            @Nullable String[] exceptions) {
        return cw.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public final byte[] toByteArray() {
        return cw.toByteArray();
    }
}
