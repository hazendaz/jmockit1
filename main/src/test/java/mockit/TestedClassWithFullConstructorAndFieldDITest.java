package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * The Class TestedClassWithFullConstructorAndFieldDITest.
 */
final class TestedClassWithFullConstructorAndFieldDITest {

    /**
     * The Class TestedClass.
     */
    static class TestedClass {

        /** The value. */
        String value;

        /** The dependency 1. */
        DependencyWithFieldDIOnly dependency1;

        /** The dependency 2. */
        DependencyWithConstructorDIOnly dependency2;
    }

    /**
     * The Class DependencyWithFieldDIOnly.
     */
    static class DependencyWithFieldDIOnly {
        /** The value. */
        String value;
    }

    /**
     * The Class DependencyWithConstructorDIOnly.
     */
    static class DependencyWithConstructorDIOnly {

        /** The value. */
        final String value;

        /**
         * Instantiates a new dependency with constructor DI only.
         *
         * @param value
         *            the value
         */
        DependencyWithConstructorDIOnly(String value) {
            this.value = value;
        }
    }

    /** The tested. */
    @Tested(fullyInitialized = true)
    TestedClass tested;

    /** The first. */
    @Injectable
    String first = "text";

    /**
     * Verify each target field gets injected with first unused injectable whether through field or constructor
     * injection.
     */
    @Test
    void verifyEachTargetFieldGetsInjectedWithFirstUnusedInjectableWhetherThroughFieldOrConstructorInjection() {
        assertEquals("text", tested.value);
        assertEquals("text", tested.dependency1.value);
        assertEquals("text", tested.dependency2.value);
    }

    /**
     * The Class ClassWithMultipleConstructors.
     */
    @SuppressWarnings("unused")
    static class ClassWithMultipleConstructors {

        /** The value. */
        final int value;

        /**
         * Instantiates a new class with multiple constructors.
         */
        ClassWithMultipleConstructors() {
            value = 1;
        }

        /**
         * Instantiates a new class with multiple constructors.
         *
         * @param value
         *            the value
         */
        ClassWithMultipleConstructors(int value) {
            throw new RuntimeException("Not to be called");
        }
    }

    /** The tested 2. */
    @Tested(fullyInitialized = true)
    ClassWithMultipleConstructors tested2;

    /**
     * Verify initialization of class with multiple constructors.
     */
    @Test
    void verifyInitializationOfClassWithMultipleConstructors() {
        assertEquals(1, tested2.value);
    }

    /**
     * The Class ClassWithFieldToInject.
     */
    static class ClassWithFieldToInject {
        /** The dependency. */
        ClassWithMultipleConstructors dependency;
    }

    /** The tested 3. */
    @Tested(fullyInitialized = true)
    ClassWithFieldToInject tested3;

    /**
     * Verify initialization of class with field of another class having multiple constructors.
     */
    @Test
    void verifyInitializationOfClassWithFieldOfAnotherClassHavingMultipleConstructors() {
        assertNotNull(tested3.dependency);
        assertEquals(1, tested3.dependency.value);
    }

    /**
     * The Class Dependency.
     */
    static final class Dependency {
    }

    /**
     * The Class AnotherClassWithMultipleConstructors.
     */
    @SuppressWarnings("unused")
    static final class AnotherClassWithMultipleConstructors {

        /** The dep. */
        final Dependency dep;

        /**
         * Instantiates a new another class with multiple constructors.
         */
        AnotherClassWithMultipleConstructors() {
            dep = new Dependency();
        }

        /**
         * Instantiates a new another class with multiple constructors.
         *
         * @param dep
         *            the dep
         */
        AnotherClassWithMultipleConstructors(Dependency dep) {
            this.dep = dep;
        }
    }

    /** The dep. */
    @Tested
    Dependency dep;

    /** The tested 4. */
    @Tested(fullyInitialized = true)
    AnotherClassWithMultipleConstructors tested4;

    /**
     * Verify initialization of class with multiple constructors having tested field for parameter.
     */
    @Test
    void verifyInitializationOfClassWithMultipleConstructorsHavingTestedFieldForParameter() {
        assertSame(dep, tested4.dep);
    }

    /**
     * The Class ClassWithFieldDI.
     */
    static class ClassWithFieldDI {
        /** The dep. */
        Dependency dep;
    }

    /**
     * The Class ClassWithConstructorDI.
     */
    static class ClassWithConstructorDI {

        /** The dependency. */
        ClassWithFieldDI dependency;

        /**
         * Instantiates a new class with constructor DI.
         *
         * @param dependency
         *            the dependency
         */
        ClassWithConstructorDI(ClassWithFieldDI dependency) {
            this.dependency = dependency;
        }
    }

    /** The tested 5. */
    @Tested(fullyInitialized = true)
    ClassWithConstructorDI tested5;

    /**
     * Initialize class with constructor injected dependency having another dependency injected into field.
     */
    @Test
    void initializeClassWithConstructorInjectedDependencyHavingAnotherDependencyInjectedIntoField() {
        assertNotNull(tested5.dependency);
        assertNotNull(tested5.dependency.dep);
    }
}
