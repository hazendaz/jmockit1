/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import mockit.integration.junit5.ExpectedException;
import mockit.integration.junit5.JMockitExtension;
import mockit.internal.expectations.invocation.MissingInvocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class ExpectationsForConstructorsTest.
 */
@ExtendWith(JMockitExtension.class)
class ExpectationsForConstructorsTest {

    /**
     * The Class BaseCollaborator.
     */
    public static class BaseCollaborator {

        /** The value. */
        protected int value;

        /**
         * Instantiates a new base collaborator.
         */
        protected BaseCollaborator() {
            value = -1;
        }

        /**
         * Instantiates a new base collaborator.
         *
         * @param value
         *            the value
         */
        protected BaseCollaborator(int value) {
            this.value = value;
        }
    }

    /**
     * The Class Collaborator.
     */
    static class Collaborator extends BaseCollaborator {

        /**
         * Instantiates a new collaborator.
         */
        Collaborator() {
        }

        /**
         * Instantiates a new collaborator.
         *
         * @param value
         *            the value
         */
        Collaborator(int value) {
            super(value);
        }
    }

    /**
     * Mock all constructors.
     *
     * @param unused
     *            the unused
     */
    @Test
    void mockAllConstructors(@Mocked Collaborator unused) {
        new Expectations() {
            {
                new Collaborator();
                new Collaborator(123);
            }
        };

        assertEquals(0, new Collaborator().value);
        assertEquals(0, new Collaborator(123).value);
    }

    /**
     * The Class A.
     */
    static class A {

        /**
         * Instantiates a new a.
         */
        @SuppressWarnings("UnusedDeclaration")
        private A() {
        }

        /**
         * Instantiates a new a.
         *
         * @param s
         *            the s
         */
        A(String s) {
            assertNotNull("A(String) executed with null", s);
        }
    }

    /**
     * The Class B.
     */
    static class B extends A {
        /**
         * Instantiates a new b.
         *
         * @param s
         *            the s
         */
        B(String s) {
            super(s);
        }
    }

    /**
     * Mock class hierarchy where first constructor in base class is private.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockClassHierarchyWhereFirstConstructorInBaseClassIsPrivate(@Mocked B mock) {
        new B("Test1");
    }

    /**
     * The Class D.
     */
    static class D {
        /**
         * Instantiates a new d.
         *
         * @param s
         *            the s
         */
        D(@SuppressWarnings("unused") String s) {
        }
    }

    /**
     * Mock class hierarchy where first constructor in base class on another package is package private.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockClassHierarchyWhereFirstConstructorInBaseClassOnAnotherPackageIsPackagePrivate(@Mocked D mock) {
        assertNotNull(mock);
        new D("Test1");
    }

    /**
     * The Class Base.
     */
    static class Base {
    }

    /**
     * The Class Derived.
     */
    static class Derived extends Base {
    }

    /**
     * Record and replay base constructor invocation.
     *
     * @param mocked
     *            the mocked
     */
    @Test
    void recordAndReplayBaseConstructorInvocation(@Mocked Base mocked) {
        new Expectations() {
            {
                new Base();
            }
        };

        new Base();
    }

    /**
     * Record expectation on base constructor and replay with call to super.
     *
     * @param mocked
     *            the mocked
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void recordExpectationOnBaseConstructorAndReplayWithCallToSuper(@Mocked Base mocked) {
        new Expectations() {
            {
                new Base();
                times = 1;
            }
        };

        new Derived();
    }

    /**
     * Verify expectation on base constructor replayed with call to super.
     *
     * @param mocked
     *            the mocked
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void verifyExpectationOnBaseConstructorReplayedWithCallToSuper(@Mocked Base mocked) {
        new Derived();

        new Verifications() {
            {
                new Base();
            }
        };
    }

    /**
     * The Class Collaborator2.
     */
    static class Collaborator2 {

        /**
         * Instantiates a new collaborator 2.
         *
         * @param l
         *            the l
         */
        Collaborator2(@SuppressWarnings("unused") long l) {
        }

        /**
         * Instantiates a new collaborator 2.
         *
         * @param c
         *            the c
         */
        Collaborator2(@SuppressWarnings("unused") Collaborator2 c) {
        }

        /**
         * Instantiates a new collaborator 2.
         */
        Collaborator2() {
            this(new Collaborator2(123L));
        }
    }

    /**
     * Mock constructor which calls two others of the same class.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockConstructorWhichCallsTwoOthersOfTheSameClass(@Mocked Collaborator2 mock) {
        new Collaborator2();
    }
}
