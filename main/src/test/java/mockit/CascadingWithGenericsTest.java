package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class CascadingWithGenericsTest.
 */
@ExtendWith(JMockitExtension.class)
@TestMethodOrder(MethodName.class)
class CascadingWithGenericsTest {

    /**
     * The Class Foo.
     */
    static class Foo {

        /**
         * Return type with wildcard.
         *
         * @return the callable
         */
        Callable<?> returnTypeWithWildcard() {
            return null;
        }

        /**
         * Return type with bounded type variable.
         *
         * @param <RT>
         *            the generic type
         *
         * @return the rt
         */
        <RT extends Baz> RT returnTypeWithBoundedTypeVariable() {
            return null;
        }

        /**
         * Generic method with non mockable bounded type variable and class parameter.
         *
         * @param <N>
         *            the number type
         * @param c
         *            the c
         *
         * @return the n
         */
        <N extends Number> N genericMethodWithNonMockableBoundedTypeVariableAndClassParameter(
                @SuppressWarnings("unused") Class<N> c) {
            return null;
        }

        /**
         * Generic method with bounded type variable and class parameter.
         *
         * @param <RT>
         *            the generic type
         * @param cl
         *            the cl
         *
         * @return the rt
         */
        <RT extends Bar> RT genericMethodWithBoundedTypeVariableAndClassParameter(
                @SuppressWarnings("unused") Class<RT> cl) {
            return null;
        }

        /**
         * Return type with multiple type variables.
         *
         * @param <T1>
         *            the generic type
         * @param <T2>
         *            the generic type
         *
         * @return the pair
         */
        <T1 extends Baz, T2 extends List<? extends Number>> Pair<T1, T2> returnTypeWithMultipleTypeVariables() {
            return null;
        }

        /**
         * Return generic type with type argument.
         *
         * @return the callable
         */
        Callable<Baz> returnGenericTypeWithTypeArgument() {
            return null;
        }

        /**
         * Bar.
         *
         * @return the bar
         */
        Bar bar() {
            return null;
        }
    }

    /**
     * The Interface Pair.
     *
     * @param <K>
     *            the key type
     * @param <V>
     *            the value type
     */
    @SuppressWarnings("unused")
    public interface Pair<K, V> {
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
         * Static method.
         *
         * @return the string
         */
        static String staticMethod() {
            return "notMocked";
        }
    }

    /**
     * The Class SubBar.
     */
    static final class SubBar extends Bar {
    }

    /**
     * The Interface Baz.
     */
    public interface Baz {
        /**
         * Gets the date.
         *
         * @return the date
         */
        Date getDate();
    }

    /**
     * Cascade one level during replay.
     *
     * @param foo
     *            the foo
     */
    @Test
    void cascadeOneLevelDuringReplay(@Mocked Foo foo) {
        assertNotNull(foo.returnTypeWithWildcard());
        assertNotNull(foo.returnTypeWithBoundedTypeVariable());

        Pair<Baz, List<Integer>> x = foo.returnTypeWithMultipleTypeVariables();
        assertNotNull(x);
    }

    /**
     * Cascade one level during record.
     *
     * @param action
     *            the action
     * @param mockFoo
     *            the mock foo
     */
    @Test
    void cascadeOneLevelDuringRecord(@Mocked Callable<String> action, @Mocked Foo mockFoo) {
        Foo foo = new Foo();
        Callable<?> cascaded = foo.returnTypeWithWildcard();

        assertSame(action, cascaded);
    }

    /**
     * Cascade two levels during record.
     *
     * @param mockFoo
     *            the mock foo
     */
    @Test
    void cascadeTwoLevelsDuringRecord(@Mocked final Foo mockFoo) {
        final Date now = new Date();

        new Expectations() {
            {
                mockFoo.returnTypeWithBoundedTypeVariable().getDate();
                result = now;
            }
        };

        Foo foo = new Foo();
        assertSame(now, foo.returnTypeWithBoundedTypeVariable().getDate());
    }

    /**
     * The Class GenericFoo.
     *
     * @param <T>
     *            the generic type
     * @param <U>
     *            the generic type
     */
    static class GenericFoo<T, U extends Bar> {

        /**
         * Return type with unbounded type variable.
         *
         * @return the t
         */
        T returnTypeWithUnboundedTypeVariable() {
            return null;
        }

        /**
         * Return type with bounded type variable.
         *
         * @return the u
         */
        U returnTypeWithBoundedTypeVariable() {
            return null;
        }
    }

    /**
     * Cascade generic methods.
     *
     * @param foo
     *            the foo
     */
    @Test
    void cascadeGenericMethods(@Mocked GenericFoo<Baz, SubBar> foo) {
        Baz t = foo.returnTypeWithUnboundedTypeVariable();
        assertNotNull(t);

        SubBar u = foo.returnTypeWithBoundedTypeVariable();
        assertNotNull(u);
    }

    /**
     * The Class A.
     */
    static class A {
        /**
         * Gets the b.
         *
         * @return the b
         */
        B<?> getB() {
            return null;
        }
    }

    /**
     * The Class B.
     *
     * @param <T>
     *            the generic type
     */
    static class B<T> {
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
     * Cascade on method returning A parameterized class with A generic method.
     *
     * @param a
     *            the a
     */
    @Test
    void cascadeOnMethodReturningAParameterizedClassWithAGenericMethod(@Injectable final A a) {
        new Expectations() {
            {
                a.getB().getValue();
                result = "test";
            }
        };

        assertEquals("test", a.getB().getValue());
    }

    /**
     * The Class C.
     *
     * @param <T>
     *            the generic type
     */
    @SuppressWarnings("unused")
    static class C<T> {
    }

    /**
     * The Class D.
     */
    static class D extends C<Foo> {
        /**
         * Do something.
         *
         * @param <T>
         *            the generic type
         *
         * @return the t
         */
        <T extends Bar> T doSomething() {
            return null;
        }
    }

    /**
     * Cascade from generic method using type parameter of same name as type parameter from base class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromGenericMethodUsingTypeParameterOfSameNameAsTypeParameterFromBaseClass(@Mocked D mock) {
        Bar cascaded = mock.doSomething();

        assertNotNull(cascaded);
    }

    /**
     * The Class Factory.
     */
    static class Factory {

        /**
         * Bar.
         *
         * @param <T>
         *            the generic type
         *
         * @return the t
         */
        static <T extends Bar> T bar() {
            return null;
        }

        /**
         * Bar.
         *
         * @param <T>
         *            the generic type
         * @param c
         *            the c
         *
         * @return the t
         */
        static <T extends Bar> T bar(@SuppressWarnings("UnusedParameters") Class<T> c) {
            return null;
        }

        /**
         * Static init.
         *
         * @return the with static init
         */
        WithStaticInit staticInit() {
            return null;
        }
    }

    /**
     * The Class WithStaticInit.
     */
    static class WithStaticInit {

        /** The Constant T. */
        static final Bar T = Factory.bar();

        /** The Constant S. */
        static final SubBar S = Factory.bar(SubBar.class);
    }

    /**
     * Cascade during static initialization of cascaded class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeDuringStaticInitializationOfCascadedClass(@Mocked Factory mock) {
        assertNotNull(mock.staticInit());
        assertNotNull(WithStaticInit.T);
        assertNotNull(WithStaticInit.S);
    }

    /**
     * Cascade from generic method where concrete return type is given by class parameter but is not mockable.
     *
     * @param foo
     *            the foo
     */
    @Test
    void cascadeFromGenericMethodWhereConcreteReturnTypeIsGivenByClassParameterButIsNotMockable(@Mocked Foo foo) {
        Integer n = foo.genericMethodWithNonMockableBoundedTypeVariableAndClassParameter(Integer.class);

        assertNotNull(n);
    }

    /**
     * Cascade from generic method where concrete return type is given by class parameter.
     *
     * @param foo
     *            the foo
     */
    @Test
    void cascadeFromGenericMethodWhereConcreteReturnTypeIsGivenByClassParameter(@Mocked Foo foo) {
        SubBar subBar = foo.genericMethodWithBoundedTypeVariableAndClassParameter(SubBar.class);

        assertNotNull(subBar);
    }

    /**
     * Cascade from generic method whose return type comes from parameter on owner type.
     *
     * @param foo
     *            the foo
     * @param cascadedBaz
     *            the cascaded baz
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void cascadeFromGenericMethodWhoseReturnTypeComesFromParameterOnOwnerType(@Mocked Foo foo,
            @Mocked final Baz cascadedBaz) throws Exception {
        final Date date = new Date();
        new Expectations() {
            {
                cascadedBaz.getDate();
                result = date;
            }
        };

        Callable<Baz> callable = foo.returnGenericTypeWithTypeArgument();
        Baz baz = callable.call();

        assertSame(cascadedBaz, baz);
        assertSame(date, baz.getDate());
    }

    /**
     * The Interface GenericInterface.
     *
     * @param <T>
     *            the generic type
     */
    public interface GenericInterface<T> {
        /**
         * Save.
         *
         * @param <S>
         *            the generic type
         * @param entity
         *            the entity
         *
         * @return the s
         */
        <S extends T> S save(S entity);
    }

    /**
     * The Interface ConcreteInterface.
     */
    public interface ConcreteInterface extends GenericInterface<Foo> {
    }

    /**
     * Cascading from generic method whose type parameter extends another.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadingFromGenericMethodWhoseTypeParameterExtendsAnother(@Mocked ConcreteInterface mock) {
        Foo value = new Foo();

        Foo saved = mock.save(value);

        assertNotNull(saved);
        assertNotSame(value, saved);
    }

    /**
     * The Interface GenericInterfaceWithBoundedTypeParameter.
     *
     * @param <B>
     *            the generic type
     */
    public interface GenericInterfaceWithBoundedTypeParameter<B extends Serializable> {
        /**
         * Gets the.
         *
         * @param id
         *            the id
         *
         * @return the b
         */
        B get(int id);
    }

    /**
     * Cascade from method returning A type variable.
     *
     * @param <T>
     *            the generic type
     * @param mock
     *            the mock
     */
    @Test
    <T extends Serializable> void cascadeFromMethodReturningATypeVariable(
            @Mocked final GenericInterfaceWithBoundedTypeParameter<T> mock) {
        new Expectations() {
            {
                mock.get(1);
                result = "test";
                mock.get(2);
                result = null;
            }
        };

        assertEquals("test", mock.get(1));
        assertNull(mock.get(2));
    }

    /**
     * The Class TypeWithUnusedTypeParameterInGenericMethod.
     */
    static class TypeWithUnusedTypeParameterInGenericMethod {
        /**
         * Foo.
         *
         * @param <U>
         *            the generic type
         *
         * @return the foo
         */
        @SuppressWarnings("unused")
        <U> Foo foo() {
            return null;
        }
    }

    /**
     * Cascade from method having unused type parameter.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromMethodHavingUnusedTypeParameter(@Mocked TypeWithUnusedTypeParameterInGenericMethod mock) {
        Foo foo = mock.foo();
        Bar bar = foo.bar();
        assertNotNull(bar);
    }

    /**
     * Cascade from generic method whose return type resolves to another generic type.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromGenericMethodWhoseReturnTypeResolvesToAnotherGenericType(@Mocked B<C<?>> mock) {
        C<?> c = mock.getValue();

        assertNotNull(c);
    }

    /**
     * The Interface BaseGenericInterface.
     *
     * @param <B>
     *            the generic type
     */
    public interface BaseGenericInterface<B> {
        /**
         * Generic method.
         *
         * @return the b
         */
        B genericMethod();
    }

    /**
     * The Interface GenericSubInterface.
     *
     * @param <S>
     *            the generic type
     */
    public interface GenericSubInterface<S> extends BaseGenericInterface<S> {
    }

    /**
     * The Interface NonGenericInterface.
     */
    public interface NonGenericInterface extends GenericSubInterface<Bar> {
    }

    /**
     * Cascade from generic method defined two levels deep in inheritance hierarchy.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromGenericMethodDefinedTwoLevelsDeepInInheritanceHierarchy(@Mocked NonGenericInterface mock) {
        Bar cascadedResult = mock.genericMethod();

        assertNotNull(cascadedResult);
    }

    /**
     * The Interface NonPublicInterfaceWithGenericMethod.
     */
    interface NonPublicInterfaceWithGenericMethod {
        /**
         * Do something.
         *
         * @param <T>
         *            the generic type
         *
         * @return the t
         */
        <T extends Runnable> T doSomething();
    }

    /**
     * Cascade from generic method of non public interface.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromGenericMethodOfNonPublicInterface(@Mocked NonPublicInterfaceWithGenericMethod mock) {
        Runnable result = mock.doSomething();

        assertNotNull(result);
    }

    /**
     * The Interface FactoryInterface.
     */
    public interface FactoryInterface {
        /**
         * Generic with class.
         *
         * @param <T>
         *            the generic type
         * @param type
         *            the type
         *
         * @return the t
         */
        <T> T genericWithClass(Class<T> type);
    }

    /**
     * Cascade from generic method with class parameter of mocked interface.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromGenericMethodWithClassParameterOfMockedInterface(@Mocked FactoryInterface mock) {
        Foo cascaded = mock.genericWithClass(Foo.class);

        assertNotNull(cascaded);
    }

    /**
     * The Class Outer.
     *
     * @param <T>
     *            the generic type
     */
    @SuppressWarnings("unused")
    static class Outer<T> {
        /**
         * The Class Inner.
         */
        class Inner {
        }
    }

    /**
     * The Class Client.
     */
    static class Client {
        /**
         * Do something.
         *
         * @return the outer. inner
         */
        Outer<String>.Inner doSomething() {
            return null;
        }
    }

    /**
     * Cascade from method returning inner instance of generic class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromMethodReturningInnerInstanceOfGenericClass(@Mocked final Client mock) {
        final Outer<?>.Inner innerInstance = new Outer().new Inner();

        new Expectations() {
            {
                mock.doSomething();
                result = innerInstance;
            }
        };

        assertSame(innerInstance, mock.doSomething());
    }

    /**
     * The Class SubB.
     *
     * @param <T>
     *            the generic type
     */
    static class SubB<T> extends B<T> {
    }

    /**
     * The Class ClassWithMethodReturningGenericClassInstance.
     */
    static class ClassWithMethodReturningGenericClassInstance {
        /**
         * Do something.
         *
         * @return the sub B
         */
        SubB<C<?>> doSomething() {
            return null;
        }
    }

    /**
     * Cascade from method returning instance of generic subclass then from generic method of generic base class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromMethodReturningInstanceOfGenericSubclassThenFromGenericMethodOfGenericBaseClass(
            @Mocked ClassWithMethodReturningGenericClassInstance mock) {
        SubB<C<?>> cascade1 = mock.doSomething();
        C<?> cascade2 = cascade1.getValue();

        assertNotNull(cascade2);
    }

    /**
     * The Interface InterfaceWithGenericMethod.
     *
     * @param <T>
     *            the generic type
     */
    public interface InterfaceWithGenericMethod<T> {
        /**
         * Generic method.
         *
         * @return the t
         */
        @SuppressWarnings("unused")
        T genericMethod();
    }

    /**
     * The Class BaseClass.
     */
    static class BaseClass {
        /**
         * Generic method.
         *
         * @return the bar
         */
        public Bar genericMethod() {
            return null;
        }
    }

    /**
     * The Class SubClass.
     */
    static class SubClass extends BaseClass implements InterfaceWithGenericMethod<Bar> {
    }

    /**
     * Cascade from generic interface method implemented in base class of mocked sub class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void cascadeFromGenericInterfaceMethodImplementedInBaseClassOfMockedSubClass(@Mocked SubClass mock) {
        Bar cascaded = mock.genericMethod();
        assertNotNull(cascaded);
    }
}
