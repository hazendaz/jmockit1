/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import java.io.File;
import java.lang.reflect.Type;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import mockit.internal.reflection.GenericTypeReflection;
import mockit.internal.util.Utilities;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TestedClass {
    @NonNull
    final Type declaredType;
    @NonNull
    final Class<?> declaredClass;
    @NonNull
    public final Class<?> targetClass;
    @NonNull
    public final GenericTypeReflection reflection;
    @NonNull
    final ProtectionDomain protectionDomainOfTestedClass;
    @Nullable
    final String codeLocationParentPath;
    @NonNull
    public final String nameOfTestedClass;
    @Nullable
    public final TestedClass parent;
    @Nullable
    public Class<?> testClass;

    public TestedClass(@NonNull Type declaredType, @NonNull Class<?> targetClass) {
        this(declaredType, targetClass, null);
    }

    public TestedClass(@NonNull Type declaredType, @NonNull Class<?> targetClass, @Nullable TestedClass parent) {
        this.declaredType = declaredType;
        declaredClass = Utilities.getClassType(declaredType);
        this.targetClass = targetClass;
        reflection = new GenericTypeReflection(declaredClass, declaredType, false);
        protectionDomainOfTestedClass = declaredClass.getProtectionDomain();
        CodeSource codeSource = protectionDomainOfTestedClass.getCodeSource();
        codeLocationParentPath = codeSource == null || codeSource.getLocation() == null ? null
                : new File(codeSource.getLocation().getPath()).getParent();
        nameOfTestedClass = declaredClass.getName();
        this.parent = parent;
    }

    @NonNull
    public Class<?> getDeclaredClass() {
        return declaredClass;
    }

    public boolean isClassFromSameModuleOrSystemAsTestedClass(@NonNull Class<?> anotherClass) {
        if (anotherClass.getClassLoader() == null) {
            return false;
        }

        ProtectionDomain anotherProtectionDomain = anotherClass.getProtectionDomain();

        if (anotherProtectionDomain == null) {
            return false;
        }

        if (anotherProtectionDomain == protectionDomainOfTestedClass) {
            return true;
        }

        CodeSource anotherCodeSource = anotherProtectionDomain.getCodeSource();

        if (anotherCodeSource == null || anotherCodeSource.getLocation() == null) {
            return false;
        }

        if (codeLocationParentPath != null) {
            String anotherClassPath = anotherCodeSource.getLocation().getPath();
            String anotherClassParentPath = new File(anotherClassPath).getParent();

            if (anotherClassParentPath.equals(codeLocationParentPath)) {
                return true;
            }
        }

        return isInSameSubpackageAsTestedClass(anotherClass);
    }

    private boolean isInSameSubpackageAsTestedClass(@NonNull Class<?> anotherClass) {
        String nameOfAnotherClass = anotherClass.getName();
        int p1 = nameOfAnotherClass.indexOf('.');
        int p2 = nameOfTestedClass.indexOf('.');
        boolean differentPackages = p1 != p2 || p1 == -1;

        if (differentPackages) {
            return false;
        }

        p1 = nameOfAnotherClass.indexOf('.', p1 + 1);
        p2 = nameOfTestedClass.indexOf('.', p2 + 1);
        boolean eitherClassDirectlyInFirstPackageLevel = p1 == -1 || p2 == -1;

        if (eitherClassDirectlyInFirstPackageLevel) {
            return true;
        }

        boolean differentSubpackages = p1 != p2;

        return !differentSubpackages && nameOfAnotherClass.substring(0, p1).equals(nameOfTestedClass.substring(0, p2));
    }
}
