package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * The Class TestedClassWithFullConstructorDITest.
 */
final class TestedClassWithFullConstructorDITest {

    /**
     * The Interface Dependency.
     */
    public interface Dependency {
    }

    /**
     * The Class DependencyImpl.
     */
    public static final class DependencyImpl implements Dependency {
    }

    /**
     * The Class Collaborator.
     */
    public static class Collaborator {
    }

    /**
     * The Class TestedClassWithSinglePublicConstructor.
     */
    @SuppressWarnings("unused")
    public static final class TestedClassWithSinglePublicConstructor {

        /** The dependency. */
        final Dependency dependency;

        /** The collaborator 1. */
        final Collaborator collaborator1;

        /** The collaborator 2. */
        Collaborator collaborator2;

        /**
         * Instantiates a new tested class with single public constructor.
         *
         * @param dependency
         *            the dependency
         * @param collaborator
         *            the collaborator
         */
        public TestedClassWithSinglePublicConstructor(Dependency dependency, Collaborator collaborator) {
            this.dependency = dependency;
            collaborator1 = collaborator;
        }

        /**
         * Instantiates a new tested class with single public constructor.
         */
        TestedClassWithSinglePublicConstructor() {
            dependency = null;
            collaborator1 = null;
        }
    }

    /** The dep. */
    @Tested
    DependencyImpl dep;

    /** The tested 1. */
    @Tested(fullyInitialized = true)
    TestedClassWithSinglePublicConstructor tested1;

    /**
     * Verify instantiation of tested objects through public constructor.
     */
    @Test
    void verifyInstantiationOfTestedObjectsThroughPublicConstructor() {
        assertTrue(tested1.dependency instanceof DependencyImpl);
        assertNotNull(tested1.collaborator1);
        assertSame(tested1.collaborator1, tested1.collaborator2);
    }

    /**
     * The Class TestedClassWithAnnotatedConstructor.
     */
    public static final class TestedClassWithAnnotatedConstructor {

        /** The i. */
        int i;

        /** The s. */
        String s;

        /** The dependency. */
        Dependency dependency;

        /** The collaborator 1. */
        Collaborator collaborator1;

        /** The collaborator 2. */
        @Inject
        Collaborator collaborator2;

        /**
         * Instantiates a new tested class with annotated constructor.
         */
        @SuppressWarnings("unused")
        public TestedClassWithAnnotatedConstructor() {
        }

        /**
         * Instantiates a new tested class with annotated constructor.
         *
         * @param i
         *            the i
         * @param s
         *            the s
         * @param dependency
         *            the dependency
         * @param collaborator
         *            the collaborator
         */
        @Inject
        TestedClassWithAnnotatedConstructor(int i, String s, Dependency dependency, Collaborator collaborator) {
            this.i = i;
            this.s = s;
            this.dependency = dependency;
            collaborator1 = collaborator;
        }
    }

    /** The tested 2. */
    @Tested(fullyInitialized = true)
    TestedClassWithAnnotatedConstructor tested2;

    /** The number. */
    @Injectable
    final int number = 123;

    /** The text. */
    @Injectable
    final String text = "text";

    /**
     * Verify instantiation of tested object through annotated constructor.
     */
    @Test
    void verifyInstantiationOfTestedObjectThroughAnnotatedConstructor() {
        assertNotNull(tested2);
        assertEquals(123, tested2.i);
        assertEquals("text", tested2.s);
        assertTrue(tested2.dependency instanceof DependencyImpl);
        assertNotNull(tested2.collaborator1);
        assertSame(tested2.collaborator1, tested2.collaborator2);
    }

    /**
     * The Class TestedClassWithQualifiedConstructorParameters.
     */
    static class TestedClassWithQualifiedConstructorParameters {

        /** The col 1. */
        final Collaborator col1;

        /** The col 2. */
        final Collaborator col2;

        /**
         * Instantiates a new tested class with qualified constructor parameters.
         *
         * @param p1
         *            the p 1
         * @param p2
         *            the p 2
         */
        TestedClassWithQualifiedConstructorParameters(@Named("one") Collaborator p1,
                @Qualifier("two") Collaborator p2) {
            col1 = p1;
            col2 = p2;
        }
    }

    /** The tested 3. */
    @Tested(fullyInitialized = true)
    TestedClassWithQualifiedConstructorParameters tested3;

    /**
     * Verify instantiation of tested class with qualified constructor parameters.
     */
    @Test
    void verifyInstantiationOfTestedClassWithQualifiedConstructorParameters() {
        assertNotNull(tested3.col1);
        assertNotSame(tested3.col1, tested3.col2);
    }

    /**
     * The Class TestedClassWithDependencyHavingConstructorParameter.
     */
    static class TestedClassWithDependencyHavingConstructorParameter {

        /** The dependency. */
        final DependencyWithConstructorParameter dependency;

        /**
         * Instantiates a new tested class with dependency having constructor parameter.
         *
         * @param dependency
         *            the dependency
         */
        TestedClassWithDependencyHavingConstructorParameter(DependencyWithConstructorParameter dependency) {
            this.dependency = dependency;
        }
    }

    /**
     * The Class DependencyWithConstructorParameter.
     */
    static class DependencyWithConstructorParameter {

        /** The par. */
        final String par;

        /**
         * Instantiates a new dependency with constructor parameter.
         *
         * @param par1
         *            the par 1
         */
        DependencyWithConstructorParameter(String par1) {
            par = par1;
        }
    }

    /** The tested 4. */
    @Tested(fullyInitialized = true)
    TestedClassWithDependencyHavingConstructorParameter tested4;

    /**
     * Verify recursive instantiation of dependency with constructor parameter.
     */
    @Test
    void verifyRecursiveInstantiationOfDependencyWithConstructorParameter() {
        assertEquals("text", tested4.dependency.par);
    }
}
