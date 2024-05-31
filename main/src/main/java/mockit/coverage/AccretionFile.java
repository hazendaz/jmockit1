/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.io.IOException;

import mockit.coverage.data.CoverageData;

final class AccretionFile {
    @NonNull
    private final File outputFile;
    @NonNull
    private final CoverageData newData;

    AccretionFile(@NonNull String outputDir, @NonNull CoverageData newData) {
        String parentDir = Configuration.getOrChooseOutputDirectory(outputDir);
        outputFile = new File(parentDir, "coverage.ser");

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
        System.out.println("JMockit: Coverage data written to " + outputFile.getCanonicalPath());
    }
}
