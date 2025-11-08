package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class FinalMockFieldsTest.
 */
@ExtendWith(JMockitExtension.class)
class FinalMockFieldsTest {

    /**
     * The Class Collaborator.
     */
    static final class Collaborator {

        /**
         * Instantiates a new collaborator.
         */
        Collaborator() {
        }

        /**
         * Instantiates a new collaborator.
         *
         * @param b
         *            the b
         */
        Collaborator(boolean b) {
            if (!b) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        int getValue() {
            return -1;
        }

        /**
         * Do something.
         */
        void doSomething() {
        }
    }

    /**
     * The Class AnotherCollaborator.
     */
    static final class AnotherCollaborator {

        /**
         * Gets the value.
         *
         * @return the value
         */
        int getValue() {
            return -1;
        }

        /**
         * Do something.
         */
        void doSomething() {
        }
    }

    /** The mock. */
    @Injectable
    final Collaborator mock = new Collaborator();

    /** The mock 2. */
    @Mocked
    final AnotherCollaborator mock2 = new AnotherCollaborator();

    /**
     * Use mocked types.
     */
    @BeforeEach
    void useMockedTypes() {
        assertEquals(0, mock.getValue());
        assertEquals(0, mock2.getValue());
        assertEquals(0, YetAnotherCollaborator.doSomethingStatic());
    }

    /**
     * Record expectations on injectable final mock field.
     */
    @Test
    void recordExpectationsOnInjectableFinalMockField() {
        new Expectations() {
            {
                mock.getValue();
                result = 12;
                mock.doSomething();
                times = 0;
            }
        };

        assertEquals(12, mock.getValue());
    }

    /**
     * Record expectations on final mock field.
     */
    @Test
    void recordExpectationsOnFinalMockField() {
        AnotherCollaborator collaborator = new AnotherCollaborator();

        new Expectations() {
            {
                mock2.doSomething();
                times = 1;
            }
        };

        collaborator.doSomething();
        assertEquals(0, collaborator.getValue());
    }

    /** The mock process builder. */
    @Mocked
    final ProcessBuilder mockProcessBuilder = null;

    /**
     * Record expectations on constructor of final mock field.
     */
    @Test
    void recordExpectationsOnConstructorOfFinalMockField() {
        new Expectations() {
            {
                new ProcessBuilder("test");
                times = 1;
            }
        };

        new ProcessBuilder("test");
    }

    /**
     * The Class YetAnotherCollaborator.
     */
    static final class YetAnotherCollaborator {

        /**
         * Instantiates a new yet another collaborator.
         *
         * @param b
         *            the b
         */
        YetAnotherCollaborator(boolean b) {
            if (!b) {
                throw new IllegalArgumentException();
            }
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        int getValue() {
            return -1;
        }

        /**
         * Do something.
         */
        void doSomething() {
        }

        /**
         * Do something static.
         *
         * @return the int
         */
        static int doSomethingStatic() {
            return -2;
        }
    }

    /** The unused. */
    @Mocked
    final YetAnotherCollaborator unused = null;

    /**
     * Record expectations on static method and constructor of final local mock field.
     */
    @Test
    void recordExpectationsOnStaticMethodAndConstructorOfFinalLocalMockField() {
        new Expectations() {
            {
                new YetAnotherCollaborator(true);
                result = new RuntimeException();
                YetAnotherCollaborator.doSomethingStatic();
                result = 123;
            }
        };

        try {
            new YetAnotherCollaborator(true);
            fail();
        } catch (RuntimeException ignore) {
        }

        assertEquals(123, YetAnotherCollaborator.doSomethingStatic());
    }
}
