package mockit.asm;

import java.util.List;

import javax.annotation.Nullable;

import mockit.asm.annotations.AnnotationVisitor;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.jvmConstants.Access;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BaseWriter {
    /**
     * The dynamically generated constant pool of the class being built/modified.
     */
    protected ConstantPoolGeneration cp;

    /**
     * The access flags of this class, field, or method.
     */
    protected int classOrMemberAccess;

    @NonNegative
    private int deprecatedAttributeIndex;
    @NonNegative
    private int syntheticAttributeIndex;

    /**
     * The runtime visible annotations of this class/field/method.
     */
    @Nullable
    protected AnnotationVisitor annotations;

    protected BaseWriter() {
    }

    protected BaseWriter(@NonNull ConstantPoolGeneration cp, int classOrMemberAccess) {
        this.cp = cp;
        this.classOrMemberAccess = classOrMemberAccess;
    }

    /**
     * Returns the {@link #cp constant pool generation helper object} used by this writer.
     *
     * @return the constant pool generation
     */
    @NonNull
    public final ConstantPoolGeneration getConstantPoolGeneration() {
        return cp;
    }

    /**
     * Visits an annotation of the class/field/method being visited.
     *
     * @param desc
     *            the descriptor of the annotation type
     *
     * @return a visitor to visit the annotation values, or <code>null</code> if this visitor is not interested in
     *         visiting the annotation
     */
    @Nullable
    public AnnotationVisitor visitAnnotation(@NonNull String desc) {
        return addAnnotation(desc);
    }

    @NonNull
    private AnnotationVisitor addAnnotation(@NonNull String desc) {
        AnnotationVisitor aw = new AnnotationVisitor(cp, desc);
        aw.setNext(annotations);
        annotations = aw;
        return aw;
    }

    /**
     * Visits the end of the class/field/method being visited. This method, which is the last one to be called, is used
     * to inform the visitor that all the annotations and attributes of the class/field/method have been visited.
     */
    public void visitEnd() {
    }

    protected final void createMarkerAttributes(int classVersion) {
        if (Access.isDeprecated(classOrMemberAccess)) {
            deprecatedAttributeIndex = cp.newUTF8("Deprecated");
        }

        if (Access.isSynthetic(classOrMemberAccess, classVersion)) {
            syntheticAttributeIndex = cp.newUTF8("Synthetic");
        }
    }

    @NonNegative
    protected final int getAnnotationsSize() {
        if (annotations != null) {
            getConstantPoolItemForRuntimeVisibleAnnotationsAttribute();
            return 8 + annotations.getSize();
        }

        return 0;
    }

    @NonNegative
    private int getConstantPoolItemForRuntimeVisibleAnnotationsAttribute() {
        return cp.newUTF8("RuntimeVisibleAnnotations");
    }

    @NonNegative
    protected final int getMarkerAttributeCount() {
        return (deprecatedAttributeIndex == 0 ? 0 : 1) + (syntheticAttributeIndex == 0 ? 0 : 1);
    }

    @NonNegative
    protected final int getMarkerAttributesSize() {
        int attributeCount = getMarkerAttributeCount();
        return 6 * attributeCount;
    }

    protected final void putAccess(@NonNull ByteVector out, int baseMask) {
        int accessFlag = Access.computeFlag(classOrMemberAccess, baseMask);
        out.putShort(accessFlag);
    }

    protected final void putMarkerAttributes(@NonNull ByteVector out) {
        if (deprecatedAttributeIndex > 0) {
            out.putShort(deprecatedAttributeIndex).putInt(0);
        }

        if (syntheticAttributeIndex > 0) {
            out.putShort(syntheticAttributeIndex).putInt(0);
        }
    }

    protected final void putAnnotations(@NonNull ByteVector out) {
        if (annotations != null) {
            int item = getConstantPoolItemForRuntimeVisibleAnnotationsAttribute();
            out.putShort(item);
            annotations.put(out);
        }
    }

    protected void put(@NonNull ByteVector out) {
    }

    protected static void put(@NonNull ByteVector out, @NonNull List<? extends BaseWriter> writers) {
        out.putShort(writers.size());

        for (BaseWriter writer : writers) {
            writer.put(out);
        }
    }
}
