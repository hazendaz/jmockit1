package mockit.asm.util;

import static mockit.asm.jvmConstants.ConstantPoolTypes.CLASS;
import static mockit.asm.jvmConstants.ConstantPoolTypes.DOUBLE;
import static mockit.asm.jvmConstants.ConstantPoolTypes.DYNAMIC;
import static mockit.asm.jvmConstants.ConstantPoolTypes.FIELD_REF;
import static mockit.asm.jvmConstants.ConstantPoolTypes.FLOAT;
import static mockit.asm.jvmConstants.ConstantPoolTypes.IMETHOD_REF;
import static mockit.asm.jvmConstants.ConstantPoolTypes.INTEGER;
import static mockit.asm.jvmConstants.ConstantPoolTypes.INVOKE_DYNAMIC;
import static mockit.asm.jvmConstants.ConstantPoolTypes.LONG;
import static mockit.asm.jvmConstants.ConstantPoolTypes.METHOD_HANDLE;
import static mockit.asm.jvmConstants.ConstantPoolTypes.METHOD_REF;
import static mockit.asm.jvmConstants.ConstantPoolTypes.METHOD_TYPE;
import static mockit.asm.jvmConstants.ConstantPoolTypes.MODULE;
import static mockit.asm.jvmConstants.ConstantPoolTypes.NAME_TYPE;
import static mockit.asm.jvmConstants.ConstantPoolTypes.PACKAGE;
import static mockit.asm.jvmConstants.ConstantPoolTypes.STRING;
import static mockit.asm.jvmConstants.ConstantPoolTypes.UTF8;

import mockit.asm.constantPool.DynamicItem;
import mockit.asm.jvmConstants.ConstantPoolTypes;
import mockit.asm.types.JavaType;
import mockit.asm.types.MethodType;
import mockit.asm.types.ReferenceType;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BytecodeReader {
    /**
     * The class to be parsed. <em>The content of this array must not be modified.</em>
     */
    @NonNull
    public final byte[] code;

    /**
     * The start index of each constant pool item in {@link #code}, plus one. The one byte offset skips the constant
     * pool item tag that indicates its type.
     */
    @NonNull
    public final int[] items;

    /**
     * The String objects corresponding to the CONSTANT_Utf8 items. This cache avoids multiple parsing of a given
     * CONSTANT_Utf8 constant pool item, which GREATLY improves performances (by a factor 2 to 3). This caching strategy
     * could be extended to all constant pool items, but its benefit would not be so great for these items (because they
     * are much less expensive to parse than CONSTANT_Utf8 items).
     */
    @NonNull
    private final String[] strings;

    /**
     * The buffer used to read strings.
     */
    @NonNull
    private final char[] buf;

    /**
     * The next index at {@link #code} to be read.
     */
    @NonNegative
    public int codeIndex;

    protected BytecodeReader(@NonNull byte[] code) {
        this.code = code;
        codeIndex = 8;

        int itemCount = readUnsignedShort();
        items = new int[itemCount];
        strings = new String[itemCount];

        int maxStringSize = readConstantPoolItems();
        buf = new char[maxStringSize];
    }

    @NonNegative
    private int readConstantPoolItems() {
        int maxStringSize = 0;

        for (int itemIndex = 1; itemIndex < items.length; itemIndex++) {
            int itemType = readSignedByte();
            items[itemIndex] = codeIndex;
            int itemSize = getItemSize(itemType);

            if (itemType == LONG || itemType == DOUBLE) {
                itemIndex++;
            } else if (itemType == UTF8 && itemSize > maxStringSize) {
                maxStringSize = itemSize;
            }

            codeIndex += itemSize - 1;
        }

        return maxStringSize;
    }

    @NonNegative
    private int getItemSize(int itemType) {
        switch (itemType) {
            case FIELD_REF:
            case METHOD_REF:
            case IMETHOD_REF:
            case INTEGER:
            case FLOAT:
            case NAME_TYPE:
            case DYNAMIC:
            case INVOKE_DYNAMIC:
                return 5;
            case LONG:
            case DOUBLE:
                return 9;
            case UTF8:
                return 3 + readUnsignedShort(codeIndex);
            case METHOD_HANDLE:
                return 4;
            case MODULE:
            case PACKAGE:
            case CLASS:
            case STRING:
            case METHOD_TYPE:
                return 3;
            default:
                throw new IllegalArgumentException("Unknown item type, cannot determine size: " + itemType);
        }
    }

    protected BytecodeReader(@NonNull BytecodeReader another) {
        code = another.code;
        items = another.items;
        strings = another.strings;
        buf = another.buf;
        codeIndex = another.codeIndex;
    }

    /**
     * Reads an unsigned <code>byte</code> value in {@link #code}, incrementing {@link #codeIndex} by 1.
     *
     * @return the int
     */
    @NonNegative
    public final int readUnsignedByte() {
        return code[codeIndex++] & 0xFF;
    }

    /**
     * Reads an unsigned byte value in {@link #code}.
     *
     * @param u1CodeIndex
     *            the start index of the value to be read in {@link #code}
     *
     * @return the int
     */
    @NonNegative
    protected final int readUnsignedByte(@NonNegative int u1CodeIndex) {
        return code[u1CodeIndex] & 0xFF;
    }

    /**
     * Reads a signed <code>byte</code> value in {@link #code}, incrementing {@link #codeIndex} by 1.
     *
     * @return the int
     */
    public final int readSignedByte() {
        return code[codeIndex++];
    }

    protected final char readChar(@NonNegative int s4CodeIndex) {
        return (char) readInt(s4CodeIndex);
    }

    protected final boolean readBoolean(@NonNegative int s4CodeIndex) {
        return readInt(s4CodeIndex) != 0;
    }

    /**
     * Reads an unsigned short value in {@link #code}, incrementing {@link #codeIndex} by 2.
     *
     * @return the int
     */
    @NonNegative
    public final int readUnsignedShort() {
        byte[] b = code;
        int i = codeIndex;
        int byte0 = (b[i] & 0xFF) << 8;
        i++;
        int byte1 = b[i] & 0xFF;
        i++;
        codeIndex = i;
        return byte0 | byte1;
    }

    /**
     * Reads an unsigned short value in {@link #code}.
     *
     * @param u2CodeIndex
     *            the start index of the value to be read in {@link #code}
     *
     * @return the int
     */
    @NonNegative
    protected final int readUnsignedShort(@NonNegative int u2CodeIndex) {
        byte[] b = code;
        int byte0 = (b[u2CodeIndex] & 0xFF) << 8;
        int byte1 = b[u2CodeIndex + 1] & 0xFF;
        return byte0 | byte1;
    }

    /**
     * Reads a signed <code>short</code> value in {@link #code}, incrementing {@link #codeIndex} by 2.
     *
     * @return the short
     */
    protected final short readShort() {
        // noinspection NumericCastThatLosesPrecision
        return (short) readUnsignedShort();
    }

    /**
     * Reads a signed short value in {@link #code}.
     *
     * @param u2CodeIndex
     *            the start index of the value to be read in {@link #code}
     *
     * @return the short
     */
    protected final short readShort(@NonNegative int u2CodeIndex) {
        // noinspection NumericCastThatLosesPrecision
        return (short) readUnsignedShort(u2CodeIndex);
    }

    /**
     * Reads a signed <code>int</code> value in {@link #code}, incrementing {@link #codeIndex} by 4.
     *
     * @return the int
     */
    public final int readInt() {
        byte[] b = code;
        int i = codeIndex;
        int byte0 = (b[i] & 0xFF) << 24;
        i++;
        int byte1 = (b[i] & 0xFF) << 16;
        i++;
        int byte2 = (b[i] & 0xFF) << 8;
        i++;
        int byte3 = b[i] & 0xFF;
        i++;
        codeIndex = i;
        return byte0 | byte1 | byte2 | byte3;
    }

    /**
     * Reads a signed int value in {@link #code}.
     *
     * @param s4CodeIndex
     *            the start index of the value to be read in {@link #code}
     *
     * @return the int
     */
    protected final int readInt(@NonNegative int s4CodeIndex) {
        byte[] b = code;
        return (b[s4CodeIndex] & 0xFF) << 24 | (b[s4CodeIndex + 1] & 0xFF) << 16 | (b[s4CodeIndex + 2] & 0xFF) << 8
                | b[s4CodeIndex + 3] & 0xFF;
    }

    /**
     * Reads a signed long value in {@link #code}, incrementing {@link #codeIndex} by 8.
     *
     * @return the long
     */
    public final long readLong() {
        long l1 = readInt();
        long l0 = readInt() & 0xFFFFFFFFL;
        return l1 << 32 | l0;
    }

    /**
     * Reads a signed long value in {@link #code}.
     *
     * @param s8CodeIndex
     *            the start index of the value to be read in {@link #code}
     *
     * @return the long
     */
    protected final long readLong(@NonNegative int s8CodeIndex) {
        long l1 = readInt(s8CodeIndex);
        long l0 = readInt(s8CodeIndex + 4) & 0xFFFFFFFFL;
        return l1 << 32 | l0;
    }

    public final double readDouble() {
        long bits = readLong();
        return Double.longBitsToDouble(bits);
    }

    protected final double readDouble(@NonNegative int s8CodeIndex) {
        long bits = readLong(s8CodeIndex);
        return Double.longBitsToDouble(bits);
    }

    public final float readFloat() {
        int bits = readInt();
        return Float.intBitsToFloat(bits);
    }

    protected final float readFloat(@NonNegative int s4CodeIndex) {
        int bits = readInt(s4CodeIndex);
        return Float.intBitsToFloat(bits);
    }

    /**
     * Reads an UTF8 string in {@link #code}.
     *
     * @param itemIndex
     *            index in {@link #items} for the UTF8 string to be read
     *
     * @return the string
     */
    @NonNull
    @SuppressWarnings("CharUsedInArithmeticContext")
    private String readUTF(@NonNegative int itemIndex) {
        int startIndex = items[itemIndex];
        int utfLen = readUnsignedShort(startIndex);
        startIndex += 2;
        int endIndex = startIndex + utfLen;
        int strLen = 0;
        int st = 0;
        @SuppressWarnings("QuestionableName")
        char cc = 0;

        while (startIndex < endIndex) {
            int c = code[startIndex];
            startIndex++;

            if (st == 0) {
                c &= 0xFF;

                if (c < 0x80) { // 0xxxxxxx
                    buf[strLen] = (char) c;
                    strLen++;
                } else if (c < 0xE0 && c > 0xBF) { // 110x xxxx 10xx xxxx
                    cc = (char) (c & 0x1F);
                    st = 1;
                } else { // 1110 xxxx 10xx xxxx 10xx xxxx
                    cc = (char) (c & 0x0F);
                    st = 2;
                }
            } else if (st == 1) { // byte 2 of 2-byte char or byte 3 of 3-byte char
                buf[strLen] = (char) (cc << 6 | c & 0x3F);
                strLen++;
                st = 0;
            } else { // byte 2 of 3-byte char
                cc = (char) (cc << 6 | c & 0x3F);
                st = 1;
            }
        }

        return new String(buf, 0, strLen);
    }

    /**
     * Reads an UTF8 string constant pool item in {@link #code}, incrementing {@link #codeIndex} by 2.
     *
     * @return the String corresponding to the UTF8 item, or <code>null</code> if {@link #codeIndex} points to an item
     *         whose value is zero
     */
    @Nullable
    protected final String readUTF8() {
        int itemIndex = readUnsignedShort();

        if (itemIndex == 0) {
            return null;
        }

        return readString(itemIndex);
    }

    /**
     * Reads an UTF8 string constant pool item in {@link #code}.
     *
     * @param u2CodeIndex
     *            the index of an unsigned short value in {@link #code}, whose value is the index of an UTF8 constant
     *            pool item
     *
     * @return the String corresponding to the UTF8 item, or <code>null</code> if index is zero or points to an item
     *         whose value is zero
     */
    @Nullable
    protected final String readUTF8(@NonNegative int u2CodeIndex) {
        if (u2CodeIndex == 0) {
            return null;
        }

        int itemIndex = readUnsignedShort(u2CodeIndex);

        if (itemIndex == 0) {
            return null;
        }

        return readString(itemIndex);
    }

    /**
     * Reads the index of an UTF8 item in {@link #code}, incrementing {@link #codeIndex} by 2.
     *
     * @return the UTF8 string found in {@link #strings} at that index
     */
    @NonNull
    public final String readNonnullUTF8() {
        int itemIndex = readUnsignedShort();
        return readString(itemIndex);
    }

    /**
     * Reads the index of an UTF8 item in {@link #code}.
     *
     * @param u2CodeIndex
     *            the u 2 code index
     *
     * @return the UTF8 string found in {@link #strings} at that index
     */
    @NonNull
    public final String readNonnullUTF8(@NonNegative int u2CodeIndex) {
        int itemIndex = readUnsignedShort(u2CodeIndex);
        return readString(itemIndex);
    }

    /**
     * Reads a string in {@link #strings} at the given index.
     *
     * @param itemIndex
     *            the item index
     *
     * @return the string
     */
    @NonNull
    public final String readString(@NonNegative int itemIndex) {
        String cachedString = strings[itemIndex];

        if (cachedString != null) {
            return cachedString;
        }

        String newString = readUTF(itemIndex);
        strings[itemIndex] = newString;
        return newString;
    }

    /**
     * Reads the index of a constant item in {@link #code}, incrementing {@link #codeIndex} by 2.
     *
     * @return the UTF8 string found in {@link #strings} at that index
     */
    @NonNull
    public final Object readConstItem() {
        int constIndex = readUnsignedShort();
        return readConst(constIndex);
    }

    @NonNull
    protected final Object readConstItem(@NonNegative int u2CodeIndex) {
        int itemIndex = readUnsignedShort(u2CodeIndex);
        return readConst(itemIndex);
    }

    /**
     * Reads a numeric or string constant pool item in {@link #code}.
     *
     * @param itemIndex
     *            the index of a constant pool item
     *
     * @return the {@link Integer}, {@link Float}, {@link Long}, {@link Double}, {@link String}, {@link JavaType} or
     *         {@link MethodHandle} corresponding to the given constant pool item
     */
    @NonNull
    protected final Object readConst(@NonNegative int itemIndex) {
        int constCodeIndex = items[itemIndex];
        byte itemType = code[constCodeIndex - 1];

        switch (itemType) {
            case INTEGER:
                return readInt(constCodeIndex);
            case FLOAT:
                return readFloat(constCodeIndex);
            case LONG:
                return readLong(constCodeIndex);
            case DOUBLE:
                return readDouble(constCodeIndex);
            case STRING:
                return readNonnullUTF8(constCodeIndex);
            case CLASS:
                String typeDesc = readNonnullUTF8(constCodeIndex);
                return ReferenceType.createFromInternalName(typeDesc);
            case METHOD_TYPE:
                String methodDesc = readNonnullUTF8(constCodeIndex);
                return MethodType.create(methodDesc);
            case DYNAMIC: {
                int bsmStartIndex = readUnsignedShort(constCodeIndex);
                int nameIndex = readItem(constCodeIndex + 2);
                String name = readNonnullUTF8(nameIndex);
                String desc = readNonnullUTF8(nameIndex + 2);
                DynamicItem dynamicItem = new DynamicItem(itemIndex);
                dynamicItem.set(ConstantPoolTypes.DYNAMIC, name, desc, bsmStartIndex);
                return dynamicItem;
            }
            case METHOD_HANDLE:
                return readMethodHandle(constCodeIndex);
            default:
                throw new IllegalArgumentException("Unknown const item type code: " + itemType);
        }
    }

    @NonNull
    public final MethodHandle readMethodHandle() {
        int itemIndex = readUnsignedShort();
        return readMethodHandle(items[itemIndex]);
    }

    @NonNull
    protected final MethodHandle readMethodHandleItem(@NonNegative int bsmCodeIndex) {
        int itemIndex = readUnsignedShort(bsmCodeIndex);
        return readMethodHandle(items[itemIndex]);
    }

    @NonNull
    private MethodHandle readMethodHandle(@NonNegative int bsmCodeIndex) {
        int tag = readUnsignedByte(bsmCodeIndex);
        if (tag < MethodHandle.Tag.TAG_GETFIELD || tag > MethodHandle.Tag.TAG_INVOKEINTERFACE) {
            throw new IllegalArgumentException("Illegal method-handle tag: " + tag);
        }

        int classIndex = readItem(bsmCodeIndex + 1);
        String owner = readNonnullClass(classIndex);

        int nameIndex = readItem(classIndex + 2);
        String name = readNonnullUTF8(nameIndex);
        String desc = readNonnullUTF8(nameIndex + 2);

        return new MethodHandle(tag, owner, name, desc);
    }

    /**
     * Reads the class name from the constant pool, incrementing {@link #codeIndex} by 2.
     *
     * @return the string
     */
    @Nullable
    protected final String readClass() {
        int itemCodeIndex = readItem();
        return readUTF8(itemCodeIndex);
    }

    /**
     * Reads a class descriptor in {@link #code}, incrementing {@link #codeIndex} by 2.
     *
     * @return the string
     */
    @NonNull
    public final String readNonnullClass() {
        int itemCodeIndex = readItem();
        return readNonnullUTF8(itemCodeIndex);
    }

    @NonNull
    public final String readNonnullClass(@NonNegative int u2CodeIndex) {
        int itemCodeIndex = readItem(u2CodeIndex);
        return readNonnullUTF8(itemCodeIndex);
    }

    /**
     * Reads an item index in {@link #code}, incrementing {@link #codeIndex} by 2.
     *
     * @return the item at that index in {@link #items}
     */
    @NonNegative
    public final int readItem() {
        int itemIndex = readUnsignedShort();
        return items[itemIndex];
    }

    @NonNegative
    public final int readItem(@NonNegative int u2CodeIndex) {
        int itemIndex = readUnsignedShort(u2CodeIndex);
        return items[itemIndex];
    }
}
