/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.dataItems;

import java.util.HashMap;
import java.util.Map;

import mockit.internal.state.TestRun;

import edu.umd.cs.findbugs.annotations.NonNull;

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
