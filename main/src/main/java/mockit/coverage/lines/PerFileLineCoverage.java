/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.lines;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import mockit.coverage.CallPoint;
import mockit.coverage.CoveragePercentage;
import mockit.coverage.data.PerFileCoverage;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class PerFileLineCoverage implements PerFileCoverage {
    private static final long serialVersionUID = 6318915843739466316L;
    private static final int[] NO_EXECUTIONS_YET = {};

    @NonNull
    private final Map<Integer, LineCoverageData> lineToLineData = new HashMap<>(128);

    @NonNull
    private int[] executionCounts = NO_EXECUTIONS_YET;

    @NonNull
    private transient LineCoverageData sharedLineData;

    // Computed on demand:
    @NonNegative
    private int lastLine;
    private transient int totalSegments;
    private transient int coveredSegments;

    public PerFileLineCoverage() {
        sharedLineData = new LineCoverageData();
        initializeCache();
    }

    private void initializeCache() {
        totalSegments = coveredSegments = -1;
    }

    private void readObject(@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        sharedLineData = new LineCoverageData();
        initializeCache();
        in.defaultReadObject();

        if (executionCounts.length == 0) {
            executionCounts = NO_EXECUTIONS_YET;
        }
    }

    public void addLine(@NonNegative int line) {
        if (!lineToLineData.containsKey(line)) {
            lineToLineData.put(line, null);
        }

        if (line > lastLine) {
            // Happens for source files with multiple types, where one is only loaded
            // after another has already executed some code.
            int[] initialExecutionCounts = executionCounts;

            if (initialExecutionCounts != NO_EXECUTIONS_YET && line >= initialExecutionCounts.length) {
                int[] newCounts = new int[line + 30];
                System.arraycopy(initialExecutionCounts, 0, newCounts, 0, initialExecutionCounts.length);
                executionCounts = newCounts;
            }

            lastLine = line;
        }
    }

    @NonNull
    public LineCoverageData getOrCreateLineData(@NonNegative int line) {
        LineCoverageData lineData = lineToLineData.get(line);

        if (lineData == null) {
            lineData = new LineCoverageData();
            lineToLineData.put(line, lineData);
        }

        return lineData;
    }

    @NonNull
    public BranchCoverageData getBranchData(@NonNegative int line, @NonNegative int index) {
        LineCoverageData lineData = lineToLineData.get(line);
        return lineData.getBranchData(index);
    }

    public void markLastLineSegmentAsEmpty(@NonNegative int line) {
        LineCoverageData lineData = lineToLineData.get(line);
        lineData.markLastSegmentAsEmpty();
    }

    public boolean acceptsAdditionalCallPoints(@NonNegative int line) {
        LineCoverageData lineData = getOrCreateLineData(line);
        return lineData.acceptsAdditionalCallPoints();
    }

    @NonNegative
    public int registerExecution(@NonNegative int line, @Nullable CallPoint callPoint) {
        if (executionCounts == NO_EXECUTIONS_YET) {
            executionCounts = new int[lastLine + 1];
        }

        int previousExecutionCount = executionCounts[line]++;

        if (callPoint != null) {
            LineCoverageData lineData = lineToLineData.get(line);
            lineData.registerExecution(callPoint);
        }

        return previousExecutionCount;
    }

    public boolean hasValidBranch(@NonNegative int line, @NonNegative int branchIndex) {
        LineCoverageData lineData = lineToLineData.get(line);
        return lineData.isValidBranch(branchIndex);
    }

    public boolean acceptsAdditionalCallPoints(@NonNegative int line, @NonNegative int branchIndex) {
        LineCoverageData lineData = lineToLineData.get(line);
        return lineData.acceptsAdditionalCallPoints(branchIndex);
    }

    @NonNegative
    public int registerExecution(@NonNegative int line, @NonNegative int branchIndex, @Nullable CallPoint callPoint) {
        LineCoverageData lineData = lineToLineData.get(line);
        return lineData.registerExecution(branchIndex, callPoint);
    }

    @NonNegative
    public int getLineCount() {
        return lastLine;
    }

    @NonNegative
    public int getExecutableLineCount() {
        return lineToLineData.size();
    }

    public boolean hasLineData(@NonNegative int line) {
        return executionCounts != NO_EXECUTIONS_YET && lineToLineData.containsKey(line);
    }

    @NonNull
    public LineCoverageData getLineData(@NonNegative int line) {
        LineCoverageData data = lineToLineData.get(line);

        if (data == null) {
            data = sharedLineData;
        }

        data.setExecutionCount(executionCounts[line]);
        return data;
    }

    public void markLineAsReachable(@NonNegative int line) {
        LineCoverageData data = lineToLineData.get(line);

        if (data != null) {
            data.markAsReachable();
        }
    }

    public int getExecutionCount(@NonNegative int line) {
        return line < executionCounts.length ? executionCounts[line] : -1;
    }

    @Override
    @NonNegative
    public int getTotalItems() {
        computeValuesIfNeeded();
        return totalSegments;
    }

    @Override
    @NonNegative
    public int getCoveredItems() {
        computeValuesIfNeeded();
        return coveredSegments;
    }

    @Override
    public int getCoveragePercentage() {
        computeValuesIfNeeded();
        return CoveragePercentage.calculate(coveredSegments, totalSegments);
    }

    private void computeValuesIfNeeded() {
        if (totalSegments >= 0) {
            return;
        }
        totalSegments = coveredSegments = 0;

        for (int line = 1, n = lastLine; line <= n; line++) {
            if (lineToLineData.containsKey(line)) {
                LineCoverageData lineData = lineToLineData.get(line);
                int executionCount = executionCounts == NO_EXECUTIONS_YET ? 0 : executionCounts[line];

                if (lineData == null) {
                    totalSegments++;

                    if (executionCount > 0) {
                        coveredSegments++;
                    }
                } else {
                    lineData.setExecutionCount(executionCount);
                    totalSegments += lineData.getNumberOfSegments();
                    coveredSegments += lineData.getNumberOfCoveredSegments();
                }
            }
        }
    }

    @NonNegative
    public int getNumberOfSegments(@NonNegative int line) {
        if (!lineToLineData.containsKey(line)) {
            return 0;
        }

        LineCoverageData lineData = lineToLineData.get(line);
        return lineData == null ? 1 : lineData.getNumberOfSegments();
    }

    @NonNegative
    public int getNumberOfBranchingSourcesAndTargets(@NonNegative int line) {
        LineCoverageData lineData = lineToLineData.get(line);

        if (lineData == null) {
            return 0;
        }

        return lineData.getNumberOfBranchingSourcesAndTargets();
    }

    public void mergeInformation(@NonNull PerFileLineCoverage previousCoverage) {
        Map<Integer, LineCoverageData> previousInfo = previousCoverage.lineToLineData;
        boolean previousRunHadLinesExecuted = previousCoverage.executionCounts.length > 0;

        for (Entry<Integer, LineCoverageData> lineAndInfo : lineToLineData.entrySet()) {
            Integer line = lineAndInfo.getKey();
            LineCoverageData previousLineInfo = previousInfo.get(line);

            if (previousLineInfo != null) {
                LineCoverageData lineInfo = lineAndInfo.getValue();

                if (lineInfo == null) {
                    lineInfo = new LineCoverageData();
                    lineAndInfo.setValue(lineInfo);
                }

                lineInfo.addCountsFromPreviousTestRun(previousLineInfo);

                if (previousRunHadLinesExecuted) {
                    createExecutionCountsArrayIfNeeded(previousCoverage);
                    executionCounts[line] += previousCoverage.executionCounts[line];
                }
            }
        }

        for (Entry<Integer, LineCoverageData> lineAndInfo : previousInfo.entrySet()) {
            Integer line = lineAndInfo.getKey();

            if (!lineToLineData.containsKey(line)) {
                LineCoverageData previousLineInfo = lineAndInfo.getValue();
                lineToLineData.put(line, previousLineInfo);

                if (previousRunHadLinesExecuted) {
                    createExecutionCountsArrayIfNeeded(previousCoverage);
                    executionCounts[line] = previousCoverage.executionCounts[line];
                }
            }
        }
    }

    private void createExecutionCountsArrayIfNeeded(@NonNull PerFileLineCoverage previousCoverage) {
        if (executionCounts == NO_EXECUTIONS_YET) {
            executionCounts = new int[previousCoverage.executionCounts.length];
        }
    }
}
