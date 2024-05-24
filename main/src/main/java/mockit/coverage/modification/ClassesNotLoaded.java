/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.modification;

import java.io.File;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Finds and loads all classes that should also be measured, but were not loaded until now.
 */
public final class ClassesNotLoaded {
    @NonNull
    private final ClassModification classModification;
    @NonNegative
    private int firstPosAfterParentDir;

    public ClassesNotLoaded(@NonNull ClassModification classModification) {
        this.classModification = classModification;
    }

    public void gatherCoverageData() {
        Set<ProtectionDomain> protectionDomainsSoFar = new HashSet<>(
                classModification.protectionDomainsWithUniqueLocations);

        for (ProtectionDomain pd : protectionDomainsSoFar) {
            File classPathEntry = new File(pd.getCodeSource().getLocation().getPath());

            if (!classPathEntry.getPath().endsWith(".jar")) {
                firstPosAfterParentDir = classPathEntry.getPath().length() + 1;
                loadAdditionalClasses(classPathEntry, pd);
            }
        }
    }

    private void loadAdditionalClasses(@NonNull File classPathEntry, @NonNull ProtectionDomain protectionDomain) {
        File[] filesInDir = classPathEntry.listFiles();

        if (filesInDir != null) {
            for (File fileInDir : filesInDir) {
                if (fileInDir.isDirectory()) {
                    loadAdditionalClasses(fileInDir, protectionDomain);
                } else {
                    loadAdditionalClass(fileInDir.getPath(), protectionDomain);
                }
            }
        }
    }

    private void loadAdditionalClass(@NonNull String filePath, @NonNull ProtectionDomain protectionDomain) {
        int p = filePath.lastIndexOf(".class");

        if (p > 0) {
            String relativePath = filePath.substring(firstPosAfterParentDir, p);
            String className = relativePath.replace(File.separatorChar, '.');

            if (classModification.isToBeConsideredForCoverage(className, protectionDomain)) {
                loadClass(className, protectionDomain);
            }
        }
    }

    private static void loadClass(@NonNull String className, @NonNull ProtectionDomain protectionDomain) {
        try {
            Class.forName(className, false, protectionDomain.getClassLoader());
        } catch (ClassNotFoundException | NoClassDefFoundError ignore) {
        }
    }
}
