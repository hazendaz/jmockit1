/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static java.util.Arrays.asList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class GenericMockedTypesTest.
 */
@ExtendWith(JMockitExtension.class)
class GenericMockedTypesTest {

    /** The mock 2. */
    @Mocked
    Callable<Integer> mock2;

    /**
     * Mock generic interfaces.
     *
     * @param mock1
     *            the mock 1
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void mockGenericInterfaces(@Mocked final Callable<?> mock1) throws Exception {
        new Expectations() {
            {
                mock1.call();
                result = "mocked";
                mock2.call();
                result = 123;
            }
        };

        assertEquals("mocked", mock1.call());
        assertEquals(123, mock2.call().intValue());
    }

    /**
     * Obtain generic superclass from class generated for non generic interface.
     *
     * @param mock
     *            the mock
     */
    @Test
    void obtainGenericSuperclassFromClassGeneratedForNonGenericInterface(@Mocked Runnable mock) {
        Class<?> generatedClass = mock.getClass();
        Type genericSuperClass = generatedClass.getGenericSuperclass();

        // At one time, a "GenericSignatureFormatError: Signature Parse error: expected a class type
        // Remaining input: nullLjava/lang/Runnable;" would occur.
        assertSame(Object.class, genericSuperClass);
    }

    /**
     * Mock generic abstract class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockGenericAbstractClass(@Mocked final Dictionary<Integer, String> mock) {
        new Expectations() {
            {
                mock.put(123, "test");
                result = "mocked";
            }
        };

        assertEquals("mocked", mock.put(123, "test"));
    }

    @Test
    public void mockRawMapInterface(@SuppressWarnings("rawtypes") @Mocked final Map rawMap) {
        new Expectations() {
            {
                rawMap.get("test");
                result = new Object();
            }
        };

        Object value = rawMap.get("test");
        assertNotNull(value);
    }

    /**
     * The Interface InterfaceWithMethodParametersMixingGenericTypesAndArrays.
     */
    public interface InterfaceWithMethodParametersMixingGenericTypesAndArrays {

        /**
         * Do something.
         *
         * @param <T>
         *            the generic type
         * @param i
         *            the i
         * @param b
         *            the b
         */
        <T> void doSomething(int[] i, T b);

        /**
         * Do something.
         *
         * @param pc
         *            the pc
         * @param ii
         *            the ii
         */
        void doSomething(Callable<int[]> pc, int[] ii);

        /**
         * Do something.
         *
         * @param pc
         *            the pc
         * @param i
         *            the i
         * @param currencies
         *            the currencies
         * @param ii
         *            the ii
         */
        void doSomething(Callable<String> pc, int[] i, boolean[] currencies, int[] ii);
    }

    /**
     * Mock methods having generics and arrays.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockMethodsHavingGenericsAndArrays(@Mocked InterfaceWithMethodParametersMixingGenericTypesAndArrays mock) {
        mock.doSomething((Callable<int[]>) null, new int[] { 1, 2 });
        mock.doSomething(null, new int[] { 1, 2 }, null, new int[] { 3, 4, 5 });
    }

    /**
     * The Interface NonGenericInterfaceWithGenericMethods.
     */
    public interface NonGenericInterfaceWithGenericMethods {

        /**
         * Generic method with unbounded return type.
         *
         * @param <T>
         *            the generic type
         *
         * @return the t
         */
        <T> T genericMethodWithUnboundedReturnType();

        /**
         * Generic method with bounded return type.
         *
         * @param <T>
         *            the generic type
         *
         * @return the t
         */
        <T extends CharSequence> T genericMethodWithBoundedReturnType();
    }

    /**
     * Result from generic methods of non generic interface.
     *
     * @param mock
     *            the mock
     */
    @Test
    void resultFromGenericMethodsOfNonGenericInterface(@Mocked final NonGenericInterfaceWithGenericMethods mock) {
        new Expectations() {
            {
                mock.genericMethodWithUnboundedReturnType();
                result = 123;
                mock.genericMethodWithBoundedReturnType();
                result = "test";
            }
        };

        Object v1 = mock.genericMethodWithUnboundedReturnType();
        assertEquals(123, v1);

        Object v2 = mock.genericMethodWithBoundedReturnType();
        assertEquals("test", v2);
    }

    /**
     * The Class Item.
     */
    static class Item implements Serializable {

        private static final long serialVersionUID = 1L;
    }

    /**
     * The Class GenericContainer.
     *
     * @param <T>
     *            the generic type
     */
    static class GenericContainer<T extends Serializable> {
        /**
         * Gets the item.
         *
         * @return the item
         */
        final T getItem() {
            return null;
        }
    }

    /**
     * Creates the first level cascaded mock from type parameter.
     *
     * @param mockContainer
     *            the mock container
     */
    @Test
    void createFirstLevelCascadedMockFromTypeParameter(@Mocked GenericContainer<Item> mockContainer) {
        Serializable mock = mockContainer.getItem();

        assertSame(Item.class, mock.getClass());
    }

    /**
     * The Class Factory1.
     */
    static class Factory1 {
        /**
         * Gets the container.
         *
         * @return the container
         */
        static GenericContainer<Item> getContainer() {
            return null;
        }
    }

    /**
     * Creates the second level cascaded mock from type parameter in generic method resolved from first level return
     * type.
     *
     * @param mockFactory
     *            the mock factory
     */
    @Test
    void createSecondLevelCascadedMockFromTypeParameterInGenericMethodResolvedFromFirstLevelReturnType(
            @Mocked Factory1 mockFactory) {
        GenericContainer<Item> mockContainer = Factory1.getContainer();
        Serializable cascadedMock = mockContainer.getItem();

        assertNotNull(cascadedMock);
        assertSame(Item.class, cascadedMock.getClass());
    }

    /**
     * The Class ConcreteContainer.
     */
    static class ConcreteContainer extends GenericContainer<Item> {
    }

    /**
     * The Class Factory2.
     */
    static class Factory2 {
        /**
         * Gets the container.
         *
         * @return the container
         */
        ConcreteContainer getContainer() {
            return null;
        }
    }

    /**
     * Creates the second level cascaded mock from type parameter in base type of method return.
     *
     * @param mockFactory
     *            the mock factory
     */
    @Test
    void createSecondLevelCascadedMockFromTypeParameterInBaseTypeOfMethodReturn(@Mocked Factory2 mockFactory) {
        ConcreteContainer mockContainer = mockFactory.getContainer();
        Serializable cascadedMock = mockContainer.getItem();

        assertSame(Item.class, cascadedMock.getClass());
    }

    /**
     * The Class Collaborator.
     */
    static class Collaborator {
        /**
         * Do something.
         *
         * @return the runnable
         */
        Runnable doSomething() {
            return null;
        }
    }

    /**
     * The Class Collaborator2.
     */
    static class Collaborator2 {
    }

    /**
     * Cascading class with name starting with another mocked class.
     *
     * @param regularMock
     *            the regular mock
     * @param cascadingMock
     *            the cascading mock
     */
    @Test
    void cascadingClassWithNameStartingWithAnotherMockedClass(@Mocked final Collaborator regularMock,
            @Mocked Collaborator2 cascadingMock) {
        new Expectations() {
            {
                regularMock.doSomething();
            }
        };

        assertNotNull(regularMock.doSomething());
    }

    /**
     * The Interface InterfaceWithBoundedTypeParameter.
     *
     * @param <T>
     *            the generic type
     */
    public interface InterfaceWithBoundedTypeParameter<T extends Runnable> {
        /**
         * Gets the foo.
         *
         * @return the foo
         */
        T getFoo();
    }

    /**
     * Creates the cascaded mock from generic interface method which returns bounded type parameter.
     *
     * @param mock
     *            the mock
     */
    @Test
    void createCascadedMockFromGenericInterfaceMethodWhichReturnsBoundedTypeParameter(
            @Mocked InterfaceWithBoundedTypeParameter<?> mock) {
        Runnable foo = mock.getFoo();
        assertNotNull(foo);
        foo.run();
    }

    /**
     * The Interface InterfaceWhichExtendsInterfaceWithBoundedTypeParameter.
     *
     * @param <T>
     *            the generic type
     */
    public interface InterfaceWhichExtendsInterfaceWithBoundedTypeParameter<T extends Runnable>
            extends InterfaceWithBoundedTypeParameter<T> {
    }

    /**
     * Creates the cascaded mock from generic method defined in super interface with bounded type parameter.
     *
     * @param mock
     *            the mock
     */
    @Test
    void createCascadedMockFromGenericMethodDefinedInSuperInterfaceWithBoundedTypeParameter(
            @Mocked InterfaceWhichExtendsInterfaceWithBoundedTypeParameter<?> mock) {
        Runnable foo = mock.getFoo();
        assertNotNull(foo);
        foo.run();
    }

    /**
     * The Class Abc.
     */
    static class Abc {
    }

    /**
     * The Class GenericBase.
     *
     * @param <T>
     *            the generic type
     */
    static class GenericBase<T> {
        /**
         * Do something.
         *
         * @return the t
         */
        T doSomething() {
            return null;
        }
    }

    /**
     * The Class GenericSubclass.
     *
     * @param <T>
     *            the generic type
     */
    static class GenericSubclass<T> extends GenericBase<T> {
        /**
         * Gets the abc.
         *
         * @return the abc
         */
        T getAbc() {
            return null;
        }
    }

    /**
     * Creates the cascaded mock from generic subclass having same type parameter name as base class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void createCascadedMockFromGenericSubclassHavingSameTypeParameterNameAsBaseClass(
            @Mocked GenericSubclass<Abc> mock) {
        Abc abc = mock.getAbc();
        assertNotNull(abc);
    }

    /**
     * Mock generic class having type argument of array type.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockGenericClassHavingTypeArgumentOfArrayType(@Mocked GenericBase<String[]> mock) {
        String[] result = mock.doSomething();

        assertEquals(0, result.length);
    }

    /**
     * Mock generic class having type argument of array type with primitive component.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockGenericClassHavingTypeArgumentOfArrayTypeWithPrimitiveComponent(@Mocked GenericBase<int[]> mock) {
        int[] result = mock.doSomething();

        assertEquals(0, result.length);
    }

    /**
     * Mock generic class having type argument of array type with 2 D primitive component.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockGenericClassHavingTypeArgumentOfArrayTypeWith2DPrimitiveComponent(@Mocked GenericBase<int[][]> mock) {
        int[][] result = mock.doSomething();

        assertEquals(0, result.length);
    }

    /**
     * Mock generic class having type argument of array type with generic component.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockGenericClassHavingTypeArgumentOfArrayTypeWithGenericComponent(@Mocked GenericBase<List<?>[]> mock) {
        List<?>[] result = mock.doSomething();

        assertEquals(0, result.length);
    }

    /**
     * The Class DerivedClass.
     */
    static final class DerivedClass extends GenericBase<Number[]> {
    }

    /**
     * Mock class extending A generic base class having type argument of array type.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockClassExtendingAGenericBaseClassHavingTypeArgumentOfArrayType(@Mocked DerivedClass mock) {
        Number[] result = mock.doSomething();

        assertEquals(0, result.length);
    }

    /**
     * The Interface BaseGenericInterface.
     *
     * @param <V>
     *            the value type
     */
    public interface BaseGenericInterface<V> {
        /**
         * Do something.
         *
         * @return the v
         */
        V doSomething();
    }

    /**
     * The Interface DerivedGenericInterface.
     *
     * @param <V>
     *            the value type
     */
    public interface DerivedGenericInterface<V> extends BaseGenericInterface<List<V>> {
        /**
         * Do something else.
         *
         * @return the v
         */
        V doSomethingElse();
    }

    /**
     * Record generic interface method with return type given by type parameter dependent on another type parameter of
     * same name.
     *
     * @param dep
     *            the dep
     */
    @Test
    void recordGenericInterfaceMethodWithReturnTypeGivenByTypeParameterDependentOnAnotherTypeParameterOfSameName(
            @Mocked final DerivedGenericInterface<String> dep) {
        final List<String> values = asList("a", "b");

        new Expectations() {
            {
                dep.doSomething();
                result = values;
                dep.doSomethingElse();
                result = "Abc";
            }
        };

        List<String> resultFromBase = dep.doSomething();
        String resultFromSub = dep.doSomethingElse();

        assertSame(values, resultFromBase);
        assertEquals("Abc", resultFromSub);
    }

    /**
     * Mock generic type with generic multi dimensional array type argument.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockGenericTypeWithGenericMultiDimensionalArrayTypeArgument(@Mocked GenericBase<List<?>[][]> mock) {
        List<?>[][] result = mock.doSomething();

        assertEquals(0, result.length);
    }

    /**
     * The Interface BaseInterface.
     *
     * @param <T>
     *            the generic type
     */
    public interface BaseInterface<T> {
        /**
         * Do something.
         *
         * @param t
         *            the t
         */
        void doSomething(T t);
    }

    /**
     * The Interface SubInterface.
     */
    public interface SubInterface extends BaseInterface<String> {

        /**
         * Do something.
         *
         * @param s
         *            the s
         */
        @SuppressWarnings("AbstractMethodOverridesAbstractMethod")
        @Override
        void doSomething(String s);
    }

    /**
     * Invoke generic base interface method that is overridden in mocked sub interface.
     *
     * @param mock
     *            the mock
     */
    @Test
    void invokeGenericBaseInterfaceMethodThatIsOverriddenInMockedSubInterface(@Mocked final SubInterface mock) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        BaseInterface<String> base = mock;
        base.doSomething("test");

        new Verifications() {
            {
                mock.doSomething("test");
            }
        };
    }

    /**
     * The Class Outer.
     *
     * @param <T>
     *            the generic type
     */
    public static class Outer<T> {

        /**
         * The Class Inner.
         */
        public abstract class Inner {
            /**
             * Gets the some value.
             *
             * @return the some value
             */
            public abstract T getSomeValue();
        }

        /**
         * The Class SubInner.
         */
        public abstract class SubInner extends Inner {
        }
    }

    /**
     * Mock inner class which uses type variable of outer class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockInnerClassWhichUsesTypeVariableOfOuterClass(@Mocked Outer<String>.Inner mock) {
        String in = mock.getSomeValue();
        assertNull(in);
    }

    /**
     * Mock abstract sub class of inner class which uses type variable of outer class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockAbstractSubClassOfInnerClassWhichUsesTypeVariableOfOuterClass(@Mocked Outer<String>.SubInner mock) {
        String in = mock.getSomeValue();
        assertNull(in);
    }

    /**
     * The Class T1.
     */
    static class T1 {
    }

    /**
     * The Class T2.
     */
    static class T2 {
    }

    /**
     * Cascade from generic method of second mocked generic type having different parameter type from first mocked type.
     *
     * @param mock1
     *            the mock 1
     * @param mock3
     *            the mock 3
     */
    @Test
    void cascadeFromGenericMethodOfSecondMockedGenericTypeHavingDifferentParameterTypeFromFirstMockedType(
            @Mocked GenericBase<T1> mock1, @Mocked GenericBase<T2> mock3) {
        T2 r = mock3.doSomething();
        assertNotNull(r);
    }

    /**
     * The Class AClass.
     */
    static class AClass {

        /**
         * Generic method.
         *
         * @param <R>
         *            the generic type
         *
         * @return the r
         */
        <R> R genericMethod() {
            return null;
        }

        /**
         * Generic method with parameter.
         *
         * @param <R>
         *            the generic type
         * @param value
         *            the value
         *
         * @return the r
         */
        <R> R genericMethodWithParameter(R value) {
            return value;
        }

        /**
         * Generic method with two type parameters.
         *
         * @param <R>
         *            the generic type
         * @param <S>
         *            the generic type
         * @param s
         *            the s
         *
         * @return the r
         */
        <R, S> R genericMethodWithTwoTypeParameters(@SuppressWarnings("unused") S s) {
            return null;
        }
    }

    /**
     * Return null from generic method.
     *
     * @param mock
     *            the mock
     */
    @Test
    void returnNullFromGenericMethod(@Mocked AClass mock) {
        String test1 = mock.genericMethod();
        String test2 = mock.genericMethodWithParameter("test2");
        String test3 = mock.genericMethodWithTwoTypeParameters(1);

        assertNull(test1);
        assertNull(test2);
        assertNull(test3);
    }
}
