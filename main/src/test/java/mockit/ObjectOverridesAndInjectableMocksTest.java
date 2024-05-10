package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The Class ObjectOverridesAndInjectableMocksTest.
 */
@SuppressWarnings({ "ObjectEqualsNull", "SimplifiableJUnitAssertion" })
final class ObjectOverridesAndInjectableMocksTest {

    /** The a. */
    @Injectable
    ClassWithObjectOverrides a;

    /** The b. */
    @Injectable
    ClassWithObjectOverrides b;

    /**
     * Verify standard behavior of overridden equals methods in mocked class.
     */
    @Test
    void verifyStandardBehaviorOfOverriddenEqualsMethodsInMockedClass() {
        assertDefaultEqualsBehavior(a, b);
        assertDefaultEqualsBehavior(b, a);
    }

    /**
     * Assert default equals behavior.
     *
     * @param obj1
     *            the obj 1
     * @param obj2
     *            the obj 2
     */
    void assertDefaultEqualsBehavior(Object obj1, Object obj2) {
        assertNotEquals(null, obj1);
        assertNotEquals("test", obj1);
        // noinspection EqualsWithItself
        assertEquals(obj1, obj1);
        assertNotEquals(obj1, obj2);
    }

    /**
     * Allow any invocations on overridden object methods for strict mocks.
     */
    @Test
    void allowAnyInvocationsOnOverriddenObjectMethodsForStrictMocks() {
        new Expectations() {
            {
                a.getIntValue();
                result = 58;
            }
        };

        assertNotEquals(a, b);
        // noinspection EqualsWithItself
        assertEquals(a, a);
        assertEquals(58, a.getIntValue());
        assertNotEquals(b, a);
        assertNotEquals(a, b);
    }

    /**
     * The Class BaseClass.
     */
    static class BaseClass {

        /** The value. */
        final int value;

        /**
         * Instantiates a new base class.
         *
         * @param value
         *            the value
         */
        BaseClass(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            return value == ((BaseClass) obj).value;
        }
    }

    /**
     * The Class Subclass1.
     */
    static class Subclass1 extends BaseClass {
        /**
         * Instantiates a new subclass 1.
         */
        Subclass1() {
            super(1);
        }
    }

    /**
     * The Class Subclass2.
     */
    static class Subclass2 extends BaseClass {
        /**
         * Instantiates a new subclass 2.
         */
        Subclass2() {
            super(2);
        }
    }

    /**
     * Execute equals override on instances of different subclass than the one mocked.
     *
     * @param mocked
     *            the mocked
     */
    @Test
    void executeEqualsOverrideOnInstancesOfDifferentSubclassThanTheOneMocked(@Injectable Subclass1 mocked) {
        Object s1 = new Subclass2();
        Object s2 = new Subclass2();

        boolean cmp = s1.equals(s2);

        assertTrue(cmp);
    }
}
