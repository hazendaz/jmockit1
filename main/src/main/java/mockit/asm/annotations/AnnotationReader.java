package mockit.asm.annotations;

import java.lang.reflect.Array;

import javax.annotation.Nullable;

import mockit.asm.types.JavaType;
import mockit.asm.types.PrimitiveType;
import mockit.asm.util.BytecodeReader;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class AnnotationReader extends BytecodeReader {
    public AnnotationReader(@NonNull BytecodeReader br) {
        super(br);
    }

    /**
     * Reads the values of a named annotation and makes the given visitor visit them.
     *
     * @param startingCodeIndex
     *            the start offset in {@link #code} of the values to be read (including the unsigned short that gives
     *            the number of values)
     * @param av
     *            the visitor that must visit the values
     *
     * @return the end offset of the annotation values
     */
    @NonNegative
    public int readNamedAnnotationValues(@NonNegative int startingCodeIndex, @Nullable AnnotationVisitor av) {
        codeIndex = startingCodeIndex;
        readAnnotationValues(true, av);
        return codeIndex;
    }

    private void readAnnotationValues(boolean named, @Nullable AnnotationVisitor av) {
        int valueCount = readUnsignedShort();
        readAnnotationValues(valueCount, named, av);
    }

    private void readAnnotationValues(@NonNegative int valueCount, boolean named, @Nullable AnnotationVisitor av) {
        while (valueCount > 0) {
            String name = named ? readNonnullUTF8() : null;
            readAnnotationValue(name, av);
            valueCount--;
        }

        if (av != null) {
            av.visitEnd();
        }
    }

    private void readAnnotationValue(@Nullable String name, @Nullable AnnotationVisitor av) {
        int typeCode = readUnsignedByte();

        if (av == null) {
            readAnnotationValue(typeCode);
        } else {
            Object value = readAnnotationValueIfPrimitiveOrString(typeCode);

            if (value != null) {
                av.visit(name, value);
            } else {
                // noinspection SwitchStatementWithoutDefaultBranch
                switch (typeCode) {
                    case 'e':
                        readEnumConstValue(name, av);
                        break; // enum_const_value
                    case 'c':
                        readClassInfo(name, av);
                        break; // class_info
                    case '@':
                        readNestedAnnotation(name, av);
                        break; // annotation_value
                    case '[':
                        readArrayValue(name, av); // array_value
                }
            }
        }
    }

    private void readAnnotationValue(@NonNegative int typeCode) {
        switch (typeCode) {
            case 'e':
                codeIndex += 4;
                break; // enum_const_value
            case '@':
                codeIndex += 2;
                readAnnotationValues(true, null);
                break; // annotation_value
            case '[':
                readAnnotationValues(false, null);
                break;
            default:
                codeIndex += 2;
        }
    }

    @Nullable
    @SuppressWarnings({ "NumericCastThatLosesPrecision", "SwitchStatementWithoutDefaultBranch" })
    private Object readAnnotationValueIfPrimitiveOrString(@NonNegative int typeCode) {
        switch (typeCode) {
            case 'I':
            case 'J':
            case 'F':
            case 'D':
                return readConstItem(); // CONSTANT_Integer/Long/Float/Double
            case 'B':
                return (byte) readValueOfOneOrTwoBytes(); // CONSTANT_Byte
            case 'Z':
                return readValueOfOneOrTwoBytes() != 0; // CONSTANT_Boolean
            case 'S':
                return (short) readValueOfOneOrTwoBytes(); // CONSTANT_Short
            case 'C':
                return (char) readValueOfOneOrTwoBytes(); // CONSTANT_Char
            case 's':
                return readNonnullUTF8(); // CONSTANT_Utf8
        }

        return null;
    }

    private int readValueOfOneOrTwoBytes() {
        int itemIndex = readUnsignedShort();
        int valueCodeIndex = items[itemIndex];
        return readInt(valueCodeIndex);
    }

    private void readEnumConstValue(@Nullable String name, @NonNull AnnotationVisitor av) {
        String enumDesc = readNonnullUTF8();
        String enumValue = readNonnullUTF8();
        av.visitEnum(name, enumDesc, enumValue);
    }

    private void readClassInfo(@Nullable String name, @NonNull AnnotationVisitor av) {
        String typeDesc = readNonnullUTF8();
        JavaType value = JavaType.getType(typeDesc);
        av.visit(name, value);
    }

    private void readNestedAnnotation(@Nullable String name, @NonNull AnnotationVisitor av) {
        String desc = readNonnullUTF8();
        AnnotationVisitor nestedVisitor = av.visitAnnotation(name, desc);
        readAnnotationValues(true, nestedVisitor);
    }

    private void readArrayValue(@Nullable String name, @NonNull AnnotationVisitor av) {
        int valueCount = readUnsignedShort();

        if (valueCount == 0) {
            AnnotationVisitor arrayVisitor = av.visitArray(name);
            arrayVisitor.visitEnd();
            return;
        }

        int typeCode = readUnsignedByte();
        PrimitiveType primitiveElementType = PrimitiveType.getPrimitiveType(typeCode);

        if (primitiveElementType == null) {
            AnnotationVisitor arrayVisitor = av.visitArray(name);
            codeIndex--;
            readAnnotationValues(valueCount, false, arrayVisitor);
            return;
        }

        Class<?> elementType = primitiveElementType.getType();
        Object array = Array.newInstance(elementType, valueCount);
        fillArrayElements(valueCount, typeCode, array);
        av.visit(name, array);
        codeIndex--;
    }

    private void fillArrayElements(@NonNegative int length, @NonNegative int typeCode, @NonNull Object array) {
        for (int i = 0; i < length; i++) {
            int itemIndex = readUnsignedShort();
            int index = items[itemIndex];
            Object value = getArrayElementValue(typeCode, index);
            Array.set(array, i, value);
            codeIndex++;
        }
    }

    @NonNull
    private Object getArrayElementValue(@NonNegative int typeCode, @NonNegative int valueCodeIndex) {
        switch (typeCode) {
            case 'Z':
                return readBoolean(valueCodeIndex);
            case 'C':
                return readChar(valueCodeIndex);
            case 'B':
                return readUnsignedByte(valueCodeIndex);
            case 'S':
                return readShort(valueCodeIndex);
            case 'F':
                return readFloat(valueCodeIndex);
            case 'D':
                return readDouble(valueCodeIndex);
            case 'J':
                return readLong(valueCodeIndex);
            default:
                return readInt(valueCodeIndex);
        }
    }
}
