package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class TestedClassWithFullDITest.
 */
@ExtendWith(JMockitExtension.class)
@TestMethodOrder(MethodName.class)
class TestedClassWithFullDITest {

    /**
     * The Class TestedClass.
     */
    public static class TestedClass {

        /** The dependency to be mocked. */
        Runnable dependencyToBeMocked;

        /** The dependency 2. */
        FirstLevelDependency dependency2;

        /** The dependency 3. */
        FirstLevelDependency dependency3;

        /** The common dependency. */
        CommonDependency commonDependency;

        /** The name. */
        String name;

        /** The description. */
        StringBuilder description;

        /** The number. */
        final Integer number = null;

        /** The flag. */
        boolean flag = true;

        /** The thread state. */
        Thread.State threadState;

        /** The sub obj. */
        AnotherTestedClass subObj;

        /** The sub obj 2. */
        YetAnotherTestedClass subObj2;

        /** The not to be injected. */
        volatile CommonDependency notToBeInjected;
    }

    /**
     * The Class FirstLevelDependency.
     */
    public static class FirstLevelDependency {

        /** The first level id. */
        String firstLevelId;

        /** The dependency. */
        SecondLevelDependency dependency;

        /** The common dependency. */
        CommonDependency commonDependency;

        /** The dependency to be mocked. */
        Runnable dependencyToBeMocked;
    }

    /**
     * The Class SecondLevelDependency.
     */
    public static class SecondLevelDependency {
        /** The common dependency. */
        CommonDependency commonDependency;
    }

    /**
     * The Class CommonDependency.
     */
    public static class CommonDependency {
    }

    /**
     * The Interface IntegrationTested.
     */
    @Tested(fullyInitialized = true)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface IntegrationTested {
    }

    /**
     * The Class YetAnotherTestedClass.
     */
    public static class YetAnotherTestedClass {
    }

    /** The tested 3. */
    @IntegrationTested
    YetAnotherTestedClass tested3;

    /** The tested. */
    @IntegrationTested
    TestedClass tested;

    /** The mocked dependency. */
    @Injectable
    Runnable mockedDependency;

    /**
     * Use fully initialized tested object with no injectable for first level dependency.
     */
    @Test
    void useFullyInitializedTestedObjectWithNoInjectableForFirstLevelDependency() {
        assertNull(tested.name);
        assertSame(tested.commonDependency, tested.dependency2.dependency.commonDependency);
        assertNull(tested.notToBeInjected);
    }

    /**
     * Use fully initialized tested object with value for first level dependency.
     *
     * @param id
     *            the id
     */
    @Test
    void useFullyInitializedTestedObjectWithValueForFirstLevelDependency(@Injectable("test") String id) {
        assertEquals("test", tested.name);
        assertNull(tested.description);
        assertNull(tested.number);
        assertTrue(tested.flag);
        assertNull(tested.threadState);
        assertSame(mockedDependency, tested.dependencyToBeMocked);
        assertNotNull(tested.dependency2);
        assertEquals("test", tested.dependency2.firstLevelId);
        assertSame(tested.dependency2, tested.dependency3);
        assertNotNull(tested.commonDependency);
        assertNotNull(tested.dependency2.dependency);
        assertSame(tested.dependency2.dependency, tested.dependency3.dependency);
        assertSame(tested.commonDependency, tested.dependency2.commonDependency);
        assertSame(tested.commonDependency, tested.dependency3.commonDependency);
        assertSame(tested.commonDependency, tested.dependency2.dependency.commonDependency);
        assertSame(mockedDependency, tested.dependency2.dependencyToBeMocked);
        assertSame(mockedDependency, tested.dependency3.dependencyToBeMocked);
    }

    /**
     * The Class AnotherTestedClass.
     */
    public static class AnotherTestedClass {
        /** The sub obj. */
        YetAnotherTestedClass subObj;
    }

    /** The tested 2. */
    @IntegrationTested
    AnotherTestedClass tested2;

    /**
     * Verify other tested objects get injected into first one.
     */
    @Test
    void verifyOtherTestedObjectsGetInjectedIntoFirstOne() {
        assertSame(tested2, tested.subObj);
        assertSame(tested3, tested.subObj2);
        assertSame(tested3, tested.subObj.subObj);
    }

    /** The concrete dependency. */
    @Tested
    DependencyImpl concreteDependency;

    /** The tested 4. */
    @IntegrationTested
    ClassWithDependencyOfAbstractType tested4;

    /**
     * The Interface Dependency.
     */
    public interface Dependency {
    }

    /**
     * The Class DependencyImpl.
     */
    static class DependencyImpl implements Dependency {
    }

    /**
     * The Class ClassWithDependencyOfAbstractType.
     */
    static class ClassWithDependencyOfAbstractType {
        /** The dependency. */
        Dependency dependency;
    }

    /**
     * Use tested object of subtype for abstract dependency type in another tested object.
     */
    @Test
    void useTestedObjectOfSubtypeForAbstractDependencyTypeInAnotherTestedObject() {
        assertSame(concreteDependency, tested4.dependency);
    }

    /**
     * The Class A.
     */
    static class A {
        /** The b 1. */
        B b1;
    }

    /**
     * The Class B.
     */
    static class B {
        /** The b 2. */
        B b2;
    }

    /** The a. */
    @Tested(fullyInitialized = true)
    A a;

    /**
     * Instantiate class dependent on another having field of its own type.
     */
    @Test
    void instantiateClassDependentOnAnotherHavingFieldOfItsOwnType() {
        B b1 = a.b1;
        assertNotNull(b1);

        B b2 = b1.b2;
        assertNotNull(b2);
        assertSame(b1, b2);
    }

    /**
     * The Class Person.
     */
    @Entity
    static class Person {
    }

    /**
     * The Class ClassWithJPAEntityField.
     */
    static class ClassWithJPAEntityField {
        /** The person. */
        Person person;
    }

    /**
     * Instantiate class with JPA entity field.
     *
     * @param tested5
     *            the tested 5
     */
    @Test
    void instantiateClassWithJPAEntityField(@Tested(fullyInitialized = true) ClassWithJPAEntityField tested5) {
        assertNull(tested5.person);
    }

    /**
     * The Class ClassWithDataSourceField.
     */
    static class ClassWithDataSourceField {
        /** The ds. */
        DataSource ds;
    }

    /**
     * Instantiate class with non annotated data source field.
     *
     * @param tested5
     *            the tested 5
     */
    @Test
    void instantiateClassWithNonAnnotatedDataSourceField(
            @Tested(fullyInitialized = true) ClassWithDataSourceField tested5) {
        assertNull(tested5.ds);
    }

    /**
     * The Class ClassWithJPAFields.
     */
    static class ClassWithJPAFields {
        /** The em factory. */
        EntityManagerFactory emFactory;
        /** The em. */
        EntityManager em;
    }

    /**
     * Instantiate class with non annotated JPA fields.
     *
     * @param tested6
     *            the tested 6
     */
    @Test
    void instantiateClassWithNonAnnotatedJPAFields(@Tested(fullyInitialized = true) ClassWithJPAFields tested6) {
        // If an EntityManagerFactory was created for a previous test, then it got stored in the global dependency
        // cache, which lasts
        // until the end of the test run; therefore, the assertion needs to allow for that.
        assertTrue(tested6.emFactory == null
                || tested6.emFactory.getClass().getName().contains("FakeEntityManagerFactory"));
        assertNull(tested6.em);
    }

    /**
     * The Class ClassWithUnsatisfiableConstructor.
     */
    static class ClassWithUnsatisfiableConstructor {
        /**
         * Instantiates a new class with unsatisfiable constructor.
         *
         * @param someValue
         *            the some value
         */
        ClassWithUnsatisfiableConstructor(@SuppressWarnings("unused") int someValue) {
        }
    }

    /**
     * The Class ClassWithFieldToInject.
     */
    static class ClassWithFieldToInject {
        /** The dependency. */
        ClassWithUnsatisfiableConstructor dependency;
    }

    /**
     * Instantiate class with field to inject whose type cannot be instantiated.
     *
     * @param cut
     *            the cut
     */
    @Test
    void instantiateClassWithFieldToInjectWhoseTypeCannotBeInstantiated(
            @Tested(fullyInitialized = true) ClassWithFieldToInject cut) {
        assertNotNull(cut);
        assertNull(cut.dependency);
    }

    /**
     * The Interface InterfaceDependency.
     */
    interface InterfaceDependency {
    }

    /**
     * The Class ClassWithInterfaceInConstructor.
     */
    static class ClassWithInterfaceInConstructor {
        /**
         * Instantiates a new class with interface in constructor.
         *
         * @param someValue
         *            the some value
         */
        ClassWithInterfaceInConstructor(@SuppressWarnings("unused") InterfaceDependency someValue) {
        }
    }

    /**
     * Instantiate class with interface in constructor.
     *
     * @param cut
     *            the cut
     */
    @Test
    void instantiateClassWithInterfaceInConstructor(
            @Tested(fullyInitialized = true) ClassWithInterfaceInConstructor cut) {
        assertNotNull(cut);
    }

}
