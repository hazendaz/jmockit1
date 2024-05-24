package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.nio.CharBuffer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The Class FakingBaseTypesTest.
 */
@TestMethodOrder(MethodName.class)
final class FakingBaseTypesTest {

    /**
     * The Class BeforeClassBaseAction.
     */
    static class BeforeClassBaseAction {
        /**
         * Perform.
         *
         * @return the int
         */
        protected int perform() {
            return -1;
        }
    }

    /**
     * The Class BeforeClassAction.
     */
    static class BeforeClassAction extends BeforeClassBaseAction {
        @Override
        protected int perform() {
            return 12;
        }
    }

    /**
     * Apply fake for all tests.
     *
     * @param <T>
     *            the generic type
     */
    @BeforeAll
    static <T extends BeforeClassBaseAction> void applyFakeForAllTests() {
        new MockUp<T>() {
            @Mock
            int perform() {
                return 34;
            }
        };
    }

    /**
     * Verify fake for all tests is in effect.
     */
    @AfterAll
    static void verifyFakeForAllTestsIsInEffect() {
        int i1 = new BeforeClassAction().perform();
        int i2 = new BeforeClassBaseAction().perform();

        assertEquals(34, i1);
        assertEquals(34, i2);
    }

    /**
     * The Interface IBeforeAction.
     */
    public interface IBeforeAction {
        /**
         * Perform.
         *
         * @return the int
         */
        @SuppressWarnings("unused")
        int perform();
    }

    /**
     * The Class BeforeAction.
     */
    static class BeforeAction implements IBeforeAction {
        @Override
        public int perform() {
            return 123;
        }
    }

    /**
     * Apply fake for each test.
     *
     * @param <T>
     *            the generic type
     */
    @BeforeEach
    <T extends IBeforeAction> void applyFakeForEachTest() {
        new MockUp<T>() {
            @Mock
            int perform() {
                return 56;
            }
        };
    }

    /**
     * Verify fake for each test is in effect.
     */
    @AfterEach
    void verifyFakeForEachTestIsInEffect() {
        int i = new BeforeAction().perform();
        assertEquals(56, i);
    }

    /**
     * The Interface IAction.
     */
    public interface IAction {

        /**
         * Perform.
         *
         * @param i
         *            the i
         *
         * @return the int
         */
        int perform(int i);

        /**
         * Not to be faked.
         *
         * @return true, if successful
         */
        boolean notToBeFaked();
    }

    /**
     * The Class ActionImpl1.
     */
    public static class ActionImpl1 implements IAction {
        @Override
        public int perform(int i) {
            return i - 1;
        }

        @Override
        public boolean notToBeFaked() {
            return true;
        }
    }

    /**
     * The Class ActionImpl2.
     */
    static final class ActionImpl2 implements IAction {
        @Override
        public int perform(int i) {
            return i - 2;
        }

        @Override
        public boolean notToBeFaked() {
            return true;
        }
    }

    /** The action I. */
    IAction actionI;

    /**
     * Test 3 fake interface implementation classes using anonymous fake.
     *
     * @param <T>
     *            the generic type
     */
    @Test
    <T extends IAction> void test3_fakeInterfaceImplementationClassesUsingAnonymousFake() {
        actionI = new ActionImpl1();

        new MockUp<T>() {
            @Mock
            int perform(int i) {
                return i + 1;
            }
        };

        assertEquals(2, actionI.perform(1));
        assertTrue(actionI.notToBeFaked());

        ActionImpl2 impl2 = new ActionImpl2();
        assertEquals(3, impl2.perform(2));
        assertTrue(impl2.notToBeFaked());
    }

    /**
     * The Interface TestInterface.
     */
    public interface TestInterface {
        /**
         * Gets the data.
         *
         * @return the data
         */
        String getData();
    }

    /**
     * The Class FakeTestInterface.
     *
     * @param <T>
     *            the generic type
     */
    public static final class FakeTestInterface<T extends TestInterface> extends MockUp<T> {

        /**
         * Gets the data.
         *
         * @param inv
         *            the inv
         *
         * @return the data
         */
        @Mock
        public String getData(Invocation inv) {
            return "faked" + inv.proceed();
        }
    }

    /**
     * Fake all classes implementing an interface using named fake with invocation parameter.
     */
    @Test
    void fakeAllClassesImplementingAnInterfaceUsingNamedFakeWithInvocationParameter() {
        TestInterface impl1 = new TestInterface() {
            @Override
            public String getData() {
                return "1";
            }
        };
        TestInterface impl2 = new TestInterface() {
            @Override
            public String getData() {
                return "2";
            }
        };
        new FakeTestInterface();

        String faked1 = impl1.getData();
        String faked2 = impl2.getData();

        assertEquals("faked1", faked1);
        assertEquals("faked2", faked2);
    }

    /**
     * The Class BaseAction.
     */
    public abstract static class BaseAction {

        /**
         * Perform.
         *
         * @param i
         *            the i
         *
         * @return the int
         */
        protected abstract int perform(int i);

        /**
         * To be faked as well.
         *
         * @return the int
         */
        public int toBeFakedAsWell() {
            return -1;
        }

        /**
         * Not to be faked.
         *
         * @return the int
         */
        int notToBeFaked() {
            return 1;
        }
    }

    /**
     * The Class ConcreteAction1.
     */
    static final class ConcreteAction1 extends BaseAction {
        @Override
        public int perform(int i) {
            return i - 1;
        }
    }

    /**
     * The Class ConcreteAction2.
     */
    static class ConcreteAction2 extends BaseAction {
        @Override
        protected int perform(int i) {
            return i - 2;
        }

        @Override
        public int toBeFakedAsWell() {
            return super.toBeFakedAsWell() - 1;
        }

        @Override
        int notToBeFaked() {
            return super.notToBeFaked() + 1;
        }
    }

    /**
     * The Class ConcreteAction3.
     */
    static class ConcreteAction3 extends ConcreteAction2 {
        @Override
        public int perform(int i) {
            return i - 3;
        }

        @Override
        public int toBeFakedAsWell() {
            return -3;
        }

        @Override
        final int notToBeFaked() {
            return 3;
        }
    }

    /** The action B. */
    BaseAction actionB;

    /**
     * Test 4 fake concrete subclasses using anonymous fake.
     *
     * @param <T>
     *            the generic type
     */
    @Test
    <T extends BaseAction> void test4_fakeConcreteSubclassesUsingAnonymousFake() {
        actionB = new ConcreteAction1();

        new MockUp<T>() {
            @Mock
            int perform(int i) {
                return i + 1;
            }

            @Mock
            int toBeFakedAsWell() {
                return 123;
            }
        };

        assertEquals(2, actionB.perform(1));
        assertEquals(123, actionB.toBeFakedAsWell());
        assertEquals(1, actionB.notToBeFaked());

        ConcreteAction2 action2 = new ConcreteAction2();
        assertEquals(3, action2.perform(2));
        assertEquals(123, action2.toBeFakedAsWell());
        assertEquals(2, action2.notToBeFaked());

        ConcreteAction3 action3 = new ConcreteAction3();
        assertEquals(4, action3.perform(3));
        assertEquals(123, action3.toBeFakedAsWell());
        assertEquals(3, action3.notToBeFaked());
    }

    /**
     * Check implementation classes are no longer faked.
     */
    @AfterEach
    void checkImplementationClassesAreNoLongerFaked() {
        if (actionI != null) {
            assertEquals(-1, actionI.perform(0));
        }

        assertEquals(-2, new ActionImpl2().perform(0));

        if (actionB != null) {
            assertEquals(-1, actionB.perform(0));
        }

        assertEquals(-2, new ConcreteAction2().perform(0));
        assertEquals(-3, new ConcreteAction3().perform(0));
    }

    /**
     * The Class FakeInterface.
     *
     * @param <T>
     *            the generic type
     */
    static final class FakeInterface<T extends IAction> extends MockUp<T> {

        /**
         * Perform.
         *
         * @param i
         *            the i
         *
         * @return the int
         */
        @Mock
        int perform(int i) {
            return i + 2;
        }
    }

    /**
     * Test 5 fake interface implementation classes using named fake.
     */
    @Test
    void test5_fakeInterfaceImplementationClassesUsingNamedFake() {
        new FakeInterface();

        actionI = new ActionImpl1();
        assertEquals(3, actionI.perform(1));
        assertEquals(4, new ActionImpl2().perform(2));
    }

    /**
     * The Class FakeBaseClass.
     *
     * @param <T>
     *            the generic type
     */
    static final class FakeBaseClass<T extends BaseAction> extends MockUp<T> {

        /**
         * Perform.
         *
         * @param i
         *            the i
         *
         * @return the int
         */
        @Mock
        int perform(int i) {
            return i + 3;
        }
    }

    /**
     * Test 6 fake concrete subclasses using named fake.
     */
    @Test
    void test6_fakeConcreteSubclassesUsingNamedFake() {
        new FakeBaseClass();

        actionB = new ConcreteAction1();
        assertEquals(4, actionB.perform(1));
        assertEquals(5, new ConcreteAction2().perform(2));
        assertEquals(6, new ConcreteAction3().perform(3));
    }

    /**
     * The Interface GenericIAction.
     *
     * @param <N>
     *            the number type
     */
    interface GenericIAction<N extends Number> {
        /**
         * Perform.
         *
         * @param n
         *            the n
         *
         * @return the n
         */
        N perform(N n);
    }

    /**
     * Test 7 fake implementations of generic interface.
     *
     * @param <M>
     *            the generic type
     */
    @Test
    <M extends GenericIAction<Number>> void test7_fakeImplementationsOfGenericInterface() {
        GenericIAction<Number> actionNumber = new GenericIAction<Number>() {
            @Override
            public Number perform(Number n) {
                return n.intValue() + 1;
            }
        };

        GenericIAction<Integer> actionInt = new GenericIAction<Integer>() {
            @Override
            public Integer perform(Integer n) {
                return n + 1;
            }
        };

        GenericIAction<Long> actionL = new GenericIAction<Long>() {
            @Override
            public Long perform(Long n) {
                return n + 2;
            }
        };

        new MockUp<M>() {
            @Mock
            Number perform(Number n) {
                return n.intValue() - 1;
            }
        };

        Number n = actionNumber.perform(1);
        assertEquals(0, n); // mocked

        int i = actionInt.perform(2);
        assertEquals(3, i); // not mocked

        long l = actionL.perform(3L);
        assertEquals(5, l); // not mocked
    }

    /**
     * Test 8 exclude JRE classes from faking for safety.
     *
     * @param <R>
     *            the generic type
     *
     * @throws Exception
     *             the exception
     */
    @Test
    <R extends Readable> void test8_excludeJREClassesFromFakingForSafety() throws Exception {
        new MockUp<R>() {
            @Mock
            int read(CharBuffer cb) {
                return 123;
            }
        };

        CharBuffer buf = CharBuffer.allocate(10);
        int r1 = new Readable() {
            @Override
            public int read(@NonNull CharBuffer cb) {
                return 1;
            }
        }.read(buf);
        assertEquals(123, r1);

        int r2 = new StringReader("test").read(buf);
        assertEquals(4, r2);
    }
}
