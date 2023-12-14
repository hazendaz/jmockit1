package integrationTests.data;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import integrationTests.CoverageTest;

class ClassWithFieldsAccessedByMultipleTestsTest extends CoverageTest {
    ClassWithFieldsAccessedByMultipleTests tested;

    @Test
    void onlyAssignStaticField1() {
        ClassWithFieldsAccessedByMultipleTests.setStaticField1(false);
    }

    @Test
    void readAndAssignStaticField1() {
        ClassWithFieldsAccessedByMultipleTests.isStaticField1();
        ClassWithFieldsAccessedByMultipleTests.setStaticField1(true);
    }

    @AfterAll
    static void staticField1ShouldBeUncovered() {
        assertStaticFieldUncovered("staticField1");
    }

    @Test
    void assignAndReadStaticField2() {
        ClassWithFieldsAccessedByMultipleTests.setStaticField2(true);
        ClassWithFieldsAccessedByMultipleTests.isStaticField2();
    }

    @Test
    void assignStaticField2() {
        ClassWithFieldsAccessedByMultipleTests.setStaticField2(false);
    }

    @AfterAll
    static void staticField2ShouldBeCovered() {
        assertStaticFieldCovered("staticField2");
    }

    @Test
    void onlyAssignInstanceField1() {
        tested.setInstanceField1(1);
    }

    @Test
    void readAndAssignInstanceField1() {
        tested.getInstanceField1();
        tested.setInstanceField1(2);
    }

    @AfterAll
    static void instanceField1ShouldBeUncovered() {
        assertInstanceFieldUncovered("instanceField1");
    }

    @Test
    void assignAndReadInstanceField2() {
        tested.setInstanceField2(3);
        tested.getInstanceField2();
    }

    @Test
    void assignInstanceField2() {
        tested.setInstanceField2(4);
    }

    @AfterAll
    static void instanceField2ShouldBeCovered() {
        assertInstanceFieldCovered("instanceField2");
    }

    @AfterAll
    static void verifyDataCoverage() {
        verifyDataCoverage(4, 2, 50);
    }
}
