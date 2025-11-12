/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.reporting.packages;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mockit.coverage.data.FileCoverageData;

import org.checkerframework.checker.index.qual.NonNegative;

final class PackageCoverageReport extends ListWithFilesAndPercentages {
    @NonNull
    private final Map<String, FileCoverageData> filesToFileData;
    @Nullable
    private final Collection<String> sourceFilesNotFound;
    @NonNull
    private final char[] fileNameWithSpaces;

    PackageCoverageReport(@NonNull PrintWriter output, @Nullable Collection<String> sourceFilesNotFound,
            @NonNull Map<String, FileCoverageData> filesToFileData,
            @NonNull Collection<List<String>> allSourceFileNames) {
        super(output, "          ");
        this.sourceFilesNotFound = sourceFilesNotFound;
        this.filesToFileData = filesToFileData;
        fileNameWithSpaces = new char[maximumSourceFileNameLength(allSourceFileNames)];
    }

    @NonNegative
    private static int maximumSourceFileNameLength(@NonNull Collection<List<String>> allSourceFileNames) {
        int maxLength = 0;

        for (List<String> files : allSourceFileNames) {
            for (String fileName : files) {
                int n = fileName.length();

                if (n > maxLength) {
                    maxLength = n;
                }
            }
        }

        return maxLength;
    }

    @Override
    protected void writeMetricsForFile(@Nullable String packageName, @NonNull String fileName) {
        String filePath = packageName == null || packageName.isEmpty() ? fileName : packageName + '/' + fileName;
        FileCoverageData fileData = filesToFileData.get(filePath);

        writeRowStart();
        printIndent();
        output.write("  <td class='");
        output.write(fileData.kindOfTopLevelType != null ? fileData.kindOfTopLevelType : "cls");
        output.write("'>");

        int fileNameLength = buildFileNameWithTrailingSpaces(fileName);
        writeTableCellWithFileName(filePath, fileNameLength);
        writeCodeCoverageMetricForFile(fileData);
        writeRowClose();
    }

    @NonNegative
    private int buildFileNameWithTrailingSpaces(@NonNull String fileName) {
        int n = fileName.length();

        fileName.getChars(0, n, fileNameWithSpaces, 0);
        Arrays.fill(fileNameWithSpaces, n, fileNameWithSpaces.length, ' ');

        return n;
    }

    private void writeTableCellWithFileName(@NonNull String filePath, @NonNegative int fileNameLen) {
        if (sourceFilesNotFound == null || sourceFilesNotFound.contains(filePath)) {
            output.write(fileNameWithSpaces);
        } else {
            output.write("<a href='");
            int p = filePath.lastIndexOf('.');
            output.write(filePath.substring(0, p));
            output.write(".html'>");
            output.write(fileNameWithSpaces, 0, fileNameLen);
            output.write("</a>");
            output.write(fileNameWithSpaces, fileNameLen, fileNameWithSpaces.length - fileNameLen);
        }

        output.println("</td>");
    }

    private void writeCodeCoverageMetricForFile(@NonNull FileCoverageData coverageInfo) {
        int percentage = coverageInfo.getCoveragePercentage();
        int covered = coverageInfo.getCoveredItems();
        int total = coverageInfo.getTotalItems();

        coveredItems += covered;
        totalItems += total;

        printCoveragePercentage(covered, total, percentage);
    }
}
