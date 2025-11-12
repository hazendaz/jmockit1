/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.reporting;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class SourceFiles {
    @NonNull
    private final List<File> srcDirs = new ArrayList<>();

    @NonNull
    List<File> buildListOfSourceDirectories(@NonNull String[] sourceDirs) {
        if (sourceDirs.length > 0) {
            buildListWithSpecifiedDirectories(sourceDirs);
        } else {
            buildListWithAllSrcSubDirectories();
        }

        return srcDirs;
    }

    private void buildListWithSpecifiedDirectories(@NonNull String[] dirs) {
        for (String dir : dirs) {
            File srcDir = Path.of(dir).toFile();

            if (srcDir.isDirectory()) {
                srcDirs.add(srcDir);
            }
        }

        if (srcDirs.isEmpty()) {
            throw new IllegalStateException("None of the specified source directories exist");
        }
    }

    private void buildListWithAllSrcSubDirectories() {
        String curDirName = Path.of(System.getProperty("user.dir")).toFile().getName();
        addSrcSubDirs(Path.of("../" + curDirName).toFile());
    }

    private void addSrcSubDirs(@NonNull File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File subDir : files) {
            if (subDir.isDirectory()) {
                String subDirName = subDir.getName();

                if ("src".equals(subDirName)) {
                    srcDirs.add(subDir);
                } else if (!isDirectoryToIgnore(subDirName)) {
                    addSrcSubDirs(subDir);
                }
            }
        }
    }

    private static final String IGNORED_DIRS = "bin build classes generated-sources out test tst web ";

    private static boolean isDirectoryToIgnore(@NonNull String subDirName) {
        int p = IGNORED_DIRS.indexOf(subDirName);
        return p >= 0 && IGNORED_DIRS.charAt(p + subDirName.length()) == ' ';
    }
}
