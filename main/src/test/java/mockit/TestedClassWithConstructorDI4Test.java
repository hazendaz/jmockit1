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

import jakarta.inject.Inject;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

/**
 * The Class TestedClassWithConstructorDI4Test.
 */
public final class TestedClassWithConstructorDI4Test {

    /**
     * The Class GenericClass.
     *
     * @param <T>
     *            the generic type
     */
    static class GenericClass<T> {
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
     * The Class TestedClass.
     */
    @SuppressWarnings("FieldMayBeFinal")
    public static final class TestedClass {

        /** The go. */
        final GenericClass<String> go;

        /** The values. */
        final List<Integer> values;

        /** The action 1. */
        final Callable<Number> action1;

        /** The action 2. */
        private Callable<Number> action2;

        /** The action 3. */
        private Callable<Number> action3;

        /** The database. */
        private DataSource database;

        /**
         * Instantiates a new tested class.
         *
         * @param go
         *            the go
         * @param values
         *            the values
         * @param actions
         *            the actions
         */
        @SuppressWarnings("unchecked")
        public TestedClass(GenericClass<String> go, List<Integer> values, Callable<Number>... actions) {
            this.go = go;
            this.values = values;
            action1 = actions[0];
            if (actions.length > 1) {
                action2 = actions[1];
            }
            if (actions.length > 2) {
                action3 = actions[2];
            }

            try {
                // noinspection JNDIResourceOpenedButNotSafelyClosed
                InitialContext context = new InitialContext();
                database = (DataSource) context.lookup("testDB");
                context.close();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** The tested. */
    @Tested
    TestedClass tested;

    /** The action 1. */
    @Injectable
    Callable<Number> action1;

    /** The mock GO. */
    @Injectable
    final GenericClass<String> mockGO = new GenericClass<>(); // still mocked

    @Injectable
    final List<Integer> numbers = asList(1, 2, 3); // not mocked when interface

    /** The jndi context. */
    @Mocked
    InitialContext jndiContext;

    /** The test DB. */
    @Mocked
    DataSource testDB;

    /**
     * Record common expectations.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    public void recordCommonExpectations() throws Exception {
        new Expectations() {
            {
                mockGO.doSomething();
                result = "test";
                minTimes = 0;
            }
        };
        new Expectations() {
            {
                jndiContext.lookup("testDB");
                result = testDB;
            }
        };
    }

    /**
     * Exercise tested object with values injected from mock fields.
     */
    @Test
    public void exerciseTestedObjectWithValuesInjectedFromMockFields() {
        assertNotNull(tested.go);
        assertEquals(asList(1, 2, 3), tested.values);
        assertSame(action1, tested.action1);
        assertEquals("test", mockGO.doSomething());
        assertNull(new GenericClass<String>().doSomething());
    }

    /**
     * Exercise tested object with values injected from mock parameters.
     *
     * @param action2
     *            the action 2
     * @param action3
     *            the action 3
     */
    @Test
    public void exerciseTestedObjectWithValuesInjectedFromMockParameters(@Injectable Callable<Number> action2,
            @Injectable Callable<Number> action3) {
        assertNotNull(tested.go);
        assertEquals(asList(1, 2, 3), tested.values);
        assertSame(action1, tested.action1);
        assertSame(action2, tested.action2);
        assertSame(action3, tested.action3);
        assertEquals("test", mockGO.doSomething());
        assertNull(new GenericClass().doSomething());
    }

    /**
     * Use mocked JRE classes during tested object creation.
     *
     * @param fileMock
     *            the file mock
     */
    @Test
    public void useMockedJREClassesDuringTestedObjectCreation(@Mocked File fileMock) {
        assertNotNull(tested.database);
        mockGO.doSomething();
    }

    /**
     * The Class TestedClass3.
     */
    static class TestedClass3 {

        /** The text. */
        final String text;

        /** The dependency. */
        final Runnable dependency;

        /** The other dep. */
        @Inject
        GenericClass<Integer> otherDep;

        /**
         * Instantiates a new tested class 3.
         *
         * @param text
         *            the text
         * @param dependency
         *            the dependency
         */
        TestedClass3(String text, Runnable dependency) {
            this.text = text;
            this.dependency = dependency;
        }
    }

    /** The tested 7. */
    @Tested
    TestedClass3 tested7;

    /** The text. */
    @Injectable
    final String text = null;

    /** The dependency. */
    @Injectable
    final Runnable dependency = null;

    /** The other dep. */
    @Injectable
    final GenericClass<Integer> otherDep = null;

    /**
     * Inject nulls through constructor parameters and into required field.
     */
    @Test
    public void injectNullsThroughConstructorParametersAndIntoRequiredField() {
        assertNull(tested7.text);
        assertNull(tested7.dependency);
        assertNull(tested7.otherDep);
    }
}
