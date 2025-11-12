/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.integration.junit4;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.Mocked;

import org.junit.Test;

public final class MockDependencyTest {
    @Mocked
    Dependency mock;

    @Test
    public void useMockedDependencies() {
        assertFalse(Dependency.alwaysTrue());

        if (AnotherDependency.mockedAtSuiteLevel) {
            assertFalse(AnotherDependency.alwaysTrue());
        } else {
            assertTrue(AnotherDependency.alwaysTrue());
        }
    }
}
