/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import edu.umd.cs.findbugs.annotations.NonNull;

import mockit.coverage.data.CoverageData;
import mockit.coverage.data.FileCoverageData;
import mockit.coverage.lines.PerFileLineCoverage;
import mockit.coverage.testRedundancy.TestCoverage;

import org.checkerframework.checker.index.qual.NonNegative;

@SuppressWarnings("unused")
public final class TestRun {
    private static final Object LOCK = new Object();
    private static boolean terminated;

    private TestRun() {
    }

    public static void lineExecuted(@NonNegative int fileIndex, @NonNegative int line) {
        if (terminated) {
            return;
        }

        synchronized (LOCK) {
            CoverageData coverageData = CoverageData.instance();
            PerFileLineCoverage fileData = coverageData.getFileData(fileIndex).lineCoverageInfo;
            CallPoint callPoint = null;

            if (coverageData.isWithCallPoints() && fileData.acceptsAdditionalCallPoints(line)) {
                callPoint = CallPoint.create(new Throwable());
            }

            int previousExecutionCount = fileData.registerExecution(line, callPoint);
            recordNewLineOrSegmentAsCoveredIfApplicable(previousExecutionCount);
        }
    }

    private static void recordNewLineOrSegmentAsCoveredIfApplicable(@NonNegative int previousExecutionCount) {
        TestCoverage testCoverage = TestCoverage.INSTANCE;

        if (testCoverage != null) {
            testCoverage.recordNewItemCoveredByTestIfApplicable(previousExecutionCount);
        }
    }

    public static void branchExecuted(@NonNegative int fileIndex, @NonNegative int line, @NonNegative int branchIndex) {
        if (terminated) {
            return;
        }

        synchronized (LOCK) {
            CoverageData coverageData = CoverageData.instance();
            PerFileLineCoverage fileData = coverageData.getFileData(fileIndex).lineCoverageInfo;

            if (fileData.hasValidBranch(line, branchIndex)) {
                CallPoint callPoint = null;

                if (coverageData.isWithCallPoints() && fileData.acceptsAdditionalCallPoints(line, branchIndex)) {
                    callPoint = CallPoint.create(new Throwable());
                }

                int previousExecutionCount = fileData.registerExecution(line, branchIndex, callPoint);
                recordNewLineOrSegmentAsCoveredIfApplicable(previousExecutionCount);
            }
        }
    }

    public static void fieldAssigned(@NonNull String file, @NonNull String classAndFieldNames) {
        if (terminated) {
            return;
        }

        synchronized (LOCK) {
            CoverageData coverageData = CoverageData.instance();
            FileCoverageData fileData = coverageData.getFileData(file);
            fileData.dataCoverageInfo.registerAssignmentToStaticField(classAndFieldNames);
        }
    }

    public static void fieldRead(@NonNull String file, @NonNull String classAndFieldNames) {
        if (terminated) {
            return;
        }

        synchronized (LOCK) {
            CoverageData coverageData = CoverageData.instance();
            FileCoverageData fileData = coverageData.getFileData(file);
            fileData.dataCoverageInfo.registerReadOfStaticField(classAndFieldNames);
        }
    }

    public static void fieldAssigned(@NonNull Object instance, @NonNull String file,
            @NonNull String classAndFieldNames) {
        if (terminated) {
            return;
        }

        synchronized (LOCK) {
            CoverageData coverageData = CoverageData.instance();
            FileCoverageData fileData = coverageData.getFileData(file);
            fileData.dataCoverageInfo.registerAssignmentToInstanceField(instance, classAndFieldNames);
        }
    }

    public static void fieldRead(@NonNull Object instance, @NonNull String file, @NonNull String classAndFieldNames) {
        if (terminated) {
            return;
        }

        synchronized (LOCK) {
            CoverageData coverageData = CoverageData.instance();
            FileCoverageData fileData = coverageData.getFileData(file);
            fileData.dataCoverageInfo.registerReadOfInstanceField(instance, classAndFieldNames);
        }
    }

    static void terminate() {
        terminated = true;
    }

    public static boolean isTerminated() {
        return terminated;
    }
}
