/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import mockit.coverage.CallPoint;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class ListOfCallPoints {
    @NonNull
    private static final String EOL = System.lineSeparator();
    private static final Pattern LESS_THAN_CHAR = Pattern.compile("<");

    @NonNull
    private final StringBuilder content;

    public ListOfCallPoints() {
        content = new StringBuilder(100);
    }

    public void insertListOfCallPoints(@Nullable List<CallPoint> callPoints) {
        if (content.length() == 0) {
            content.append(EOL).append("      ");
        }

        content.append("  <ol style='display:none'>");

        if (callPoints == null) {
            content.append("</ol>").append(EOL).append("      ");
            return;
        }

        content.append(EOL);

        CallPoint currentCP = callPoints.get(0);
        appendTestMethod(currentCP.getStackTraceElement());
        appendRepetitionCountIfNeeded(currentCP);

        for (int i = 1, n = callPoints.size(); i < n; i++) {
            CallPoint nextCP = callPoints.get(i);
            StackTraceElement ste = nextCP.getStackTraceElement();

            if (nextCP.isSameTestMethod(currentCP)) {
                content.append(", ").append(ste.getLineNumber());
            } else {
                content.append("</li>").append(EOL);
                appendTestMethod(ste);
            }

            appendRepetitionCountIfNeeded(nextCP);
            currentCP = nextCP;
        }

        content.append("</li>").append(EOL).append("        </ol>").append(EOL).append("      ");
    }

    private void appendTestMethod(@NonNull StackTraceElement current) {
        content.append("          <li>");
        content.append(current.getClassName()).append('#');
        content.append(LESS_THAN_CHAR.matcher(current.getMethodName()).replaceFirst("&lt;")).append(": ");
        content.append(current.getLineNumber());
    }

    private void appendRepetitionCountIfNeeded(@NonNull CallPoint callPoint) {
        int repetitionCount = callPoint.getRepetitionCount();

        if (repetitionCount > 0) {
            content.append('x').append(1 + repetitionCount);
        }
    }

    @NonNull
    public String getContents() {
        String result = content.toString();
        content.setLength(0);
        return result;
    }
}
