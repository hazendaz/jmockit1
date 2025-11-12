/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class MockedEnumsTest.
 */
@ExtendWith(JMockitExtension.class)
class MockedEnumsTest {

    /**
     * The Enum MyEnum.
     */
    enum MyEnum {

        /** The First. */
        First(true, 10, "First"),

        /** The Second. */
        Second(false, 6, "Second");

        /** The flag. */
        private final boolean flag;

        /** The num. */
        private final int num;

        /** The desc. */
        private final String desc;

        /**
         * Instantiates a new my enum.
         *
         * @param flag
         *            the flag
         * @param num
         *            the num
         * @param desc
         *            the desc
         */
        MyEnum(boolean flag, int num, String desc) {
            this.flag = flag;
            this.num = num;
            this.desc = desc;
        }

        /**
         * Gets the value.
         *
         * @param f
         *            the f
         *
         * @return the value
         */
        public double getValue(double f) {
            return f * num;
        }

        /**
         * Gets the description.
         *
         * @return the description
         */
        public String getDescription() {
            return num + desc + flag;
        }
    }

    /**
     * One enum being mocked must not affect other enums.
     *
     * @param e
     *            the e
     */
    @Test
    void oneEnumBeingMockedMustNotAffectOtherEnums(@Mocked MyEnum e) {
        assertNotNull(RetentionPolicy.valueOf("RUNTIME"));
    }

    /**
     * Mock enum values.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockEnumValues(@Mocked final MyEnum mock) {
        new Expectations() {
            {
                MyEnum.values();
                result = new MyEnum[] { mock };
                mock.getValue(anyDouble);
                result = 50.0;
            }
        };

        MyEnum[] values = MyEnum.values();
        assertEquals(1, values.length);

        double value = values[0].getValue(0.5);
        assertEquals(50.0, value, 0.0);
    }

    /**
     * Mock instance method on any enum element.
     *
     * @param anyEnum
     *            the any enum
     */
    @Test
    void mockInstanceMethodOnAnyEnumElement(@Mocked final MyEnum anyEnum) {
        final double f = 2.5;

        new Expectations() {
            {
                anyEnum.getValue(f);
                result = 12.3;
            }
        };

        assertEquals(12.3, MyEnum.First.getValue(f), 0.0);
        assertEquals(12.3, MyEnum.Second.getValue(f), 0.0);
    }

    /**
     * Verify instance method invocation on any enum element.
     *
     * @param anyEnum
     *            the any enum
     */
    @Test
    void verifyInstanceMethodInvocationOnAnyEnumElement(@Mocked MyEnum anyEnum) {
        assertNull(MyEnum.First.getDescription());
        assertNull(MyEnum.Second.getDescription());
        assertNull(anyEnum.getDescription());

        new Verifications() {
            {
                MyEnum.Second.getDescription();
                times = 1;
            }
        };
    }

    /**
     * Mock specific enum elements by using two mock instances.
     *
     * @param mock1
     *            the mock 1
     * @param mock2
     *            the mock 2
     */
    @Test
    void mockSpecificEnumElementsByUsingTwoMockInstances(@Mocked MyEnum mock1, @Mocked MyEnum mock2) {
        new Expectations() {
            {
                MyEnum.First.getValue(anyDouble);
                result = 12.3;
                MyEnum.Second.getValue(anyDouble);
                result = -5.01;
            }
        };

        assertEquals(12.3, MyEnum.First.getValue(2.5), 0.0);
        assertEquals(-5.01, MyEnum.Second.getValue(1), 0.0);
    }

    /**
     * Mock specific enum elements even when using A single mock instance.
     *
     * @param unused
     *            the unused
     */
    @Test
    void mockSpecificEnumElementsEvenWhenUsingASingleMockInstance(@Mocked MyEnum unused) {
        new Expectations() {
            {
                MyEnum.First.getValue(anyDouble);
                result = 12.3;
                MyEnum.Second.getValue(anyDouble);
                result = -5.01;
            }
        };

        assertEquals(-5.01, MyEnum.Second.getValue(1), 0.0);
        assertEquals(12.3, MyEnum.First.getValue(2.5), 0.0);

        new Verifications() {
            {
                MyEnum.First.getValue(2.5);
                MyEnum.Second.getValue(1);
            }
        };
    }

    /**
     * Mock non abstract methods in enum with abstract method.
     *
     * @param tm
     *            the tm
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void mockNonAbstractMethodsInEnumWithAbstractMethod(@Mocked final TimeUnit tm) throws Exception {
        new Expectations() {
            {
                tm.convert(anyLong, TimeUnit.SECONDS);
                result = 1L;
                tm.sleep(anyLong);
            }
        };

        assertEquals(1, tm.convert(1000, TimeUnit.SECONDS));
        tm.sleep(10000);
    }

    /**
     * The Enum EnumWithValueSpecificMethods.
     */
    public enum EnumWithValueSpecificMethods {

        /** The One. */
        One {
            @Override
            public int getValue() {
                return 1;
            }

            @Override
            public String getDescription() {
                return "one";
            }
        },

        /** The Two. */
        Two {
            @Override
            public int getValue() {
                return 2;
            }

            @Override
            public String getDescription() {
                return "two";
            }
        };

        /**
         * Gets the value.
         *
         * @return the value
         */
        public abstract int getValue();

        /**
         * Gets the description.
         *
         * @return the description
         */
        @SuppressWarnings("unused")
        public String getDescription() {
            return String.valueOf(getValue());
        }
    }

    /**
     * Mock enum with value specific methods.
     *
     * @param mockedEnum
     *            the mocked enum
     */
    @Test
    void mockEnumWithValueSpecificMethods(@Mocked EnumWithValueSpecificMethods mockedEnum) {
        new Expectations() {
            {
                EnumWithValueSpecificMethods.One.getValue();
                result = 123;
                EnumWithValueSpecificMethods.Two.getValue();
                result = -45;

                EnumWithValueSpecificMethods.One.getDescription();
                result = "1";
                EnumWithValueSpecificMethods.Two.getDescription();
                result = "2";
            }
        };

        assertEquals(123, EnumWithValueSpecificMethods.One.getValue());
        assertEquals(-45, EnumWithValueSpecificMethods.Two.getValue());
        assertEquals("1", EnumWithValueSpecificMethods.One.getDescription());
        assertEquals("2", EnumWithValueSpecificMethods.Two.getDescription());
    }

    /**
     * The Enum Foo.
     */
    enum Foo {
        /** The foo. */
        FOO;

        /**
         * Value.
         *
         * @return the string
         */
        String value() {
            return "foo";
        }
    }

    /**
     * The Interface InterfaceWhichReturnsAnEnum.
     */
    interface InterfaceWhichReturnsAnEnum {
        /**
         * Gets the foo.
         *
         * @return the foo
         */
        Foo getFoo();
    }

    /**
     * Cascaded enum.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadedEnum(@Mocked final InterfaceWhichReturnsAnEnum mock) {
        final Foo foo = Foo.FOO;

        new Expectations() {
            {
                mock.getFoo();
                result = foo;
            }
        };

        Foo cascadedFoo = mock.getFoo();
        assertSame(foo, cascadedFoo);
        assertEquals("foo", foo.value());
    }
}
