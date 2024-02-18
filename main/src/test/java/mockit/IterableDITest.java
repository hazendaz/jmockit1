package mockit;

import static java.util.Arrays.asList;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Test;

/**
 * The Class IterableDITest.
 */
public final class IterableDITest {

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /** The value. */
        final int value;

        /**
         * Instantiates a new collaborator.
         */
        Collaborator() {
            value = 0;
        }

        /**
         * Instantiates a new collaborator.
         *
         * @param value
         *            the value
         */
        Collaborator(int value) {
            this.value = value;
        }
    }

    /**
     * The Class TestedClassWithIterableInjectionPoints.
     */
    static final class TestedClassWithIterableInjectionPoints {

        /** The names. */
        final List<String> names;

        /** The collaborators. */
        @Inject
        Collection<Collaborator> collaborators;

        /** The numbers. */
        Set<? extends Number> numbers;

        /**
         * Instantiates a new tested class with iterable injection points.
         *
         * @param names
         *            the names
         */
        @Inject
        TestedClassWithIterableInjectionPoints(List<String> names) {
            this.names = names;
        }
    }

    /** The name list. */
    @Tested
    final List<String> nameList = asList("One", "Two");

    /** The col list. */
    @Tested
    final Collection<Collaborator> colList = asList(new Collaborator(1), new Collaborator(2));

    /** The tested 1. */
    @Tested
    TestedClassWithIterableInjectionPoints tested1;

    /**
     * Inject multi valued injectables into injection points of the same collection types.
     */
    @Test
    public void injectMultiValuedInjectablesIntoInjectionPointsOfTheSameCollectionTypes() {
        assertSame(nameList, tested1.names);
        assertSame(colList, tested1.collaborators);
        assertNull(tested1.numbers);
    }

    /**
     * The Class Dependency.
     */
    static class Dependency {
    }

    /**
     * The Class SubDependency.
     */
    static class SubDependency extends Dependency {
    }

    /**
     * The Class TestedClassWithInjectedList.
     */
    static class TestedClassWithInjectedList {

        /** The dependencies. */
        @Inject
        List<Dependency> dependencies;

        /** The names. */
        Set<String> names;
    }

    /** The tested 2. */
    @Tested
    TestedClassWithInjectedList tested2;

    /** The dependency. */
    @Injectable
    Dependency dependency;

    /**
     * Inject mocked instance into list.
     */
    @Test
    public void injectMockedInstanceIntoList() {
        assertTrue(tested2.dependencies.contains(dependency));
    }

    /**
     * Do not inject string into unannotated set.
     *
     * @param name
     *            the name
     */
    @Test
    public void doNotInjectStringIntoUnannotatedSet(@Injectable("test") String name) {
        assertNull(tested2.names);
    }

    /**
     * Inject sub type instance into list of base type.
     *
     * @param sub
     *            the sub
     */
    @Test
    public void injectSubTypeInstanceIntoListOfBaseType(@Injectable SubDependency sub) {
        assertTrue(tested2.dependencies.contains(sub));
    }
}
