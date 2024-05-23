package mockit.asm.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A Java field or method type. This class can be used to make it easier to manipulate type and method descriptors.
 */
@SuppressWarnings("ClassReferencesSubclass")
public abstract class JavaType {
    private static final JavaType[] NO_ARGS = {};

    /**
     * The length of the internal name of this Java type.
     */
    @NonNegative
    final int len;

    /**
     * Constructs a Java type.
     *
     * @param len
     *            the length of this descriptor.
     */
    JavaType(@NonNegative int len) {
        this.len = len;
    }

    /**
     * Returns the Java type corresponding to the given type descriptor.
     *
     * @param typeDescriptor
     *            a field or method type descriptor.
     */
    @NonNull
    public static JavaType getType(@NonNull String typeDescriptor) {
        return getType(typeDescriptor.toCharArray(), 0);
    }

    /**
     * Returns the Java types corresponding to the argument types of the given method descriptor.
     */
    @NonNull
    public static JavaType[] getArgumentTypes(@NonNull String methodDescriptor) {
        char[] buf = methodDescriptor.toCharArray();
        int off = 1;
        int size = 0;

        while (true) {
            char c = buf[off];
            off++;

            if (c == ')') {
                break;
            }
            if (c == 'L') {
                off = findNextTypeTerminatorCharacter(buf, off);
                size++;
            } else if (c != '[') {
                size++;
            }
        }

        return getArgumentTypes(buf, size);
    }

    @NonNegative
    private static int findNextTypeTerminatorCharacter(@NonNull char[] desc, @NonNegative int i) {
        while (desc[i++] != ';') {
        }
        return i;
    }

    @NonNull
    private static JavaType[] getArgumentTypes(@NonNull char[] buf, @NonNegative int argCount) {
        if (argCount == 0) {
            return NO_ARGS;
        }

        JavaType[] argTypes = new JavaType[argCount];
        int off = 1;

        for (int i = 0; buf[off] != ')'; i++) {
            JavaType argType = getType(buf, off);
            argTypes[i] = argType;
            off += argType.len + (argType instanceof ObjectType ? 2 : 0);
        }

        return argTypes;
    }

    /**
     * Returns the Java type corresponding to the return type of the given method descriptor.
     */
    @NonNull
    public static JavaType getReturnType(@NonNull String methodDescriptor) {
        char[] buf = methodDescriptor.toCharArray();
        return getType(buf, methodDescriptor.indexOf(')') + 1);
    }

    /**
     * Computes the size of the arguments and of the return value of a method.
     *
     * @param desc
     *            the descriptor of a method.
     *
     * @return the size of the arguments of the method (plus one for the implicit <code>this</code> argument),
     *         <code>argSize</code>, and the size of its return value, <code>retSize</code>, packed into a single
     *
     *         <pre>{@code int i = (argSize << 2) | retSize }</pre>
     *
     *         (<code>argSize</code> is therefore equal to
     *
     *         <pre>{@code i >> 2 }</pre>
     *
     *         , and
     *
     *         <pre>{@code retSize }</pre>
     *
     *         to
     *
     *         <pre>
     * &#64;{code i &amp; 0x03 }
     *         </pre>
     *
     *         ).
     */
    public static int getArgumentsAndReturnSizes(@NonNull String desc) {
        int argSize = 1;
        int i = 1;

        while (true) {
            char currentChar = desc.charAt(i);
            i++;

            switch (currentChar) {
                case ')': {
                    char nextChar = desc.charAt(i);
                    return argSize << 2 | (nextChar == 'V' ? 0 : isDoubleSizePrimitiveType(nextChar) ? 2 : 1);
                }
                case 'L':
                    i = findNextTypeTerminatorCharacter(desc, i);
                    argSize++;
                    break;
                case '[': {
                    i = findStartOfArrayElementType(desc, i);
                    char arrayElementType = desc.charAt(i);
                    if (isDoubleSizePrimitiveType(arrayElementType)) {
                        argSize--;
                    }
                    break;
                }
                default:
                    if (isDoubleSizePrimitiveType(currentChar)) {
                        argSize += 2;
                    } else {
                        argSize++;
                    }
                    break;
            }
        }
    }

    private static boolean isDoubleSizePrimitiveType(char typeCode) {
        return typeCode == 'D' || typeCode == 'J';
    }

    @NonNegative
    private static int findNextTypeTerminatorCharacter(@NonNull String desc, @NonNegative int i) {
        while (desc.charAt(i++) != ';') {
        }
        return i;
    }

    @NonNegative
    private static int findStartOfArrayElementType(@NonNull String desc, @NonNegative int i) {
        while (desc.charAt(i) == '[') {
            i++;
        }
        return i;
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
    static JavaType getType(@NonNull char[] buf, @NonNegative int off) {
        PrimitiveType primitiveType = PrimitiveType.getPrimitiveType(buf[off]);

        if (primitiveType != null) {
            return primitiveType;
        }

        return ReferenceType.getReferenceType(buf, off);
    }

    /**
     * Returns the binary name of the class corresponding to this type. This method must not be used on method types.
     */
    @NonNull
    public abstract String getClassName();

    // ------------------------------------------------------------------------
    // Conversion to type descriptors
    // ------------------------------------------------------------------------

    /**
     * Returns the descriptor corresponding to this Java type.
     */
    @NonNull
    public final String getDescriptor() {
        StringBuilder buf = new StringBuilder();
        getDescriptor(buf);
        return buf.toString();
    }

    /**
     * Appends the descriptor corresponding to this Java type to the given string buffer.
     *
     * @param typeDesc
     *            the string builder to which the descriptor must be appended
     */
    abstract void getDescriptor(@NonNull StringBuilder typeDesc);

    // -------------------------------------------------------------------------------------------------------
    // Direct conversion from classes to type descriptors, and vice-versa, without intermediate JavaType objects
    // -------------------------------------------------------------------------------------------------------

    /**
     * Returns the internal name of the given class. The internal name of a class is its fully qualified name, as
     * returned by Class.getName(), where '.' are replaced by '/'.
     *
     * @param aClass
     *            an object or array class
     */
    @NonNull
    public static String getInternalName(@NonNull Class<?> aClass) {
        return aClass.getName().replace('.', '/');
    }

    /**
     * Returns the descriptor corresponding to the given constructor.
     */
    @NonNull
    public static String getConstructorDescriptor(@NonNull Constructor<?> constructor) {
        StringBuilder buf = getMemberDescriptor(constructor.getParameterTypes());
        buf.append('V');
        return buf.toString();
    }

    @NonNull
    private static StringBuilder getMemberDescriptor(@NonNull Class<?>[] parameterTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append('(');

        for (Class<?> parameterType : parameterTypes) {
            getDescriptor(buf, parameterType);
        }

        buf.append(')');
        return buf;
    }

    /**
     * Returns the descriptor corresponding to the given method.
     */
    @NonNull
    public static String getMethodDescriptor(@NonNull Method method) {
        StringBuilder buf = getMemberDescriptor(method.getParameterTypes());
        getDescriptor(buf, method.getReturnType());
        return buf.toString();
    }

    /**
     * Appends the descriptor of the given class to the given string builder.
     */
    private static void getDescriptor(@NonNull StringBuilder buf, @NonNull Class<?> aClass) {
        Class<?> d = aClass;

        while (true) {
            if (d.isPrimitive()) {
                char typeCode = PrimitiveType.getPrimitiveType(d).getTypeCode();
                buf.append(typeCode);
                return;
            }
            if (!d.isArray()) {
                ReferenceType.getDescriptor(buf, d);
                return;
            }
            buf.append('[');
            d = d.getComponentType();
        }
    }

    // ------------------------------------------------------------------------
    // Corresponding size and opcodes
    // ------------------------------------------------------------------------

    /**
     * Returns the size of values of this type. This method must not be used for method types.
     *
     * @return the size of values of this type, i.e., 2 for <code>long</code> and <code>double</code>, 0 for
     *         <code>void</code> and 1 otherwise.
     */
    @NonNegative
    public abstract int getSize();

    /**
     * Returns a JVM instruction opcode adapted to this Java type. This method must not be used for method types.
     *
     * @param opcode
     *            a JVM instruction opcode. This opcode must be one of ILOAD, ISTORE, IALOAD, IASTORE, IADD, ISUB, IMUL,
     *            IDIV, IREM, INEG, ISHL, ISHR, IUSHR, IAND, IOR, IXOR and IRETURN.
     *
     * @return an opcode that is similar to the given opcode, but adapted to this Java type. For example, if this type
     *         is <code>float</code> and <code>opcode</code> is IRETURN, this method returns FRETURN.
     */
    public abstract int getOpcode(int opcode);

    public abstract int getLoadOpcode();

    public abstract int getConstOpcode();

    /**
     * Returns a string representation of this type.
     *
     * @return the descriptor of this type.
     */
    @Override
    public final String toString() {
        return getDescriptor();
    }
}
