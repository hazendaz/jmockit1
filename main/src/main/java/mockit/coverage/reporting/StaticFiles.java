/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.CodeSource;

import mockit.internal.util.Utilities;

import org.checkerframework.checker.nullness.qual.NonNull;

final class StaticFiles {
    @NonNull
    private final String outputDir;
    private long lastModifiedTimeOfCoverageJar;

    StaticFiles(@NonNull String outputDir) {
        this.outputDir = outputDir;
    }

    void copyToOutputDir(boolean withSourceFilePages) throws IOException {
        copyFile("index.css");
        copyFile("coverage.js");
        copyFile("logo.png");
        copyFile("package.png");
        copyFile("class.png");
        copyFile("abstractClass.png");
        copyFile("interface.png");
        copyFile("annotation.png");
        copyFile("exception.png");
        copyFile("enum.png");

        if (withSourceFilePages) {
            copyFile("source.css");
            copyFile("prettify.js");
        }
    }

    private void copyFile(@NonNull String fileName) throws IOException {
        File outputFile = new File(outputDir, fileName);

        if (outputFile.exists() && outputFile.lastModified() > getLastModifiedTimeOfCoverageJar()) {
            return;
        }

        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));
                InputStream input = new BufferedInputStream(StaticFiles.class.getResourceAsStream(fileName))) {
            int b;

            while ((b = input.read()) != -1) {
                output.write(b);
            }
        }
    }

    private long getLastModifiedTimeOfCoverageJar() {
        if (lastModifiedTimeOfCoverageJar == 0) {
            CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();

            if (codeSource == null) {
                lastModifiedTimeOfCoverageJar = -1;
            } else {
                String pathToThisJar = Utilities.getClassFileLocationPath(codeSource);
                lastModifiedTimeOfCoverageJar = new File(pathToThisJar).lastModified();
            }
        }

        return lastModifiedTimeOfCoverageJar;
    }
}
