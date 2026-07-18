/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.classes;

import edu.umd.cs.findbugs.annotations.NonNull;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Generates the "PermittedSubclasses" attribute of a sealed class (Java 17+). The attribute is a u2 count followed by
 * that many u2 CONSTANT_Class indices, structurally identical to "NestMembers". Preserving it verbatim lets a redefined
 * sealed class keep the attribute the JVM requires to stay unchanged across redefinition.
 */
final class PermittedSubclassesWriter extends AttributeWriter {
    @NonNegative
    private final int[] permittedSubclassIndices;

    PermittedSubclassesWriter(@NonNull ConstantPoolGeneration cp, @NonNull String[] permittedSubclassNames) {
        super(cp, "PermittedSubclasses");

        permittedSubclassIndices = new int[permittedSubclassNames.length];

        for (int i = 0; i < permittedSubclassNames.length; i++) {
            permittedSubclassIndices[i] = cp.newClass(permittedSubclassNames[i]);
        }
    }

    @NonNegative
    @Override
    public int getSize() {
        return 8 + 2 * permittedSubclassIndices.length;
    }

    @Override
    public void put(@NonNull ByteVector out) {
        int numberOfSubclasses = permittedSubclassIndices.length;
        put(out, 2 + 2 * numberOfSubclasses);
        out.putShort(numberOfSubclasses);

        for (int permittedSubclassIndex : permittedSubclassIndices) {
            out.putShort(permittedSubclassIndex);
        }
    }
}
