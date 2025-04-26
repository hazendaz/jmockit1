/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import mockit.coverage.data.CoverageData;

final class DataFileMerging {
    @NonNull
    private final List<File> inputFiles;

    DataFileMerging(@NonNull String[] inputPaths) {
        inputFiles = new ArrayList<>(inputPaths.length);

        for (String path : inputPaths) {
            addInputFileToList(path.trim());
        }
    }

    private void addInputFileToList(@NonNull String path) {
        if (!path.isEmpty()) {
            File inputFile = Path.of(path).toFile();

            if (inputFile.isDirectory()) {
                inputFile = inputFile.toPath().resolve("coverage.ser").toFile();
            }

            inputFiles.add(inputFile);
        }
    }

    @NonNull
    CoverageData merge() throws IOException {
        CoverageData mergedData = null;

        for (File inputFile : inputFiles) {
            if (inputFile.exists()) {
                CoverageData existingData = CoverageData.readDataFromFile(inputFile);

                if (mergedData == null) {
                    mergedData = existingData;
                } else {
                    mergedData.merge(existingData);
                }
            }
        }

        if (mergedData == null) {
            throw new IllegalArgumentException("No input \"coverage.ser\" files found");
        }

        return mergedData;
    }
}
