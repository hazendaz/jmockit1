/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

import javax.sql.DataSource;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * The Class TestedClassWithQualifiedDependencyTest.
 */
@ExtendWith(JMockitExtension.class)
class TestedClassWithQualifiedDependencyTest {

    /**
     * The Class TestedClass.
     */
    public static class TestedClass {

        /** The dep 1. */
        @Inject
        private Dependency1 dep1;

        /** The dep 2. */
        @Autowired
        private Dependency2 dep2;

        /** The ds. */
        @Resource(name = "main-db")
        private DataSource ds;

        /** The text. */
        @Named("some.textual.value")
        private String text;

        /** The numeric value. */
        @Qualifier("a.BC-12")
        private Long numericValue;

        /** The numeric value 2. */
        @Qualifier("a.BC-12b")
        private Long numericValue2;
    }

    /**
     * The Class Dependency1.
     */
    public static class Dependency1 {

        /** The action. */
        @Inject
        @Named("action1")
        private Runnable action;

        /** The qualified dep. */
        @Autowired
        @Qualifier("foo")
        private Serializable qualifiedDep;
    }

    /**
     * The Class Dependency2.
     */
    public static class Dependency2 {

        /** The action. */
        @Qualifier("action2")
        private Runnable action;

        /** The qualified dep. */
        @Named("bar")
        private Serializable qualifiedDep;
    }

    /** The foo. */
    @Tested
    Serializable foo;

    /**
     * Creates the qualified dependency.
     */
    @BeforeEach
    void createQualifiedDependency() {
        foo = "foo";
    }

    /** The dependency 2. */
    @Tested
    Dependency2 dependency2;

    /** The a BC 12. */
    @Tested
    final Long aBC12 = 123L;

    /** The a BC 12 b. */
    @Tested
    final Long aBC12b = 45L;

    /** The tested. */
    @Tested(fullyInitialized = true)
    TestedClass tested;

    /** The action 1. */
    @Injectable
    Runnable action1;

    /**
     * Use tested object with different dependencies each having A qualified sub dependency.
     *
     * @param action2
     *            the action 2
     */
    @Test
    void useTestedObjectWithDifferentDependenciesEachHavingAQualifiedSubDependency(@Injectable Runnable action2) {
        assertSame(action2, dependency2.action);
        assertSame(dependency2, tested.dep2);
        assertSame(action1, tested.dep1.action);
        assertSame(foo, tested.dep1.qualifiedDep);
        assertNull(tested.dep2.qualifiedDep);
    }

    /** The dep 1. */
    @Tested
    DependencyImpl dep1;

    /** The tested 2. */
    @Tested(fullyInitialized = true)
    ClassWithQualifiedDependencyOfAbstractType tested2;

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
     * The Class ClassWithQualifiedDependencyOfAbstractType.
     */
    static class ClassWithQualifiedDependencyOfAbstractType {
        /** The dependency. */
        @Named("dep1")
        Dependency dependency;
    }

    /**
     * Use tested object of subtype for qualified abstract dependency type in another tested object.
     */
    @Test
    void useTestedObjectOfSubtypeForQualifiedAbstractDependencyTypeInAnotherTestedObject() {
        assertSame(dep1, tested2.dependency);
    }

    /** The main db. */
    @Injectable
    DataSource mainDb;

    /**
     * Verify dependencies having composite names.
     *
     * @param someTextualValue
     *            the some textual value
     */
    @Test
    void verifyDependenciesHavingCompositeNames(@Injectable("text value") String someTextualValue) {
        assertSame(mainDb, tested.ds);
        assertEquals(someTextualValue, tested.text);
        assertEquals(aBC12, tested.numericValue);
        assertEquals(aBC12b, tested.numericValue2);
    }
}
