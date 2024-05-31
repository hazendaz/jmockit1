/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.lineCoverage;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.PrintWriter;

import mockit.coverage.lines.PerFileLineCoverage;
import mockit.coverage.reporting.parsing.LineParser;

import org.checkerframework.checker.index.qual.NonNegative;

public final class LineCoverageOutput {
    @NonNull
    private final PrintWriter output;
    @NonNull
    private final PerFileLineCoverage lineCoverageData;
    @NonNull
    private final LineCoverageFormatter lineCoverageFormatter;

    public LineCoverageOutput(@NonNull PrintWriter output, @NonNull PerFileLineCoverage lineCoverageData,
            boolean withCallPoints) {
        this.output = output;
        this.lineCoverageData = lineCoverageData;
        lineCoverageFormatter = new LineCoverageFormatter(withCallPoints);
    }

    public boolean writeLineWithCoverageInfo(@NonNull LineParser lineParser) {
        int line = lineParser.getNumber();

        if (!lineCoverageData.hasLineData(line)) {
            return false;
        }

        int lineExecutionCount = lineCoverageData.getExecutionCount(line);

        if (lineExecutionCount < 0) {
            return false;
        }

        writeLineExecutionCount(lineExecutionCount);
        writeExecutableCode(lineParser);
        return true;
    }

    private void writeLineExecutionCount(@NonNegative int lineExecutionCount) {
        output.write("<td class='ct'>");
        output.print(lineExecutionCount);
        output.println("</td>");
    }

    private void writeExecutableCode(@NonNull LineParser lineParser) {
        String formattedLine = lineCoverageFormatter.format(lineParser, lineCoverageData);
        output.write("      <td>");
        output.write(formattedLine);
        output.println("</td>");
    }
}
