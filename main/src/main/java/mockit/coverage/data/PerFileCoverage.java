/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.data;

import java.io.Serializable;

import org.checkerframework.checker.index.qual.NonNegative;

public interface PerFileCoverage extends Serializable {
    @NonNegative
    int getTotalItems();

    @NonNegative
    int getCoveredItems();

    int getCoveragePercentage();
}
