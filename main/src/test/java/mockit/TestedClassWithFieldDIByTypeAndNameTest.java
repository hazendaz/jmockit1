package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

/**
 * The Class TestedClassWithFieldDIByTypeAndNameTest.
 */
public final class TestedClassWithFieldDIByTypeAndNameTest {

    /**
     * The Class TestedClass.
     */
    static class TestedClass {

        /** The some value. */
        int someValue;

        /** The another value. */
        int anotherValue;

        /**
         * Gets the some value base.
         *
         * @return the some value base
         */
        final int getSomeValue_base() {
            return someValue;
        }
    }

    /**
     * The Class TestedSubclass.
     */
    static class TestedSubclass extends TestedClass {

        /** The some value. */
        @SuppressWarnings("FieldNameHidesFieldInSuperclass")
        int someValue;

        /** The yet another value. */
        int yetAnotherValue;
    }

    /** The tested. */
    @Tested
    TestedSubclass tested;

    /**
     * Inject by field type and name with tested class having multiple fields of same type.
     *
     * @param anotherValue
     *            the another value
     */
    @Test
    public void injectByFieldTypeAndNameWithTestedClassHavingMultipleFieldsOfSameType(
            @Injectable("12") int anotherValue) {
        assertEquals(0, tested.getSomeValue_base());
        assertEquals(0, tested.someValue);
        assertEquals(12, tested.anotherValue);
        assertEquals(0, tested.yetAnotherValue);
    }

    /**
     * Inject by field type and name with tested class having fields of same type but different names.
     *
     * @param val
     *            the val
     */
    @Test
    public void injectByFieldTypeAndNameWithTestedClassHavingFieldsOfSameTypeButDifferentNames(
            @Injectable("45") int val) {
        assertEquals(0, tested.getSomeValue_base());
        assertEquals(0, tested.someValue);
        assertEquals(0, tested.anotherValue);
        assertEquals(0, tested.yetAnotherValue);
    }

    /**
     * Inject by field type and name into fields at different levels of class hierarchy.
     *
     * @param someValue
     *            the some value
     * @param yetAnotherValue
     *            the yet another value
     * @param unused
     *            the unused
     */
    @Test
    public void injectByFieldTypeAndNameIntoFieldsAtDifferentLevelsOfClassHierarchy(@Injectable("1") int someValue,
            @Injectable("2") int yetAnotherValue, @Injectable("3") int unused) {
        assertEquals(0, tested.getSomeValue_base());
        assertEquals(1, tested.someValue);
        assertEquals(0, tested.anotherValue);
        assertEquals(2, tested.yetAnotherValue);
    }
}
