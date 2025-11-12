/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertSame;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

class BaseTestClass {
    static final class Dependency {
    }

    @Tested
    final Dependency dependency = new Dependency();
}

/**
 * The Class TestedClassInjectedFromBaseTest.
 */
@ExtendWith(JMockitExtension.class)
class TestedClassInjectedFromBaseTest extends BaseTestClass {

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
