/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import static java.util.Arrays.asList;

import static mockit.internal.util.ClassLoad.loadClassAtStartup;
import static mockit.internal.util.ClassLoad.searchTypeInClasspath;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.instrument.Instrumentation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mockit.MockUp;
import mockit.coverage.CodeCoverage;
import mockit.integration.junit4.FakeFrameworkMethod;
import mockit.integration.junit4.FakeRunNotifier;
import mockit.internal.reflection.ConstructorReflection;
import mockit.internal.util.StackTrace;

final class JMockitInitialization {
    private JMockitInitialization() {
    }

    static void initialize(@NonNull Instrumentation inst, boolean activateCoverage) {
        if (activateCoverage || CodeCoverage.active()) {
            inst.addTransformer(new CodeCoverage());
        }

        applyInternalStartupFakesAsNeeded();
        applyUserSpecifiedStartupFakesIfAny();
    }

    private static void applyInternalStartupFakesAsNeeded() {
        if (searchTypeInClasspath("org.junit.runners.model.FrameworkMethod", true) != null
                || searchTypeInClasspath("org.junit.vintage.engine.VintageTestEngine", true) != null) {
            new FakeRunNotifier();
            new FakeFrameworkMethod();
        }

        if (searchTypeInClasspath("org.junit.jupiter.api.extension.Extension", true) != null) {
            System.setProperty("junit.jupiter.extensions.autodetection.enabled", "true");
        }
    }

    private static void applyUserSpecifiedStartupFakesIfAny() {
        Collection<String> fakeClasses = getFakeClasses();

        for (String fakeClassName : fakeClasses) {
            applyStartupFake(fakeClassName);
        }
    }

    @NonNull
    private static Collection<String> getFakeClasses() {
        String commaOrSpaceSeparatedValues = System.getProperty("fakes");

        if (commaOrSpaceSeparatedValues == null) {
            return Collections.emptyList();
        }

        // noinspection DynamicRegexReplaceableByCompiledPattern
        String[] fakeClassNames = commaOrSpaceSeparatedValues.split("\\s*,\\s*|\\s+");
        Set<String> uniqueClassNames = new HashSet<>(asList(fakeClassNames));
        uniqueClassNames.remove("");
        return uniqueClassNames;
    }

    private static void applyStartupFake(@NonNull String fakeClassName) {
        String argument = null;
        int p = fakeClassName.indexOf('=');

        if (p > 0) {
            argument = fakeClassName.substring(p + 1);
            fakeClassName = fakeClassName.substring(0, p);
        }

        try {
            Class<?> fakeClass = loadClassAtStartup(fakeClassName);

            if (MockUp.class.isAssignableFrom(fakeClass)) {
                if (argument == null) {
                    ConstructorReflection.newInstanceUsingDefaultConstructor(fakeClass);
                } else {
                    ConstructorReflection.newInstanceUsingCompatibleConstructor(fakeClass, argument);
                }
            }
        } catch (UnsupportedOperationException ignored) {
        } catch (Throwable unexpectedFailure) {
            StackTrace.filterStackTrace(unexpectedFailure);
            unexpectedFailure.printStackTrace();
        }
    }
}
