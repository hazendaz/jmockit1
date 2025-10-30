/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mockit.coverage.Configuration;
import mockit.coverage.data.CoverageData;
import mockit.coverage.data.FileCoverageData;
import mockit.coverage.reporting.packages.IndexPage;
import mockit.coverage.reporting.sourceFiles.FileCoverageReport;
import mockit.coverage.reporting.sourceFiles.InputFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CoverageReport {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(CoverageReport.class);

    @NonNull
    private final String outputDir;
    private boolean outputDirCreated;
    @Nullable
    private final List<File> sourceDirs;
    @NonNull
    private final Map<String, FileCoverageData> fileToFileData;
    @NonNull
    private final Map<String, List<String>> packageToFiles;
    private final boolean withCallPoints;
    @Nullable
    private final Collection<String> sourceFilesNotFound;

    public CoverageReport(@NonNull String outputDir, boolean outputDirCreated, @Nullable String[] srcDirs,
            @NonNull CoverageData coverageData, boolean withCallPoints) {
        this.outputDir = Configuration.getOrChooseOutputDirectory(outputDir, "coverage-report");
        this.outputDirCreated = outputDirCreated;
        sourceDirs = srcDirs == null ? null : new SourceFiles().buildListOfSourceDirectories(srcDirs);
        fileToFileData = coverageData.getFileToFileData();
        packageToFiles = new HashMap<>();
        this.withCallPoints = withCallPoints;
        sourceFilesNotFound = srcDirs == null ? null : new ArrayList<>();
    }

    public void generate() throws IOException {
        createReportOutputDirIfNotExists();

        File outputFile = createOutputFileForIndexPage();

        if (outputFile == null) {
            return;
        }

        boolean withSourceFilePages = sourceDirs != null;

        if (withSourceFilePages && sourceDirs.size() > 1) {
            logger.info("JMockit: Coverage source dirs: {}", sourceDirs);
        }

        generateFileCoverageReportsWhileBuildingPackageLists();

        new StaticFiles(outputDir).copyToOutputDir(withSourceFilePages);
        new IndexPage(outputFile, sourceDirs, sourceFilesNotFound, packageToFiles, fileToFileData).generate();

        logger.info("JMockit: Coverage report written to {}", outputFile.getParentFile().getCanonicalPath());
    }

    private void createReportOutputDirIfNotExists() {
        if (!outputDirCreated) {
            File outDir = Path.of(outputDir).toFile();
            outputDirCreated = outDir.mkdirs();
        }
    }

    @Nullable
    private File createOutputFileForIndexPage() throws IOException {
        File outputFile = Path.of(outputDir, "index.html").toFile();

        if (outputFile.exists() && !outputFile.canWrite()) {
            logger.info("JMockit: {} is read-only; report generation canceled", outputFile.getCanonicalPath());
            return null;
        }

        return outputFile;
    }

    private void generateFileCoverageReportsWhileBuildingPackageLists() throws IOException {
        Set<Entry<String, FileCoverageData>> files = fileToFileData.entrySet();

        for (Entry<String, FileCoverageData> fileAndFileData : files) {
            generateFileCoverageReport(fileAndFileData.getKey(), fileAndFileData.getValue());
        }
    }

    private void generateFileCoverageReport(@NonNull String sourceFile, @NonNull FileCoverageData fileData)
            throws IOException {
        if (sourceDirs != null) {
            InputFile inputFile = InputFile.createIfFileExists(sourceDirs, sourceFile);

            if (inputFile != null) {
                new FileCoverageReport(outputDir, inputFile, fileData, withCallPoints).generate();
            } else {
                deleteOutdatedHTMLFileIfExists(sourceFile);

                if (sourceFilesNotFound != null) {
                    sourceFilesNotFound.add(sourceFile);
                }
            }
        }

        addFileToPackageFileList(sourceFile);
    }

    private void addFileToPackageFileList(@NonNull String file) {
        int p = file.lastIndexOf('/');
        String filePackage = p < 0 ? "" : file.substring(0, p);
        List<String> filesInPackage = packageToFiles.computeIfAbsent(filePackage, k -> new ArrayList<>());
        filesInPackage.add(file.substring(p + 1));
    }

    private void deleteOutdatedHTMLFileIfExists(@NonNull String filePath) {
        if (!outputDirCreated) {
            File outputFile = OutputFile.getOutputFile(outputDir, filePath);
            // noinspection ResultOfMethodCallIgnored
            outputFile.delete();
        }
    }
}
