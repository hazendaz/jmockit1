/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

// CoverageCheck reads the "coverage-check" system property into a static final field the first time the class is
// loaded, so its configured thresholds cannot be varied per-test within the same JVM. This test only verifies the
// documented, side-effect-free default behavior: no thresholds configured means the check is a no-op.
final class CoverageCheckTest {

    @Test
    void createIfApplicableReturnsNullWhenNoCheckIsConfigured() {
        assertNull(CoverageCheck.createIfApplicable());
    }
}
