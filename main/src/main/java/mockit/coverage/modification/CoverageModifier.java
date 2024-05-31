/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.modification;

import static mockit.asm.jvmConstants.Access.ABSTRACT;
import static mockit.asm.jvmConstants.Access.ANNOTATION;
import static mockit.asm.jvmConstants.Access.ENUM;
import static mockit.asm.jvmConstants.Access.FINAL;
import static mockit.asm.jvmConstants.Access.INTERFACE;
import static mockit.asm.jvmConstants.Access.STATIC;
import static mockit.asm.jvmConstants.Access.SUPER;
import static mockit.asm.jvmConstants.Access.SYNTHETIC;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import mockit.asm.classes.ClassInfo;
import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassWriter;
import mockit.asm.classes.WrappingClassVisitor;
import mockit.asm.fields.FieldVisitor;
import mockit.asm.methods.MethodVisitor;
import mockit.asm.methods.MethodWriter;
import mockit.coverage.data.CoverageData;
import mockit.coverage.data.FileCoverageData;
import mockit.internal.ClassFile;

final class CoverageModifier extends WrappingClassVisitor {
    private static final Map<String, CoverageModifier> INNER_CLASS_MODIFIERS = new HashMap<>();
    private static final int FIELD_MODIFIERS_TO_IGNORE = FINAL + SYNTHETIC;

    @Nullable
    static byte[] recoverModifiedByteCodeIfAvailable(@NonNull String innerClassName) {
        CoverageModifier modifier = INNER_CLASS_MODIFIERS.remove(innerClassName);
        return modifier == null ? null : modifier.toByteArray();
    }

    @Nullable
    private String internalClassName;
    @Nullable
    private String simpleClassName;
    @NonNull
    private String sourceFileName;
    @Nullable
    private FileCoverageData fileData;
    private final boolean forInnerClass;
    private boolean forEnumClass;
    @Nullable
    private String kindOfTopLevelType;

    CoverageModifier(@NonNull ClassReader cr) {
        this(cr, false);
    }

    private CoverageModifier(@NonNull ClassReader cr, boolean forInnerClass) {
        super(new ClassWriter(cr));
        sourceFileName = "";
        this.forInnerClass = forInnerClass;
    }

    private CoverageModifier(@NonNull ClassReader cr, @NonNull CoverageModifier other,
            @Nullable String simpleClassName) {
        this(cr, true);
        sourceFileName = other.sourceFileName;
        fileData = other.fileData;
        internalClassName = other.internalClassName;
        this.simpleClassName = simpleClassName;
    }

    @Override
    public void visit(int version, int access, @NonNull String name, @NonNull ClassInfo additionalInfo) {
        if ((access & SYNTHETIC) != 0) {
            throw new VisitInterruptedException();
        }

        boolean nestedType = name.indexOf('$') > 0;

        if (!nestedType && kindOfTopLevelType == null) {
            // noinspection ConstantConditions
            kindOfTopLevelType = getKindOfJavaType(access, additionalInfo.superName);
        }

        forEnumClass = (access & ENUM) != 0;

        String sourceFileDebugName = getSourceFileDebugName(additionalInfo);

        if (!forInnerClass) {
            extractClassAndSourceFileName(name);

            boolean cannotModify = (access & ANNOTATION) != 0;

            if (cannotModify) {
                throw VisitInterruptedException.INSTANCE;
            }

            registerAsInnerClassModifierIfApplicable(access, name, nestedType);
            createFileData(sourceFileDebugName);
        }

        cw.visit(version, access, name, additionalInfo);
    }

    @NonNull
    private static String getKindOfJavaType(int typeModifiers, @NonNull String superName) {
        if ((typeModifiers & ANNOTATION) != 0) {
            return "ant";
        }
        if ((typeModifiers & INTERFACE) != 0) {
            return "itf";
        }
        if ((typeModifiers & ENUM) != 0) {
            return "enm";
        }
        if ((typeModifiers & ABSTRACT) != 0) {
            return "absCls";
        }
        if (superName.endsWith("Exception") || superName.endsWith("Error")) {
            return "exc";
        }
        return "cls";
    }

    @NonNull
    private static String getSourceFileDebugName(@NonNull ClassInfo additionalInfo) {
        String sourceFileDebugName = additionalInfo.sourceFileName;

        if (sourceFileDebugName == null || !sourceFileDebugName.endsWith(".java")) {
            throw VisitInterruptedException.INSTANCE;
        }

        return sourceFileDebugName;
    }

    private void extractClassAndSourceFileName(@NonNull String className) {
        internalClassName = className;
        int p = className.lastIndexOf('/');

        if (p < 0) {
            simpleClassName = className;
            sourceFileName = "";
        } else {
            simpleClassName = className.substring(p + 1);
            sourceFileName = className.substring(0, p + 1);
        }
    }

    private void registerAsInnerClassModifierIfApplicable(int access, @NonNull String name, boolean nestedType) {
        if (!forEnumClass && (access & SUPER) != 0 && nestedType) {
            INNER_CLASS_MODIFIERS.put(name.replace('/', '.'), this);
        }
    }

    private void createFileData(@NonNull String sourceFileDebugName) {
        sourceFileName += sourceFileDebugName;
        fileData = CoverageData.instance().getOrAddFile(sourceFileName, kindOfTopLevelType);
    }

    @Override
    public void visitInnerClass(@NonNull String name, @Nullable String outerName, @Nullable String innerName,
            int access) {
        cw.visitInnerClass(name, outerName, innerName, access);

        if (forInnerClass || isSyntheticOrEnumClass(access) || !isNestedInsideClassBeingModified(name, outerName)) {
            return;
        }

        String innerClassName = name.replace('/', '.');

        if (INNER_CLASS_MODIFIERS.containsKey(innerClassName)) {
            return;
        }

        ClassReader innerCR = ClassFile.createClassReader(CoverageModifier.class.getClassLoader(), name);

        if (innerCR != null) {
            CoverageModifier innerClassModifier = new CoverageModifier(innerCR, this, innerName);
            innerCR.accept(innerClassModifier);
            INNER_CLASS_MODIFIERS.put(innerClassName, innerClassModifier);
        }
    }

    private static boolean isSyntheticOrEnumClass(int access) {
        return (access & SYNTHETIC) != 0 || access == STATIC + ENUM;
    }

    private boolean isNestedInsideClassBeingModified(@NonNull String internalName, @Nullable String outerName) {
        String className = outerName == null ? internalName : outerName;
        int p = className.indexOf('$');
        String outerClassName = p < 0 ? className : className.substring(0, p);

        return outerClassName.equals(internalClassName);
    }

    @Override
    public FieldVisitor visitField(int access, @NonNull String name, @NonNull String desc, @Nullable String signature,
            @Nullable Object value) {
        if (fileData != null && simpleClassName != null && (access & FIELD_MODIFIERS_TO_IGNORE) == 0) {
            fileData.dataCoverageInfo.addField(simpleClassName, name, (access & STATIC) != 0);
        }

        return cw.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, @NonNull String name, @NonNull String desc, @Nullable String signature,
            @Nullable String[] exceptions) {
        MethodWriter mw = cw.visitMethod(access, name, desc, signature, exceptions);

        if ((access & SYNTHETIC) != 0 || fileData == null || "<clinit>".equals(name) && forEnumClass) {
            return mw;
        }

        return new MethodModifier(mw, sourceFileName, fileData);
    }
}
