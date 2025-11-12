/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.types;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class ObjectType extends ReferenceType {
    @NonNull
    public static ObjectType create(@NonNull String internalName) {
        return new ObjectType(internalName.toCharArray());
    }

    /**
     * Initializes an object reference type.
     *
     * @param typeDesc
     *            a buffer containing the descriptor of the type
     * @param off
     *            the offset of the descriptor in the buffer
     */
    @NonNull
    static ObjectType create(@NonNull char[] typeDesc, @NonNegative int off) {
        int len = findTypeNameLength(typeDesc, off, 0);
        return new ObjectType(typeDesc, off + 1, len - 1);
    }

    private ObjectType(@NonNull char[] typeDesc, @NonNegative int off, @NonNegative int len) {
        super(typeDesc, off, len);
    }

    ObjectType(@NonNull char[] internalName) {
        super(internalName);
    }

    @Override
    void getDescriptor(@NonNull StringBuilder typeDesc) {
        typeDesc.append('L');
        super.getDescriptor(typeDesc);
        typeDesc.append(';');
    }

    @NonNull
    @Override
    public String getClassName() {
        return getInternalName().replace('/', '.');
    }
}
