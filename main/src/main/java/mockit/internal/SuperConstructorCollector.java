/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal;

import static java.lang.reflect.Modifier.PRIVATE;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

import mockit.asm.metadata.ClassMetadataReader;
import mockit.asm.metadata.ClassMetadataReader.MethodInfo;

final class SuperConstructorCollector {
    @NonNull
    static final SuperConstructorCollector INSTANCE = new SuperConstructorCollector();
    @NonNull
    private final Map<String, String> cache = new HashMap<>();

    private SuperConstructorCollector() {
    }

    @NonNull
    synchronized String findConstructor(@NonNull String classDesc, @NonNull String superClassDesc) {
        String constructorDesc = cache.get(superClassDesc);

        if (constructorDesc != null) {
            return constructorDesc;
        }

        boolean samePackage = areBothClassesInSamePackage(classDesc, superClassDesc);

        byte[] classfile = ClassFile.getClassFile(superClassDesc);
        ClassMetadataReader cmr = new ClassMetadataReader(classfile);

        for (MethodInfo methodOrConstructor : cmr.getMethods()) {
            int access = methodOrConstructor.accessFlags;

            if (access != PRIVATE && (access != 0 || samePackage) && methodOrConstructor.isConstructor()) {
                if (constructorDesc == null || constructorDesc.length() > methodOrConstructor.desc.length()) {
                    constructorDesc = methodOrConstructor.desc;
                }
            }
        }

        assert constructorDesc != null;
        cache.put(superClassDesc, constructorDesc);
        return constructorDesc;
    }

    private static boolean areBothClassesInSamePackage(@NonNull String classDesc, @NonNull String superClassDesc) {
        int p1 = classDesc.lastIndexOf('/');
        int p2 = superClassDesc.lastIndexOf('/');
        return p1 == p2 && (p1 < 0 || classDesc.substring(0, p1).equals(superClassDesc.substring(0, p2)));
    }
}
