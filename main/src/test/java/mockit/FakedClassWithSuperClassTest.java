package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

/**
 * The Class FakedClassWithSuperClassTest.
 */
public final class FakedClassWithSuperClassTest {

    /**
     * The Class BaseClass.
     */
    static class BaseClass {
        /**
         * Do something.
         *
         * @return the int
         */
        protected int doSomething() {
            return 123;
        }
    }

    /**
     * The Class Subclass.
     */
    public static class Subclass extends BaseClass {
        /**
         * Gets the single instance of Subclass.
         *
         * @return single instance of Subclass
         */
        BaseClass getInstance() {
            return this;
        }
    }

    /**
     * The Class FakeForSubclass.
     */
    public static final class FakeForSubclass extends MockUp<Subclass> {

        /**
         * Do something.
         *
         * @return the int
         */
        @Mock
        public int doSomething() {
            return 1;
        }
    }

    /**
     * Fake only instances of the class specified to be faked.
     */
    @Test
    public void fakeOnlyInstancesOfTheClassSpecifiedToBeFaked() {
        BaseClass d = new Subclass();
        assertEquals(123, d.doSomething());

        new FakeForSubclass();

        assertEquals(1, d.doSomething());
        assertEquals(123, new BaseClass().doSomething());
        assertEquals(1, new Subclass().doSomething());
        assertEquals(123, new BaseClass() {
        }.doSomething());
        assertEquals(1, new Subclass() {
        }.doSomething());
    }

    /**
     * Fake only instances of the class specified to be faked using fake method bridge.
     */
    @Test
    public void fakeOnlyInstancesOfTheClassSpecifiedToBeFaked_usingFakeMethodBridge() {
        BaseClass d = new Subclass();
        assertEquals(123, d.doSomething());

        new MockUp<Subclass>() {
            @Mock
            int doSomething() {
                return 2;
            }
        };

        assertEquals(123, new BaseClass().doSomething());
        assertEquals(2, d.doSomething());
        assertEquals(2, new Subclass().doSomething());
        assertEquals(123, new BaseClass() {
        }.doSomething());
        assertEquals(2, new Subclass() {
        }.doSomething());
    }
}
