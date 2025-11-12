/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.dataItems;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.checkerframework.checker.index.qual.NonNegative;

public abstract class FieldData implements Serializable {
    private static final long serialVersionUID = 8565599590976858508L;

    @NonNegative
    int readCount;
    @NonNegative
    int writeCount;
    @Nullable
    Boolean covered;

    private void writeObject(@NonNull ObjectOutputStream out) throws IOException {
        isCovered();
        out.defaultWriteObject();
    }

    @NonNegative
    public final int getReadCount() {
        return readCount;
    }

    @NonNegative
    public final int getWriteCount() {
        return writeCount;
    }

    public final boolean isCovered() {
        if (covered == null) {
            covered = false;
            markAsCoveredIfNoUnreadValuesAreLeft();
        }

        return covered;
    }

    abstract void markAsCoveredIfNoUnreadValuesAreLeft();

    final void addCountsFromPreviousTestRun(@NonNull FieldData previousInfo) {
        readCount += previousInfo.readCount;
        writeCount += previousInfo.writeCount;
        covered = isCovered() || previousInfo.isCovered();
    }
}
