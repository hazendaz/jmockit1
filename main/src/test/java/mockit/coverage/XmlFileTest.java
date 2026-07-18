/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import mockit.coverage.data.CoverageData;
import mockit.coverage.data.FileCoverageData;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class XmlFileTest {
    @TempDir
    File outputDir;

    @Test
    void generateWritesExpectedXmlContent() throws IOException {
        CoverageData data = new CoverageData();
        FileCoverageData fileData = data.getOrAddFile("com/example/Foo.java", "class");
        fileData.lineCoverageInfo.addLine(5);
        fileData.lineCoverageInfo.addLine(8);
        fileData.lineCoverageInfo.registerExecution(5, null);

        XmlFile xmlFile = new XmlFile(outputDir.getPath(), data);
        xmlFile.generate();

        File generatedFile = new File(outputDir, "coverage.xml");
        assertTrue(generatedFile.exists());

        String content = Files.readString(generatedFile.toPath());
        assertTrue(content.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(content.contains("<coverage version=\"1\">"));
        assertTrue(content.contains("<file path=\"com/example/Foo.java\">"));
        assertTrue(content.contains("lineNumber=\"5\" covered=\"true\""));
        assertTrue(content.contains("lineNumber=\"8\" covered=\"false\""));
        assertTrue(content.contains("</coverage>"));
    }

    @Test
    void generateWritesNoLinesForFilesWithoutExecutableLines() throws IOException {
        CoverageData data = new CoverageData();
        data.getOrAddFile("com/example/Empty.java", "class");

        XmlFile xmlFile = new XmlFile(outputDir.getPath(), data);
        xmlFile.generate();

        String content = Files.readString(new File(outputDir, "coverage.xml").toPath());
        assertTrue(content.contains("<file path=\"com/example/Empty.java\">"));
        assertTrue(!content.contains("lineToCover"));
    }
}
