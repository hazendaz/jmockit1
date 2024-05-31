/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import mockit.coverage.data.CoverageData;
import mockit.coverage.modification.ClassModification;
import mockit.coverage.modification.ClassesNotLoaded;
import mockit.internal.startup.Startup;

public final class CodeCoverage implements ClassFileTransformer {
    @NonNull
    private final ClassModification classModification;

    public static void main(@NonNull String[] args) {
        OutputFileGenerator generator = createOutputFileGenerator();
        generator.generateAggregateReportFromInputFiles(args);
    }

    @NonNull
    private static OutputFileGenerator createOutputFileGenerator() {
        OutputFileGenerator generator = new OutputFileGenerator();
        CoverageData.instance().setWithCallPoints(generator.isWithCallPoints());
        return generator;
    }

    public static boolean active() {
        String coverageOutput = Configuration.getProperty("output");
        String coverageClasses = Configuration.getProperty("classes");
        return (coverageOutput != null || coverageClasses != null) && !"none".equals(coverageOutput)
                && !"none".equals(coverageClasses);
    }

    public CodeCoverage() {
        classModification = new ClassModification();
        final OutputFileGenerator outputGenerator = createOutputFileGenerator();
        final CoverageCheck coverageCheck = CoverageCheck.createIfApplicable();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                TestRun.terminate();

                if (outputGenerator.isOutputToBeGenerated()) {
                    if (classModification.shouldConsiderClassesNotLoaded()) {
                        new ClassesNotLoaded(classModification).gatherCoverageData();
                    }

                    Startup.instrumentation().removeTransformer(CodeCoverage.this);
                    outputGenerator.generate();
                } else {
                    Startup.instrumentation().removeTransformer(CodeCoverage.this);
                }

                if (coverageCheck != null) {
                    coverageCheck.verifyThresholds();
                }
            }
        });
    }

    @Nullable
    @Override
    public byte[] transform(@Nullable ClassLoader loader, @NonNull String internalClassName,
            @Nullable Class<?> classBeingRedefined, @Nullable ProtectionDomain protectionDomain,
            @NonNull byte[] originalClassfile) {
        if (loader == null || classBeingRedefined != null || protectionDomain == null) {
            return null;
        }

        String className = internalClassName.replace('/', '.');
        return classModification.modifyClass(className, protectionDomain, originalClassfile);
    }
}
