package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * The Class TestedClassWithAnnotatedDITest.
 */
@FixMethodOrder(NAME_ASCENDING)
public final class TestedClassWithAnnotatedDITest {

    /**
     * The Class TestedClass1.
     */
    static class TestedClass1 {

        /** The action 2. */
        @Resource(name = "secondAction")
        Runnable action2;

        /** The some value. */
        @Autowired
        int someValue;

        /** The action 1. */
        @Resource(name = "firstAction")
        Runnable action1;

        /** The action 3. */
        @Resource(name = "thirdAction")
        Runnable action3;

        /** The another value. */
        @Inject
        int anotherValue;

        /** The string field with value. */
        // @Value is supported implicitly (no specific handling for it).
        @Value("textValue")
        String stringFieldWithValue;

        /** The numeric field with value. */
        @Value("123.45")
        double numericFieldWithValue;

        /** The system property. */
        @Value("#{systemProperties.someProperty}")
        String systemProperty;

        /** The an int. */
        @Value("${anotherSystemProperty}")
        int anInt;

        /** The a long value. */
        @Value("${propertyWithDefault:12345}")
        long aLongValue; // default value is ignored, left as 0 if no @Injectable or @Tested is found
    }

    /**
     * The Class TestedClass2.
     */
    static class TestedClass2 {

        /** The some value. */
        final int someValue;

        /** The action. */
        final Runnable action;

        /** The another action. */
        @Resource
        Runnable anotherAction;

        /** The text. */
        String text;

        /** The another text. */
        @Inject
        String anotherText;

        /** The optional action. */
        @Autowired(required = false)
        Runnable optionalAction;

        /**
         * Instantiates a new tested class 2.
         *
         * @param someValue
         *            the some value
         * @param action
         *            the action
         * @param textValue
         *            the text value
         */
        @Autowired
        TestedClass2(int someValue, Runnable action, String textValue) {
            this.someValue = someValue;
            this.action = action;
            text = textValue;
        }
    }

    /** The tested 1. */
    @Tested
    TestedClass1 tested1;

    /** The tested 2. */
    @Tested
    TestedClass2 tested2;

    /** The first action. */
    @Injectable
    Runnable firstAction;

    /** The some value. */
    @Injectable
    final int someValue = 1;

    /** The action. */
    @Injectable
    Runnable action;

    /** The text value. */
    @Injectable
    String textValue = "test";

    /** The another text. */
    @Injectable
    String anotherText = "name2";

    /** The action 3. */
    @Injectable
    Runnable action3; // matches @Resource(name = "thirdAction") by field name, after failing to match on "thirdAction"

    /**
     * Inject all annotated injection points.
     *
     * @param anotherValue
     *            the another value
     * @param secondAction
     *            the second action
     * @param anotherAction
     *            the another action
     * @param unused
     *            the unused
     * @param stringFieldWithValue
     *            the string field with value
     * @param numericFieldWithValue
     *            the numeric field with value
     * @param systemProperty
     *            the system property
     * @param anInt
     *            the an int
     * @param aLong
     *            the a long
     */
    @Test
    public void injectAllAnnotatedInjectionPoints(@Injectable("2") int anotherValue, @Injectable Runnable secondAction,
            @Injectable Runnable anotherAction, @Injectable("true") boolean unused,
            @Injectable("test") String stringFieldWithValue, @Injectable("123.45") double numericFieldWithValue,
            @Injectable("propertyValue") String systemProperty, @Injectable("123") int anInt,
            @Injectable("987654") long aLong) {
        assertSame(firstAction, tested1.action1);
        assertSame(secondAction, tested1.action2);
        assertSame(action3, tested1.action3);
        assertEquals(1, tested1.someValue);
        assertEquals(2, tested1.anotherValue);
        assertEquals("test", tested1.stringFieldWithValue);
        assertEquals(123.45, tested1.numericFieldWithValue, 0);
        assertEquals("propertyValue", tested1.systemProperty);
        assertEquals(123, tested1.anInt);
        assertEquals(987654, tested1.aLongValue);

        assertEquals(1, tested2.someValue);
        assertSame(action, tested2.action);
        assertSame(anotherAction, tested2.anotherAction);
        assertSame(textValue, tested2.text);
        assertSame(anotherText, tested2.anotherText);
        assertNull(tested2.optionalAction);
    }

    /**
     * Leave value annotated injection points with default initialization value.
     *
     * @param action2
     *            the action 2
     * @param anotherAction
     *            the another action
     * @param anotherValue
     *            the another value
     */
    @Test
    public void leaveValueAnnotatedInjectionPointsWithDefaultInitializationValue(@Injectable Runnable action2,
            @Injectable Runnable anotherAction, @Injectable("2") int anotherValue) {
        assertNull(tested1.systemProperty);
        assertEquals(0, tested1.anInt);
        assertEquals(0, tested1.aLongValue);
    }

    /**
     * Fail for annotated field which lacks an injectable.
     */
    @Test(expected = IllegalStateException.class)
    public void failForAnnotatedFieldWhichLacksAnInjectable() {
        fail("Must fail before starting");
    }

    /**
     * Fail for annotated field having an injectable of the same type which was already consumed.
     *
     * @param secondAction
     *            the second action
     */
    @Test(expected = IllegalStateException.class)
    public void failForAnnotatedFieldHavingAnInjectableOfTheSameTypeWhichWasAlreadyConsumed(
            @Injectable Runnable secondAction) {
        fail("Must fail before starting");
    }
}
