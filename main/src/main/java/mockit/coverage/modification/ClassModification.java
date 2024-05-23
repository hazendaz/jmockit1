/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.modification;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mockit.asm.classes.ClassReader;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ClassModification {
    @NonNull
    private final Set<String> modifiedClasses;
    @NonNull
    final List<ProtectionDomain> protectionDomainsWithUniqueLocations;
    @NonNull
    private final ClassSelection classSelection;

    public ClassModification() {
        modifiedClasses = new HashSet<>();
        protectionDomainsWithUniqueLocations = new ArrayList<>();
        classSelection = new ClassSelection();
    }

    public boolean shouldConsiderClassesNotLoaded() {
        return !classSelection.loadedOnly;
    }

    boolean isToBeConsideredForCoverage(@NonNull String className, @NonNull ProtectionDomain protectionDomain) {
        return !modifiedClasses.contains(className) && classSelection.isSelected(className, protectionDomain);
    }

    @Nullable
    public byte[] modifyClass(@NonNull String className, @NonNull ProtectionDomain protectionDomain,
            @NonNull byte[] originalClassfile) {
        if (isToBeConsideredForCoverage(className, protectionDomain)) {
            try {
                byte[] modifiedClassfile = modifyClassForCoverage(className, originalClassfile);
                registerModifiedClass(className, protectionDomain);
                return modifiedClassfile;
            } catch (VisitInterruptedException ignore) {
                // Ignore the class if the modification was refused for some reason.
            } catch (RuntimeException | AssertionError | ClassCircularityError e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @NonNull
    private static byte[] modifyClassForCoverage(@NonNull String className, @NonNull byte[] classBytecode) {
        byte[] modifiedBytecode = CoverageModifier.recoverModifiedByteCodeIfAvailable(className);

        if (modifiedBytecode != null) {
            return modifiedBytecode;
        }

        ClassReader cr = new ClassReader(classBytecode);
        CoverageModifier modifier = new CoverageModifier(cr);
        cr.accept(modifier);
        return modifier.toByteArray();
    }

    private void registerModifiedClass(@NonNull String className, @NonNull ProtectionDomain pd) {
        modifiedClasses.add(className);

        if (pd.getClassLoader() != null && pd.getCodeSource() != null && pd.getCodeSource().getLocation() != null) {
            addProtectionDomainIfHasUniqueNewPath(pd);
        }
    }

    private void addProtectionDomainIfHasUniqueNewPath(@NonNull ProtectionDomain newPD) {
        String newPath = newPD.getCodeSource().getLocation().getPath();

        for (int i = protectionDomainsWithUniqueLocations.size() - 1; i >= 0; i--) {
            ProtectionDomain previousPD = protectionDomainsWithUniqueLocations.get(i);
            String previousPath = previousPD.getCodeSource().getLocation().getPath();

            if (previousPath.startsWith(newPath)) {
                return;
            }
            if (newPath.startsWith(previousPath)) {
                protectionDomainsWithUniqueLocations.set(i, newPD);
                return;
            }
        }

        protectionDomainsWithUniqueLocations.add(newPD);
    }
}
