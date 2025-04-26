package java8testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.Mocked;
import mockit.Verifications;
import mockit.VerificationsInOrder;

import org.junit.jupiter.api.Test;

/**
 * The Class InterfacesWithMethodBodiesTest.
 */
final class InterfacesWithMethodBodiesTest {

    /**
     * The Interface InterfaceWithDefaultMethods.
     */
    @FunctionalInterface
    public interface InterfaceWithDefaultMethods {

        /**
         * Regular method.
         *
         * @return the int
         */
        int regularMethod();

        /**
         * Default method.
         *
         * @return the int
         */
        default int defaultMethod() {
            return -1;
        }
    }

    /**
     * The Class ClassWhichOverridesDefaultMethodFromInterface.
     */
    public static final class ClassWhichOverridesDefaultMethodFromInterface implements InterfaceWithDefaultMethods {
        @Override
        public int regularMethod() {
            return 4;
        }

        @Override
        public int defaultMethod() {
            return 5;
        }
    }

    /**
     * Mock interface with default methods.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockInterfaceWithDefaultMethods(@Mocked InterfaceWithDefaultMethods mock) {
        new Expectations() {
            {
                mock.defaultMethod();
                result = 2;
                mock.regularMethod();
                result = 1;
            }
        };

        assertEquals(1, mock.regularMethod());
        assertEquals(2, mock.defaultMethod());
    }

    /**
     * Mock class with overridden default method.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockClassWithOverriddenDefaultMethod(@Mocked ClassWhichOverridesDefaultMethodFromInterface mock) {
        new Expectations() {
            {
                mock.defaultMethod();
                result = 2;
                mock.regularMethod();
                result = 1;
            }
        };

        assertEquals(1, mock.regularMethod());
        assertEquals(2, mock.defaultMethod());
    }

    /**
     * The Class ClassWhichInheritsDefaultMethodFromInterface.
     */
    public static class ClassWhichInheritsDefaultMethodFromInterface implements InterfaceWithDefaultMethods {
        @Override
        public int regularMethod() {
            return 3;
        }
    }

    /**
     * Mock class with inherited default method.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockClassWithInheritedDefaultMethod(@Mocked ClassWhichInheritsDefaultMethodFromInterface mock) {
        new Expectations() {
            {
                mock.defaultMethod();
                result = 123;
            }
        };

        assertEquals(123, mock.defaultMethod());
    }

    /**
     * The Interface SubInterfaceWithDefaultMethods.
     */
    public interface SubInterfaceWithDefaultMethods extends InterfaceWithDefaultMethods {

        /**
         * Another default method.
         *
         * @param i
         *            the i
         *
         * @return the string
         */
        default String anotherDefaultMethod(int i) {
            return String.valueOf(i);
        }

        /**
         * Another regular method.
         *
         * @param b
         *            the b
         * @param names
         *            the names
         */
        @SuppressWarnings("unused")
        void anotherRegularMethod(boolean b, String... names);
    }

    /**
     * The Class ClassInheritingFromInterfaceHierarchy.
     */
    static final class ClassInheritingFromInterfaceHierarchy implements SubInterfaceWithDefaultMethods {
        @Override
        public int regularMethod() {
            return 4;
        }

        @Override
        public void anotherRegularMethod(boolean b, String... names) {
        }
    }

    /**
     * Mock class inheriting from interface hierarchy.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockClassInheritingFromInterfaceHierarchy(@Injectable ClassInheritingFromInterfaceHierarchy mock) {
        new Expectations() {
            {
                mock.defaultMethod();
                result = 123;
                mock.regularMethod();
                result = 22;
                mock.anotherDefaultMethod(anyInt);
                result = "one";
            }
        };

        assertEquals(123, mock.defaultMethod());
        assertEquals(22, mock.regularMethod());
        assertEquals("one", mock.anotherDefaultMethod(1));
    }

    /**
     * The Interface AnotherInterfaceWithDefaultMethods.
     */
    public interface AnotherInterfaceWithDefaultMethods {

        /**
         * Default method 1.
         *
         * @return the int
         */
        default int defaultMethod1() {
            return 1;
        }

        /**
         * Default method 2.
         *
         * @return the int
         */
        default int defaultMethod2() {
            return 2;
        }
    }

    /**
     * The Class ClassInheritingMultipleDefaultMethods.
     */
    static final class ClassInheritingMultipleDefaultMethods
            implements SubInterfaceWithDefaultMethods, AnotherInterfaceWithDefaultMethods {
        @Override
        public int regularMethod() {
            return 5;
        }

        @Override
        public void anotherRegularMethod(boolean b, String... names) {
        }
    }

    /**
     * Partially mock object inheriting default methods from multiple interfaces.
     */
    @Test
    void partiallyMockObjectInheritingDefaultMethodsFromMultipleInterfaces() {
        ClassInheritingMultipleDefaultMethods obj = new ClassInheritingMultipleDefaultMethods();

        new Expectations(obj) {
            {
                obj.defaultMethod();
                result = 123;
                obj.defaultMethod2();
                result = 22;
                obj.anotherDefaultMethod(1);
                result = "one";
            }
        };

        assertEquals(123, obj.defaultMethod());
        assertEquals(5, obj.regularMethod());
        assertEquals(1, obj.defaultMethod1());
        assertEquals(22, obj.defaultMethod2());
        assertEquals("one", obj.anotherDefaultMethod(1));
        obj.anotherRegularMethod(true);

        new Verifications() {
            {
                obj.anotherRegularMethod(anyBoolean, (String[]) any);
            }
        };
    }

    /**
     * The Interface InterfaceWithStaticMethod.
     */
    public interface InterfaceWithStaticMethod {
        /**
         * New instance.
         *
         * @return the interface with static method
         */
        static InterfaceWithStaticMethod newInstance() {
            return null;
        }
    }

    /**
     * Mock static method in interface.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockStaticMethodInInterface(@Mocked InterfaceWithStaticMethod mock) {
        InterfaceWithStaticMethod actual = InterfaceWithStaticMethod.newInstance();
        assertSame(mock, actual);
    }

    /**
     * Mock functional interface from JRE.
     *
     * @param mockConsumer
     *            the mock consumer
     */
    @Test
    void mockFunctionalInterfaceFromJRE(@Mocked Consumer<String> mockConsumer) {
        StringBuilder concatenated = new StringBuilder();

        new Expectations() {
            {
                mockConsumer.accept(anyString);
                result = new Delegate<Object>() {
                    @Mock
                    void delegate(String s) {
                        concatenated.append(s).append(' ');
                    }
                };
            }
        };

        List<String> list = Arrays.asList("mocking", "a", "lambda");
        list.forEach(mockConsumer);

        assertEquals("mocking a lambda ", concatenated.toString());
    }

    /**
     * The Interface NonPublicBase.
     */
    interface NonPublicBase {

        /**
         * Base default.
         *
         * @return the int
         */
        default int baseDefault() {
            return -1;
        }

        /**
         * Gets the default.
         *
         * @return the default
         */
        default String getDefault() {
            return "default";
        }

        /**
         * Do static.
         */
        static void doStatic() {
            throw new RuntimeException("1");
        }
    }

    /**
     * The Interface NonPublicDerived.
     */
    interface NonPublicDerived extends NonPublicBase {

        /**
         * Gets the default.
         *
         * @return the default
         */
        @Override
        default String getDefault() {
            return "default derived";
        }

        /**
         * Do another static.
         */
        static void doAnotherStatic() {
            throw new RuntimeException("2");
        }
    }

    /**
     * Mock non public interface hierarchy with default and static methods.
     *
     * @param base
     *            the base
     * @param derived
     *            the derived
     */
    @Test
    void mockNonPublicInterfaceHierarchyWithDefaultAndStaticMethods(@Mocked NonPublicBase base,
            @Mocked NonPublicDerived derived) {
        new Expectations() {
            {
                base.baseDefault();
                result = 1;
                derived.baseDefault();
                result = 2;
            }
        };

        assertEquals(1, base.baseDefault());
        assertEquals(2, derived.baseDefault());
        assertNull(base.getDefault());
        NonPublicBase.doStatic();
        NonPublicDerived.doAnotherStatic();

        new VerificationsInOrder() {
            {
                NonPublicBase.doStatic();
                NonPublicDerived.doAnotherStatic();
            }
        };
    }
}
