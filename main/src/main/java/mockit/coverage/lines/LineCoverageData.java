/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.lines;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mockit.asm.controlFlow.Label;
import mockit.coverage.CallPoint;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Coverage data gathered for a single executable line of code in a source file.
 */
public final class LineCoverageData extends LineSegmentData {
    private static final long serialVersionUID = -6233980722802474992L;

    // Static data:
    @NonNull
    private List<BranchCoverageData> branches;
    @NonNegative
    private transient int segments;

    LineCoverageData() {
        branches = emptyList();
    }

    @NonNegative
    public int addBranchingPoint(@NonNull Label jumpSource, @NonNull Label jumpTarget) {
        int initialIndex = branches.size();

        if (initialIndex == 0) {
            branches = new ArrayList<>(4);
        }

        branches.add(new BranchCoverageData(jumpSource));
        branches.add(new BranchCoverageData(jumpTarget));
        return initialIndex;
    }

    void markLastSegmentAsEmpty() {
        BranchCoverageData lastBranch = branches.get(branches.size() - 1);
        lastBranch.markAsEmpty();
    }

    private boolean noBranchesYet() {
        return branches == Collections.<BranchCoverageData>emptyList();
    }

    @NonNull
    public BranchCoverageData getBranchData(@NonNegative int index) {
        return branches.get(index);
    }

    boolean acceptsAdditionalCallPoints(@NonNegative int branchIndex) {
        BranchCoverageData data = branches.get(branchIndex);
        return data.acceptsAdditionalCallPoints();
    }

    @NonNegative
    int registerExecution(@NonNegative int branchIndex, @Nullable CallPoint callPoint) {
        BranchCoverageData data = branches.get(branchIndex);
        return data.registerExecution(callPoint);
    }

    public boolean containsBranches() {
        return !noBranchesYet();
    }

    @NonNull
    public List<BranchCoverageData> getBranches() {
        return branches;
    }

    boolean isValidBranch(@NonNegative int branchIndex) {
        return branches.get(branchIndex) != BranchCoverageData.INVALID;
    }

    @NonNegative
    public int getNumberOfSegments() {
        int previouslyCounted = segments;

        if (previouslyCounted > 0) {
            return previouslyCounted;
        }

        int n = branches.size();
        int count = 1;

        for (int targetBranchIndex = 1; targetBranchIndex < n; targetBranchIndex += 2) {
            BranchCoverageData targetBranch = branches.get(targetBranchIndex);
            int targetLine = targetBranch.getLine();

            if (targetLine > 0) {
                BranchCoverageData sourceBranch = branches.get(targetBranchIndex - 1);
                int sourceLine = sourceBranch.getLine();

                if (targetLine == sourceLine) {
                    count++;
                }

                if (!targetBranch.isEmpty()) {
                    count++;
                }
            }
        }

        segments = count;
        return count;
    }

    @NonNegative
    public int getNumberOfCoveredSegments() {
        int segmentsCovered = isCovered() ? 1 : 0;
        int n = branches.size();

        if (n == 0) {
            return segmentsCovered;
        }

        for (int sourceBranchIndex = 0; sourceBranchIndex < n; sourceBranchIndex += 2) {
            BranchCoverageData sourceBranch = branches.get(sourceBranchIndex);
            BranchCoverageData targetBranch = branches.get(sourceBranchIndex + 1);

            if (sourceBranch.isCovered() && !targetBranch.isEmpty()) {
                segmentsCovered++;
            }

            if (targetBranch.isCovered()) {
                int targetLine = targetBranch.getLine();

                if (targetLine == sourceBranch.getLine()) {
                    segmentsCovered++;
                }
            }
        }

        return segmentsCovered;
    }

    @NonNegative
    public int getNumberOfBranchingSourcesAndTargets() {
        int n = branches.size();

        if (n == 0) {
            return 0;
        }

        int count = 0;

        for (int sourceBranchIndex = 0; sourceBranchIndex < n; sourceBranchIndex += 2) {
            BranchCoverageData sourceBranch = branches.get(sourceBranchIndex);

            if (!sourceBranch.isEmpty()) {
                count++;
            }

            count++;
        }

        return count;
    }

    @NonNegative
    public int getNumberOfCoveredBranchingSourcesAndTargets() {
        int n = branches.size();

        if (n == 0) {
            return 0;
        }

        int sourcesAndTargetsCovered = 0;

        for (int sourceBranchIndex = 0; sourceBranchIndex < n; sourceBranchIndex += 2) {
            BranchCoverageData sourceBranch = branches.get(sourceBranchIndex);
            BranchCoverageData targetBranch = branches.get(sourceBranchIndex + 1);

            if (sourceBranch.isCovered()) {
                sourcesAndTargetsCovered++;
            }

            if (targetBranch.isCovered()) {
                int targetLine = targetBranch.getLine();

                if (targetLine == sourceBranch.getLine()) {
                    sourcesAndTargetsCovered++;
                }
            }
        }

        return sourcesAndTargetsCovered;
    }

    void addCountsFromPreviousTestRun(@NonNull LineCoverageData previousData) {
        addExecutionCountAndCallPointsFromPreviousTestRun(previousData);

        if (containsBranches()) {
            for (int i = 0, n = branches.size(); i < n; i++) {
                BranchCoverageData segmentData = branches.get(i);
                BranchCoverageData previousSegmentData = previousData.branches.get(i);

                segmentData.addExecutionCountAndCallPointsFromPreviousTestRun(previousSegmentData);
            }
        }
    }
}
