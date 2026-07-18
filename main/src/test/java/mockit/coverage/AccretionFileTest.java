/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import mockit.coverage.data.CoverageData;
import mockit.coverage.data.FileCoverageData;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class AccretionFileTest {
    // A real, loadable class is required: AccretionFile computes the .class file's last-modified time in order to
    // decide whether merging across runs is safe, and unrecognized/unloadable "source files" are dropped.
    private static final String SOURCE_FILE = "mockit/coverage/AccretionFileTest.java";

    @TempDir
    File outputDir;

    @Test
    void generateWritesCoverageDataToFile() throws IOException {
        CoverageData data = new CoverageData();
        FileCoverageData fileData = data.getOrAddFile(SOURCE_FILE, "class");
        fileData.lineCoverageInfo.addLine(10);

        AccretionFile accretionFile = new AccretionFile(outputDir.getPath(), data);
        accretionFile.mergeDataFromExistingFileIfAny();
        accretionFile.generate();

        File serFile = outputDir.toPath().resolve("coverage.ser").toFile();
        assertTrue(serFile.exists());
        assertTrue(serFile.length() > 0);
    }

    @Test
    void mergeDataFromExistingFileIfAnyMergesDataFromAPreviousRun() throws IOException {
        CoverageData firstRunData = new CoverageData();
        FileCoverageData firstFileData = firstRunData.getOrAddFile(SOURCE_FILE, "class");
        firstFileData.lineCoverageInfo.addLine(10);

        AccretionFile firstAccretionFile = new AccretionFile(outputDir.getPath(), firstRunData);
        firstAccretionFile.mergeDataFromExistingFileIfAny();
        firstAccretionFile.generate();

        CoverageData secondRunData = new CoverageData();
        secondRunData.getOrAddFile(SOURCE_FILE, "class");

        AccretionFile secondAccretionFile = new AccretionFile(outputDir.getPath(), secondRunData);
        secondAccretionFile.mergeDataFromExistingFileIfAny();

        // The line added only in the first run should now have been merged into the second run's file data.
        assertEquals(1, secondRunData.getFileData(SOURCE_FILE).lineCoverageInfo.getExecutableLineCount());
    }

    @Test
    void mergeDataFromExistingFileIfAnyDoesNothingWhenNoPreviousFileExists() throws IOException {
        CoverageData data = new CoverageData();
        data.getOrAddFile(SOURCE_FILE, "class");

        AccretionFile accretionFile = new AccretionFile(outputDir.getPath(), data);

        // Should not throw, and should leave the data untouched.
        accretionFile.mergeDataFromExistingFileIfAny();

        assertEquals(1, data.getFileToFileData().size());
    }
}
