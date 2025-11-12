/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.reporting.sourceFiles;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.checkerframework.checker.index.qual.NonNegative;

public final class InputFile {
    @NonNull
    final String filePath;
    @NonNull
    private final File sourceFile;
    @NonNull
    private final BufferedReader input;

    @Nullable
    public static InputFile createIfFileExists(@NonNull List<File> sourceDirs, @NonNull String filePath)
            throws IOException {
        File sourceFile = findSourceFile(sourceDirs, filePath);
        return sourceFile == null ? null : new InputFile(filePath, sourceFile);
    }

    @Nullable
    private static File findSourceFile(@NonNull List<File> sourceDirs, @NonNull String filePath) {
        int p = filePath.indexOf('/');
        String topLevelPackage = p < 0 ? "" : filePath.substring(0, p);
        int n = sourceDirs.size();

        for (int i = 0; i < n; i++) {
            File sourceDir = sourceDirs.get(i);
            File sourceFile = getSourceFile(sourceDir, topLevelPackage, filePath);

            if (sourceFile != null) {
                giveCurrentSourceDirHighestPriority(sourceDirs, i);
                addRootSourceDirIfNew(sourceDirs, filePath, sourceFile);
                return sourceFile;
            }
        }

        return null;
    }

    @Nullable
    private static File getSourceFile(@NonNull File sourceDir, @NonNull final String topLevelPackage,
            @NonNull String filePath) {
        File file = sourceDir.toPath().resolve(filePath).toFile();

        if (file.exists()) {
            return file;
        }

        File[] subDirs = sourceDir.listFiles((FileFilter) subDir -> subDir.isDirectory() && !subDir.isHidden()
                && !subDir.getName().equals(topLevelPackage));

        if (subDirs != null && subDirs.length > 0) {
            for (File subDir : subDirs) {
                File sourceFile = getSourceFile(subDir, topLevelPackage, filePath);

                if (sourceFile != null) {
                    return sourceFile;
                }
            }
        }

        return null;
    }

    private static void giveCurrentSourceDirHighestPriority(@NonNull List<File> sourceDirs,
            @NonNegative int currentSourceDirIndex) {
        if (currentSourceDirIndex > 0) {
            File firstSourceDir = sourceDirs.get(0);
            File currentSourceDir = sourceDirs.get(currentSourceDirIndex);

            if (!firstSourceDir.getPath().startsWith(currentSourceDir.getPath())) {
                sourceDirs.set(currentSourceDirIndex, firstSourceDir);
                sourceDirs.set(0, currentSourceDir);
            }
        }
    }

    private static void addRootSourceDirIfNew(@NonNull List<File> sourceDirs, @NonNull String filePath,
            @NonNull File sourceFile) {
        String sourceFilePath = sourceFile.getPath();
        String sourceRootDir = sourceFilePath.substring(0, sourceFilePath.length() - filePath.length());
        File newSourceDir = Path.of(sourceRootDir).toFile();

        if (!sourceDirs.contains(newSourceDir)) {
            sourceDirs.add(0, newSourceDir);
        }
    }

    private InputFile(@NonNull String filePath, @NonNull File sourceFile) throws IOException {
        this.filePath = filePath;
        this.sourceFile = sourceFile;
        input = Files.newBufferedReader(sourceFile.toPath(), StandardCharsets.UTF_8);
    }

    @NonNull
    String getSourceFileName() {
        return sourceFile.getName();
    }

    @NonNull
    String getSourceFilePath() {
        String path = sourceFile.getPath();
        return path.startsWith("..") ? path.substring(3) : path;
    }

    @Nullable
    String nextLine() throws IOException {
        return input.readLine();
    }

    void close() throws IOException {
        input.close();
    }
}
