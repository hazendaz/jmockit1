/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.reporting.packages;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import mockit.coverage.CoveragePercentage;

import org.checkerframework.checker.index.qual.NonNegative;

abstract class ListWithFilesAndPercentages {
    @NonNull
    protected final PrintWriter output;
    @NonNull
    private final String baseIndent;
    @NonNegative
    int totalItems;
    @NonNegative
    int coveredItems;

    ListWithFilesAndPercentages(@NonNull PrintWriter output, @NonNull String baseIndent) {
        this.output = output;
        this.baseIndent = baseIndent;
    }

    final void writeMetricsForEachFile(@Nullable String packageName, @NonNull List<String> fileNames) {
        if (fileNames.isEmpty()) {
            return;
        }

        Collections.sort(fileNames);
        totalItems = 0;
        coveredItems = 0;

        for (String fileName : fileNames) {
            writeMetricsForFile(packageName, fileName);
        }
    }

    final void writeRowStart() {
        printIndent();
        output.println("<tr>");
    }

    final void writeRowClose() {
        printIndent();
        output.println("</tr>");
    }

    final void printIndent() {
        output.write(baseIndent);
    }

    protected abstract void writeMetricsForFile(@Nullable String packageName, @NonNull String fileName);

    final void printCoveragePercentage(@NonNegative int covered, @NonNegative int total, int percentage) {
        printIndent();
        output.write("  <td ");

        if (total > 0) {
            writeRowCellWithCoveragePercentage(covered, total, percentage);
        } else {
            output.write("class='nocode'>N/A");
        }

        output.println("</td>");
    }

    private void writeRowCellWithCoveragePercentage(@NonNegative int covered, @NonNegative int total,
            @NonNegative int percentage) {
        output.write("style='background-color:#");
        output.write(CoveragePercentage.percentageColor(covered, total));
        output.write("' title='Items: ");
        output.print(covered);
        output.write('/');
        output.print(total);
        output.write("'>");
        writePercentageValue(covered, total, percentage);
        output.print("%");
    }

    private void writePercentageValue(@NonNegative int covered, @NonNegative int total, @NonNegative int percentage) {
        if (percentage < 100) {
            output.print(percentage);
        } else if (covered == total) {
            output.print("100");
        } else {
            output.print(">99");
        }
    }
}
