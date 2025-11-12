/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.reporting.dataCoverage;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.coverage.dataItems.FieldData;
import mockit.coverage.dataItems.InstanceFieldData;
import mockit.coverage.dataItems.PerFileDataCoverage;
import mockit.coverage.dataItems.StaticFieldData;
import mockit.coverage.reporting.parsing.FileParser;
import mockit.coverage.reporting.parsing.LineElement;

import org.checkerframework.checker.index.qual.NonNegative;

public final class DataCoverageOutput {
    @NonNull
    private final StringBuilder openingTag;
    @NonNull
    private final PerFileDataCoverage coverageInfo;
    @NonNegative
    private int nextField;
    @Nullable
    private String classAndFieldNames;
    @Nullable
    private String className;
    @Nullable
    private String fieldName;

    public DataCoverageOutput(@NonNull PerFileDataCoverage coverageInfo) {
        openingTag = new StringBuilder(50);
        this.coverageInfo = coverageInfo;
        moveToNextField();
    }

    private void moveToNextField() {
        if (nextField >= coverageInfo.allFields.size()) {
            classAndFieldNames = null;
            className = null;
            fieldName = null;
            return;
        }

        classAndFieldNames = coverageInfo.allFields.get(nextField);
        nextField++;

        int p = classAndFieldNames.indexOf('.');
        className = classAndFieldNames.substring(0, p);
        fieldName = classAndFieldNames.substring(p + 1);
    }

    public void writeCoverageInfoIfLineStartsANewFieldDeclaration(@NonNull FileParser fileParser) {
        if (classAndFieldNames != null) {
            assert className != null;

            if (className.equals(fileParser.getCurrentlyPendingClass())) {
                LineElement initialLineElement = fileParser.lineParser.getInitialElement();

                assert fieldName != null;
                LineElement elementWithFieldName = initialLineElement.findWord(fieldName);

                if (elementWithFieldName != null) {
                    buildOpeningTagForFieldWrapper();
                    elementWithFieldName.wrapText(openingTag.toString(), "</span>");
                    moveToNextField();
                }
            }
        }
    }

    private void buildOpeningTagForFieldWrapper() {
        openingTag.setLength(0);
        openingTag.append("<span class='");

        assert classAndFieldNames != null;
        StaticFieldData staticData = coverageInfo.getStaticFieldData(classAndFieldNames);
        boolean staticField = staticData != null;
        openingTag.append(staticField ? "static" : "instance");

        openingTag.append(coverageInfo.isCovered(classAndFieldNames) ? " cvd" : " uncvd");

        InstanceFieldData instanceData = coverageInfo.getInstanceFieldData(classAndFieldNames);

        if (staticField || instanceData != null) {
            openingTag.append("' title='");
            appendAccessCounts(staticField ? staticData : instanceData);
        }

        openingTag.append("'>");
    }

    private void appendAccessCounts(@NonNull FieldData fieldData) {
        openingTag.append("Reads: ").append(fieldData.getReadCount());
        openingTag.append(" Writes: ").append(fieldData.getWriteCount());
    }
}
