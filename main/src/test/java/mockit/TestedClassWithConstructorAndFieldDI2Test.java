package mockit;

import static java.util.Arrays.asList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The Class TestedClassWithConstructorAndFieldDI2Test.
 */
final class TestedClassWithConstructorAndFieldDI2Test {

    /**
     * The Class TestedClass.
     */
    public static final class TestedClass {

        /** The i. */
        private final int i;

        /** The name. */
        private final String name;

        /** The action 1. */
        private final Runnable action1;

        /** The action 2. */
        Runnable action2;

        /** The i 2. */
        int i2;

        /** The text. */
        String text;

        /** The text 2. */
        String text2;

        /** The text 3. */
        String text3;

        /** The names. */
        List<String> names;

        /**
         * Instantiates a new tested class.
         *
         * @param i
         *            the i
         * @param name
         *            the name
         * @param action1
         *            the action 1
         */
        public TestedClass(int i, String name, Runnable action1) {
            this.i = i;
            this.name = name;
            this.action1 = action1;
        }
    }

    /**
     * The Class TestedClass2.
     */
    static final class TestedClass2 {
        /** The flag. */
        boolean flag;
    }

    /** The names. */
    @Tested
    final List<String> names = asList("Abc", "xyz");

    /** The tested 1. */
    @Tested
    final TestedClass tested1 = new TestedClass(123, "test", null);

    /** The tested 2. */
    @Tested
    TestedClass tested2;

    /** The tested 3. */
    @Tested
    TestedClass2 tested3;

    /** The action. */
    @Injectable
    Runnable action;

    /** The i 2. */
    @Injectable("-67")
    int i2; // must match the target field by name

    /** The text. */
    @Injectable
    String text = "text";

    /** The int value 2. */
    @Injectable("8")
    int intValue2; // won't be used

    /** The int value 3. */
    @Injectable
    final int intValue3 = 9; // won't be used

    /**
     * Sets the up.
     */
    @BeforeEach
    void setUp() {
        Runnable action1 = new Runnable() {
            @Override
            public void run() {
            }
        };
        tested2 = new TestedClass(45, "another", action1);
    }

    /**
     * Verify tested objects injected from fields in the test class.
     */
    @Test
    void verifyTestedObjectsInjectedFromFieldsInTheTestClass() {
        assertFieldsSetByTheConstructor();
        assertFieldsSetThroughFieldInjectionFromInjectableFields();

        // Fields not set either way:
        assertNull(tested1.text2);
        assertNull(tested2.text2);
    }

    /**
     * Assert fields set by the constructor.
     */
    void assertFieldsSetByTheConstructor() {
        assertEquals(123, tested1.i);
        assertEquals("test", tested1.name);
        assertNull(tested1.action1);

        assertEquals(45, tested2.i);
        assertEquals("another", tested2.name);
        assertNotNull(tested2.action1);
        assertNotSame(action, tested2.action1);
    }

    /**
     * Assert fields set through field injection from injectable fields.
     */
    void assertFieldsSetThroughFieldInjectionFromInjectableFields() {
        assertSame(action, tested1.action2);
        assertEquals(-67, tested1.i2);
        assertEquals("text", tested1.text);

        assertSame(action, tested2.action2);
        assertEquals(-67, tested2.i2);
        assertEquals("text", tested2.text);

        assertEquals(asList("Abc", "xyz"), tested1.names);
        assertSame(tested1.names, tested2.names);
    }

    /**
     * Verify tested objects injected from injectable fields and parameters.
     *
     * @param text2
     *            the text 2
     */
    @Test
    void verifyTestedObjectsInjectedFromInjectableFieldsAndParameters(@Injectable("Test") String text2) {
        assertFieldsSetByTheConstructor();

        // Fields set from injectable parameters:
        assertEquals("Test", tested1.text2);
        assertEquals("Test", tested2.text2);

        // Fields not set:
        assertNull(tested1.text3);
        assertNull(tested2.text3);
    }

    /**
     * Verify tested objects injected from parameters by name.
     *
     * @param text2
     *            the text 2
     * @param text3
     *            the text 3
     * @param flag
     *            the flag
     */
    @Test
    void verifyTestedObjectsInjectedFromParametersByName(@Injectable("two") String text2,
            @Injectable("three") String text3, @Injectable("true") boolean flag) {
        assertFieldsSetByTheConstructor();

        // Fields set from injectable parameters:
        assertEquals("two", tested1.text2);
        assertEquals("three", tested1.text3);
        assertEquals("two", tested2.text2);
        assertEquals("three", tested2.text3);
        assertTrue(tested3.flag);
    }

    /**
     * The Class ClassWithConstructorHavingReferenceTypeParameterAndDoubleSizedLocalVar.
     */
    static class ClassWithConstructorHavingReferenceTypeParameterAndDoubleSizedLocalVar {

        /**
         * Instantiates a new class with constructor having reference type parameter and double sized local var.
         *
         * @param s
         *            the s
         */
        @SuppressWarnings("unused")
        ClassWithConstructorHavingReferenceTypeParameterAndDoubleSizedLocalVar(String s) {
            long var = 1;
        }
    }

    /** The sut. */
    @Tested
    ClassWithConstructorHavingReferenceTypeParameterAndDoubleSizedLocalVar sut;
}
