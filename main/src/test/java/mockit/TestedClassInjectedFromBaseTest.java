package mockit;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class BaseTestClass {
    static final class Dependency {
    }

    @Tested
    final Dependency dependency = new Dependency();
}

/**
 * The Class TestedClassInjectedFromBaseTest.
 */
final class TestedClassInjectedFromBaseTest extends BaseTestClass {

    /**
     * The Class TestedClass.
     */
    static final class TestedClass {
        /** The dependency. */
        Dependency dependency;
    }

    /** The tested. */
    @Tested(fullyInitialized = true)
    TestedClass tested;

    /**
     * Verify tested object injected with tested dependency provided by base test class.
     */
    @Test
    void verifyTestedObjectInjectedWithTestedDependencyProvidedByBaseTestClass() {
        assertSame(dependency, tested.dependency);
    }
}
