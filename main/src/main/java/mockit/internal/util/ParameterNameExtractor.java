/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.util.EnumSet;
import java.util.List;

import mockit.asm.metadata.ClassMetadataReader;
import mockit.asm.metadata.ClassMetadataReader.Attribute;
import mockit.asm.metadata.ClassMetadataReader.MethodInfo;
import mockit.internal.ClassFile;
import mockit.internal.state.ParameterNames;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class ParameterNameExtractor {
    private static final EnumSet<Attribute> PARAMETERS = EnumSet.of(Attribute.Parameters);

    private ParameterNameExtractor() {
    }

    @NonNull
    public static String extractNames(@NonNull Class<?> classOfInterest) {
        String className = classOfInterest.getName();
        String classDesc = className.replace('.', '/');

        if (!ParameterNames.hasNamesForClass(classDesc)) {
            byte[] classfile = ClassFile.readBytesFromClassFile(classDesc);
            ClassMetadataReader cmr = new ClassMetadataReader(classfile, PARAMETERS);
            List<MethodInfo> methods = cmr.getMethods();

            for (MethodInfo method : methods) {
                if (!method.isSynthetic()) {
                    String[] parameters = method.parameters;

                    if (parameters != null) {
                        ParameterNames.register(classDesc, method.name, method.desc, parameters);
                    }
                }
            }
        }

        return classDesc;
    }
}
