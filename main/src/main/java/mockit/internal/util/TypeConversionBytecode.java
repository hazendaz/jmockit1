/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import static mockit.asm.jvmConstants.Opcodes.ASTORE;
import static mockit.asm.jvmConstants.Opcodes.CHECKCAST;
import static mockit.asm.jvmConstants.Opcodes.INVOKESTATIC;
import static mockit.asm.jvmConstants.Opcodes.INVOKEVIRTUAL;
import static mockit.asm.jvmConstants.Opcodes.POP;

import javax.annotation.Nonnull;

import mockit.asm.methods.MethodVisitor;
import mockit.asm.types.JavaType;
import mockit.asm.types.PrimitiveType;
import mockit.asm.types.ReferenceType;

import org.checkerframework.checker.index.qual.NonNegative;

public final class TypeConversionBytecode {
    private TypeConversionBytecode() {
    }

    public static void generateCastToObject(@Nonnull MethodVisitor mv, @Nonnull JavaType type) {
        if (type instanceof PrimitiveType) {
            String wrapperTypeDesc = ((PrimitiveType) type).getWrapperTypeDesc();
            String desc = '(' + type.getDescriptor() + ")L" + wrapperTypeDesc + ';';

            mv.visitMethodInsn(INVOKESTATIC, wrapperTypeDesc, "valueOf", desc, false);
        }
    }

    public static void generateCastFromObject(@Nonnull MethodVisitor mv, @Nonnull JavaType toType) {
        if (toType instanceof PrimitiveType) {
            PrimitiveType primitiveType = (PrimitiveType) toType;

            if (primitiveType.getType() == void.class) {
                mv.visitInsn(POP);
            } else {
                generateTypeCheck(mv, primitiveType);
                generateUnboxing(mv, primitiveType);
            }
        } else {
            generateTypeCheck(mv, toType);
        }
    }

    private static void generateTypeCheck(@Nonnull MethodVisitor mv, @Nonnull JavaType toType) {
        String typeDesc;

        if (toType instanceof ReferenceType) {
            typeDesc = ((ReferenceType) toType).getInternalName();
        } else {
            typeDesc = ((PrimitiveType) toType).getWrapperTypeDesc();
        }

        mv.visitTypeInsn(CHECKCAST, typeDesc);
    }

    private static void generateUnboxing(@Nonnull MethodVisitor mv, @Nonnull PrimitiveType primitiveType) {
        String owner = primitiveType.getWrapperTypeDesc();
        String methodName = primitiveType.getClassName() + "Value";
        String methodDesc = "()" + primitiveType.getTypeCode();

        mv.visitMethodInsn(INVOKEVIRTUAL, owner, methodName, methodDesc, false);
    }

    public static void generateCastOrUnboxing(@Nonnull MethodVisitor mv, @Nonnull JavaType parameterType,
            @NonNegative int opcode) {
        if (opcode == ASTORE) {
            generateTypeCheck(mv, parameterType);
            return;
        }

        String typeDesc = ((ReferenceType) parameterType).getInternalName();
        mv.visitTypeInsn(CHECKCAST, typeDesc);

        PrimitiveType primitiveType = PrimitiveType.getCorrespondingPrimitiveTypeIfWrapperType(typeDesc);
        assert primitiveType != null;

        generateUnboxing(mv, primitiveType);
    }

    public static boolean isPrimitiveWrapper(@Nonnull String typeDesc) {
        return PrimitiveType.getCorrespondingPrimitiveTypeIfWrapperType(typeDesc) != null;
    }

    public static boolean isBoxing(@Nonnull String owner, @Nonnull String name, @Nonnull String desc) {
        return desc.charAt(2) == ')' && "valueOf".equals(name) && isPrimitiveWrapper(owner);
    }

    public static boolean isUnboxing(@NonNegative int opcode, @Nonnull String owner, @Nonnull String desc) {
        return opcode == INVOKEVIRTUAL && desc.charAt(1) == ')' && isPrimitiveWrapper(owner);
    }
}
