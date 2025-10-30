package integration.tests.data;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import integration.tests.CoverageTest;

class ClassWithInstanceFieldsTest extends CoverageTest {
    ClassWithInstanceFields tested;

    @Test
    void finalField() {
        assert tested.getFinalField() == 123;

        assertFieldIgnored("finalField");
    }

    @Test
    void coveredBooleanFieldOnMultipleInstances() {
        tested.setBooleanField(true);
        assert tested.isBooleanField();

        ClassWithInstanceFields tested2 = new ClassWithInstanceFields();
        tested2.setBooleanField(false);
        assert !tested2.isBooleanField();

        assertInstanceFieldCovered("booleanField");
    }

    @Test
    void uncoveredByteFieldOnMultipleInstances() {
        assert tested.getByteField() == 0;
        tested.setByteField((byte) 1);
        assert tested.getByteField() == 1;

        ClassWithInstanceFields tested2 = new ClassWithInstanceFields();
        assert tested2.getByteField() == 0;

        ClassWithInstanceFields tested3 = new ClassWithInstanceFields();
        tested3.setByteField((byte) 3);

        assertInstanceFieldUncovered("byteField", tested3);
    }

    @Test
    void coveredCharField() {
        tested.setCharField('c');
        assert tested.getCharField() == 'c';
        assert tested.getCharField() != 'd';

        assertInstanceFieldCovered("charField");
    }

    @Test
    void uncoveredShortFieldOnMultipleInstances() {
        tested.setShortField((short) 1);

        ClassWithInstanceFields tested2 = new ClassWithInstanceFields();
        tested2.setShortField((short) 2);

        assertInstanceFieldUncovered("shortField", tested, tested2);
    }

    @Test
    void coveredIntFieldOnMultipleInstances() {
        ClassWithInstanceFields tested2 = new ClassWithInstanceFields();

        tested.setIntField(1);
        tested2.setIntField(-1);

        assert tested2.getIntField() == -1;
        assert tested.getIntField() == 1;

        assertInstanceFieldCovered("intField");
    }

    @Test
    void coveredLongFieldOnMultipleInstances() {
        tested.setLongField(1);
        assert tested.getLongField() == 1;

        ClassWithInstanceFields tested2 = new ClassWithInstanceFields();
        tested2.setLongField(2);
        assert tested2.getLongField() == 2;

        assertInstanceFieldCovered("longField");
    }

    @Test
    void uncoveredFloatFieldOnMultipleInstances() {
        tested.setFloatField(1);

        ClassWithInstanceFields tested2 = new ClassWithInstanceFields();
        tested2.setFloatField(2.0F);
        assert tested2.getFloatField() >= 2.0F;

        assertInstanceFieldUncovered("floatField", tested);
    }

    @Test
    void coveredDoubleFieldOnMultipleInstances() {
        tested.setDoubleField(1);
        assert tested.getDoubleField() >= 1;

        ClassWithInstanceFields tested2 = new ClassWithInstanceFields();
        tested2.setDoubleField(2);
        assert tested2.getDoubleField() <= 2;

        assertInstanceFieldCovered("doubleField");
    }

    @Test
    void coveredArrayFieldOnMultipleInstances() {
        tested.setArrayField(null);
        assert tested.getArrayField() == null;

        ClassWithInstanceFields tested2 = new ClassWithInstanceFields();
        tested2.setArrayField(new int[0]);
        assert tested2.getArrayField().length == 0;

        assertInstanceFieldCovered("arrayField");
    }

    @AfterAll
    static void verifyDataCoverage() {
        verifyDataCoverage(9, 6, 67);
    }
}
