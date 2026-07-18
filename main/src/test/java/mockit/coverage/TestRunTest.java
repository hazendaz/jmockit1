/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.asm.controlFlow.Label;
import mockit.coverage.data.CoverageData;
import mockit.coverage.data.FileCoverageData;
import mockit.coverage.dataItems.PerFileDataCoverage;
import mockit.coverage.lines.PerFileLineCoverage;

import org.junit.jupiter.api.Test;

// Note: TestRun.lineExecuted/branchExecuted/fieldAssigned/fieldRead all delegate to the process-wide
// CoverageData.instance() singleton, so each test below uses its own uniquely named "source file" to avoid
// interfering with other tests. TestRun.terminate() is intentionally never invoked here, since it permanently
// disables coverage recording for the rest of the JVM with no way to reverse it.
final class TestRunTest {

    @Test
    void lineExecutedIncrementsTheExecutionCountForTheGivenLine() {
        FileCoverageData fileData = CoverageData.instance().getOrAddFile("mockit/coverage/TestRunTest$line.java",
                "class");
        fileData.lineCoverageInfo.addLine(3);

        TestRun.lineExecuted(fileData.index, 3);
        TestRun.lineExecuted(fileData.index, 3);

        assertEquals(2, fileData.lineCoverageInfo.getExecutionCount(3));
    }

    @Test
    void branchExecutedIncrementsTheExecutionCountForAValidBranch() {
        FileCoverageData fileData = CoverageData.instance().getOrAddFile("mockit/coverage/TestRunTest$branch.java",
                "class");
        PerFileLineCoverage lineCoverage = fileData.lineCoverageInfo;
        lineCoverage.addLine(7);

        Label source = new Label();
        source.line = 7;
        Label target = new Label();
        target.line = 9;
        int branchIndex = lineCoverage.getOrCreateLineData(7).addBranchingPoint(source, target);

        TestRun.branchExecuted(fileData.index, 7, branchIndex);

        assertEquals(1, lineCoverage.getBranchData(7, branchIndex).getExecutionCount());
    }

    @Test
    void fieldAssignedAndReadAreRegisteredForStaticFields() {
        String file = "mockit/coverage/TestRunTest$staticField.java";
        FileCoverageData fileData = CoverageData.instance().getOrAddFile(file, "class");
        PerFileDataCoverage dataCoverage = fileData.dataCoverageInfo;
        dataCoverage.addField("SomeClass", "someStaticField", true);

        String classAndField = "SomeClass.someStaticField";
        TestRun.fieldAssigned(file, classAndField);
        TestRun.fieldRead(file, classAndField);

        assertTrue(dataCoverage.isCovered(classAndField));
    }

    @Test
    void fieldAssignedAndReadAreRegisteredForInstanceFields() {
        String file = "mockit/coverage/TestRunTest$instanceField.java";
        FileCoverageData fileData = CoverageData.instance().getOrAddFile(file, "class");
        PerFileDataCoverage dataCoverage = fileData.dataCoverageInfo;
        dataCoverage.addField("SomeClass", "someInstanceField", false);

        Object instance = new Object();
        String classAndField = "SomeClass.someInstanceField";
        TestRun.fieldAssigned(instance, file, classAndField);
        TestRun.fieldRead(instance, file, classAndField);

        assertTrue(dataCoverage.isCovered(classAndField));
    }

    @Test
    void isTerminatedIsFalseUnderNormalTestExecution() {
        assertFalse(TestRun.isTerminated());
    }
}
