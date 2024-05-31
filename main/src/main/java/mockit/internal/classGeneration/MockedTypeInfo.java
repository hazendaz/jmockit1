/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.classGeneration;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import mockit.internal.reflection.GenericTypeReflection;
import mockit.internal.util.Utilities;

public final class MockedTypeInfo {
    @NonNull
    public final GenericTypeReflection genericTypeMap;
    @NonNull
    public final String implementationSignature;

    public MockedTypeInfo(@NonNull Type mockedType) {
        Class<?> mockedClass = Utilities.getClassType(mockedType);
        genericTypeMap = new GenericTypeReflection(mockedClass, mockedType);

        String signature = getGenericClassSignature(mockedType);
        String classDesc = mockedClass.getName().replace('.', '/');
        implementationSignature = 'L' + classDesc + signature;
    }

    @NonNull
    private static String getGenericClassSignature(@NonNull Type mockedType) {
        StringBuilder signature = new StringBuilder(100);

        if (mockedType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) mockedType;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (typeArguments.length > 0) {
                signature.append('<');

                for (Type typeArg : typeArguments) {
                    if (typeArg instanceof Class<?>) {
                        Class<?> classArg = (Class<?>) typeArg;
                        signature.append('L').append(classArg.getName().replace('.', '/')).append(';');
                    } else {
                        signature.append('*');
                    }
                }

                signature.append('>');
            }
        }

        signature.append(';');
        return signature.toString();
    }
}
