/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class OutputFile extends PrintWriter {
    private static final Pattern PATH_SEPARATOR = Pattern.compile("/");

    @NonNull
    private final String relPathToOutDir;
    private final boolean sourceFile;

    public OutputFile(@NonNull File file) throws IOException {
        super(file, StandardCharsets.UTF_8);
        relPathToOutDir = "";
        sourceFile = false;
    }

    public OutputFile(@NonNull String outputDir, @NonNull String sourceFilePath) throws IOException {
        super(getOutputFileCreatingDirIfNeeded(outputDir, sourceFilePath));
        relPathToOutDir = getRelativeSubPathToOutputDir(sourceFilePath);
        sourceFile = true;
    }

    @NonNull
    private static File getOutputFileCreatingDirIfNeeded(@NonNull String outputDir, @NonNull String sourceFilePath) {
        File outputFile = getOutputFile(outputDir, sourceFilePath);
        File parentDir = outputFile.getParentFile();

        if (!parentDir.exists()) {
            boolean outputDirCreated = parentDir.mkdirs();
            assert outputDirCreated : "Failed to create output dir: " + outputDir;
        }

        return outputFile;
    }

    @NonNull
    static File getOutputFile(@NonNull String outputDir, @NonNull String sourceFilePath) {
        int p = sourceFilePath.lastIndexOf('.');
        String outputFileName = sourceFilePath.substring(0, p) + ".html";
        return Path.of(outputDir, outputFileName).toFile();
    }

    @NonNull
    private static String getRelativeSubPathToOutputDir(@NonNull String filePath) {
        StringBuilder cssRelPath = new StringBuilder();
        int n = PATH_SEPARATOR.split(filePath).length;

        for (int i = 1; i < n; i++) {
            cssRelPath.append("../");
        }

        return cssRelPath.toString();
    }

    public void writeCommonHeader(@NonNull String pageTitle) {
        println("<!DOCTYPE html>");
        println("<html>");
        println("<head>");
        println("  <title>" + pageTitle + "</title>");
        println("  <meta charset='UTF-8'>");
        println("  <link rel='stylesheet' type='text/css' href='" + relPathToOutDir + (sourceFile ? "source" : "index")
                + ".css'>");
        println("  <link rel='shortcut icon' type='image/png' href='" + relPathToOutDir + "logo.png'>");
        println("  <script src='" + relPathToOutDir + "coverage.js'></script>");
        println("  <base target='_blank'>");

        if (sourceFile) {
            println("  <script src='" + relPathToOutDir + "prettify.js'></script>");
        }

        println("</head>");
        println(sourceFile ? "<body onload='prettyPrint()'>" : "<body>");
    }

    public void writeCommonFooter() {
        println("</body>");
        println("</html>");
    }
}
