/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.classes;

import static mockit.asm.jvmConstants.ConstantPoolTypes.INVOKE_DYNAMIC;

import edu.umd.cs.findbugs.annotations.NonNull;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.BootstrapMethodItem;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.constantPool.DynamicItem;
import mockit.asm.constantPool.Item;
import mockit.asm.constantPool.MethodHandleItem;
import mockit.asm.util.ByteVector;
import mockit.asm.util.MethodHandle;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Generates the "BootstrapMethods" attribute in a class file being written by a {@link ClassWriter}.
 */
final class BootstrapMethodsWriter extends AttributeWriter {
    @NonNull
    private final ByteVector bootstrapMethods;
    @NonNegative
    private final int bootstrapMethodsCount;
    @NonNegative
    private final int bsmStartCodeIndex;

    BootstrapMethodsWriter(@NonNull ConstantPoolGeneration cp, @NonNull ClassReader cr) {
        super(cp);

        int attrSize = cr.readInt();
        bootstrapMethods = new ByteVector(attrSize + 62);
        bootstrapMethodsCount = cr.readUnsignedShort();

        bsmStartCodeIndex = cr.codeIndex;
        bootstrapMethods.putByteArray(cr.code, bsmStartCodeIndex, attrSize - 2);
    }

    /**
     * Copies the bootstrap method data from the given {@link ClassReader}.
     */
    void copyBootstrapMethods(@NonNull ClassReader cr, @NonNull Item[] items) {
        int previousCodeIndex = cr.codeIndex;
        cr.codeIndex = bsmStartCodeIndex;

        for (int bsmIndex = 0, bsmCount = bootstrapMethodsCount; bsmIndex < bsmCount; bsmIndex++) {
            copyBootstrapMethod(cr, items, bsmIndex);
        }

        cr.codeIndex = previousCodeIndex;
    }

    private void copyBootstrapMethod(@NonNull ClassReader cr, @NonNull Item[] items, @NonNegative int bsmIndex) {
        int position = cr.codeIndex - bsmStartCodeIndex;
        MethodHandle bsm = cr.readMethodHandle();
        int hashCode = bsm.hashCode();

        for (int bsmArgCount = cr.readUnsignedShort(); bsmArgCount > 0; bsmArgCount--) {
            Object bsmArg = cr.readConstItem();
            hashCode ^= bsmArg.hashCode();
        }

        BootstrapMethodItem item = new BootstrapMethodItem(bsmIndex, position, hashCode);
        item.setNext(items);
    }

    /**
     * Adds an invokedynamic reference to the constant pool of the class being built. Does nothing if the constant pool
     * already contains a similar item.
     *
     * @param name
     *            name of the invoked method
     * @param desc
     *            descriptor of the invoke method
     * @param bsm
     *            the bootstrap method
     * @param bsmArgs
     *            the bootstrap method constant arguments
     *
     * @return a new or an already existing invokedynamic type reference item
     */
    @NonNull
    DynamicItem addInvokeDynamicReference(@NonNull String name, @NonNull String desc, @NonNull MethodHandle bsm,
            @NonNull Object... bsmArgs) {
        ByteVector methods = bootstrapMethods;
        int position = methods.getLength(); // record current position

        MethodHandleItem methodHandleItem = cp.newMethodHandleItem(bsm);
        methods.putShort(methodHandleItem.index);

        int argsLength = bsmArgs.length;
        methods.putShort(argsLength);

        int hashCode = bsm.hashCode();
        hashCode = putBSMArgs(hashCode, bsmArgs);
        hashCode &= 0x7FFFFFFF;

        methods.setLength(position); // revert to old position

        BootstrapMethodItem bsmItem = getBSMItem(hashCode);
        return cp.createDynamicItem(INVOKE_DYNAMIC, name, desc, bsmItem.index);
    }

    private int putBSMArgs(int hashCode, @NonNull Object[] bsmArgs) {
        for (Object bsmArg : bsmArgs) {
            hashCode ^= bsmArg.hashCode();

            Item constItem = cp.newConstItem(bsmArg);
            bootstrapMethods.putShort(constItem.index);
        }

        return hashCode;
    }

    @NonNull
    private BootstrapMethodItem getBSMItem(@NonNegative int hashCode) {
        Item item = cp.getItem(hashCode);

        while (item != null) {
            if (item instanceof BootstrapMethodItem && item.getHashCode() == hashCode) {
                return (BootstrapMethodItem) item;
            }

            item = item.getNext();
        }

        throw new IllegalStateException("BootstrapMethodItem not found for hash code " + hashCode);
    }

    @NonNegative
    @Override
    public int getSize() {
        return 8 + bootstrapMethods.getLength();
    }

    @Override
    public void put(@NonNull ByteVector out) {
        setAttribute("BootstrapMethods");
        put(out, 2 + bootstrapMethods.getLength());
        out.putShort(bootstrapMethodsCount);
        out.putByteVector(bootstrapMethods);
    }
}
