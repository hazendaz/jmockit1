/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import mockit.coverage.data.CoverageData;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class DataFileMergingTest {
    private static final String SOURCE_FILE_1 = "mockit/coverage/DataFileMergingTest.java";
    private static final String SOURCE_FILE_2 = "mockit/coverage/AccretionFileTest.java";

    @TempDir
    File tempDir;

    @Test
    void mergeCombinesDataFromMultipleInputFiles() throws IOException {
        File firstFile = new File(tempDir, "first.ser");
        File secondFile = new File(tempDir, "second.ser");

        CoverageData firstData = new CoverageData();
        firstData.getOrAddFile(SOURCE_FILE_1, "class");
        firstData.writeDataToFile(firstFile);

        CoverageData secondData = new CoverageData();
        secondData.getOrAddFile(SOURCE_FILE_2, "class");
        secondData.writeDataToFile(secondFile);

        DataFileMerging merging = new DataFileMerging(new String[] { firstFile.getPath(), secondFile.getPath() });
        CoverageData merged = merging.merge();

        assertEquals(2, merged.getFileToFileData().size());
    }

    @Test
    void mergeResolvesADirectoryArgumentToItsCoverageSerFile() throws IOException {
        CoverageData data = new CoverageData();
        data.getOrAddFile(SOURCE_FILE_1, "class");
        data.writeDataToFile(new File(tempDir, "coverage.ser"));

        DataFileMerging merging = new DataFileMerging(new String[] { tempDir.getPath() });
        CoverageData merged = merging.merge();

        assertTrue(merged.getFileToFileData().containsKey(SOURCE_FILE_1));
    }

    @Test
    void mergeIgnoresBlankInputPaths() throws IOException {
        File firstFile = new File(tempDir, "first.ser");
        CoverageData data = new CoverageData();
        data.getOrAddFile(SOURCE_FILE_1, "class");
        data.writeDataToFile(firstFile);

        DataFileMerging merging = new DataFileMerging(new String[] { "", "   ", firstFile.getPath() });
        CoverageData merged = merging.merge();

        assertEquals(1, merged.getFileToFileData().size());
    }

    @Test
    void mergeThrowsWhenNoInputFilesAreFound() {
        DataFileMerging merging = new DataFileMerging(new String[] { new File(tempDir, "missing.ser").getPath() });

        assertThrows(IllegalArgumentException.class, merging::merge);
    }
}
