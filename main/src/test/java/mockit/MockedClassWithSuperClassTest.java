package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.Writer;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class MockedClassWithSuperClassTest.
 */
@ExtendWith(JMockitExtension.class)
class MockedClassWithSuperClassTest {

    /**
     * The Class SubclassOfJREClass.
     */
    static class SubclassOfJREClass extends Writer {
        @Override
        public void write(@NonNull char[] cbuf, int off, int len) {
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * The Class BaseClass.
     */
    static class BaseClass {

        /**
         * Do something.
         *
         * @return the int
         */
        protected int doSomething() {
            return 123;
        }

        /**
         * Static method.
         *
         * @return the int
         */
        static int staticMethod() {
            return -1;
        }
    }

    /**
     * The Class Subclass.
     */
    public static class Subclass extends BaseClass {
        /**
         * Gets the single instance of Subclass.
         *
         * @return single instance of Subclass
         */
        BaseClass getInstance() {
            return this;
        }
    }

    /**
     * Mocked class extending JRE class.
     *
     * @param mock
     *            the mock
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void mockedClassExtendingJREClass(@Mocked SubclassOfJREClass mock) throws Exception {
        // Mocked:
        assertSame(mock, mock.append("a"));
        assertSame(mock, mock.append('a'));
        mock.close();

        // Not mocked:
        Writer w = new Writer() {
            @Override
            public void write(@NonNull char[] cbuf, int off, int len) {
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        assertSame(w, w.append("Test1"));
        assertSame(w, w.append('b'));

        try {
            new SubclassOfJREClass() {
            }.close();
        } catch (UnsupportedOperationException ignore) {
        }
    }

    /**
     * Mocked class extending non JRE class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockedClassExtendingNonJREClass(@Mocked final Subclass mock) {
        new Expectations() {
            {
                mock.doSomething();
                result = 45;
                times = 2;
            }
        };

        // Mocked:
        assertEquals(45, mock.doSomething());
        assertEquals(45, new Subclass().doSomething());

        // Not mocked:
        assertEquals(123, new Subclass() {
        }.doSomething());

        BaseClass b1 = new BaseClass();
        BaseClass b2 = new BaseClass() {
            @Override
            protected int doSomething() {
                return super.doSomething() - 23;
            }
        };
        assertEquals(123, b1.doSomething());
        assertEquals(100, b2.doSomething());
    }

    /**
     * Cascading subclass with method returning cascaded base class instance.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadingSubclassWithMethodReturningCascadedBaseClassInstance(@Mocked Subclass mock) {
        // The subclass is already mocked at this point, when the cascaded instance gets created.
        BaseClass cascaded = mock.getInstance();

        assertEquals(0, cascaded.doSomething());
        assertEquals(0, mock.doSomething());
    }

    /**
     * Record expectation on static method from base class.
     *
     * @param unused
     *            the unused
     */
    @Test
    void recordExpectationOnStaticMethodFromBaseClass(@Mocked Subclass unused) {
        new Expectations() {
            {
                BaseClass.staticMethod();
                result = 123;
            }
        };

        assertEquals(123, BaseClass.staticMethod());
    }

    /**
     * The Class BaseClassWithConstructor.
     */
    static class BaseClassWithConstructor {
        /**
         * Instantiates a new base class with constructor.
         *
         * @param b
         *            the b
         */
        BaseClassWithConstructor(@SuppressWarnings("unused") boolean b) {
        }
    }

    /**
     * The Class DerivedClass.
     */
    static class DerivedClass extends BaseClassWithConstructor {

        /**
         * Instantiates a new derived class.
         */
        protected DerivedClass() {
            // TRYCATCHBLOCK instruction appears before call to super, which caused a VerifyError.
            super(true);
            try {
                doSomething();
            } catch (RuntimeException ignore) {
            }
        }

        /**
         * Do something.
         */
        private void doSomething() {
        }
    }

    /**
     * Mock subclass with constructor containing try catch.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockSubclassWithConstructorContainingTryCatch(@Mocked DerivedClass mock) {
        new DerivedClass();
    }

    /**
     * The Class Subclass2.
     */
    static class Subclass2 extends BaseClass {
    }

    /**
     * Record same method on different mocked subclasses.
     *
     * @param mock1
     *            the mock 1
     * @param mock2
     *            the mock 2
     */
    @Test
    void recordSameMethodOnDifferentMockedSubclasses(@Mocked final Subclass mock1, @Mocked final Subclass2 mock2) {
        new Expectations() {
            {
                mock1.doSomething();
                result = 1;
                mock2.doSomething();
                result = 2;
            }
        };

        assertEquals(1, mock1.doSomething());
        assertEquals(2, mock2.doSomething());
    }

    /**
     * Record method on mocked base class but replay on subclass instance.
     *
     * @param baseMock
     *            the base mock
     */
    @Test
    void recordMethodOnMockedBaseClassButReplayOnSubclassInstance(@Mocked final BaseClass baseMock) {
        new Expectations() {
            {
                baseMock.doSomething();
                result = 45;
            }
        };

        Subclass derived = new Subclass();
        assertEquals(123, derived.doSomething());
        assertEquals(45, baseMock.doSomething());
    }

    /**
     * The Class BaseClassWithTwoConstructors.
     */
    static class BaseClassWithTwoConstructors {

        /** The value. */
        final int value;

        /**
         * Instantiates a new base class with two constructors.
         */
        @SuppressWarnings("unused")
        BaseClassWithTwoConstructors() {
            value = 1;
        }

        /**
         * Instantiates a new base class with two constructors.
         *
         * @param value
         *            the value
         */
        BaseClassWithTwoConstructors(int value) {
            this.value = value;
        }
    }

    /**
     * The Class SubclassWithOneConstructor.
     */
    static class SubclassWithOneConstructor extends BaseClassWithTwoConstructors {
        /**
         * Instantiates a new subclass with one constructor.
         */
        SubclassWithOneConstructor() {
            super(2);
        }
    }

    /**
     * The Class SecondLevelSubclass1.
     */
    static final class SecondLevelSubclass1 extends SubclassWithOneConstructor {
    }

    /**
     * Mock direct subclass of base with two constructors but instantiate second level subclass.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockDirectSubclassOfBaseWithTwoConstructorsButInstantiateSecondLevelSubclass(
            @Mocked SubclassWithOneConstructor mock) {
        int value = new SecondLevelSubclass1().value;
        assertEquals(2, value);
    }

    /**
     * The Class SecondLevelSubclass2.
     */
    static final class SecondLevelSubclass2 extends SubclassWithOneConstructor {
    }

    /**
     * Mock one subclass but instantiate unmocked sibling.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockOneSubclassButInstantiateUnmockedSibling(@Mocked SecondLevelSubclass1 mock) {
        int value = new SecondLevelSubclass2().value;
        assertEquals(2, value);
    }
}
