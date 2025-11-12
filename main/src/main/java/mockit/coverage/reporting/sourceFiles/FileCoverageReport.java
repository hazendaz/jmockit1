/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.reporting.sourceFiles;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;

import mockit.coverage.data.FileCoverageData;
import mockit.coverage.dataItems.PerFileDataCoverage;
import mockit.coverage.reporting.OutputFile;
import mockit.coverage.reporting.dataCoverage.DataCoverageOutput;
import mockit.coverage.reporting.lineCoverage.LineCoverageOutput;
import mockit.coverage.reporting.parsing.FileParser;
import mockit.coverage.reporting.parsing.LineElement;
import mockit.coverage.reporting.parsing.LineParser;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Generates an HTML page containing line-by-line coverage information for a single source file.
 */
public final class FileCoverageReport {
    @NonNull
    private final InputFile inputFile;
    @NonNull
    private final OutputFile output;
    @NonNull
    private final FileParser fileParser;
    @NonNull
    private final NeutralOutput neutralOutput;
    @NonNull
    private final LineCoverageOutput lineCoverage;
    @Nullable
    private final DataCoverageOutput dataCoverage;

    public FileCoverageReport(@NonNull String outputDir, @NonNull InputFile inputFile,
            @NonNull FileCoverageData fileData, boolean withCallPoints) throws IOException {
        this.inputFile = inputFile;
        output = new OutputFile(outputDir, inputFile.filePath);
        fileParser = new FileParser();
        neutralOutput = new NeutralOutput(output);
        lineCoverage = new LineCoverageOutput(output, fileData.getLineCoverageData(), withCallPoints);
        dataCoverage = createDataCoverageOutput(fileData);
    }

    @Nullable
    private static DataCoverageOutput createDataCoverageOutput(@NonNull FileCoverageData fileData) {
        PerFileDataCoverage dataCoverageInfo = fileData.dataCoverageInfo;
        return dataCoverageInfo.hasFields() ? new DataCoverageOutput(dataCoverageInfo) : null;
    }

    public void generate() throws IOException {
        try {
            writeHeader();
            writeFormattedSourceLines();
            writeFooter();
        } finally {
            inputFile.close();
            output.close();
        }
    }

    private void writeHeader() {
        output.writeCommonHeader(inputFile.getSourceFileName());
        output.println("  <table>");
        output.println("    <caption>" + inputFile.getSourceFilePath() + "</caption>");
    }

    private void writeFormattedSourceLines() throws IOException {
        LineParser lineParser = fileParser.lineParser;
        String line;

        while ((line = inputFile.nextLine()) != null) {
            boolean lineWithCodeElements = fileParser.parseCurrentLine(line);

            if (lineWithCodeElements && dataCoverage != null) {
                dataCoverage.writeCoverageInfoIfLineStartsANewFieldDeclaration(fileParser);
            }

            if (!neutralOutput.writeLineWithoutCoverageInfo(lineParser)) {
                writeOpeningOfNewLine(lineParser.getNumber());

                if (!lineCoverage.writeLineWithCoverageInfo(lineParser)) {
                    writeLineWithoutCoverageInfo(lineParser.getInitialElement());
                }

                output.println("    </tr>");
            }
        }
    }

    private void writeOpeningOfNewLine(@NonNegative int lineNumber) {
        output.println("    <tr>");
        output.write("      <td>");
        output.print(lineNumber);
        output.write("</td>");
    }

    private void writeLineWithoutCoverageInfo(@NonNull LineElement initialElement) {
        output.println("<td></td>");
        output.write("      <td><pre class='");
        output.write(initialElement.isComment() ? "cm'>" : "pp'>");
        output.write(initialElement.toString());
        output.println("</pre></td>");
    }

    private void writeFooter() {
        output.println("  </table>");
        output.writeCommonFooter();
    }
}
