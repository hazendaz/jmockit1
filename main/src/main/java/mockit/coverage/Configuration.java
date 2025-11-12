/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Configuration {
    private static final String COVERAGE_PREFIX = "coverage-";

    private Configuration() {
    }

    @Nullable
    public static String getProperty(@NonNull String nameSuffix) {
        return getProperty(nameSuffix, null);
    }

    public static String getProperty(@NonNull String nameSuffix, @Nullable String defaultValue) {
        return System.getProperty(COVERAGE_PREFIX + nameSuffix, defaultValue);
    }

    @Nullable
    public static String getOrChooseOutputDirectory(@NonNull String outputDir) {
        if (!outputDir.isEmpty()) {
            return outputDir;
        }

        return isTargetSubDirectoryAvailable() ? "target" : null;
    }

    private static boolean isTargetSubDirectoryAvailable() {
        return System.getProperty("basedir") != null || Files.exists(Path.of("target"));
    }

    @NonNull
    public static String getOrChooseOutputDirectory(@NonNull String outputDir, @NonNull String defaultDir) {
        if (!outputDir.isEmpty()) {
            return outputDir;
        }

        return isTargetSubDirectoryAvailable() ? "target/" + defaultDir : defaultDir;
    }
}
