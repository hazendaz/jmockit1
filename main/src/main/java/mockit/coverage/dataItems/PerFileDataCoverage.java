/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.dataItems;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mockit.coverage.CoveragePercentage;
import mockit.coverage.data.PerFileCoverage;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PerFileDataCoverage implements PerFileCoverage {
    private static final long serialVersionUID = -4561686103982673490L;

    @NonNull
    public final List<String> allFields = new ArrayList<>(2);
    @NonNull
    public final Map<String, StaticFieldData> staticFieldsData = new LinkedHashMap<>();
    @NonNull
    public final Map<String, InstanceFieldData> instanceFieldsData = new LinkedHashMap<>();

    private transient int coveredDataItems = -1;

    private void readObject(@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        coveredDataItems = -1;
        in.defaultReadObject();
    }

    public void addField(@NonNull String className, @NonNull String fieldName, boolean isStatic) {
        String classAndField = className + '.' + fieldName;

        if (!allFields.contains(classAndField)) {
            allFields.add(classAndField);
        }

        if (isStatic) {
            staticFieldsData.put(classAndField, new StaticFieldData());
        } else {
            instanceFieldsData.put(classAndField, new InstanceFieldData());
        }
    }

    public boolean isFieldWithCoverageData(@NonNull String classAndFieldNames) {
        return instanceFieldsData.containsKey(classAndFieldNames) || staticFieldsData.containsKey(classAndFieldNames);
    }

    public void registerAssignmentToStaticField(@NonNull String classAndFieldNames) {
        StaticFieldData staticData = getStaticFieldData(classAndFieldNames);

        if (staticData != null) {
            staticData.registerAssignment();
        }
    }

    @Nullable
    public StaticFieldData getStaticFieldData(@NonNull String classAndFieldNames) {
        return staticFieldsData.get(classAndFieldNames);
    }

    public void registerReadOfStaticField(@NonNull String classAndFieldNames) {
        StaticFieldData staticData = getStaticFieldData(classAndFieldNames);

        if (staticData != null) {
            staticData.registerRead();
        }
    }

    public void registerAssignmentToInstanceField(@NonNull Object instance, @NonNull String classAndFieldNames) {
        InstanceFieldData instanceData = getInstanceFieldData(classAndFieldNames);

        if (instanceData != null) {
            instanceData.registerAssignment(instance);
        }
    }

    @Nullable
    public InstanceFieldData getInstanceFieldData(@NonNull String classAndFieldNames) {
        return instanceFieldsData.get(classAndFieldNames);
    }

    public void registerReadOfInstanceField(@NonNull Object instance, @NonNull String classAndFieldNames) {
        InstanceFieldData instanceData = getInstanceFieldData(classAndFieldNames);

        if (instanceData != null) {
            instanceData.registerRead(instance);
        }
    }

    public boolean hasFields() {
        return !allFields.isEmpty();
    }

    public boolean isCovered(@NonNull String classAndFieldNames) {
        InstanceFieldData instanceData = getInstanceFieldData(classAndFieldNames);

        if (instanceData != null && instanceData.isCovered()) {
            return true;
        }

        StaticFieldData staticData = getStaticFieldData(classAndFieldNames);

        return staticData != null && staticData.isCovered();
    }

    @Override
    @NonNegative
    public int getTotalItems() {
        return staticFieldsData.size() + instanceFieldsData.size();
    }

    @Override
    @NonNegative
    public int getCoveredItems() {
        if (coveredDataItems >= 0) {
            return coveredDataItems;
        }

        coveredDataItems = 0;

        for (StaticFieldData staticData : staticFieldsData.values()) {
            if (staticData.isCovered()) {
                coveredDataItems++;
            }
        }

        for (InstanceFieldData instanceData : instanceFieldsData.values()) {
            if (instanceData.isCovered()) {
                coveredDataItems++;
            }
        }

        return coveredDataItems;
    }

    @Override
    public int getCoveragePercentage() {
        int totalFields = getTotalItems();

        if (totalFields == 0) {
            return -1;
        }

        int coveredFields = getCoveredItems();
        return CoveragePercentage.calculate(coveredFields, totalFields);
    }

    public void mergeInformation(@NonNull PerFileDataCoverage previousInfo) {
        addInfoFromPreviousTestRun(staticFieldsData, previousInfo.staticFieldsData);
        addFieldsFromPreviousTestRunIfAbsent(staticFieldsData, previousInfo.staticFieldsData);

        addInfoFromPreviousTestRun(instanceFieldsData, previousInfo.instanceFieldsData);
        addFieldsFromPreviousTestRunIfAbsent(instanceFieldsData, previousInfo.instanceFieldsData);
    }

    private static <FI extends FieldData> void addInfoFromPreviousTestRun(@NonNull Map<String, FI> currentInfo,
            @NonNull Map<String, FI> previousInfo) {
        for (Entry<String, FI> nameAndInfo : currentInfo.entrySet()) {
            String fieldName = nameAndInfo.getKey();
            FieldData previousFieldInfo = previousInfo.get(fieldName);

            if (previousFieldInfo != null) {
                FieldData fieldInfo = nameAndInfo.getValue();
                fieldInfo.addCountsFromPreviousTestRun(previousFieldInfo);
            }
        }
    }

    private static <FI extends FieldData> void addFieldsFromPreviousTestRunIfAbsent(
            @NonNull Map<String, FI> currentInfo, @NonNull Map<String, FI> previousInfo) {
        for (Entry<String, FI> nameAndInfo : previousInfo.entrySet()) {
            String fieldName = nameAndInfo.getKey();

            if (!currentInfo.containsKey(fieldName)) {
                currentInfo.put(fieldName, previousInfo.get(fieldName));
            }
        }
    }
}
