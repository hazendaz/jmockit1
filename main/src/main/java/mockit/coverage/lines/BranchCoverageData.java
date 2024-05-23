/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.lines;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import mockit.asm.controlFlow.Label;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Coverage data gathered for a branch inside a line of source code.
 */
public final class BranchCoverageData extends LineSegmentData {
    private static final long serialVersionUID = 1003335601845442606L;
    static final BranchCoverageData INVALID = new BranchCoverageData(new Label());

    @NonNull
    private transient Label label;

    BranchCoverageData(@NonNull Label label) {
        this.label = label;
    }

    @Override
    public boolean isEmpty() {
        return empty || label.line == 0 && label.jumpTargetLine == 0;
    }

    @NonNegative
    int getLine() {
        return label.jumpTargetLine == 0 ? label.line : label.jumpTargetLine;
    }

    private void readObject(@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        label = new Label();
        label.line = in.readInt();
        in.defaultReadObject();
    }

    private void writeObject(@NonNull ObjectOutputStream out) throws IOException {
        int line = getLine();
        out.writeInt(line);
        out.defaultWriteObject();
    }
}
