/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.asm.annotations.AnnotationReader;
import mockit.asm.annotations.AnnotationVisitor;
import mockit.asm.jvmConstants.Access;
import mockit.asm.util.BytecodeReader;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A bytecode reader for reading common elements (signature, annotations) of a class, field, or method.
 */
public abstract class AnnotatedReader extends BytecodeReader {
    @NonNull
    private final AnnotationReader annotationReader = new AnnotationReader(this);

    @NonNegative
    private int annotationsCodeIndex;

    /**
     * The access flags of the class, field, or method currently being parsed.
     */
    protected int access;

    /**
     * The generic type signature of the class/field/method, if it has one.
     */
    @Nullable
    protected String signature;

    protected AnnotatedReader(@NonNull byte[] code) {
        super(code);
    }

    protected AnnotatedReader(@NonNull AnnotatedReader another) {
        super(another);
    }

    protected final void readAttributes() {
        signature = null;
        annotationsCodeIndex = 0;

        for (int attributeCount = readUnsignedShort(); attributeCount > 0; attributeCount--) {
            String attributeName = readNonnullUTF8();
            int codeOffsetToNextAttribute = readInt();

            if ("Signature".equals(attributeName)) {
                signature = readNonnullUTF8();
                continue;
            }

            Boolean outcome = readAttribute(attributeName);

            if (outcome == Boolean.TRUE) {
                continue;
            }

            if (outcome == null) {
                // noinspection SwitchStatementWithoutDefaultBranch
                switch (attributeName) {
                    case "RuntimeVisibleAnnotations":
                        annotationsCodeIndex = codeIndex;
                        break;
                    case "Deprecated":
                        access = Access.asDeprecated(access);
                        break;
                    case "Synthetic":
                        access = Access.asSynthetic(access);
                }
            }

            codeIndex += codeOffsetToNextAttribute;
        }
    }

    /**
     * Attempts to read the next attribute, provided it's one recognizable by the implementing subclass.
     *
     * @param attributeName
     *            the attribute name
     *
     * @return {@code true} if {@link #codeIndex} is already pointing to the next attribute in the classfile,
     *         {@code false} or {@code null} otherwise; in the case of {@code null}, the current attribute was not yet
     *         identified, but is one of the more general ones ("RuntimeVisibleAnnotations", "Deprecated", or
     *         "Synthetic")
     */
    @Nullable
    protected abstract Boolean readAttribute(@NonNull String attributeName);

    protected final void readAnnotations(@NonNull BaseWriter visitor) {
        if (annotationsCodeIndex > 0) {
            int previousCodeIndex = codeIndex;
            codeIndex = annotationsCodeIndex;

            for (int annotationCount = readUnsignedShort(); annotationCount > 0; annotationCount--) {
                String annotationTypeDesc = readNonnullUTF8();
                AnnotationVisitor av = visitor.visitAnnotation(annotationTypeDesc);
                readAnnotationValues(av);
            }

            codeIndex = previousCodeIndex;
        }
    }

    protected final void readAnnotationValues(@Nullable AnnotationVisitor av) {
        codeIndex = annotationReader.readNamedAnnotationValues(codeIndex, av);
    }
}
