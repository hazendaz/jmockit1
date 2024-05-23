package integrationTests;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isFinal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import mockit.coverage.CallPoint;
import mockit.coverage.data.CoverageData;
import mockit.coverage.data.FileCoverageData;
import mockit.coverage.dataItems.InstanceFieldData;
import mockit.coverage.dataItems.PerFileDataCoverage;
import mockit.coverage.dataItems.StaticFieldData;
import mockit.coverage.lines.BranchCoverageData;
import mockit.coverage.lines.LineCoverageData;
import mockit.coverage.lines.PerFileLineCoverage;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;

@SuppressWarnings("JUnitTestCaseWithNoTests")
public class CoverageTest {
    @Nullable
    protected static FileCoverageData fileData;
    @Nullable
    private static String testedClassSimpleName;

    @BeforeEach
    public final void findCoverageData() throws Exception {
        Field testedField = getClass().getDeclaredField("tested");
        Class<?> testedClass = testedField.getType();

        if (testedClass != Object.class) {
            findFileDate(testedClass);
            setTestedFieldToNewInstanceIfApplicable(testedField);
        }
    }

    private void findFileDate(@NonNull Class<?> testedClass) {
        testedClassSimpleName = testedClass.getSimpleName();

        String classFilePath = testedClass.getName().replace('.', '/') + ".java";
        Map<String, FileCoverageData> data = CoverageData.instance().getFileToFileData();
        fileData = data.get(classFilePath);

        assertNotNull(fileData, "FileCoverageData not found for " + classFilePath);
    }

    private void setTestedFieldToNewInstanceIfApplicable(@NonNull Field testedField) throws Exception {
        Class<?> testedClass = testedField.getType();

        if (!testedClass.isEnum() && !isAbstract(testedClass.getModifiers()) && !isFinal(testedField.getModifiers())) {
            testedField.setAccessible(true);

            if (testedField.get(this) == null) {
                // noinspection ClassNewInstance
                Object newTestedInstance = testedClass.getDeclaredConstructor().newInstance();

                testedField.set(this, newTestedInstance);
            }
        }
    }

    @NonNull
    private FileCoverageData fileData() {
        if (fileData == null) {
            Object testedInstance;

            try {
                Field testedField = getClass().getDeclaredField("tested");
                testedInstance = testedField.get(this);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            Class<?> testedClass = testedInstance.getClass();
            findFileDate(testedClass);
        }

        return fileData;
    }

    // Line Coverage assertions
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected final void assertLines(@NonNegative int startingLine, @NonNegative int endingLine,
            @NonNegative int expectedLinesExecuted) {
        PerFileLineCoverage lineCoverageInfo = fileData().lineCoverageInfo;
        int lineCount = lineCoverageInfo.getLineCount();
        assertTrue(lineCount >= startingLine, "Starting line not found");
        assertTrue(lineCount >= endingLine, "Ending line not found");

        int linesExecuted = 0;

        for (int line = startingLine; line <= endingLine; line++) {
            if (lineCoverageInfo.getExecutionCount(line) > 0) {
                linesExecuted++;
            }
        }

        assertEquals(expectedLinesExecuted, linesExecuted, "Unexpected number of lines executed:");
    }

    protected final void assertLine(@NonNegative int line, @NonNegative int expectedSegments,
            @NonNegative int expectedCoveredSegments, int... expectedExecutionCounts) {
        PerFileLineCoverage info = fileData().lineCoverageInfo;
        LineCoverageData lineData = info.getLineData(line);

        assertEquals(expectedSegments, info.getNumberOfSegments(line), "Segments:");
        assertEquals(expectedCoveredSegments, lineData.getNumberOfCoveredSegments(), "Covered segments:");
        assertEquals(expectedExecutionCounts[0], info.getExecutionCount(line), "Execution count:");

        for (int i = 1; i < expectedExecutionCounts.length; i++) {
            BranchCoverageData segmentData = lineData.getBranchData(i - 1);

            int executionCount = segmentData.getExecutionCount();
            assertEquals(expectedExecutionCounts[i], executionCount,
                    "Execution count for line " + line + ", segment " + i + ':');

            List<CallPoint> callPoints = segmentData.getCallPoints();

            if (callPoints != null) {
                int callPointCount = 0;

                for (CallPoint callPoint : callPoints) {
                    callPointCount++;
                    callPointCount += callPoint.getRepetitionCount();
                }

                assertEquals(executionCount, callPointCount, "Missing call points for line " + line + ", segment " + i);
            }
        }
    }

    protected final void assertBranchingPoints(@NonNegative int line, @NonNegative int expectedSourcesAndTargets,
            @NonNegative int expectedCoveredSourcesAndTargets) {
        PerFileLineCoverage lineCoverageInfo = fileData().lineCoverageInfo;
        LineCoverageData lineData = lineCoverageInfo.getLineData(line);

        int sourcesAndTargets = lineCoverageInfo.getNumberOfBranchingSourcesAndTargets(line);
        assertEquals(expectedSourcesAndTargets, sourcesAndTargets, "Sources and targets:");

        int coveredSourcesAndTargets = lineData.getNumberOfCoveredBranchingSourcesAndTargets();
        assertEquals(expectedCoveredSourcesAndTargets, coveredSourcesAndTargets, "Covered sources and targets:");
    }

    // Data Coverage assertions
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected final void assertFieldIgnored(@NonNull String fieldName) {
        String fieldId = testedClassSimpleName + '.' + fieldName;
        PerFileDataCoverage info = fileData().dataCoverageInfo;
        assertFalse(info.staticFieldsData.containsKey(fieldId),
                "Field " + fieldName + " should not have static coverage data");
        assertFalse(info.instanceFieldsData.containsKey(fieldId),
                "Field " + fieldName + " should not have instance coverage data");
    }

    protected static void assertStaticFieldCovered(@NonNull String fieldName) {
        assertTrue(isStaticFieldCovered(fieldName), "Static field " + fieldName + " should be covered");
    }

    private static boolean isStaticFieldCovered(@NonNull String fieldName) {
        String classAndFieldNames = testedClassSimpleName + '.' + fieldName;
        StaticFieldData staticFieldData = fileData.dataCoverageInfo.staticFieldsData.get(classAndFieldNames);

        return staticFieldData.isCovered();
    }

    protected static void assertStaticFieldUncovered(@NonNull String fieldName) {
        assertFalse(isStaticFieldCovered(fieldName), "Static field " + fieldName + " should not be covered");
    }

    protected static void assertInstanceFieldCovered(@NonNull String fieldName) {
        assertTrue(isInstanceFieldCovered(fieldName), "Instance field " + fieldName + " should be covered");
    }

    private static boolean isInstanceFieldCovered(@NonNull String fieldName) {
        return getInstanceFieldData(fieldName).isCovered();
    }

    private static InstanceFieldData getInstanceFieldData(@NonNull String fieldName) {
        String classAndFieldNames = testedClassSimpleName + '.' + fieldName;
        return fileData.dataCoverageInfo.instanceFieldsData.get(classAndFieldNames);
    }

    protected static void assertInstanceFieldUncovered(@NonNull String fieldName) {
        assertFalse(isInstanceFieldCovered(fieldName), "Instance field " + fieldName + " should not be covered");
    }

    protected static void assertInstanceFieldUncovered(@NonNull String fieldName,
            @NonNull Object... uncoveredInstances) {
        String msg = "Instance field " + fieldName + " should not be covered";
        InstanceFieldData fieldData = getInstanceFieldData(fieldName);
        List<Integer> ownerInstances = fieldData.getOwnerInstancesWithUnreadAssignments();

        assertEquals(uncoveredInstances.length, ownerInstances.size(), msg);

        for (Object uncoveredInstance : uncoveredInstances) {
            Integer instanceId = System.identityHashCode(uncoveredInstance);
            assertTrue(ownerInstances.contains(instanceId), msg);
        }
    }

    protected static void verifyDataCoverage(@NonNegative int expectedItems, @NonNegative int expectedCoveredItems,
            @NonNegative int expectedCoverage) {
        PerFileDataCoverage info = fileData.dataCoverageInfo;
        assertEquals(expectedItems, info.getTotalItems(), "Total data items:");
        assertEquals(expectedCoveredItems, info.getCoveredItems(), "Covered data items:");
        assertEquals(expectedCoverage, info.getCoveragePercentage(), "Data coverage:");
    }
}
