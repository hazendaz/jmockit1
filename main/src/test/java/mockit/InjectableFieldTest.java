/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class InjectableFieldTest.
 */
@ExtendWith(JMockitExtension.class)
class InjectableFieldTest {

    /**
     * The Class Base.
     */
    static class Base {
        /**
         * Gets the value.
         *
         * @return the value
         */
        protected int getValue() {
            return 1;
        }
    }

    /**
     * The Class Foo.
     */
    static class Foo extends Base {

        /**
         * Do something.
         *
         * @param s
         *            the s
         */
        void doSomething(String s) {
            throw new RuntimeException(s);
        }

        /**
         * Gets the another value.
         *
         * @return the another value
         */
        int getAnotherValue() {
            return 2;
        }

        /**
         * Gets the boolean value.
         *
         * @return the boolean value
         */
        Boolean getBooleanValue() {
            return true;
        }

        /**
         * Gets the list.
         *
         * @return the list
         */
        final List<Integer> getList() {
            return null;
        }

        /**
         * Do something else.
         *
         * @return the string
         */
        static String doSomethingElse() {
            return "";
        }
    }

    /** The foo. */
    @Injectable
    Foo foo;

    /**
     * Record common expectations.
     */
    @BeforeEach
    void recordCommonExpectations() {
        new Expectations() {
            {
                foo.getValue();
                result = 12;
                foo.getAnotherValue();
                result = 123;
            }
        };

        assertEquals(123, foo.getAnotherValue());
        assertEquals(12, foo.getValue());
        assertEquals(1, new Base().getValue());
        assertEquals(2, new Foo().getAnotherValue());
    }

    /**
     * Cascade one level.
     */
    @Test
    void cascadeOneLevel() {
        try {
            new Foo().doSomething("");
            fail();
        } catch (RuntimeException ignore) {
        }

        new Expectations() {
            {
                foo.doSomething("test");
                times = 1;
            }
        };

        assertEquals(123, foo.getAnotherValue());
        assertFalse(foo.getBooleanValue());
        assertTrue(foo.getList().isEmpty());

        foo.doSomething("test");
    }

    /**
     * Override expectation recorded in before method.
     */
    @Test
    void overrideExpectationRecordedInBeforeMethod() {
        new Expectations() {
            {
                foo.getAnotherValue();
                result = 45;
            }
        };

        assertEquals(45, foo.getAnotherValue());
        foo.doSomething("sdf");
    }

    /**
     * Partially mock instance without affecting injectable instances.
     */
    @Test
    void partiallyMockInstanceWithoutAffectingInjectableInstances() {
        final Foo localFoo = new Foo();

        new Expectations(localFoo) {
            {
                localFoo.getAnotherValue();
                result = 3;
                Foo.doSomethingElse();
                result = "test";
            }
        };

        assertEquals(3, localFoo.getAnotherValue());
        assertEquals(123, foo.getAnotherValue());
        assertEquals(2, new Foo().getAnotherValue());
        assertEquals("test", Foo.doSomethingElse());
        foo.doSomething("");
    }

    /** The primitive int. */
    @Injectable
    int primitiveInt = 123;

    /** The wrapper int. */
    @Injectable
    Integer wrapperInt = 45;

    /** The string. */
    @Injectable
    String string = "Abc";

    /**
     * Use non mockable injectables with values provided through field assignment.
     */
    @Test
    void useNonMockableInjectablesWithValuesProvidedThroughFieldAssignment() {
        assertEquals(123, primitiveInt);
        assertEquals(45, wrapperInt.intValue());
        assertEquals("Abc", string);
    }

    /** The default int. */
    @Injectable
    int defaultInt;

    /** The null integer. */
    @Injectable
    Integer nullInteger;

    /** The null string. */
    @Injectable
    String nullString;

    /** The empty string. */
    @Injectable
    String emptyString = "";

    /**
     * Use null and empty injectables of non mockable types.
     */
    @Test
    void useNullAndEmptyInjectablesOfNonMockableTypes() {
        assertEquals(0, defaultInt);
        assertNull(nullInteger);
        assertNull(nullString);
        assertTrue(emptyString.isEmpty());
    }
}
