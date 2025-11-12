/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.locks.ReentrantLock;

import mockit.coverage.data.CoverageData;
import mockit.coverage.data.FileCoverageData;
import mockit.coverage.lines.PerFileLineCoverage;
import mockit.coverage.testRedundancy.TestCoverage;

import org.checkerframework.checker.index.qual.NonNegative;

@SuppressWarnings("unused")
public final class TestRun {
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static boolean terminated;

    private TestRun() {
    }

    public static void lineExecuted(@NonNegative int fileIndex, @NonNegative int line) {
        if (terminated) {
            return;
        }

        LOCK.lock();
        try {
            CoverageData coverageData = CoverageData.instance();
            PerFileLineCoverage fileData = coverageData.getFileData(fileIndex).lineCoverageInfo;
            CallPoint callPoint = null;

            if (coverageData.isWithCallPoints() && fileData.acceptsAdditionalCallPoints(line)) {
                callPoint = CallPoint.create(new Throwable());
            }

            int previousExecutionCount = fileData.registerExecution(line, callPoint);
            recordNewLineOrSegmentAsCoveredIfApplicable(previousExecutionCount);
        } finally {
            LOCK.unlock();
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

        LOCK.lock();
        try {
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
        } finally {
            LOCK.unlock();
        }
    }

    public static void fieldAssigned(@NonNull String file, @NonNull String classAndFieldNames) {
        if (terminated) {
            return;
        }

        LOCK.lock();
        try {
            CoverageData coverageData = CoverageData.instance();
            FileCoverageData fileData = coverageData.getFileData(file);
            fileData.dataCoverageInfo.registerAssignmentToStaticField(classAndFieldNames);
        } finally {
            LOCK.unlock();
        }
    }

    public static void fieldRead(@NonNull String file, @NonNull String classAndFieldNames) {
        if (terminated) {
            return;
        }

        LOCK.lock();
        try {
            CoverageData coverageData = CoverageData.instance();
            FileCoverageData fileData = coverageData.getFileData(file);
            fileData.dataCoverageInfo.registerReadOfStaticField(classAndFieldNames);
        } finally {
            LOCK.unlock();
        }
    }

    public static void fieldAssigned(@NonNull Object instance, @NonNull String file,
            @NonNull String classAndFieldNames) {
        if (terminated) {
            return;
        }

        LOCK.lock();
        try {
            CoverageData coverageData = CoverageData.instance();
            FileCoverageData fileData = coverageData.getFileData(file);
            fileData.dataCoverageInfo.registerAssignmentToInstanceField(instance, classAndFieldNames);
        } finally {
            LOCK.unlock();
        }
    }

    public static void fieldRead(@NonNull Object instance, @NonNull String file, @NonNull String classAndFieldNames) {
        if (terminated) {
            return;
        }

        LOCK.lock();
        try {
            CoverageData coverageData = CoverageData.instance();
            FileCoverageData fileData = coverageData.getFileData(file);
            fileData.dataCoverageInfo.registerReadOfInstanceField(instance, classAndFieldNames);
        } finally {
            LOCK.unlock();
        }
    }

    static void terminate() {
        terminated = true;
    }

    public static boolean isTerminated() {
        return terminated;
    }
}
