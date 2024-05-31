package mockit.asm.types;

import static mockit.asm.jvmConstants.Opcodes.ACONST_NULL;
import static mockit.asm.jvmConstants.Opcodes.ALOAD;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public abstract class ReferenceType extends JavaType {
    /**
     * The internal name of this Java reference type.
     */
    @NonNull
    final char[] typeDescChars;

    /**
     * The offset of the internal name of this Java type in {@link #typeDescChars}.
     */
    @NonNegative
    final int off;

    ReferenceType(@NonNull char[] typeDesc) {
        super(typeDesc.length);
        typeDescChars = typeDesc;
        off = 0;
    }

    ReferenceType(@NonNull char[] typeDesc, @NonNegative int off, @NonNegative int len) {
        super(len);
        typeDescChars = typeDesc;
        this.off = off;
    }

    /**
     * Returns the Java type corresponding to the given type descriptor.
     *
     * @param typeDesc
     *            a type descriptor.
     */
    @NonNull
    public static ReferenceType createFromTypeDescriptor(@NonNull String typeDesc) {
        return getReferenceType(typeDesc.toCharArray(), 0);
    }

    /**
     * Returns the Java type corresponding to the given type descriptor. For method descriptors, <code>buf</code> is
     * supposed to contain nothing more than the descriptor itself.
     *
     * @param buf
     *            a buffer containing a type descriptor.
     * @param off
     *            the offset of this descriptor in the previous buffer.
     */
    @NonNull
    static ReferenceType getReferenceType(@NonNull char[] buf, @NonNegative int off) {
        switch (buf[off]) {
            case '[':
                return ArrayType.create(buf, off);
            case 'L':
                return ObjectType.create(buf, off);
            case '(':
                return new MethodType(buf, off, buf.length - off);
            default:
                throw new IllegalArgumentException("Invalid type descriptor: " + new String(buf));
        }
    }

    /**
     * Returns the object or array type corresponding to the given internal name.
     */
    @NonNull
    public static ReferenceType createFromInternalName(@NonNull String internalName) {
        char[] buf = internalName.toCharArray();
        return buf[0] == '[' ? new ArrayType(buf) : new ObjectType(buf);
    }

    static int findTypeNameLength(@NonNull char[] buf, @NonNegative int off, @NonNegative int len) {
        len++;

        while (buf[off + len] != ';') {
            len++;
        }

        return len;
    }

    static void getDescriptor(@NonNull StringBuilder buf, @NonNull Class<?> aClass) {
        buf.append('L');

        String name = aClass.getName();
        int len = name.length();

        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            buf.append(c == '.' ? '/' : c);
        }

        buf.append(';');
    }

    @Override
    void getDescriptor(@NonNull StringBuilder typeDesc) {
        typeDesc.append(typeDescChars, off, len);
    }

    /**
     * Returns the internal name of the class corresponding to this object or array type. The internal name of a class
     * is its fully qualified name (as returned by Class.getName(), where '.' are replaced by '/'. For an array type, it
     * starts with "[" and ends with the type descriptor of the array element type.
     *
     * @return the internal name of the class corresponding to this object or array type.
     */
    @NonNull
    public final String getInternalName() {
        return new String(typeDescChars, off, len);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public int getOpcode(int opcode) {
        return opcode + 4;
    }

    @Override
    public final int getLoadOpcode() {
        return ALOAD;
    }

    @Override
    public final int getConstOpcode() {
        return ACONST_NULL;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ReferenceType)) {
            return false;
        }

        ReferenceType t = (ReferenceType) o;

        if (getClass() != t.getClass() || len != t.len) {
            return false;
        }

        for (int i = off, j = t.off, end = i + len; i < end; i++, j++) {
            if (typeDescChars[i] != t.typeDescChars[j]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public final int hashCode() {
        int hc = 13;

        for (int i = off, end = i + len; i < end; i++) {
            // noinspection CharUsedInArithmeticContext
            hc = 17 * (hc + typeDescChars[i]);
        }

        return hc;
    }
}
