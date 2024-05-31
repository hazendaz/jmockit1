/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
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
            File srcDir = new File(dir);

            if (srcDir.isDirectory()) {
                srcDirs.add(srcDir);
            }
        }

        if (srcDirs.isEmpty()) {
            throw new IllegalStateException("None of the specified source directories exist");
        }
    }

    private void buildListWithAllSrcSubDirectories() {
        String curDirName = new File(System.getProperty("user.dir")).getName();
        addSrcSubDirs(new File("../" + curDirName));
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
