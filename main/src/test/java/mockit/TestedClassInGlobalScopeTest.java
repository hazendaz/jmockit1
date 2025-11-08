package mockit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class TestedClassInGlobalScopeTest.
 */
@ExtendWith(JMockitExtension.class)
@TestMethodOrder(MethodName.class)
class TestedClassInGlobalScopeTest {

    /**
     * The Class TestedClass.
     */
    static class TestedClass {
        /** The some value. */
        Integer someValue;
    }

    /** The tested global. */
    @Tested(fullyInitialized = true, global = true)
    TestedClass testedGlobal;

    /** The tested local. */
    @Tested(fullyInitialized = true)
    TestedClass testedLocal;

    /**
     * Use tested object in first step of tested scenario.
     */
    @Test
    void useTestedObjectInFirstStepOfTestedScenario() {
        assertNull(testedGlobal.someValue);
        assertNotSame(testedGlobal, testedLocal);
        testedGlobal.someValue = 123;
    }

    /**
     * Use tested object in second step of tested scenario.
     */
    @Test
    void useTestedObjectInSecondStepOfTestedScenario() {
        assertNotNull(testedGlobal.someValue);
        assertNull(testedLocal.someValue);
    }
}
