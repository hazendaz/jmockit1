/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package java8testing;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * The Class JUnit5Test.
 */
final class JUnit5Test {

    /** The utils. */
    @Tested(availableDuringSetup = true)
    TestUtils utils;

    /** The cut. */
    @Tested
    BusinessService cut;

    /** The collaborator. */
    @Injectable
    Collaborator collaborator;

    /**
     * Check mock and tested fields.
     */
    @BeforeEach
    void checkMockAndTestedFields() {
        assertNotNull(utils);
        assertNotNull(collaborator);
        assertNull(cut);
    }

    /**
     * Check mock and tested fields again.
     */
    @AfterEach
    void checkMockAndTestedFieldsAgain() {
        assertNotNull(utils);
        assertNotNull(collaborator);
        assertNull(cut);
    }

    /**
     * With parameter provided by J unit.
     *
     * @param testInfo
     *            the test info
     */
    @Test
    void withParameterProvidedByJUnit(TestInfo testInfo) {
        assertNotNull(testInfo);
    }

    /**
     * With mock parameters.
     *
     * @param mock
     *            the mock
     * @param text
     *            the text
     */
    @Test
    void withMockParameters(@Mocked Runnable mock, @Injectable("test") String text) {
        assertNotNull(mock);
        assertEquals("test", text);
        assertNotNull(collaborator);
        assertSame(collaborator, cut.getCollaborator());
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
    public void injectParameterUsingTestedAsMetaAnnotation(@InjectedDependency BusinessService col) {
        assertNotNull(col);
    }

    /**
     * Record expectation on collaborator.
     */
    @Test
    void recordExpectationOnCollaborator() {
        new Expectations() {
            {
                collaborator.doSomething(anyInt);
                result = "Test";
            }
        };

        String result = cut.performBusinessOperation(123);

        assertEquals("Test", result);
    }

    /**
     * The Class InnerTest.
     */
    @Nested
    final class InnerTest {

        /**
         * Sets the up.
         */
        @BeforeEach
        void setUp() {
            assertNotNull(utils);
            assertNotNull(collaborator);
            assertNull(cut);
        }

        /**
         * Inner test.
         */
        @Test
        void innerTest() {
            assertNotNull(collaborator);
            assertSame(collaborator, cut.getCollaborator());
        }

        /**
         * Inner test with mock parameter.
         *
         * @param number
         *            the number
         */
        @Test
        void innerTestWithMockParameter(@Injectable("123") int number) {
            assertEquals(123, number);
        }
    }
}
