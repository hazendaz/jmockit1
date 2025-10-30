package integration.tests.data;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import integration.tests.CoverageTest;

class ClassWithFieldsTest extends CoverageTest {
    ClassWithFields tested;

    @Test
    void setGetStatic1() {
        ClassWithFields.setStatic1(1);
        ClassWithFields.setStatic1(2);
        assert ClassWithFields.getStatic1() == 2;

        assertStaticFieldCovered("static1");
    }

    @Test
    void setStatic2() {
        ClassWithFields.setStatic2("test");

        assertStaticFieldUncovered("static2");
    }

    @Test
    void setGetSetStatic3() {
        ClassWithFields.setStatic3(1);
        assert ClassWithFields.getStatic3() == 1;
        ClassWithFields.setStatic3(2);

        assertStaticFieldUncovered("static3");
    }

    @Test
    void setGetInstance1() {
        tested.setInstance1(true);
        assert tested.isInstance1();

        assertInstanceFieldCovered("instance1");
    }

    @Test
    void setInstance2() {
        tested.setInstance2(false);

        assertInstanceFieldUncovered("instance2", tested);
    }

    @Test
    void setGetSetInstance3() {
        tested.setInstance3(2.5);
        assert tested.getInstance3() >= 2.5;
        tested.setInstance3(-0.9);

        assertInstanceFieldUncovered("instance3", tested);
    }

    @AfterAll
    static void verifyDataCoverage() {
        verifyDataCoverage(6, 2, 33);
    }
}
