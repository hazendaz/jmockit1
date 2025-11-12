/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
import org.junit.jupiter.api.Test;

class ClassInDefaultPackageTest {
    @Test
    void firstTest() {
        new ClassInDefaultPackage().doSomething(ClassInDefaultPackage.NestedEnum.First);
    }

    @Test
    void secondTest() {
        new ClassInDefaultPackage().doSomething(ClassInDefaultPackage.NestedEnum.Second);
    }
}
