/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;

import mockit.coverage.data.CoverageData;
import mockit.coverage.data.FileCoverageData;
import mockit.coverage.lines.LineCoverageData;
import mockit.coverage.lines.PerFileLineCoverage;

import org.checkerframework.checker.index.qual.NonNegative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a XML file containing the coverage data gathered by the test run. The XML schema used is the one
 * <a href="http://docs.sonarqube.org/display/SONAR/Generic+Test+Data">defined</a> by the SonarQube project:
 *
 * <pre>{@code
 * &lt;coverage version="1">
 *    &lt;file path="com/example/MyClass.java">
 *       &lt;lineToCover lineNumber="5" covered="false"/>
 *       &lt;lineToCover lineNumber="8" covered="true" branchesToCover="2" coveredBranches="1"/>
 *    &lt;/file>
 * &lt;/coverage>
 * }</pre>
 */
final class XmlFile {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(XmlFile.class);

    @NonNull
    private final String srcDir;
    @NonNull
    private final File outputFile;
    @NonNull
    private final CoverageData coverageData;

    XmlFile(@NonNull String outputDir, @NonNull CoverageData coverageData) {
        // noinspection DynamicRegexReplaceableByCompiledPattern
        String firstSrcDir = Configuration.getProperty("srcDirs", "").split("\\s*,\\s*")[0];
        srcDir = firstSrcDir.isEmpty() ? "" : firstSrcDir + '/';

        String parentDir = Configuration.getOrChooseOutputDirectory(outputDir);
        outputFile = Path.of(parentDir, "coverage.xml").toFile();
        this.coverageData = coverageData;
    }

    void generate() throws IOException {
        try (Writer out = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.write("<coverage version=\"1\">\n");

            for (Entry<String, FileCoverageData> fileAndData : coverageData.getFileToFileData().entrySet()) {
                String sourceFileName = fileAndData.getKey();
                writeOpeningXmlElementForSourceFile(out, sourceFileName);

                PerFileLineCoverage lineInfo = fileAndData.getValue().lineCoverageInfo;
                writeXmlElementsForExecutableLines(out, lineInfo);

                out.write("\t</file>\n");
            }

            out.write("</coverage>\n");
        }

        logger.info("JMockit: Coverage data written to {}", outputFile.getCanonicalPath());
    }

    private void writeOpeningXmlElementForSourceFile(@NonNull Writer out, @NonNull String sourceFileName)
            throws IOException {
        out.write("\t<file path=\"");
        out.write(srcDir);
        out.write(sourceFileName);
        out.write("\">\n");
    }

    private static void writeXmlElementsForExecutableLines(@NonNull Writer out, @NonNull PerFileLineCoverage lineInfo)
            throws IOException {
        int lineCount = lineInfo.getLineCount();

        for (int lineNum = 1; lineNum <= lineCount; lineNum++) {
            if (lineInfo.hasLineData(lineNum)) {
                LineCoverageData lineData = lineInfo.getLineData(lineNum);

                out.write("\t\t<lineToCover lineNumber=\"");
                writeNumber(out, lineNum);
                out.write("\" covered=\"");
                out.write(Boolean.toString(lineData.isCovered()));

                if (lineData.containsBranches()) {
                    out.write("\" branchesToCover=\"");
                    writeNumber(out, lineData.getNumberOfBranchingSourcesAndTargets());
                    out.write("\" coveredBranches=\"");
                    writeNumber(out, lineData.getNumberOfCoveredBranchingSourcesAndTargets());
                }

                out.write("\"/>\n");
            }
        }
    }

    private static void writeNumber(@NonNull Writer out, @NonNegative int value) throws IOException {
        out.write(Integer.toString(value));
    }
}
