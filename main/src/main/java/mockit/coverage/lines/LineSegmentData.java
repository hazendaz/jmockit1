/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.lines;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mockit.coverage.CallPoint;
import mockit.coverage.Configuration;

import org.checkerframework.checker.index.qual.NonNegative;

public class LineSegmentData implements Serializable {
    private static final long serialVersionUID = -6233980722802474992L;
    private static final int MAX_CALL_POINTS = Integer.parseInt(Configuration.getProperty("maxCallPoints", "10"));

    // Constant data:
    private boolean unreachable;
    protected boolean empty;

    // Runtime data:
    @NonNegative
    int executionCount;
    @Nullable
    private List<CallPoint> callPoints;

    public final void markAsUnreachable() {
        unreachable = true;
    }

    final void markAsReachable() {
        unreachable = false;
    }

    public boolean isEmpty() {
        return empty;
    }

    final void markAsEmpty() {
        empty = true;
    }

    final boolean acceptsAdditionalCallPoints() {
        return callPoints == null || callPoints.size() < MAX_CALL_POINTS;
    }

    @NonNegative
    final int registerExecution(@Nullable CallPoint callPoint) {
        int previousExecutionCount = executionCount++;

        if (callPoint != null) {
            addCallPoint(callPoint);
        }

        return previousExecutionCount;
    }

    private void addCallPoint(@NonNull CallPoint callPoint) {
        if (callPoints == null) {
            callPoints = new ArrayList<>(MAX_CALL_POINTS);
        }

        for (int i = callPoints.size() - 1; i >= 0; i--) {
            CallPoint previousCallPoint = callPoints.get(i);

            if (callPoint.isSameLineInTestCode(previousCallPoint)) {
                previousCallPoint.incrementRepetitionCount();
                return;
            }
        }

        callPoints.add(callPoint);
    }

    public final boolean containsCallPoints() {
        return callPoints != null;
    }

    @Nullable
    public final List<CallPoint> getCallPoints() {
        return callPoints;
    }

    @NonNegative
    public final int getExecutionCount() {
        return executionCount;
    }

    final void setExecutionCount(@NonNegative int executionCount) {
        this.executionCount = executionCount;
    }

    public final boolean isCovered() {
        return unreachable || !empty && executionCount > 0;
    }

    final void addExecutionCountAndCallPointsFromPreviousTestRun(@NonNull LineSegmentData previousData) {
        executionCount += previousData.executionCount;

        if (previousData.callPoints != null) {
            if (callPoints != null) {
                callPoints.addAll(0, previousData.callPoints);
            } else {
                callPoints = previousData.callPoints;
            }
        }
    }
}
