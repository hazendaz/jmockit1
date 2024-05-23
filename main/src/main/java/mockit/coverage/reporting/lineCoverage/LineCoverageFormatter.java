/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.lineCoverage;

import java.util.List;

import javax.annotation.Nullable;

import mockit.coverage.CallPoint;
import mockit.coverage.lines.LineCoverageData;
import mockit.coverage.lines.PerFileLineCoverage;
import mockit.coverage.reporting.ListOfCallPoints;
import mockit.coverage.reporting.parsing.LineParser;

import org.checkerframework.checker.nullness.qual.NonNull;

final class LineCoverageFormatter {
    @NonNull
    private final StringBuilder formattedLine;
    @NonNull
    private final LineSegmentsFormatter segmentsFormatter;
    @Nullable
    private final ListOfCallPoints listOfCallPoints;

    LineCoverageFormatter(boolean withCallPoints) {
        formattedLine = new StringBuilder(200);
        segmentsFormatter = new LineSegmentsFormatter(withCallPoints, formattedLine);
        listOfCallPoints = withCallPoints ? new ListOfCallPoints() : null;
    }

    String format(@NonNull LineParser lineParser, @NonNull PerFileLineCoverage lineCoverageData) {
        formattedLine.setLength(0);
        formattedLine.append("<pre class='pp");

        int line = lineParser.getNumber();
        LineCoverageData lineData = lineCoverageData.getLineData(line);

        if (lineData.containsBranches()) {
            formatLineWithMultipleSegments(lineParser, lineData);
        } else {
            formatLineWithSingleSegment(lineParser, lineData);
        }

        return formattedLine.toString();
    }

    private void formatLineWithMultipleSegments(@NonNull LineParser lineParser, @NonNull LineCoverageData lineData) {
        formattedLine.append(" jmp'>");
        segmentsFormatter.formatSegments(lineParser, lineData);
    }

    private void formatLineWithSingleSegment(@NonNull LineParser lineParser, @NonNull LineCoverageData lineData) {
        formattedLine.append(lineData.isCovered() ? " cvd" : " uncvd");

        List<CallPoint> callPoints = lineData.getCallPoints();

        if (listOfCallPoints != null && callPoints != null) {
            formattedLine.append(" cp' onclick='sh(this)");
        }

        formattedLine.append("' id='l").append(lineParser.getNumber()).append("s0'>");
        String content = lineParser.getInitialElement().toString();
        formattedLine.append(content).append("</pre>");

        if (listOfCallPoints != null) {
            listOfCallPoints.insertListOfCallPoints(callPoints);
            formattedLine.append(listOfCallPoints.getContents());
        }
    }
}
