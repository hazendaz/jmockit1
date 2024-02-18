package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.Test;

class BaseTest {
    static class Dependency {
        int doSomething() {
            return -1;
        }
    }

    static class Collaborator {
    }

    public static class TestedClass {
        protected final int i;
        protected Dependency dependency;
        Runnable action;

        public TestedClass() {
            i = -1;
        }

        public TestedClass(int i) {
            this.i = i;
        }

        public boolean doSomeOperation() {
            return dependency.doSomething() > 0;
        }
    }

    @Tested
    TestedClass tested1;
    @Injectable
    Dependency dependency;
    @Tested
    Collaborator collaborator;

    final void verifyTestedObjectFromBaseTestClass(int expectedValueForIntField) {
        assertEquals(expectedValueForIntField, tested1.i);
        assertSame(dependency, tested1.dependency);
        assertNotNull(tested1.action);
    }
}

/**
 * The Class TestedClassWithConstructorAndFieldDITest.
 */
public final class TestedClassWithConstructorAndFieldDITest extends BaseTest {

    /**
     * The Class AnotherTestedClass.
     */
    @SuppressWarnings("unused")
    public static class AnotherTestedClass extends TestedClass {

        /** The another action. */
        Runnable anotherAction;

        /** The dependency 3. */
        Dependency dependency3;

        /** The dependency 2. */
        Dependency dependency2;

        /**
         * Instantiates a new another tested class.
         */
        public AnotherTestedClass() {
            super(-2);
        }

        /**
         * Instantiates a new another tested class.
         *
         * @param value
         *            the value
         * @param dependency1
         *            the dependency 1
         */
        public AnotherTestedClass(int value, Dependency dependency1) {
            super(value);
            // noinspection UnnecessarySuperQualifier
            super.dependency = dependency1;
        }

        @Override
        public boolean doSomeOperation() {
            boolean b = dependency2.doSomething() > 0;
            return super.doSomeOperation() && b;
        }
    }

    /** The tested 2. */
    @Tested
    AnotherTestedClass tested2;

    /** The another action. */
    @Injectable
    Runnable anotherAction;

    /** The dependency 2. */
    @Injectable
    Dependency dependency2;

    /**
     * Exercise tested subclass object with fields injected by type and name.
     */
    @Test
    public void exerciseTestedSubclassObjectWithFieldsInjectedByTypeAndName() {
        verifyTestedObjectFromBaseTestClass(-1);

        assertEquals(-2, tested2.i);
        assertSame(anotherAction, tested2.anotherAction);
        assertSame(dependency, tested2.dependency);
        assertSame(dependency2, tested2.dependency2);
        assertNull(tested2.dependency3);
        assertFalse(tested2.doSomeOperation());

        new Verifications() {
            {
                anotherAction.run();
                times = 0;
                dependency.doSomething();
                times = 1;
                dependency2.doSomething();
            }
        };
    }

    /**
     * Exercise tested subclass object with fields injected from mock fields and mock parameter.
     *
     * @param dependency3
     *            the dependency 3
     */
    @Test
    public void exerciseTestedSubclassObjectWithFieldsInjectedFromMockFieldsAndMockParameter(
            @Injectable Dependency dependency3) {
        verifyTestedObjectFromBaseTestClass(-1);

        assertEquals(-2, tested2.i);
        assertSame(dependency, tested2.dependency);
        assertSame(dependency2, tested2.dependency2);
        assertSame(dependency3, tested2.dependency3);
        assertSame(anotherAction, tested2.anotherAction);
        assertFalse(tested2.doSomeOperation());
    }

    /**
     * Exercise tested subclass object using constructor and field injection.
     *
     * @param value
     *            the value
     * @param dependency1
     *            the dependency 1
     */
    @Test
    public void exerciseTestedSubclassObjectUsingConstructorAndFieldInjection(@Injectable("45") int value,
            @Injectable Dependency dependency1) {
        verifyTestedObjectFromBaseTestClass(45);

        assertEquals(45, tested2.i);
        assertSame(dependency1, tested2.dependency);
        assertSame(dependency2, tested2.dependency2);
        assertNull(tested2.dependency3);
        assertSame(anotherAction, tested2.anotherAction);
        assertFalse(tested2.doSomeOperation());
    }

    /**
     * The Class ClassWithFieldHavingTestedFieldInBaseTestClass.
     */
    static class ClassWithFieldHavingTestedFieldInBaseTestClass {
        /** The collaborator. */
        Collaborator collaborator;
    }

    /** The tested 3. */
    @Tested
    ClassWithFieldHavingTestedFieldInBaseTestClass tested3;

    /**
     * Creates the tested parameter injecting from tested field in base test class.
     *
     * @param tested4
     *            the tested 4
     */
    @Test
    public void createTestedParameterInjectingFromTestedFieldInBaseTestClass(
            @Tested ClassWithFieldHavingTestedFieldInBaseTestClass tested4) {
        assertNotNull(collaborator);
        assertSame(collaborator, tested3.collaborator);
        assertSame(collaborator, tested4.collaborator);
    }
}
