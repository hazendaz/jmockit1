package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;

/**
 * The Class TestedFieldExtractionTest.
 */
final class TestedFieldExtractionTest {

    /**
     * The Class Dependency.
     */
    static class Dependency {
    }

    /**
     * The Class TestedClassWithMultipleFieldsOfSameType.
     */
    static class TestedClassWithMultipleFieldsOfSameType {

        /** The dep 1. */
        Dependency dep1;

        /** The dep 2. */
        Dependency dep2;
    }

    /** The tested 1. */
    @Tested(fullyInitialized = true)
    TestedClassWithMultipleFieldsOfSameType tested1;

    /** The dep. */
    @Tested
    Dependency dep;

    /**
     * Extract multiple fields of same type into single tested field.
     */
    @Test
    void extractMultipleFieldsOfSameTypeIntoSingleTestedField() {
        assertNotNull(tested1.dep1);
        assertNotNull(tested1.dep2);
        assertSame(tested1.dep1, tested1.dep2); // unqualified fields of same type get the same created instance
        assertSame(tested1.dep1, dep);
    }

    /**
     * The Class TestedClassWithNamedFields.
     */
    static class TestedClassWithNamedFields {

        /** The dep 1. */
        @Inject
        @Named("first")
        Dependency dep1;

        /** The dep 2. */
        @Inject
        @Named("second")
        Dependency dep2;
    }

    /** The tested 2. */
    @Tested(fullyInitialized = true)
    TestedClassWithNamedFields tested2;

    /** The first. */
    @Tested
    Dependency first;

    /** The second. */
    @Tested
    Dependency second;

    /**
     * Extract multiple qualified fields of same type into separate tested fields.
     */
    @Test
    void extractMultipleQualifiedFieldsOfSameTypeIntoSeparateTestedFields() {
        assertNotNull(tested2.dep1);
        assertNotNull(tested2.dep2);
        assertNotSame(tested2.dep1, tested2.dep2);
        assertSame(tested2.dep1, first);
        assertSame(tested2.dep2, second);
    }

    /**
     * The Class TestedClassWithInitializedFieldsOfVariousTypes.
     */
    static class TestedClassWithInitializedFieldsOfVariousTypes {

        /** The name. */
        final String name = "test";

        /** The number. */
        int number = 123;

        /** The names. */
        @Inject
        @Named("test")
        final List<String> names = new ArrayList<>();

        /** The numbers and names. */
        Map<Integer, String> numbersAndNames = new HashMap<>();
    }

    /** The tested 3. */
    @Tested
    TestedClassWithInitializedFieldsOfVariousTypes tested3;

    /** The name. */
    @Tested
    String name;

    /** The test. */
    @Tested
    List<String> test;

    /** The numbers and names. */
    @Tested
    Map<Integer, String> numbersAndNames;

    /** The number. */
    @Tested
    int number;

    /**
     * Extract fields initialized by constructor of tested class.
     */
    @Test
    void extractFieldsInitializedByConstructorOfTestedClass() {
        assertEquals(tested3.name, name);
        assertEquals(tested3.number, number);
        assertSame(tested3.names, test);
        assertSame(tested3.numbersAndNames, numbersAndNames);
    }
}
