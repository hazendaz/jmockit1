package mockit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The Class TestedClassWithGenericSubtypesTest.
 */
final class TestedClassWithGenericSubtypesTest {

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
     * The Class Subclass1.
     */
    static class Subclass1 extends GenericClass<String> {
    }

    /**
     * The Class Subclass2.
     */
    static class Subclass2 extends GenericClass<Double> {
    }

    /**
     * The Class SUT1.
     */
    static class SUT1 {

        /** The dependency 1. */
        GenericClass<String> dependency1;

        /** The dependency 2. */
        GenericClass<Double> dependency2;
    }

    /**
     * Inject subclass instances into fields of base generic class.
     *
     * @param sut
     *            the sut
     * @param s1
     *            the s 1
     * @param s2
     *            the s 2
     */
    @Test
    void injectSubclassInstancesIntoFieldsOfBaseGenericClass(@Tested SUT1 sut, @Injectable Subclass1 s1,
            @Injectable Subclass2 s2) {
        assertSame(s1, sut.dependency1);
        assertSame(s2, sut.dependency2);
    }

    /**
     * The Interface GenericInterface.
     *
     * @param <T>
     *            the generic type
     */
    @SuppressWarnings("unused")
    public interface GenericInterface<T> {
    }

    /**
     * The Class Impl1.
     */
    static class Impl1 implements GenericInterface<String> {
    }

    /**
     * The Class Impl2.
     */
    static class Impl2 implements GenericInterface<Double> {
    }

    /**
     * The Class SUT2.
     */
    static class SUT2 {

        /** The dependency 1. */
        final GenericInterface<String> dependency1;

        /** The dependency 2. */
        final GenericInterface<Double> dependency2;

        /**
         * Instantiates a new sut2.
         *
         * @param dep1
         *            the dep 1
         * @param dep2
         *            the dep 2
         */
        SUT2(GenericInterface<String> dep1, GenericInterface<Double> dep2) {
            dependency1 = dep1;
            dependency2 = dep2;
        }
    }

    /**
     * Inject implementation instances into fields of base generic interface.
     *
     * @param sut
     *            the sut
     * @param i1
     *            the i 1
     * @param i2
     *            the i 2
     */
    @Test
    void injectImplementationInstancesIntoFieldsOfBaseGenericInterface(@Tested SUT2 sut, @Injectable Impl1 i1,
            @Injectable Impl2 i2) {
        assertSame(i1, sut.dependency1);
        assertSame(i2, sut.dependency2);
    }

    /**
     * The Class Dependency.
     */
    static final class Dependency {
    }

    /**
     * The Class Service1.
     */
    static final class Service1 extends GenericClass<Dependency> {
    }

    /**
     * The Class Service2.
     */
    static final class Service2 {
        /** The service 1. */
        Service1 service1;
    }

    /**
     * Inject instance into type variable of second level class.
     *
     * @param service2
     *            the service 2
     */
    @Test
    void injectInstanceIntoTypeVariableOfSecondLevelClass(@Tested(fullyInitialized = true) Service2 service2) {
        Service1 service1 = service2.service1;
        assertNotNull(service1);
        assertTrue(service1.value instanceof Dependency);
    }
}
