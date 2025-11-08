package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import mockit.integration.junit5.JMockitExtension;
import mockit.internal.util.ObjectMethods;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class ObjectOverridesTest.
 */
@ExtendWith(JMockitExtension.class)
class ObjectOverridesTest {

    /**
     * Verify standard behavior of overridable object methods in mocked interface.
     *
     * @param r1
     *            the r 1
     * @param r2
     *            the r 2
     */
    @Test
    void verifyStandardBehaviorOfOverridableObjectMethodsInMockedInterface(@Mocked Runnable r1, @Mocked Runnable r2) {
        assertDefaultEqualsBehavior(r1, r2);
        assertDefaultEqualsBehavior(r2, r1);

        assertDefaultHashCodeBehavior(r1);
        assertDefaultHashCodeBehavior(r2);

        assertDefaultToStringBehavior(r1);
        assertDefaultToStringBehavior(r2);
    }

    /**
     * Assert default equals behavior.
     *
     * @param obj1
     *            the obj 1
     * @param obj2
     *            the obj 2
     */
    @SuppressWarnings("SimplifiableJUnitAssertion")
    void assertDefaultEqualsBehavior(Object obj1, Object obj2) {
        assertFalse(obj1.equals(null));
        assertFalse(obj1.equals("test"));
        // noinspection EqualsWithItself
        assertTrue(obj1.equals(obj1));
        assertFalse(obj1.equals(obj2));
    }

    /**
     * Assert default hash code behavior.
     *
     * @param obj
     *            the obj
     */
    void assertDefaultHashCodeBehavior(Object obj) {
        assertEquals(System.identityHashCode(obj), obj.hashCode());
    }

    /**
     * Assert default to string behavior.
     *
     * @param obj
     *            the obj
     */
    void assertDefaultToStringBehavior(Object obj) {
        assertEquals(ObjectMethods.objectIdentity(obj), obj.toString());
    }

    /**
     * Verify standard behavior of overridden object methods in mocked JRE class.
     *
     * @param d1
     *            the d 1
     * @param d2
     *            the d 2
     */
    @Test
    public void verifyStandardBehaviorOfOverriddenObjectMethodsInMockedJREClass(@Mocked Date d1, @Mocked Date d2) {
        assertDefaultEqualsBehavior(d1, d2);
        assertDefaultEqualsBehavior(d2, d1);

        assertDefaultHashCodeBehavior(d1);
        assertDefaultHashCodeBehavior(d2);

        assertDefaultToStringBehavior(d1);
        assertDefaultToStringBehavior(d2);
    }

    /** The a. */
    @Mocked
    ClassWithObjectOverrides a;

    /** The b. */
    @Mocked
    ClassWithObjectOverrides b;

    /**
     * Call object methods in mock before every test.
     */
    @BeforeEach
    void callObjectMethodsInMockBeforeEveryTest() {
        assertEquals(System.identityHashCode(a), a.hashCode());
        assertEquals(b, b);
    }

    /**
     * Verify standard behavior of overridden object methods in mocked class.
     *
     * @throws Throwable
     *             the throwable
     */
    @Test
    @SuppressWarnings("FinalizeCalledExplicitly")
    void verifyStandardBehaviorOfOverriddenObjectMethodsInMockedClass() throws Throwable {
        assertDefaultEqualsBehavior(a, b);
        assertDefaultEqualsBehavior(b, a);

        assertDefaultHashCodeBehavior(a);
        assertDefaultHashCodeBehavior(b);

        assertDefaultToStringBehavior(a);
        assertDefaultToStringBehavior(b);

        a.finalize();
        b.finalize();
    }

    /**
     * Mock override of equals method.
     */
    @Test
    @SuppressWarnings({ "SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes" })
    void mockOverrideOfEqualsMethod() {
        new Expectations() {
            {
                a.equals(null);
                result = true;
                a.equals(anyString);
                result = true;
            }
        };

        new Expectations() {
            {
                b.equals(a);
                result = true;
            }
        };

        assertTrue(a.equals(null));
        assertTrue(a.equals("test"));
        assertTrue(b.equals(a));
    }

    /**
     * Mock override of hash code method.
     */
    @Test
    void mockOverrideOfHashCodeMethod() {
        assertTrue(a.hashCode() != b.hashCode());

        new Expectations() {
            {
                a.hashCode();
                result = 123;
                b.hashCode();
                result = 45;
                times = 1;
            }
        };

        assertEquals(123, a.hashCode());
        assertEquals(45, b.hashCode());
    }

    /**
     * Mock override of to string method.
     */
    @Test
    void mockOverrideOfToStringMethod() {
        // noinspection SimplifiableJUnitAssertion
        assertFalse(a.toString().equals(b.toString()));

        new Expectations() {
            {
                a.toString();
                result = "mocked";
            }
        };

        // noinspection SimplifiableJUnitAssertion
        assertTrue("mocked".equals(a.toString()));

        new Verifications() {
            {
                a.toString();
                b.toString();
                times = 0;
            }
        };
    }

    /**
     * Mock override of clone method.
     */
    @Test
    void mockOverrideOfCloneMethod() {
        new Expectations() {
            {
                a.clone();
                result = b;
            }
        };

        assertSame(b, a.clone());
    }

    /**
     * Record expectations on overridden object method as always non strict.
     */
    @Test
    void recordExpectationsOnOverriddenObjectMethodAsAlwaysNonStrict() {
        new Expectations() {
            {
                a.doSomething();
                a.hashCode();
                result = 1;
                a.equals(any);
                a.toString();
            }
        };

        a.doSomething();
    }

    /**
     * The Class ClassWithEqualsOverride.
     */
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    static class ClassWithEqualsOverride {

        /** The value. */
        private final int value;

        /**
         * Instantiates a new class with equals override.
         *
         * @param value
         *            the value
         */
        ClassWithEqualsOverride(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object other) {
            return ((ClassWithEqualsOverride) other).value == value;
        }
    }

    /**
     * Partially mock instances of class with equals override whose instance gets passed in recorded expectation.
     */
    @Test
    void partiallyMockInstancesOfClassWithEqualsOverrideWhoseInstanceGetsPassedInRecordedExpectation() {
        final Object o1 = new ClassWithEqualsOverride(123);
        Object o2 = new ClassWithEqualsOverride(123);

        new Expectations(o1, o2) {
            {
                a.doSomething(o1);
            }
        };

        a.doSomething(o2);
    }

    /**
     * Partially mock instances of JRE class with equals override whose instance gets passed in recorded expectation.
     */
    @Test
    void partiallyMockInstancesOfJREClassWithEqualsOverrideWhoseInstanceGetsPassedInRecordedExpectation() {
        final Object o1 = new Date(123);
        Object o2 = new Date(123);

        new Expectations(o1, o2) {
            {
                a.doSomething(o1);
            }
        };

        a.doSomething(o2);
    }
}
