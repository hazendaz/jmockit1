/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import jakarta.inject.Inject;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class TestedParametersTest.
 */
@ExtendWith(JMockitExtension.class)
class TestedParametersTest {

    /**
     * The Class TestedClass.
     */
    static class TestedClass {

        /** The i. */
        final int i;

        /** The collaborator. */
        final Collaborator collaborator;

        /** The dependency. */
        Dependency dependency;

        /**
         * Instantiates a new tested class.
         */
        TestedClass() {
            i = -1;
            collaborator = null;
        }

        /**
         * Instantiates a new tested class.
         *
         * @param i
         *            the i
         * @param collaborator
         *            the collaborator
         */
        TestedClass(int i, Collaborator collaborator) {
            this.i = i;
            this.collaborator = collaborator;
        }
    }

    /**
     * The Class Dependency.
     */
    static class Dependency {
    }

    /**
     * The Class Collaborator.
     */
    static final class Collaborator {
    }

    /**
     * Creates the tested object for test method parameter.
     *
     * @param dep
     *            the dep
     */
    @Test
    void createTestedObjectForTestMethodParameter(@Tested Dependency dep) {
        assertNotNull(dep);
    }

    /** The tested 1. */
    @Tested
    TestedClass tested1;

    /** The tested 2. */
    @Tested(fullyInitialized = true)
    TestedClass tested2;

    /**
     * Inject tested object from test method parameter into fully initialized tested object.
     *
     * @param dep
     *            the dep
     */
    @Test
    void injectTestedObjectFromTestMethodParameterIntoFullyInitializedTestedObject(@Tested Dependency dep) {
        assertEquals(-1, tested2.i);
        assertNull(tested2.collaborator);
        assertSame(dep, tested2.dependency);
    }

    /**
     * Inject tested parameters into tested fields using constructor.
     *
     * @param i
     *            the i
     * @param collaborator
     *            the collaborator
     */
    @Test
    void injectTestedParametersIntoTestedFieldsUsingConstructor(@Tested("123") int i,
            @Tested Collaborator collaborator) {
        assertEquals(123, i);
        assertNotNull(collaborator);

        assertEquals(123, tested1.i);
        assertSame(collaborator, tested1.collaborator);
        assertNull(tested1.dependency);

        assertEquals(123, tested2.i);
        assertSame(collaborator, tested2.collaborator);
        assertNotNull(tested2.dependency);
    }

    /**
     * The Class TestedClass2.
     */
    static class TestedClass2 {
        /** The text. */
        CharSequence text;
        /** The n. */
        Number n;
        /** The cmp. */
        Comparable<Float> cmp;
    }

    /**
     * Inject tested parameters into tested fields of supertypes.
     *
     * @param s
     *            the s
     * @param n
     *            the n
     * @param cmp
     *            the cmp
     * @param tested
     *            the tested
     */
    @Test
    void injectTestedParametersIntoTestedFieldsOfSupertypes(@Tested("test") String s, @Tested("123") Integer n,
            @Tested("5.2") Float cmp, @Tested(fullyInitialized = true) TestedClass2 tested) {
        assertEquals("test", tested.text);
        assertEquals(123, tested.n.intValue());
        assertEquals(5.2F, tested.cmp);
    }

    /**
     * The Class TestedClass3.
     */
    static class TestedClass3 {
        /** The text. */
        String text;
        /** The number. */
        Number number;
    }

    /**
     * Inject tested parameters with values into fields of regular tested object.
     *
     * @param s
     *            the s
     * @param n
     *            the n
     * @param tested
     *            the tested
     */
    @Test
    void injectTestedParametersWithValuesIntoFieldsOfRegularTestedObject(@Tested("test") String s,
            @Tested("123") Integer n, @Tested TestedClass3 tested) {
        assertEquals("test", tested.text);
        assertEquals(123, tested.number);
    }

    /**
     * The Class TestedClass4.
     */
    static class TestedClass4 {

        /** The text. */
        final String text;

        /** The number. */
        final Number number;

        /**
         * Instantiates a new tested class 4.
         *
         * @param text
         *            the text
         * @param number
         *            the number
         */
        TestedClass4(String text, Number number) {
            this.text = text;
            this.number = number;
        }
    }

    /**
     * Inject tested parameter with value into regular tested object through constructor parameter.
     *
     * @param text
     *            the text
     * @param number
     *            the number
     * @param tested
     *            the tested
     */
    @Test
    void injectTestedParameterWithValueIntoRegularTestedObjectThroughConstructorParameter(@Tested("test") String text,
            @Tested("1.23") Double number, @Tested TestedClass4 tested) {
        assertEquals("test", tested.text);
        assertEquals(1.23, tested.number);
    }

    /**
     * The Class AnotherDependency.
     */
    static class AnotherDependency {
    }

    /**
     * The Class TestedClassWithDIAnnotatedField.
     */
    static class TestedClassWithDIAnnotatedField {
        /** The dep. */
        @Inject
        AnotherDependency dep;
    }

    /** The another dep. */
    @Injectable
    AnotherDependency anotherDep;

    /**
     * Inject injectable field into tested parameter.
     *
     * @param tested
     *            the tested
     */
    @Test
    void injectInjectableFieldIntoTestedParameter(@Tested TestedClassWithDIAnnotatedField tested) {
        assertSame(anotherDep, tested.dep);
    }

    /**
     * The Interface InjectedDependency.
     */
    @Target(PARAMETER)
    @Retention(RUNTIME)
    @Tested
    public @interface InjectedDependency {
    }

    /**
     * Inject parameter using tested as meta annotation.
     *
     * @param col
     *            the col
     */
    @Test
    void injectParameterUsingTestedAsMetaAnnotation(@InjectedDependency Collaborator col) {
        assertNotNull(col);
    }
}
