/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.fields;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.asm.AnnotatedReader;
import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassVisitor;

import org.checkerframework.checker.index.qual.NonNegative;

public final class FieldReader extends AnnotatedReader {
    @NonNull
    private final ClassVisitor cv;
    @Nullable
    private Object constantValue;

    public FieldReader(@NonNull ClassReader cr, @NonNull ClassVisitor cv) {
        super(cr);
        this.cv = cv;
    }

    /**
     * Reads each field and makes the given visitor visit it.
     *
     * @return the offset of the first byte following the last field in the class.
     */
    @NonNegative
    public int readFields() {
        for (int fieldCount = readUnsignedShort(); fieldCount > 0; fieldCount--) {
            readField();
        }

        return codeIndex;
    }

    private void readField() {
        access = readUnsignedShort();
        String name = readNonnullUTF8();
        String desc = readNonnullUTF8();
        constantValue = null;

        readAttributes();

        FieldVisitor fv = cv.visitField(access, name, desc, signature, constantValue);

        if (fv != null) {
            readAnnotations(fv);
            fv.visitEnd();
        }
    }

    @Nullable
    @Override
    protected Boolean readAttribute(@NonNull String attributeName) {
        if ("ConstantValue".equals(attributeName)) {
            int constItemIndex = readUnsignedShort();

            if (constItemIndex > 0) {
                constantValue = readConst(constItemIndex);
            }

            return true;
        }

        return null;
    }
}
