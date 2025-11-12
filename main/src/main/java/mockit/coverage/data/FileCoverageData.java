/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.data;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.Serializable;

import mockit.coverage.CoveragePercentage;
import mockit.coverage.TestRun;
import mockit.coverage.dataItems.PerFileDataCoverage;
import mockit.coverage.lines.PerFileLineCoverage;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Coverage data gathered for the lines, branching points, and fields of a single source file.
 */
public final class FileCoverageData implements Serializable {
    private static final long serialVersionUID = 3508572808457541012L;

    @NonNull
    private static final PerFileLineCoverage NO_LINE_INFO = new PerFileLineCoverage();
    @NonNull
    private static final PerFileDataCoverage NO_DATA_INFO = new PerFileDataCoverage();

    @NonNull
    public PerFileLineCoverage lineCoverageInfo;
    @NonNull
    public PerFileDataCoverage dataCoverageInfo;

    // Used for fast indexed access.
    @NonNegative
    public final int index;

    // Used for output styling in the HTML report.
    @Nullable
    public String kindOfTopLevelType;

    // Used to track the last time the ".class" file was modified, to decide if merging can be done.
    @NonNegative
    long lastModified;

    private final boolean loadedAfterTestCompletion;

    FileCoverageData(@NonNegative int index, @Nullable String kindOfTopLevelType) {
        this.index = index;
        this.kindOfTopLevelType = kindOfTopLevelType;
        lineCoverageInfo = new PerFileLineCoverage();
        dataCoverageInfo = new PerFileDataCoverage();
        loadedAfterTestCompletion = TestRun.isTerminated();
    }

    boolean wasLoadedAfterTestCompletion() {
        return loadedAfterTestCompletion;
    }

    @NonNull
    public PerFileLineCoverage getLineCoverageData() {
        return lineCoverageInfo;
    }

    @NonNegative
    public int getTotalItems() {
        return lineCoverageInfo.getTotalItems() + dataCoverageInfo.getTotalItems();
    }

    @NonNegative
    public int getCoveredItems() {
        return lineCoverageInfo.getCoveredItems() + dataCoverageInfo.getCoveredItems();
    }

    public int getCoveragePercentage() {
        int totalItems = getTotalItems();
        int coveredItems = getCoveredItems();
        return CoveragePercentage.calculate(coveredItems, totalItems);
    }

    void mergeWithDataFromPreviousTestRun(@NonNull FileCoverageData previousInfo) {
        if (lineCoverageInfo == NO_LINE_INFO) {
            lineCoverageInfo = previousInfo.lineCoverageInfo;
        } else if (previousInfo.lineCoverageInfo != NO_LINE_INFO) {
            lineCoverageInfo.mergeInformation(previousInfo.lineCoverageInfo);
        }

        if (dataCoverageInfo == NO_DATA_INFO) {
            dataCoverageInfo = previousInfo.dataCoverageInfo;
        } else if (previousInfo.dataCoverageInfo != NO_DATA_INFO) {
            dataCoverageInfo.mergeInformation(previousInfo.dataCoverageInfo);
        }
    }
}
