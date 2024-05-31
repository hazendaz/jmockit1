/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.data;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import mockit.coverage.CoveragePercentage;
import mockit.internal.util.Utilities;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Coverage data captured for all source files exercised during a test run.
 */
public final class CoverageData implements Serializable {
    private static final long serialVersionUID = -4860004226098360259L;
    @NonNull
    private static final CoverageData instance = new CoverageData();

    @NonNull
    public static CoverageData instance() {
        return instance;
    }

    private boolean withCallPoints;

    @NonNull
    private final Map<String, FileCoverageData> fileToFileData = new LinkedHashMap<>();
    @NonNull
    private final List<FileCoverageData> indexedFileData = new ArrayList<>(100);

    public boolean isWithCallPoints() {
        return withCallPoints;
    }

    public void setWithCallPoints(boolean withCallPoints) {
        this.withCallPoints = withCallPoints;
    }

    @NonNull
    public Map<String, FileCoverageData> getFileToFileData() {
        return fileToFileData;
    }

    @NonNull
    public FileCoverageData getOrAddFile(@NonNull String file, @Nullable String kindOfTopLevelType) {
        FileCoverageData fileData = fileToFileData.get(file);

        // For a class with nested/inner classes, a previous class in the same source file may already have been added.
        if (fileData == null) {
            int fileIndex = indexedFileData.size();
            fileData = new FileCoverageData(fileIndex, kindOfTopLevelType);
            indexedFileData.add(fileData);
            fileToFileData.put(file, fileData);
        } else if (kindOfTopLevelType != null) {
            fileData.kindOfTopLevelType = kindOfTopLevelType;
        }

        return fileData;
    }

    @NonNull
    public FileCoverageData getFileData(@NonNull String file) {
        return fileToFileData.get(file);
    }

    @NonNull
    public FileCoverageData getFileData(@NonNegative int fileIndex) {
        return indexedFileData.get(fileIndex);
    }

    public boolean isEmpty() {
        return fileToFileData.isEmpty();
    }

    public void clear() {
        fileToFileData.clear();
    }

    /**
     * Computes the coverage percentage over a subset of the available source files.
     *
     * @param fileNamePrefix
     *            a regular expression for matching the names of the source files to be considered, or <code>null</code>
     *            to consider <em>all</em> files
     *
     * @return the computed percentage from <code>0</code> to <code>100</code> (inclusive), or <code>-1</code> if no
     *         meaningful value could be computed
     */
    public int getPercentage(@Nullable String fileNamePrefix) {
        int coveredItems = 0;
        int totalItems = 0;

        for (Entry<String, FileCoverageData> fileAndFileData : fileToFileData.entrySet()) {
            String sourceFile = fileAndFileData.getKey();

            if (fileNamePrefix == null || sourceFile.startsWith(fileNamePrefix)) {
                FileCoverageData fileData = fileAndFileData.getValue();
                coveredItems += fileData.getCoveredItems();
                totalItems += fileData.getTotalItems();
            }
        }

        return CoveragePercentage.calculate(coveredItems, totalItems);
    }

    /**
     * Finds the source file with the smallest coverage percentage.
     *
     * @return the percentage value for the file found, or <code>Integer.MAX_VALUE</code> if no file is found with a
     *         meaningful percentage
     */
    @NonNegative
    public int getSmallestPerFilePercentage() {
        int minPercentage = Integer.MAX_VALUE;

        for (FileCoverageData fileData : fileToFileData.values()) {
            if (!fileData.wasLoadedAfterTestCompletion()) {
                int percentage = fileData.getCoveragePercentage();

                if (percentage >= 0 && percentage < minPercentage) {
                    minPercentage = percentage;
                }
            }
        }

        return minPercentage;
    }

    public void fillLastModifiedTimesForAllClassFiles() {
        for (Iterator<Entry<String, FileCoverageData>> itr = fileToFileData.entrySet().iterator(); itr.hasNext();) {
            Entry<String, FileCoverageData> fileAndFileData = itr.next();
            long lastModified = getLastModifiedTimeForClassFile(fileAndFileData.getKey());

            if (lastModified > 0L) {
                FileCoverageData fileCoverageData = fileAndFileData.getValue();
                fileCoverageData.lastModified = lastModified;
                continue;
            }

            itr.remove();
        }
    }

    private long getLastModifiedTimeForClassFile(@NonNull String sourceFilePath) {
        String sourceFilePathNoExt = sourceFilePath.substring(0, sourceFilePath.lastIndexOf('.'));
        String className = sourceFilePathNoExt.replace('/', '.');

        Class<?> coveredClass = findCoveredClass(className);

        if (coveredClass == null) {
            return 0L;
        }

        String locationPath = Utilities.getClassFileLocationPath(coveredClass);

        if (locationPath.endsWith(".jar")) {
            try {
                return getLastModifiedTimeFromJarEntry(sourceFilePathNoExt, locationPath);
            } catch (IOException ignore) {
                return 0L;
            }
        }

        String pathToClassFile = locationPath + sourceFilePathNoExt + ".class";

        return new File(pathToClassFile).lastModified();
    }

    private static long getLastModifiedTimeFromJarEntry(@NonNull String sourceFilePathNoExt,
            @NonNull String locationPath) throws IOException {

        try (JarFile jarFile = new JarFile(locationPath)) {
            JarEntry classEntry = jarFile.getJarEntry(sourceFilePathNoExt + ".class");
            return classEntry.getTime();
        }
    }

    @Nullable
    private Class<?> findCoveredClass(@NonNull String className) {
        ClassLoader currentCL = getClass().getClassLoader();
        Class<?> coveredClass = loadClass(className, currentCL);

        if (coveredClass == null) {
            ClassLoader systemCL = ClassLoader.getSystemClassLoader();

            if (systemCL != currentCL) {
                coveredClass = loadClass(className, systemCL);
            }

            if (coveredClass == null) {
                ClassLoader contextCL = Thread.currentThread().getContextClassLoader();

                if (contextCL != null && contextCL != systemCL) {
                    coveredClass = loadClass(className, contextCL);
                }
            }
        }

        return coveredClass;
    }

    @Nullable
    private static Class<?> loadClass(@NonNull String className, @Nullable ClassLoader loader) {
        try {
            return Class.forName(className, false, loader);
        } catch (ClassNotFoundException | NoClassDefFoundError ignore) {
            return null;
        }
    }

    /**
     * Reads a serialized <code>CoverageData</code> object from the given file (normally, a "<code>coverage.ser</code>"
     * file generated at the end of a previous test run).
     *
     * @param dataFile
     *            the ".ser" file containing a serialized <code>CoverageData</code> instance
     *
     * @return a new object containing all coverage data resulting from a previous test run
     */
    @NonNull
    public static CoverageData readDataFromFile(@NonNull File dataFile) throws IOException {
        try (ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(dataFile)))) {
            return (CoverageData) input.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Serialized class in coverage data file \"" + dataFile + "\" not found in classpath", e);
        }
    }

    public void writeDataToFile(@NonNull File dataFile) throws IOException {
        try (ObjectOutputStream output = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(dataFile)))) {
            output.writeObject(this);
        }
    }

    public void merge(@NonNull CoverageData previousData) {
        withCallPoints |= previousData.withCallPoints;

        for (Entry<String, FileCoverageData> previousFileAndFileData : previousData.fileToFileData.entrySet()) {
            String previousFile = previousFileAndFileData.getKey();
            FileCoverageData previousFileData = previousFileAndFileData.getValue();
            FileCoverageData fileData = fileToFileData.get(previousFile);

            if (fileData == null) {
                fileToFileData.put(previousFile, previousFileData);
            } else if (fileData.lastModified > 0 && previousFileData.lastModified == fileData.lastModified) {
                fileData.mergeWithDataFromPreviousTestRun(previousFileData);
            }
        }
    }
}
