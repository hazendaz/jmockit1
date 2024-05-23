/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.capturing;

import static mockit.internal.util.GeneratedClasses.isExternallyGeneratedSubclass;
import static mockit.internal.util.GeneratedClasses.isGeneratedClass;

import java.security.ProtectionDomain;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class CapturedType {
    private static final ProtectionDomain JMOCKIT_DOMAIN = CapturedType.class.getProtectionDomain();

    @NonNull
    final Class<?> baseType;

    CapturedType(@NonNull Class<?> baseType) {
        this.baseType = baseType;
    }

    boolean isToBeCaptured(@NonNull Class<?> aClass) {
        if (aClass == baseType || aClass.isArray() || !baseType.isAssignableFrom(aClass)
                || extendsJMockitBaseType(aClass)) {
            return false;
        }

        return !aClass.isInterface() && !isNotToBeCaptured(aClass.getProtectionDomain(), aClass.getName());
    }

    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    private static boolean extendsJMockitBaseType(@NonNull Class<?> aClass) {
        return mockit.MockUp.class.isAssignableFrom(aClass) || mockit.Expectations.class.isAssignableFrom(aClass)
                || mockit.Verifications.class.isAssignableFrom(aClass)
                || mockit.Delegate.class.isAssignableFrom(aClass);
    }

    static boolean isNotToBeCaptured(@Nullable ProtectionDomain pd, @NonNull String classNameOrDesc) {
        return pd == JMOCKIT_DOMAIN || classNameOrDesc.endsWith("Test")
                || isNonEligibleInternalJDKClass(classNameOrDesc) || isNonEligibleStandardJavaClass(classNameOrDesc)
                || isNonEligibleClassFromIDERuntime(classNameOrDesc)
                || isNonEligibleClassFromThirdPartyLibrary(classNameOrDesc) || isGeneratedClass(classNameOrDesc)
                || isExternallyGeneratedSubclass(classNameOrDesc);
    }

    private static boolean isNonEligibleInternalJDKClass(@NonNull String classNameOrDesc) {
        return classNameOrDesc.startsWith("jdk/")
                || classNameOrDesc.startsWith("sun") && !hasSubPackage(classNameOrDesc, 4, "management")
                || classNameOrDesc.startsWith("com") && hasSubPackage(classNameOrDesc, 4, "sun")
                        && !hasSubPackages(classNameOrDesc, 8, "proxy org");
    }

    private static boolean isNonEligibleStandardJavaClass(@NonNull String classNameOrDesc) {
        return classNameOrDesc.startsWith("java") && !hasSubPackage(classNameOrDesc, 10, "concurrent");
    }

    private static boolean isNonEligibleClassFromIDERuntime(@NonNull String classNameOrDesc) {
        return classNameOrDesc.startsWith("com") && hasSubPackage(classNameOrDesc, 4, "intellij");
    }

    private static boolean isNonEligibleClassFromThirdPartyLibrary(@NonNull String classNameOrDesc) {
        return classNameOrDesc.startsWith("junit") || classNameOrDesc.startsWith("org")
                && hasSubPackages(classNameOrDesc, 4, "junit testng hamcrest gradle");
    }

    private static boolean hasSubPackage(@NonNull String nameOrDesc, @NonNegative int offset,
            @NonNull String subPackage) {
        return nameOrDesc.regionMatches(offset, subPackage, 0, subPackage.length());
    }

    private static boolean hasSubPackages(@NonNull String nameOrDesc, @NonNegative int offset,
            @NonNull String subPackages) {
        int subPackageStart = 0;
        int subPackageEnd;

        do {
            subPackageEnd = subPackages.indexOf(' ', subPackageStart);
            int subPackageLength = (subPackageEnd > 0 ? subPackageEnd : subPackages.length()) - subPackageStart;

            if (nameOrDesc.regionMatches(offset, subPackages, subPackageStart, subPackageLength)) {
                return true;
            }

            subPackageStart = subPackageEnd + 1;
        } while (subPackageEnd > 0);

        return false;
    }
}
