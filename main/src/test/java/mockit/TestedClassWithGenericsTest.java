package mockit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;

/**
 * The Class TestedClassWithGenericsTest.
 */
public final class TestedClassWithGenericsTest {

    /**
     * The Interface Collaborator.
     *
     * @param <T>
     *            the generic type
     */
    public interface Collaborator<T> {
        /**
         * Gets the value.
         *
         * @return the value
         */
        T getValue();
    }

    /**
     * The Class SUTWithUnboundedTypeParameter.
     *
     * @param <T>
     *            the generic type
     */
    static class SUTWithUnboundedTypeParameter<T> {

        /** The value. */
        T value;

        /** The collaborator. */
        final Collaborator<T> collaborator;

        /** The collaborators. */
        final Iterable<Collaborator<T>> collaborators;

        /** The action 1. */
        Callable<T> action1;

        /** The action 2. */
        Callable<?> action2;

        /**
         * Instantiates a new SUT with unbounded type parameter.
         *
         * @param c
         *            the c
         */
        @SuppressWarnings("unused")
        SUTWithUnboundedTypeParameter(Collaborator<T> c) {
            collaborator = c;
            collaborators = null;
        }

        /**
         * Instantiates a new SUT with unbounded type parameter.
         *
         * @param collaborators
         *            the collaborators
         * @param action
         *            the action
         */
        @SuppressWarnings("unused")
        SUTWithUnboundedTypeParameter(Iterable<Collaborator<T>> collaborators, Callable<String> action) {
            collaborator = null;
            this.collaborators = collaborators;
            action2 = action;
        }
    }

    /** The tested 1. */
    @Tested
    SUTWithUnboundedTypeParameter<Integer> tested1;

    /** The number to inject. */
    @Injectable
    final Integer numberToInject = 123;

    /** The mock collaborator. */
    @Injectable
    Collaborator<Integer> mockCollaborator;

    /**
     * Use SUT created with constructor of single generic parameter and with generic field injected from concrete
     * injectables.
     */
    @Test
    public void useSUTCreatedWithConstructorOfSingleGenericParameterAndWithGenericFieldInjectedFromConcreteInjectables() {
        assertSame(mockCollaborator, tested1.collaborator);
        assertNull(tested1.collaborators);
        assertSame(numberToInject, tested1.value);
        assertNull(tested1.action1);
        assertNull(tested1.action2);
    }

    /**
     * Use SUT instantiated with constructor having multiple generic parameters.
     *
     * @param collaborators
     *            the collaborators
     * @param mockAction1
     *            the mock action 1
     * @param action1
     *            the action 1
     */
    @Test
    public void useSUTInstantiatedWithConstructorHavingMultipleGenericParameters(
            @Injectable Iterable<Collaborator<Integer>> collaborators, @Injectable Callable<String> mockAction1,
            @Injectable Callable<Integer> action1) {
        assertNull(tested1.collaborator);
        assertSame(collaborators, tested1.collaborators);
        assertSame(mockAction1, tested1.action2);
        assertSame(action1, tested1.action1);
        assertSame(numberToInject, tested1.value);
    }

    /**
     * Use SUT instantiated with generic constructor parameters injected from concrete injectables.
     *
     * @param mockCollaborators
     *            the mock collaborators
     * @param mockAction
     *            the mock action
     */
    @Test
    public void useSUTInstantiatedWithGenericConstructorParametersInjectedFromConcreteInjectables(
            @Injectable Iterable<Collaborator<Integer>> mockCollaborators, @Injectable Callable<String> mockAction) {
        assertNull(tested1.collaborator);
        assertSame(mockCollaborators, tested1.collaborators);
        assertNull(tested1.action1);
        assertSame(mockAction, tested1.action2);
        assertSame(numberToInject, tested1.value);
    }

    /**
     * The Class SUTWithGenericConstructor.
     *
     * @param <T>
     *            the generic type
     */
    static class SUTWithGenericConstructor<T> {

        /** The values. */
        final Map<T, ?> values;

        /**
         * Instantiates a new SUT with generic constructor.
         *
         * @param <V>
         *            the value type
         * @param values
         *            the values
         */
        @SuppressWarnings("unused")
        <V extends CharSequence & Serializable> SUTWithGenericConstructor(Map<T, V> values) {
            this.values = values;
        }
    }

    /** The map values. */
    @Tested
    final Map<Integer, String> mapValues = new HashMap<>();

    /** The tested 8. */
    @Tested
    SUTWithGenericConstructor<Integer> tested8;

    /**
     * Use SUT instantiated with generic constructor.
     */
    @Test
    public void useSUTInstantiatedWithGenericConstructor() {
        assertSame(mapValues, tested8.values);
    }

    /**
     * The Class GenericClass.
     *
     * @param <T>
     *            the generic type
     */
    static class GenericClass<T> {
        /** The value. */
        T value;
    }

    /**
     * The Class SUTWithBoundedTypeParameter.
     *
     * @param <N>
     *            the number type
     * @param <C>
     *            the generic type
     */
    static class SUTWithBoundedTypeParameter<N extends Number, C extends CharSequence> {

        /** The text value. */
        C textValue;

        /** The number value. */
        N numberValue;

        /** The collaborator. */
        GenericClass<N> collaborator;

        /** The action. */
        Callable<C> action;
    }

    /** The tested 2. */
    @Tested
    SUTWithBoundedTypeParameter<Integer, String> tested2;

    /** The tested 3. */
    @Tested
    SUTWithBoundedTypeParameter<Number, CharSequence> tested3;

    /** The tested 4. */
    @Tested
    SUTWithBoundedTypeParameter<?, ?> tested4;

    /** The tested 5. */
    @Tested
    SUTWithBoundedTypeParameter<Long, StringBuilder> tested5;

    /**
     * Use SUT declared with type bound.
     *
     * @param name
     *            the name
     * @param textAction
     *            the text action
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void useSUTDeclaredWithTypeBound(@Injectable("test") String name, @Injectable Callable<String> textAction,
            @Injectable GenericClass<? extends Number> collaborator) {
        assertSame(numberToInject, tested2.numberValue);
        assertSame(name, tested2.textValue);
        assertSame(collaborator, tested2.collaborator);
        assertSame(textAction, tested2.action);

        assertSame(numberToInject, tested3.numberValue);
        assertSame(name, tested3.textValue);
        assertSame(collaborator, tested3.collaborator);
        assertSame(textAction, tested3.action);

        assertSame(numberToInject, tested4.numberValue);
        assertSame(name, tested4.textValue);
        assertSame(collaborator, tested4.collaborator);
        assertSame(textAction, tested4.action);

        assertNull(tested5.numberValue);
        assertNull(tested5.textValue);
        assertSame(collaborator, tested5.collaborator);
        assertNull(tested5.action);
    }

    /**
     * Use SUT declared with type bound having non matching injectable with wildcard.
     *
     * @param action
     *            the action
     */
    @Test
    public void useSUTDeclaredWithTypeBoundHavingNonMatchingInjectableWithWildcard(
            @Injectable Callable<? extends Number> action) {
        assertNull(tested2.action);
        assertNull(tested3.action);
        assertNull(tested4.action);
        assertNull(tested5.action);
    }

    /**
     * The Class Base.
     *
     * @param <B>
     *            the generic type
     */
    static class Base<B> {
        /** The dep. */
        B dep;
    }

    /**
     * The Class Derived.
     *
     * @param <D>
     *            the generic type
     */
    static class Derived<D> extends Base<D> {
    }

    /**
     * The Class Concrete.
     */
    static final class Concrete extends Derived<Dep> {
    }

    /**
     * The Interface Dep.
     */
    public interface Dep {
    }

    /** The dep. */
    @Injectable
    final Dep dep = new Dep() {
    };

    /** The sut. */
    @Tested
    Concrete sut;

    /**
     * Use SUT class extending generic base class which extends another generic base class containing A generic
     * dependency.
     */
    @Test
    public void useSUTClassExtendingGenericBaseClassWhichExtendsAnotherGenericBaseClassContainingAGenericDependency() {
        assertSame(dep, sut.dep);
    }

    /**
     * The Class AnotherDep.
     */
    public static class AnotherDep {
    }

    /**
     * The Class Concrete2.
     */
    static class Concrete2 extends Base<AnotherDep> {
    }

    /** The sut 2. */
    @Tested(fullyInitialized = true)
    Concrete2 sut2;

    /**
     * Use fully initialized SUT class extending generic base class.
     */
    @Test
    public void useFullyInitializedSUTClassExtendingGenericBaseClass() {
        AnotherDep anotherDep = sut2.dep;
        assertNotNull(anotherDep);
    }

    /**
     * The Class Concrete3.
     */
    static class Concrete3 extends Derived<AnotherDep> {
    }

    /** The sut 3. */
    @Tested(fullyInitialized = true)
    Concrete3 sut3;

    /**
     * Use fully initialized SUT class extending generic class which extends another generic class.
     */
    @Test
    public void useFullyInitializedSUTClassExtendingGenericClassWhichExtendsAnotherGenericClass() {
        AnotherDep anotherDep = sut3.dep;
        assertNotNull(anotherDep);
    }

    /**
     * The Class TestedClassWithConstructorParameterOfGenericType.
     */
    static class TestedClassWithConstructorParameterOfGenericType {

        /** The a class. */
        private final Class<?> aClass;

        /**
         * Instantiates a new tested class with constructor parameter of generic type.
         *
         * @param aClass
         *            the a class
         */
        TestedClassWithConstructorParameterOfGenericType(Class<?> aClass) {
            this.aClass = aClass;
        }
    }

    /** The a class. */
    @Tested
    final Class<?> aClass = Long.class;

    /** The tested 6. */
    @Tested(fullyInitialized = true)
    TestedClassWithConstructorParameterOfGenericType tested6;

    /**
     * Verify instantiation of class with constructor parameter of generic type.
     */
    @Test
    public void verifyInstantiationOfClassWithConstructorParameterOfGenericType() {
        assertSame(aClass, tested6.aClass);
    }

    /**
     * The Class GenericClassWithDependencyUsingTypeParameter.
     *
     * @param <T>
     *            the generic type
     */
    static class GenericClassWithDependencyUsingTypeParameter<T> {
        /** The dependency. */
        GenericClass<T> dependency;
    }

    /** The dependency. */
    @Tested
    final GenericClass<String> dependency = new GenericClass<>();

    /** The tested 7. */
    @Tested(fullyInitialized = true)
    GenericClassWithDependencyUsingTypeParameter<String> tested7;

    /**
     * Verify instantiation of generic class with dependency using type parameter.
     */
    @Test
    public void verifyInstantiationOfGenericClassWithDependencyUsingTypeParameter() {
        assertSame(dependency, tested7.dependency);
    }

    /**
     * The Interface Interface.
     */
    public interface Interface {
    }

    /**
     * The Class Implementation.
     */
    static class Implementation implements Interface {
    }

    /**
     * The Class Derived2.
     */
    static class Derived2 extends Base<Interface> {
    }

    /** The impl. */
    @Tested
    Implementation impl;

    /** The tested. */
    @Tested(fullyInitialized = true)
    Derived2 tested;

    /**
     * Use tested object of implementation type for type variable in generic base class.
     */
    @Test
    public void useTestedObjectOfImplementationTypeForTypeVariableInGenericBaseClass() {
        assertSame(impl, tested.dep);
    }

    /**
     * The Class ClassWithFieldOfGenericTypeContainingGenericArray.
     */
    static class ClassWithFieldOfGenericTypeContainingGenericArray {
        /** The n. */
        @SuppressWarnings("unused")
        int n;
        /** The list. */
        List<Comparable<?>[]> list;
    }

    /**
     * Instantiate object containing generic type field with generic array element.
     *
     * @param t
     *            the t
     */
    @Test
    public void instantiateObjectContainingGenericTypeFieldWithGenericArrayElement(
            @Tested ClassWithFieldOfGenericTypeContainingGenericArray t) {
        assertNotNull(t);
    }
}
