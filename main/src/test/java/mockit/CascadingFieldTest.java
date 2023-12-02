package mockit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

/**
 * The Class CascadingFieldTest.
 */
@FixMethodOrder(NAME_ASCENDING)
public final class CascadingFieldTest {

    /**
     * The Class Foo.
     */
    static class Foo {

        /**
         * Gets the bar.
         *
         * @return the bar
         */
        Bar getBar() {
            return null;
        }

        /**
         * Global bar.
         *
         * @return the bar
         */
        static Bar globalBar() {
            return null;
        }

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
         * Gets the int value.
         *
         * @return the int value
         */
        int getIntValue() {
            return 1;
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
         * Gets the string value.
         *
         * @return the string value
         */
        String getStringValue() {
            return "abc";
        }

        /**
         * Gets the date.
         *
         * @return the date
         */
        public final Date getDate() {
            return null;
        }

        /**
         * Gets the list.
         *
         * @return the list
         */
        final List<Integer> getList() {
            return null;
        }
    }

    /**
     * The Class Bar.
     */
    static class Bar {

        /**
         * Instantiates a new bar.
         */
        Bar() {
            throw new RuntimeException();
        }

        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething() {
            return 1;
        }

        /**
         * Checks if is done.
         *
         * @return true, if is done
         */
        boolean isDone() {
            return false;
        }

        /**
         * Gets the short.
         *
         * @return the short
         */
        Short getShort() {
            return 1;
        }

        /**
         * Gets the list.
         *
         * @return the list
         */
        List<?> getList() {
            return null;
        }

        /**
         * Gets the baz.
         *
         * @return the baz
         */
        Baz getBaz() {
            return null;
        }

        /**
         * Gets the task.
         *
         * @return the task
         */
        Runnable getTask() {
            return null;
        }
    }

    /**
     * The Class Baz.
     */
    static final class Baz {

        /** The e. */
        final E e;

        /**
         * Instantiates a new baz.
         *
         * @param e
         *            the e
         */
        Baz(E e) {
            this.e = e;
        }

        /**
         * Gets the e.
         *
         * @return the e
         */
        E getE() {
            return e;
        }

        /**
         * Do something.
         */
        void doSomething() {
        }
    }

    /**
     * The Enum E.
     */
    public enum E {
        /** The a. */
        A,
        /** The b. */
        B
    }

    /**
     * The Interface A.
     */
    public interface A {
        /**
         * Gets the b.
         *
         * @return the b
         */
        B getB();
    }

    /**
     * The Interface B.
     */
    public interface B {
        /**
         * Gets the c.
         *
         * @return the c
         */
        C getC();
    }

    /**
     * The Interface C.
     */
    public interface C {
    }

    /** The foo. */
    @Mocked
    Foo foo;

    /** The a. */
    @Mocked
    A a;

    /**
     * Record common expectations.
     */
    @Before
    public void recordCommonExpectations() {
        new Expectations() {
            {
                Bar bar = foo.getBar();
                minTimes = 0;
                bar.isDone();
                result = true;
                minTimes = 0;
            }
        };
    }

    /**
     * Obtain cascaded instances at all levels.
     */
    @Test
    public void obtainCascadedInstancesAtAllLevels() {
        assertNotNull(foo.getBar());
        assertNotNull(foo.getBar().getList());
        assertNotNull(foo.getBar().getBaz());
        assertNotNull(foo.getBar().getTask());

        B b = a.getB();
        assertNotNull(b);
        assertNotNull(b.getC());
    }

    /**
     * Obtain cascaded instances at all levels again.
     */
    @Test
    public void obtainCascadedInstancesAtAllLevelsAgain() {
        Bar bar = foo.getBar();
        assertNotNull(bar);
        assertNotNull(bar.getList());
        assertNotNull(bar.getBaz());
        assertNotNull(bar.getTask());

        assertNotNull(a.getB());
        assertNotNull(a.getB().getC());
    }

    /**
     * Cascade one level.
     */
    @Test
    public void cascadeOneLevel() {
        assertTrue(foo.getBar().isDone());
        assertEquals(0, foo.getBar().doSomething());
        assertEquals(0, Foo.globalBar().doSomething());
        assertNotSame(foo.getBar(), Foo.globalBar());
        assertEquals(0, foo.getBar().getShort().intValue());

        foo.doSomething("test");
        assertEquals(0, foo.getIntValue());
        assertFalse(foo.getBooleanValue());
        assertNull(foo.getStringValue());
        assertNotNull(foo.getDate());
        assertTrue(foo.getList().isEmpty());

        new Verifications() {
            {
                foo.doSomething(anyString);
            }
        };
    }

    /**
     * Exercise cascading mock again.
     */
    @Test
    public void exerciseCascadingMockAgain() {
        assertTrue(foo.getBar().isDone());
    }

    /**
     * Record unambiguous expectations producing different cascaded instances.
     *
     * @param foo1
     *            the foo 1
     * @param foo2
     *            the foo 2
     */
    @Test
    public void recordUnambiguousExpectationsProducingDifferentCascadedInstances(@Mocked final Foo foo1,
            @Mocked final Foo foo2) {
        new Expectations() {
            {
                Date c1 = foo1.getDate();
                Date c2 = foo2.getDate();
                assertNotSame(c1, c2);
            }
        };

        Date d1 = foo1.getDate();
        Date d2 = foo2.getDate();
        assertNotSame(d1, d2);
    }

    /**
     * Record ambiguous expectations on instance method producing the same cascaded instance.
     */
    @Test
    public void recordAmbiguousExpectationsOnInstanceMethodProducingTheSameCascadedInstance() {
        new Expectations() {
            {
                Bar c1 = foo.getBar();
                Bar c2 = foo.getBar();
                assertSame(c1, c2);
            }
        };

        Bar b1 = foo.getBar();
        Bar b2 = foo.getBar();
        assertSame(b1, b2);
    }

    /**
     * Record ambiguous expectations on static method producing the same cascaded instance.
     */
    @Test
    public void recordAmbiguousExpectationsOnStaticMethodProducingTheSameCascadedInstance() {
        new Expectations() {
            {
                Bar c1 = Foo.globalBar();
                Bar c2 = Foo.globalBar();
                assertSame(c1, c2);
            }
        };

        Bar b1 = Foo.globalBar();
        Bar b2 = Foo.globalBar();
        assertSame(b1, b2);
    }

    /**
     * Record ambiguous expectation with multiple cascading candidates followed by expectation recorded on first
     * candidate.
     *
     * @param bar1
     *            the bar 1
     * @param bar2
     *            the bar 2
     */
    @Test
    public void recordAmbiguousExpectationWithMultipleCascadingCandidatesFollowedByExpectationRecordedOnFirstCandidate(
            @Injectable final Bar bar1, @Injectable Bar bar2) {
        new Expectations() {
            {
                foo.getBar();
                bar1.doSomething();
            }
        };

        foo.getBar();
        bar1.doSomething();
    }

    /**
     * The Class AnotherFoo.
     */
    static final class AnotherFoo {
        /**
         * Gets the bar.
         *
         * @return the bar
         */
        Bar getBar() {
            return null;
        }
    }

    /** The another foo. */
    @Mocked
    AnotherFoo anotherFoo;

    /**
     * Cascading mock field.
     */
    @Test
    public void cascadingMockField() {
        new Expectations() {
            {
                anotherFoo.getBar().doSomething();
                result = 123;
            }
        };

        assertEquals(123, new AnotherFoo().getBar().doSomething());
    }

    /**
     * Cascading instance accessed from delegate method.
     */
    @Test
    public void cascadingInstanceAccessedFromDelegateMethod() {
        new Expectations() {
            {
                foo.getIntValue();
                result = new Delegate<Object>() {
                    @Mock
                    int delegate() {
                        return foo.getBar().doSomething();
                    }
                };
            }
        };

        assertEquals(0, foo.getIntValue());
    }

    /** The baz creator and consumer. */
    @Mocked
    BazCreatorAndConsumer bazCreatorAndConsumer;

    /**
     * The Class BazCreatorAndConsumer.
     */
    static class BazCreatorAndConsumer {

        /**
         * Creates the.
         *
         * @return the baz
         */
        Baz create() {
            return null;
        }

        /**
         * Consume.
         *
         * @param arg
         *            the arg
         */
        void consume(Baz arg) {
            arg.toString();
        }
    }

    /**
     * Call method on non cascaded instance from custom argument matcher with cascaded instance also created.
     */
    @Test
    public void callMethodOnNonCascadedInstanceFromCustomArgumentMatcherWithCascadedInstanceAlsoCreated() {
        Baz nonCascadedInstance = new Baz(E.A);
        Baz cascadedInstance = bazCreatorAndConsumer.create();
        assertNotSame(nonCascadedInstance, cascadedInstance);

        bazCreatorAndConsumer.consume(nonCascadedInstance);

        new Verifications() {
            {
                bazCreatorAndConsumer.consume(with(new Delegate<Baz>() {
                    @SuppressWarnings("unused")
                    boolean matches(Baz actual) {
                        return actual.getE() == E.A;
                    }
                }));
            }
        };
    }

    // Tests for cascaded instances obtained from generic methods //////////////////////////////////////////////////////

    /**
     * The Class GenericBaseClass1.
     *
     * @param <T>
     *            the generic type
     */
    static class GenericBaseClass1<T> {
        /**
         * Gets the value.
         *
         * @return the value
         */
        T getValue() {
            return null;
        }
    }

    /**
     * Cascade generic method from specialized generic class.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void cascadeGenericMethodFromSpecializedGenericClass(@Mocked GenericBaseClass1<C> mock) {
        C value = mock.getValue();
        assertNotNull(value);
    }

    /**
     * The Class ConcreteSubclass1.
     */
    static class ConcreteSubclass1 extends GenericBaseClass1<A> {
    }

    /**
     * Cascade generic method of concrete subclass which extends generic class.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void cascadeGenericMethodOfConcreteSubclassWhichExtendsGenericClass(@Mocked final ConcreteSubclass1 mock) {
        new Expectations() {
            {
                mock.getValue().getB().getC();
                result = new C() {
                };
            }
        };

        A value = mock.getValue();
        assertNotNull(value);
        B b = value.getB();
        assertNotNull(b);
        assertNotNull(b.getC());

        new FullVerifications() {
            {
                mock.getValue().getB().getC();
            }
        };
    }

    /**
     * The Interface Ab.
     */
    interface Ab extends A {
    }

    /**
     * The Class GenericBaseClass2.
     *
     * @param <T>
     *            the generic type
     */
    static class GenericBaseClass2<T extends A> {
        /**
         * Gets the value.
         *
         * @return the value
         */
        T getValue() {
            return null;
        }
    }

    /**
     * The Class ConcreteSubclass2.
     */
    static class ConcreteSubclass2 extends GenericBaseClass2<Ab> {
    }

    /**
     * Cascade generic method of subclass which extends generic class with upper bound using interface.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void cascadeGenericMethodOfSubclassWhichExtendsGenericClassWithUpperBoundUsingInterface(
            @Mocked final ConcreteSubclass2 mock) {
        Ab value = mock.getValue();
        assertNotNull(value);
        value.getB().getC();

        new Verifications() {
            {
                mock.getValue().getB().getC();
                times = 1;
            }
        };
    }

    /**
     * Cascade generic method of subclass which extends generic class with upper bound only in verification block.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void cascadeGenericMethodOfSubclassWhichExtendsGenericClassWithUpperBoundOnlyInVerificationBlock(
            @Mocked final ConcreteSubclass2 mock) {
        new FullVerifications() {
            {
                Ab value = mock.getValue();
                times = 0;
                B b = value.getB();
                times = 0;
                b.getC();
                times = 0;
            }
        };
    }

    /**
     * The Class Action.
     */
    static final class Action implements A {
        @Override
        public B getB() {
            return null;
        }
    }

    /**
     * The Class ActionHolder.
     */
    static final class ActionHolder extends GenericBaseClass2<Action> {
    }

    /**
     * Cascade generic method of subclass which extends generic class with upper bound using class.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void cascadeGenericMethodOfSubclassWhichExtendsGenericClassWithUpperBoundUsingClass(
            @Mocked final ActionHolder mock) {
        new Expectations() {
            {
                mock.getValue().getB().getC();
            }
        };

        mock.getValue().getB().getC();
    }

    /**
     * The Class Base.
     *
     * @param <T>
     *            the generic type
     */
    static class Base<T extends Serializable> {
        /**
         * Value.
         *
         * @return the t
         */
        @SuppressWarnings("unchecked")
        T value() {
            return (T) Long.valueOf(123);
        }
    }

    /**
     * The Class Derived1.
     */
    class Derived1 extends Base<Long> {
    }

    /**
     * The Class Derived2.
     */
    class Derived2 extends Base<Long> {
    }

    /**
     * The Interface Factory1.
     */
    interface Factory1 {
        /**
         * Gets the 1.
         *
         * @return the 1
         */
        Derived1 get1();
    }

    /**
     * The Interface Factory2.
     */
    interface Factory2 {
        /**
         * Gets the 2.
         *
         * @return the 2
         */
        Derived2 get2();
    }

    /** The factory 1. */
    @Mocked
    Factory1 factory1;

    /** The factory 2. */
    @Mocked
    Factory2 factory2;

    /**
     * Use subclass mocked through cascading.
     */
    @Test
    public void useSubclassMockedThroughCascading() {
        Derived1 d1 = factory1.get1(); // cascade-mocks Derived1 (per-instance)
        Long v1 = d1.value();
        assertNull(v1);

        Long v2 = new Derived1().value(); // new instance, not mocked
        assertEquals(123, v2.longValue());
    }

    /**
     * Use subclass previously mocked through cascading while mocking sibling subclass.
     *
     * @param d2
     *            the d 2
     */
    @Test
    public void useSubclassPreviouslyMockedThroughCascadingWhileMockingSiblingSubclass(@Injectable Derived2 d2) {
        Long v1 = new Derived1().value();
        assertEquals(123, v1.longValue());

        Long v2 = d2.value();
        assertNull(v2);

        Long v3 = new Derived2().value();
        assertEquals(123, v3.longValue());
    }
}
