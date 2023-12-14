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
