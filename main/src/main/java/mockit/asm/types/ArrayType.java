/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.types;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class ArrayType extends ReferenceType {
    @NonNull
    public static ArrayType create(@NonNull String typeDesc) {
        return new ArrayType(typeDesc.toCharArray());
    }

    /**
     * Initializes an array type.
     *
     * @param typeDesc
     *            a buffer containing the descriptor of the array type
     * @param off
     *            the offset of the descriptor in the buffer
     */
    @NonNull
    static ArrayType create(@NonNull char[] typeDesc, @NonNegative int off) {
        int len = findNumberOfDimensions(typeDesc, off);

        if (typeDesc[off + len] == 'L') {
            len = findTypeNameLength(typeDesc, off, len);
        }

        return new ArrayType(typeDesc, off, len + 1);
    }

    @NonNegative
    private static int findNumberOfDimensions(@NonNull char[] typeDesc, @NonNegative int off) {
        int dimensions = 1;

        while (typeDesc[off + dimensions] == '[') {
            dimensions++;
        }

        return dimensions;
    }

    private ArrayType(@NonNull char[] typeDesc, @NonNegative int off, @NonNegative int len) {
        super(typeDesc, off, len);
    }

    ArrayType(@NonNull char[] typeDesc) {
        super(typeDesc);
    }

    /**
     * Returns the number of dimensions of this array type.
     */
    @NonNegative
    public int getDimensions() {
        return findNumberOfDimensions(typeDescChars, off);
    }

    /**
     * Returns the type of the elements of this array type.
     */
    @NonNull
    public JavaType getElementType() {
        int dimensions = getDimensions();
        return getType(typeDescChars, off + dimensions);
    }

    @NonNull
    @Override
    public String getClassName() {
        String className = getElementType().getClassName();
        StringBuilder sb = new StringBuilder(className);
        int dimensions = getDimensions();

        for (int i = dimensions; i > 0; i--) {
            sb.append("[]");
        }

        return sb.toString();
    }
}
