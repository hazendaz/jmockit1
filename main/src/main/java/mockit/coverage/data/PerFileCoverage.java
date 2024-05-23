/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
