/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import mockit.coverage.data.CoverageData;
import mockit.coverage.reporting.CoverageReport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
final class OutputFileGenerator {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(CoverageReport.class);

    private static final String[] ALL_SOURCE_DIRS = {};

    @NonNull
    private final String[] outputFormats;
    @NonNull
    private final String outputDir;
    @Nullable
    private final String[] sourceDirs;

    OutputFileGenerator() {
        outputFormats = getOutputFormat();
        outputDir = Configuration.getProperty("outputDir", "");

        String commaSeparatedDirs = Configuration.getProperty("srcDirs");

        if (commaSeparatedDirs == null) {
            sourceDirs = ALL_SOURCE_DIRS;
        } else if (commaSeparatedDirs.isEmpty()) {
            sourceDirs = null;
        } else {
            sourceDirs = commaSeparatedDirs.split("\\s*,\\s*");
        }
    }

    @NonNull
    private static String[] getOutputFormat() {
        String format = Configuration.getProperty("output", "");
        return format.isEmpty() ? new String[] { "html" } : format.trim().split("\\s*,\\s*|\\s+");
    }

    boolean isOutputToBeGenerated() {
        return isHTMLWithNoCallPoints() || isWithCallPoints() || hasOutputFormat("serial")
                || hasOutputFormat("serial-append") || hasOutputFormat("xml");
    }

    private boolean isHTMLWithNoCallPoints() {
        return hasOutputFormat("html") || hasOutputFormat("html-nocp");
    }

    boolean isWithCallPoints() {
        return hasOutputFormat("html-cp");
    }

    private boolean hasOutputFormat(@NonNull String format) {
        for (String outputFormat : outputFormats) {
            if (format.equals(outputFormat)) {
                return true;
            }
        }

        return false;
    }

    void generate() {
        CoverageData coverageData = CoverageData.instance();

        if (coverageData.isEmpty()) {
            logger.info("JMockit: No classes were instrumented for coverage; please make sure that ");

            String classesRegexp = Configuration.getProperty("classes");

            if (classesRegexp == null) {
                logger.info("classes exercised by tests are in a directory included in the runtime classpath");
            } else {
                logger.info(
                        "classes selected for coverage through the regular expression '{}' are available from the runtime classpath",
                        classesRegexp);
            }

            logger.info(", and that they have been compiled with debug information.");
            return;
        }

        boolean outputDirCreated = createOutputDirIfSpecifiedButNotExists();

        try {
            generateAccretionDataFileIfRequested(coverageData);
            generateXmlDataFileIfRequested(coverageData);
            generateHTMLReportIfRequested(coverageData, outputDirCreated);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void generateAggregateReportFromInputFiles(@NonNull String[] inputPaths) {
        boolean outputDirCreated = createOutputDirIfSpecifiedButNotExists();

        try {
            CoverageData coverageData = new DataFileMerging(inputPaths).merge();
            generateHTMLReportIfRequested(coverageData, outputDirCreated);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean createOutputDirIfSpecifiedButNotExists() {
        if (outputDir.isEmpty()) {
            return false;
        }

        File outDir = Path.of(outputDir).toFile();
        return outDir.mkdirs();
    }

    private void generateAccretionDataFileIfRequested(@NonNull CoverageData newData) throws IOException {
        if (hasOutputFormat("serial")) {
            new AccretionFile(outputDir, newData).generate();
        } else if (hasOutputFormat("serial-append")) {
            AccretionFile accretionFile = new AccretionFile(outputDir, newData);
            accretionFile.mergeDataFromExistingFileIfAny();
            accretionFile.generate();
        }
    }

    private void generateXmlDataFileIfRequested(@NonNull CoverageData newData) throws IOException {
        if (hasOutputFormat("xml")) {
            new XmlFile(outputDir, newData).generate();
        }
    }

    private void generateHTMLReportIfRequested(@NonNull CoverageData coverageData, boolean outputDirCreated)
            throws IOException {
        if (isHTMLWithNoCallPoints()) {
            new CoverageReport(outputDir, outputDirCreated, sourceDirs, coverageData, false).generate();
        } else if (isWithCallPoints()) {
            new CoverageReport(outputDir, outputDirCreated, sourceDirs, coverageData, true).generate();
        }
    }
}
