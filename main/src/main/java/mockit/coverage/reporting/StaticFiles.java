/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.reporting;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;

import mockit.internal.util.Utilities;

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
        File outputFile = Path.of(outputDir, fileName).toFile();

        if (outputFile.exists() && outputFile.lastModified() > getLastModifiedTimeOfCoverageJar()) {
            return;
        }

        try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(outputFile.toPath()));
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
                lastModifiedTimeOfCoverageJar = Path.of(pathToThisJar).toFile().lastModified();
            }
        }

        return lastModifiedTimeOfCoverageJar;
    }
}
