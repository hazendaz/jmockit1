/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import mockit.coverage.data.CoverageData;

import org.checkerframework.checker.index.qual.NonNegative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CoverageCheck {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(CoverageCheck.class);

    private static final String configuration = Configuration.getProperty("check", "");

    @Nullable
    static CoverageCheck createIfApplicable() {
        return configuration.isEmpty() ? null : new CoverageCheck();
    }

    private static final class Threshold {
        private static final Pattern PARAMETER_SEPARATORS = Pattern.compile(":|=");

        @Nullable
        private final String sourceFilePrefix;
        @NonNull
        private final String scopeDescription;
        @NonNegative
        private int minPercentage;

        Threshold(@NonNull String configurationParameter) {
            String[] sourceFilePrefixAndMinPercentage = PARAMETER_SEPARATORS.split(configurationParameter);
            String textualPercentage;

            if (sourceFilePrefixAndMinPercentage.length == 1) {
                sourceFilePrefix = null;
                scopeDescription = "";
                textualPercentage = sourceFilePrefixAndMinPercentage[0];
            } else {
                String scope = sourceFilePrefixAndMinPercentage[0].trim();

                if (isPerFile(scope)) {
                    sourceFilePrefix = scope;
                    scopeDescription = " for some source files";
                } else {
                    sourceFilePrefix = scope.replace('.', '/');
                    scopeDescription = " for " + scope;
                }

                textualPercentage = sourceFilePrefixAndMinPercentage[1];
            }

            try {
                minPercentage = Integer.parseInt(textualPercentage.trim());
            } catch (NumberFormatException ignore) {
            }
        }

        private static boolean isPerFile(@Nullable String scope) {
            return "perFile".equalsIgnoreCase(scope);
        }

        boolean verifyMinimum() {
            CoverageData coverageData = CoverageData.instance();
            int percentage;

            if (isPerFile(sourceFilePrefix)) {
                percentage = coverageData.getSmallestPerFilePercentage();
            } else {
                percentage = coverageData.getPercentage(sourceFilePrefix);
            }

            return percentage < 0 || verifyMinimum(percentage);
        }

        private boolean verifyMinimum(@NonNegative int percentage) {
            if (percentage < minPercentage) {
                logger.info("JMockit: coverage too low {}: {}% < {}%", scopeDescription, percentage, minPercentage);
                return false;
            }

            return true;
        }
    }

    @NonNull
    private final List<Threshold> thresholds;
    private boolean allThresholdsSatisfied;

    private CoverageCheck() {
        String[] configurationParameters = configuration.split(";");
        int n = configurationParameters.length;
        thresholds = new ArrayList<>(n);

        for (String configurationParameter : configurationParameters) {
            thresholds.add(new Threshold(configurationParameter));
        }
    }

    void verifyThresholds() {
        allThresholdsSatisfied = true;

        for (Threshold threshold : thresholds) {
            allThresholdsSatisfied &= threshold.verifyMinimum();
        }

        createOrDeleteIndicatorFile();

        if (!allThresholdsSatisfied) {
            throw new AssertionError("JMockit: minimum coverage percentages not reached; see previous messages.");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void createOrDeleteIndicatorFile() {
        String parentDir = Configuration.getOrChooseOutputDirectory("");
        File indicatorFile = Path.of(parentDir, "coverage.check.failed").toFile();

        if (indicatorFile.exists()) {
            if (allThresholdsSatisfied) {
                indicatorFile.delete();
            } else {
                indicatorFile.setLastModified(System.currentTimeMillis());
            }
        } else if (!allThresholdsSatisfied) {
            try {
                indicatorFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
