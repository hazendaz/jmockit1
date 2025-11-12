/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.dataItems;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

import mockit.internal.state.TestRun;

public final class StaticFieldData extends FieldData {
    private static final long serialVersionUID = -6596622341651601060L;

    @NonNull
    private final transient Map<Integer, Boolean> testIdsToAssignments = new HashMap<>();

    void registerAssignment() {
        int testId = TestRun.getTestId();
        testIdsToAssignments.put(testId, Boolean.TRUE);
        writeCount++;
    }

    void registerRead() {
        int testId = TestRun.getTestId();
        testIdsToAssignments.put(testId, null);
        readCount++;
    }

    @Override
    void markAsCoveredIfNoUnreadValuesAreLeft() {
        for (Boolean withUnreadValue : testIdsToAssignments.values()) {
            if (withUnreadValue == null) {
                covered = true;
                break;
            }
        }
    }
}
