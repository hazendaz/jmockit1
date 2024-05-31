/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.asm.metadata;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import mockit.asm.jvmConstants.Access;

import org.checkerframework.checker.index.qual.NonNegative;

public final class ClassMetadataReader extends ObjectWithAttributes {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final ConstantPoolTag[] CONSTANT_POOL_TAGS = ConstantPoolTag.values();

    enum ConstantPoolTag { // values from JVM spec Table 4.4.A
        No0, // 0
        Utf8(2), // 1 (has variable size)
        No2, // 2
        Integer(4), // 3
        Float(4), // 4
        Long(8), // 5
        Double(8), // 6
        Class(2), // 7
        String(2), // 8
        FieldRef(4), // 9
        MethodRef(4), // 10
        InterfaceMethodRef(4), // 11
        NameAndType(4), // 12
        No13, No14, MethodHandle(3), // 15, added in Java 7
        MethodType(2), // 16, added in Java 7
        ConstantDynamic(4), // 17, added in Java 11
        InvokeDynamic(4), // 18, added in Java 7
        Module(2), // 19, added in Java 9
        Package(2); // 20, added in Java 9

        @NonNegative
        final int itemSize;

        ConstantPoolTag() {
            itemSize = 0;
        }

        ConstantPoolTag(@NonNegative int itemSize) {
            this.itemSize = itemSize;
        }
    }

    public enum Attribute {
        Annotations, Parameters, Signature
    }

    @NonNull
    private final byte[] code;
    @NonNull
    private final int[] cpItemCodeIndexes;
    @Nullable
    private final EnumSet<Attribute> attributesToRead;

    /**
     * The constant pool starts at index 10 in the code array; this is the end index, which must be computed as it's not
     * stored anywhere.
     */
    @NonNegative
    private final int cpEndIndex;

    @NonNegative
    private int fieldsEndIndex;
    @NonNegative
    private int methodsEndIndex;

    @NonNegative
    public static int readVersion(@NonNull byte[] code) {
        int byte0 = (code[4] & 0xFF) << 24;
        int byte1 = (code[5] & 0xFF) << 16;
        int byte2 = (code[6] & 0xFF) << 8;
        int byte3 = code[7] & 0xFF;
        return byte0 | byte1 | byte2 | byte3;
    }

    public ClassMetadataReader(@NonNull byte[] code) {
        this(code, null);
    }

    public ClassMetadataReader(@NonNull byte[] code, @Nullable EnumSet<Attribute> attributesToRead) {
        this.code = code;
        int cpItemCount = readUnsignedShort(8);
        int[] cpTable = new int[cpItemCount];
        cpItemCodeIndexes = cpTable;
        this.attributesToRead = attributesToRead;
        cpEndIndex = findEndIndexOfConstantPoolTable(cpTable);
    }

    @NonNegative
    private int readUnsignedShort(@NonNegative int codeIndex) {
        byte[] b = code;
        int i = codeIndex;
        int byte0 = (b[i] & 0xFF) << 8;
        i++;
        int byte1 = b[i] & 0xFF;
        return byte0 | byte1;
    }

    private int readInt(@NonNegative int codeIndex) {
        byte[] b = code;
        int i = codeIndex;
        int byte0 = (b[i] & 0xFF) << 24;
        i++;
        int byte1 = (b[i] & 0xFF) << 16;
        i++;
        int byte2 = (b[i] & 0xFF) << 8;
        i++;
        int byte3 = b[i] & 0xFF;
        return byte0 | byte1 | byte2 | byte3;
    }

    @NonNegative
    private int findEndIndexOfConstantPoolTable(@NonNull int[] cpTable) {
        byte[] b = code;
        int codeIndex = 10;

        for (int cpItemIndex = 1, n = cpTable.length; cpItemIndex < n; cpItemIndex++) {
            int tagValue = b[codeIndex];
            codeIndex++;
            ConstantPoolTag tag = CONSTANT_POOL_TAGS[tagValue];

            cpTable[cpItemIndex] = codeIndex;

            int cpItemSize = tag.itemSize;

            if (tag == ConstantPoolTag.Long || tag == ConstantPoolTag.Double) {
                cpItemIndex++;
            } else if (tag == ConstantPoolTag.Utf8) {
                int stringLength = readUnsignedShort(codeIndex);
                cpItemSize += stringLength;
            }

            codeIndex += cpItemSize;
        }

        return codeIndex;
    }

    @NonNegative
    public int getVersion() {
        return readVersion(code);
    }

    @NonNegative
    public int getAccessFlags() {
        return readUnsignedShort(cpEndIndex);
    }

    @NonNull
    public String getThisClass() {
        int cpClassIndex = readUnsignedShort(cpEndIndex + 2);
        return getTypeDescription(cpClassIndex);
    }

    @NonNull
    private String getTypeDescription(@NonNegative int cpClassIndex) {
        int cpClassCodeIndex = cpItemCodeIndexes[cpClassIndex];
        int cpDescriptionIndex = readUnsignedShort(cpClassCodeIndex);
        return getString(cpDescriptionIndex);
    }

    @NonNull
    private String getString(@NonNegative int cpStringIndex) {
        int codeIndex = cpItemCodeIndexes[cpStringIndex];
        int stringLength = readUnsignedShort(codeIndex);
        return new String(code, codeIndex + 2, stringLength, UTF8);
    }

    @Nullable
    public String getSuperClass() {
        int cpClassIndex = readUnsignedShort(cpEndIndex + 4);

        if (cpClassIndex == 0) {
            return null;
        }

        return getTypeDescription(cpClassIndex);
    }

    @Nullable
    public String[] getInterfaces() {
        int codeIndex = cpEndIndex + 6;
        int interfaceCount = readUnsignedShort(codeIndex);

        if (interfaceCount == 0) {
            return null;
        }

        codeIndex += 2;

        String[] interfaces = new String[interfaceCount];

        for (int i = 0; i < interfaceCount; i++) {
            int cpInterfaceIndex = readUnsignedShort(codeIndex);
            codeIndex += 2;
            interfaces[i] = getTypeDescription(cpInterfaceIndex);
        }

        return interfaces;
    }

    private static class MemberInfo extends ObjectWithAttributes {
        @NonNegative
        public final int accessFlags;
        @NonNull
        public final String name;
        @NonNull
        public final String desc;
        @Nullable
        public String signature;

        MemberInfo(@NonNegative int accessFlags, @NonNull String name, @NonNull String desc,
                @NonNegative int attributeCount) {
            this.accessFlags = accessFlags;
            this.name = name;
            this.desc = desc;
        }

        public final boolean isStatic() {
            return (accessFlags & Access.STATIC) != 0;
        }

        public final boolean isAbstract() {
            return (accessFlags & Access.ABSTRACT) != 0;
        }

        public final boolean isSynthetic() {
            return (accessFlags & Access.SYNTHETIC) != 0;
        }
    }

    public static final class FieldInfo extends MemberInfo {
        FieldInfo(int accessFlags, @NonNull String name, @NonNull String desc, @NonNegative int attributeCount) {
            super(accessFlags, name, desc, attributeCount);
        }
    }

    @NonNull
    public List<FieldInfo> getFields() {
        int codeIndex = cpEndIndex + 6;
        int interfaceCount = readUnsignedShort(codeIndex);
        codeIndex += 2 + 2 * interfaceCount;

        int fieldCount = readUnsignedShort(codeIndex);
        codeIndex += 2;

        List<FieldInfo> fields;

        if (fieldCount == 0) {
            fields = Collections.emptyList();
        } else {
            fields = new ArrayList<>(fieldCount);

            for (int i = 0; i < fieldCount; i++) {
                int accessFlags = readUnsignedShort(codeIndex);
                codeIndex += 2;

                int cpNameIndex = readUnsignedShort(codeIndex);
                codeIndex += 2;
                String fieldName = getString(cpNameIndex);

                int cpDescIndex = readUnsignedShort(codeIndex);
                codeIndex += 2;
                String fieldDesc = getString(cpDescIndex);

                int attributeCount = readUnsignedShort(codeIndex);
                codeIndex += 2;

                FieldInfo fieldInfo = new FieldInfo(accessFlags, fieldName, fieldDesc, attributeCount);
                codeIndex = readAttributes(attributeCount, fieldInfo, codeIndex);
                fields.add(fieldInfo);
            }
        }

        fieldsEndIndex = codeIndex;
        return fields;
    }

    @NonNegative
    private int readAttributes(@NonNegative int attributeCount, @Nullable ObjectWithAttributes attributeOwner,
            @NonNegative int codeIndex) {
        EnumSet<Attribute> attributes = attributesToRead;
        boolean readAnnotations = false;

        if (attributes == null) {
            // noinspection AssignmentToMethodParameter
            attributeOwner = null;
        } else {
            readAnnotations = attributes.contains(Attribute.Annotations);
        }

        MethodInfo method = attributeOwner instanceof MethodInfo ? (MethodInfo) attributeOwner : null;

        for (int i = 0; i < attributeCount; i++) {
            int cpNameIndex = readUnsignedShort(codeIndex);
            codeIndex += 2;
            String attributeName = getString(cpNameIndex);

            int attributeLength = readInt(codeIndex);
            codeIndex += 4;

            if (attributeOwner != null) {
                if (method != null) {
                    method.readAttributes(attributeName, codeIndex);
                }

                if (readAnnotations && "RuntimeVisibleAnnotations".equals(attributeName)) {
                    attributeOwner.annotations = readAnnotations(codeIndex);
                }
            }

            codeIndex += attributeLength;
        }

        return codeIndex;
    }

    public static final class AnnotationInfo {
        @NonNull
        public final String name;

        AnnotationInfo(@NonNull String name) {
            this.name = name;
        }
    }

    @NonNull
    private List<AnnotationInfo> readAnnotations(@NonNegative int codeIndex) {
        int numAnnotations = readUnsignedShort(codeIndex);
        codeIndex += 2;

        List<AnnotationInfo> annotationInfos = new ArrayList<>(numAnnotations);

        for (int i = 0; i < numAnnotations; i++) {
            codeIndex = readAnnotation(annotationInfos, codeIndex);
        }

        return annotationInfos;
    }

    @NonNegative
    private int readAnnotation(@NonNull List<AnnotationInfo> currentAnnotations, @NonNegative int codeIndex) {
        int cpTypeIndex = readUnsignedShort(codeIndex);
        codeIndex += 2;

        String annotationTypeDesc = getString(cpTypeIndex);

        readUnsignedShort(codeIndex);
        codeIndex += 2;

        // for (int i = 0; i < numElementValuePairs; i++) {
        // int cpElementNameIndex = readUnsignedShort(codeIndex);
        // codeIndex += 2;
        //
        // int tag = code[codeIndex++];
        // // TODO: continue implementing
        // }

        AnnotationInfo annotation = new AnnotationInfo(annotationTypeDesc);
        currentAnnotations.add(annotation);

        return codeIndex;
    }

    @NonNegative
    private int getFieldsEndIndex() {
        int codeIndex = fieldsEndIndex;

        if (codeIndex == 0) {
            codeIndex = cpEndIndex + 6;
            int interfaceCount = readUnsignedShort(codeIndex);
            codeIndex += 2 + 2 * interfaceCount;

            int fieldCount = readUnsignedShort(codeIndex);
            codeIndex += 2;

            for (int i = 0; i < fieldCount; i++) {
                codeIndex += 6;

                int attributeCount = readUnsignedShort(codeIndex);
                codeIndex += 2;

                codeIndex = readAttributes(attributeCount, null, codeIndex);
            }

            fieldsEndIndex = codeIndex;
        }

        return codeIndex;
    }

    public final class MethodInfo extends MemberInfo {
        @Nullable
        public String[] parameters;

        MethodInfo(int accessFlags, @NonNull String name, @NonNull String desc, @NonNegative int attributeCount) {
            super(accessFlags, name, desc, attributeCount);
        }

        public boolean isMethod() {
            return name.charAt(0) != '<';
        }

        public boolean isConstructor() {
            return "<init>".equals(name);
        }

        void readAttributes(@NonNull String attributeName, @NonNegative int codeIndex) {
            assert attributesToRead != null;

            if ("Code".equals(attributeName)) {
                if (attributesToRead.contains(Attribute.Parameters)) {
                    readParameters(codeIndex);
                }
            } else if ("Signature".equals(attributeName) && attributesToRead.contains(Attribute.Signature)) {
                readSignature(codeIndex);
            }
        }

        private void readParameters(@NonNegative int codeIndex) {
            codeIndex += 4;

            int codeLength = readInt(codeIndex);
            codeIndex += 4 + codeLength;

            int exceptionTableLength = readUnsignedShort(codeIndex);
            codeIndex += 2 + 8 * exceptionTableLength;

            int attributeCount = readUnsignedShort(codeIndex);
            codeIndex += 2;

            readParameters(attributeCount, codeIndex);
        }

        private void readParameters(@NonNegative int attributeCount, @NonNegative int codeIndex) {
            for (int i = 0; i < attributeCount; i++) {
                int cpNameIndex = readUnsignedShort(codeIndex);
                codeIndex += 2;
                String attributeName = getString(cpNameIndex);

                int attributeLength = readInt(codeIndex);
                codeIndex += 4;

                if ("LocalVariableTable".equals(attributeName)) {
                    parameters = readParametersFromLocalVariableTable(codeIndex);
                    break;
                }

                codeIndex += attributeLength;
            }
        }

        @Nullable
        private String[] readParametersFromLocalVariableTable(@NonNegative int codeIndex) {
            int localVariableTableLength = readUnsignedShort(codeIndex);
            codeIndex += 2;

            int arraySize = getSumOfArgumentSizes(desc);

            if (arraySize == 0) {
                return null;
            }

            if (!isStatic()) {
                arraySize++;
            }

            String[] parameterNames = new String[arraySize];

            for (int i = 0; i < localVariableTableLength; i++) {
                codeIndex += 4;

                int cpLocalVarNameIndex = readUnsignedShort(codeIndex);
                codeIndex += 2;
                String localVarName = getString(cpLocalVarNameIndex);

                if ("this".equals(localVarName)) {
                    codeIndex += 4;
                    continue;
                }

                codeIndex += 2;

                int localVarIndex = readUnsignedShort(codeIndex);
                codeIndex += 2;

                if (localVarIndex < arraySize) {
                    parameterNames[localVarIndex] = localVarName;
                }
            }

            return compactArray(parameterNames);
        }

        @NonNegative
        private int getSumOfArgumentSizes(@NonNull String memberDesc) {
            int sum = 0;
            int i = 1;

            while (true) {
                char c = memberDesc.charAt(i);
                i++;

                switch (c) {
                    case ')':
                        return sum;
                    case 'L':
                        while (memberDesc.charAt(i) != ';') {
                            i++;
                        }
                        i++;
                        sum++;
                        break;
                    case '[':
                        while ((c = memberDesc.charAt(i)) == '[') {
                            i++;
                        }
                        if (isDoubleSizeType(c)) { // if the array element type is double size...
                            i++;
                            sum++; // ...then count it here, otherwise let the outer loop count it
                        }
                        break;
                    default:
                        if (isDoubleSizeType(c)) {
                            sum += 2;
                        } else {
                            sum++;
                        }
                        break;
                }
            }
        }

        private boolean isDoubleSizeType(char typeCode) {
            return typeCode == 'D' || typeCode == 'J';
        }

        @Nullable
        private String[] compactArray(@NonNull String[] arrayPossiblyWithNulls) {
            int n = arrayPossiblyWithNulls.length;
            int j = n - 1;
            int i = 0;

            while (i < j) {
                if (arrayPossiblyWithNulls[i] == null) {
                    System.arraycopy(arrayPossiblyWithNulls, i + 1, arrayPossiblyWithNulls, i, j - i);
                    arrayPossiblyWithNulls[j] = null;
                    j--;
                } else {
                    i++;
                }
            }

            return n == 1 && arrayPossiblyWithNulls[0] == null ? null : arrayPossiblyWithNulls;
        }

        private void readSignature(@NonNegative int codeIndex) {
            int cpSignatureIndex = readUnsignedShort(codeIndex);
            signature = getString(cpSignatureIndex);
        }
    }

    @NonNull
    public List<MethodInfo> getMethods() {
        int codeIndex = getFieldsEndIndex();
        int methodCount = readUnsignedShort(codeIndex);
        codeIndex += 2;

        List<MethodInfo> methods = new ArrayList<>(methodCount);

        for (int i = 0; i < methodCount; i++) {
            int accessFlags = readUnsignedShort(codeIndex);
            codeIndex += 2;

            int cpNameIndex = readUnsignedShort(codeIndex);
            codeIndex += 2;
            String methodName = getString(cpNameIndex);

            int cpDescIndex = readUnsignedShort(codeIndex);
            codeIndex += 2;
            String methodDesc = getString(cpDescIndex);

            int attributeCount = readUnsignedShort(codeIndex);
            codeIndex += 2;

            MethodInfo methodInfo = new MethodInfo(accessFlags, methodName, methodDesc, attributeCount);
            codeIndex = readAttributes(attributeCount, methodInfo, codeIndex);
            methods.add(methodInfo);
        }

        methodsEndIndex = codeIndex;
        return methods;
    }

    @NonNegative
    private int getMethodsEndIndex() {
        int codeIndex = methodsEndIndex;

        if (codeIndex == 0) {
            codeIndex = getFieldsEndIndex();

            int methodCount = readUnsignedShort(codeIndex);
            codeIndex += 2;

            for (int i = 0; i < methodCount; i++) {
                codeIndex += 6;

                int attributeCount = readUnsignedShort(codeIndex);
                codeIndex += 2;

                codeIndex = readAttributes(attributeCount, null, codeIndex);
            }

            methodsEndIndex = codeIndex;
        }

        return codeIndex;
    }

    @NonNull
    public List<AnnotationInfo> getAnnotations() {
        if (annotations == null) {
            int codeIndex = getMethodsEndIndex();
            int attributeCount = readUnsignedShort(codeIndex);
            codeIndex += 2;

            readAttributes(attributeCount, this, codeIndex);

            if (annotations == null) {
                annotations = Collections.emptyList();
            }
        }

        return annotations;
    }
}
