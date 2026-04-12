/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.injection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

final class TestedClassTest {

    @Test
    void isClassFromSameModuleReturnsFalseForBootstrapLoadedClass() {
        TestedClass tc = new TestedClass(TestedClassTest.class, TestedClassTest.class);
        assertFalse(tc.isClassFromSameModuleOrSystemAsTestedClass(String.class));
    }

    @Test
    void isClassFromSameModuleReturnsTrueForSameProtectionDomain() {
        TestedClass tc = new TestedClass(TestedClassTest.class, TestedClassTest.class);
        assertTrue(tc.isClassFromSameModuleOrSystemAsTestedClass(TestedClassTest.class));
    }

    @Test
    void isClassFromSameModuleReturnsTrueForSameCodeLocation() {
        TestedClass tc = new TestedClass(TestedClassTest.class, TestedClassTest.class);
        assertTrue(tc.isClassFromSameModuleOrSystemAsTestedClass(TestedClass.class));
    }

    @Test
    void isInSameSubpackageReturnsFalseWhenDifferentFirstLevelPackage() throws Exception {
        TestedClass tc = new TestedClass(TestedClassTest.class, TestedClassTest.class);
        Method method = TestedClass.class.getDeclaredMethod("isInSameSubpackageAsTestedClass", Class.class);
        method.setAccessible(true);

        boolean result = (Boolean) method.invoke(tc, org.junit.jupiter.api.Test.class);
        assertFalse(result);
    }

    @Test
    void isInSameSubpackageReturnsTrueWhenSameFirstTwoPackageLevels() throws Exception {
        TestedClass tc = new TestedClass(TestedClassTest.class, TestedClassTest.class);
        Method method = TestedClass.class.getDeclaredMethod("isInSameSubpackageAsTestedClass", Class.class);
        method.setAccessible(true);

        boolean result = (Boolean) method.invoke(tc, TestedClass.class);
        assertTrue(result);
    }

    @Test
    void isInSameSubpackageReturnsFalseWhenSameFirstButDifferentSecondLevel() throws Exception {
        TestedClass tc = new TestedClass(mockit.internal.injection.TestedClass.class,
                mockit.internal.injection.TestedClass.class);
        Method method = TestedClass.class.getDeclaredMethod("isInSameSubpackageAsTestedClass", Class.class);
        method.setAccessible(true);

        boolean result = (Boolean) method.invoke(tc, mockit.asm.types.PrimitiveType.class);
        assertFalse(result);
    }

    @Test
    void isInSameSubpackageReturnsTrueWhenSameFirstLevelAndOneClassHasNoSecondLevel() throws Exception {
        TestedClass tc = new TestedClass(TestedClassTest.class, TestedClassTest.class);
        Method method = TestedClass.class.getDeclaredMethod("isInSameSubpackageAsTestedClass", Class.class);
        method.setAccessible(true);

        // Both "mockit.internal.injection.TestedClassTest" and "mockit.Expectations" start with "mockit."
        // "mockit.Expectations" has second dot at -1, which causes eitherClassDirectlyInFirstPackageLevel = true
        boolean result = (Boolean) method.invoke(tc, mockit.Expectations.class);
        assertTrue(result);
    }

    @Test
    void constructorSetsDeclaredClassAndTargetClass() {
        TestedClass tc = new TestedClass(TestedClassTest.class, TestedClassTest.class);
        assertNotNull(tc.getDeclaredClass());
        assertNotNull(tc.targetClass);
        assertNotNull(tc.nameOfTestedClass);
    }

    @Test
    void constructorWithParentSetsParentField() {
        TestedClass parent = new TestedClass(TestedClassTest.class, TestedClassTest.class);
        TestedClass child = new TestedClass(TestedClass.class, TestedClass.class, parent);
        assertNotNull(child.parent);
        assertTrue(child.parent == parent);
    }

    @Test
    void isInSameSubpackageReturnsFalseWhenNoFirstLevelPackage() throws Exception {
        // A class in the default package would have indexOf('.') = -1
        // We simulate this by checking differentPackages logic when p1 == -1
        TestedClass tc = new TestedClass(TestedClassTest.class, TestedClassTest.class);
        Method method = TestedClass.class.getDeclaredMethod("isInSameSubpackageAsTestedClass", Class.class);
        method.setAccessible(true);

        // int[].class has name "[I" which has no dot - p1 = -1, p2 != -1, so differentPackages = true
        boolean result = (Boolean) method.invoke(tc, int[].class);
        assertFalse(result);
    }
}
