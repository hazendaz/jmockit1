package mockit.asm.classes;

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

import mockit.asm.constantPool.ClassMemberItem;
import mockit.asm.constantPool.DoubleItem;
import mockit.asm.constantPool.DynamicItem;
import mockit.asm.constantPool.FloatItem;
import mockit.asm.constantPool.IntItem;
import mockit.asm.constantPool.Item;
import mockit.asm.constantPool.LongItem;
import mockit.asm.constantPool.MethodHandleItem;
import mockit.asm.constantPool.ModuleItem;
import mockit.asm.constantPool.NameAndTypeItem;
import mockit.asm.constantPool.PackageItem;
import mockit.asm.constantPool.StringItem;
import mockit.asm.util.MethodHandle;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Copies the constant pool data from a {@link ClassReader} into a {@link ClassWriter}.
 */
final class ConstantPoolCopying {
    @NonNull
    private final ClassReader source;
    @NonNull
    private final ClassWriter destination;
    @NonNull
    private final Item[] newItems;
    @NonNegative
    private int itemIndex;

    ConstantPoolCopying(@NonNull ClassReader source, @NonNull ClassWriter destination) {
        this.source = source;
        this.destination = destination;
        newItems = new Item[source.items.length];
    }

    void copyPool(@Nullable BootstrapMethodsWriter bootstrapMethods) {
        if (bootstrapMethods != null) {
            bootstrapMethods.copyBootstrapMethods(source, newItems);
        }

        int[] items = source.items;
        int itemCount = items.length;

        for (itemIndex = 1; itemIndex < itemCount; itemIndex++) {
            source.codeIndex = items[itemIndex] - 1;
            int itemType = source.readSignedByte();

            Item newItem = copyItem(itemType);
            newItem.setNext(newItems);
        }

        int off = items[1] - 1;
        destination.getConstantPoolGeneration().copy(source.code, off, source.header, newItems);
    }

    @NonNull
    @SuppressWarnings("OverlyComplexMethod")
    private Item copyItem(int itemType) {
        switch (itemType) {
            case UTF8:
                return copyUTF8Item();
            case INTEGER:
                return copyIntItem();
            case FLOAT:
                return copyFloatItem();
            case LONG:
                return copyLongItem();
            case DOUBLE:
                return copyDoubleItem();
            case FIELD_REF:
            case METHOD_REF:
            case IMETHOD_REF:
                return copyFieldOrMethodReferenceItem(itemType);
            case NAME_TYPE:
                return copyNameAndTypeItem();
            case METHOD_HANDLE:
                return copyHandleItem();
            case DYNAMIC:
            case INVOKE_DYNAMIC:
                return copyDynamicItem(itemType);
            case STRING:
            case CLASS:
            case METHOD_TYPE:
                return copyNameReferenceItem(itemType);
            case MODULE:
                return copyModule();
            case PACKAGE:
                return copyPackage();
            default:
                throw new IllegalArgumentException("Unknown CP type, cannot copy: " + itemType);
        }
    }

    @NonNull
    private Item copyIntItem() {
        int itemValue = source.readInt();
        IntItem item = new IntItem(itemIndex);
        item.setValue(itemValue);
        return item;
    }

    @NonNull
    private Item copyLongItem() {
        long itemValue = source.readLong();
        LongItem item = new LongItem(itemIndex);
        item.setValue(itemValue);
        itemIndex++;
        return item;
    }

    @NonNull
    private Item copyFloatItem() {
        float itemValue = source.readFloat();
        FloatItem item = new FloatItem(itemIndex);
        item.set(itemValue);
        return item;
    }

    @NonNull
    private Item copyDoubleItem() {
        double itemValue = source.readDouble();
        DoubleItem item = new DoubleItem(itemIndex);
        item.set(itemValue);
        itemIndex++;
        return item;
    }

    @NonNull
    private Item copyUTF8Item() {
        String strVal = source.readString(itemIndex);
        return new StringItem(itemIndex, UTF8, strVal);
    }

    @NonNull
    private Item copyNameReferenceItem(int type) {
        String strVal = source.readNonnullUTF8();
        return new StringItem(itemIndex, type, strVal);
    }

    @NonNull
    private Item copyNameAndTypeItem() {
        String name = source.readNonnullUTF8();
        String type = source.readNonnullUTF8();

        NameAndTypeItem item = new NameAndTypeItem(itemIndex);
        item.set(name, type);
        return item;
    }

    @NonNull
    private Item copyFieldOrMethodReferenceItem(int type) {
        String classDesc = source.readNonnullClass();
        int nameCodeIndex = source.readItem();
        String methodName = source.readNonnullUTF8(nameCodeIndex);
        String methodDesc = source.readNonnullUTF8(nameCodeIndex + 2);

        ClassMemberItem item = new ClassMemberItem(itemIndex);
        item.set(type, classDesc, methodName, methodDesc);
        return item;
    }

    @NonNull
    private Item copyHandleItem() {
        int tag = source.readUnsignedByte();

        int fieldOrMethodRef = source.readItem();
        int nameCodeIndex = source.readItem(fieldOrMethodRef + 2);

        String classDesc = source.readNonnullClass(fieldOrMethodRef);
        String name = source.readNonnullUTF8(nameCodeIndex);
        String desc = source.readNonnullUTF8(nameCodeIndex + 2);

        MethodHandle handle = new MethodHandle(tag, classDesc, name, desc);
        MethodHandleItem item = new MethodHandleItem(itemIndex);
        item.set(handle);
        return item;
    }

    @NonNull
    private Item copyDynamicItem(int type) {
        int bsmIndex = source.readUnsignedShort();
        int nameCodeIndex = source.readItem();
        String name = source.readNonnullUTF8(nameCodeIndex);
        String desc = source.readNonnullUTF8(nameCodeIndex + 2);

        DynamicItem item = new DynamicItem(itemIndex);
        item.set(type, name, desc, bsmIndex);
        return item;
    }

    @NonNull
    private Item copyModule() {
        int nameIndex = source.readItem();
        String name = source.readNonnullUTF8(nameIndex);
        return new ModuleItem(itemIndex, MODULE, name);
    }

    @NonNull
    private Item copyPackage() {
        int nameIndex = source.readItem();
        String name = source.readNonnullUTF8(nameIndex);
        return new PackageItem(itemIndex, PACKAGE, name);
    }
}
