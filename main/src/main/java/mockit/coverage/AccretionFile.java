/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import mockit.coverage.data.CoverageData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AccretionFile {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(AccretionFile.class);

    @NonNull
    private final File outputFile;
    @NonNull
    private final CoverageData newData;

    AccretionFile(@NonNull String outputDir, @NonNull CoverageData newData) {
        String parentDir = Configuration.getOrChooseOutputDirectory(outputDir);
        outputFile = Path.of(parentDir, "coverage.ser").toFile();

        newData.fillLastModifiedTimesForAllClassFiles();
        this.newData = newData;
    }

    void mergeDataFromExistingFileIfAny() throws IOException {
        if (outputFile.exists()) {
            CoverageData previousData = CoverageData.readDataFromFile(outputFile);
            newData.merge(previousData);
        }
    }

    void generate() throws IOException {
        newData.writeDataToFile(outputFile);
        logger.info("JMockit: Coverage data written to {}", outputFile.getCanonicalPath());
    }
}
