/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.modification;

import static java.util.regex.Pattern.compile;

import static mockit.internal.util.GeneratedClasses.isExternallyGeneratedSubclass;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mockit.coverage.Configuration;

final class ClassSelection {
    private static final String THIS_CLASS_NAME = ClassSelection.class.getName();
    private static final ClassLoader THIS_CLASS_LOADER = ClassSelection.class.getClassLoader();
    private static final Pattern CSV = compile(",");
    private static final Pattern DOT = compile("\\.");
    private static final Pattern STAR = compile("\\*");
    private static final Pattern TEST_CLASS_NAME = compile(".+Test(\\$.+)?");

    boolean loadedOnly;
    @Nullable
    private Matcher classesToInclude;
    @Nullable
    private Matcher classesToExclude;
    @NonNull
    private final Matcher testCode;
    private boolean configurationRead;

    ClassSelection() {
        testCode = TEST_CLASS_NAME.matcher("");
    }

    @Nullable
    private static Matcher newMatcherForClassSelection(@NonNull String specification) {
        if (specification.isEmpty()) {
            return null;
        }

        String[] specs = CSV.split(specification);
        StringBuilder finalRegexBuilder = new StringBuilder();
        String sep = "";

        for (String spec : specs) {
            String regex = null;

            if (spec.indexOf('\\') >= 0) {
                regex = spec;
            } else if (!spec.isEmpty()) {
                regex = DOT.matcher(spec).replaceAll("\\.");
                regex = STAR.matcher(regex).replaceAll(".*");
                regex = regex.replace('?', '.');
            }

            if (regex != null) {
                finalRegexBuilder.append(sep).append(regex);
                sep = "|";
            }
        }

        String finalRegex = finalRegexBuilder.toString();
        return finalRegex.isEmpty() ? null : compile(finalRegex).matcher("");
    }

    boolean isSelected(@NonNull String className, @NonNull ProtectionDomain protectionDomain) {
        CodeSource codeSource = protectionDomain.getCodeSource();

        if (codeSource == null || isIneligibleForSelection(className)
                || !canAccessJMockitFromClassToBeMeasured(protectionDomain)) {
            return false;
        }

        URL location = findLocationInCodeSource(className, protectionDomain);

        if (location == null) {
            return false;
        }

        if (!configurationRead) {
            readConfiguration();
        }

        if (isClassExcludedFromCoverage(className)) {
            return false;
        }

        if (classesToInclude != null) {
            return classesToInclude.reset(className).matches();
        }

        return !isClassFromExternalLibrary(location);
    }

    private static boolean isIneligibleForSelection(@NonNull String className) {
        return className.charAt(0) == '[' || className.startsWith("mockit.") || className.startsWith("org.hamcrest.")
                || className.startsWith("org.junit.") || className.startsWith("junit.")
                || className.startsWith("org.testng.") || className.startsWith("org.apache.maven.surefire.")
                || isExternallyGeneratedSubclass(className);
    }

    private static boolean canAccessJMockitFromClassToBeMeasured(@NonNull ProtectionDomain protectionDomain) {
        ClassLoader loaderOfClassToBeMeasured = protectionDomain.getClassLoader();

        if (loaderOfClassToBeMeasured != null) {
            try {
                Class<?> thisClass = loaderOfClassToBeMeasured.loadClass(THIS_CLASS_NAME);
                return thisClass == ClassSelection.class;
            } catch (ClassNotFoundException ignore) {
            }
        }

        return false;
    }

    @Nullable
    private static URL findLocationInCodeSource(@NonNull String className, @NonNull ProtectionDomain protectionDomain) {
        URL location = protectionDomain.getCodeSource().getLocation();

        if (location == null) {
            if (protectionDomain.getClassLoader() == THIS_CLASS_LOADER) {
                return null; // it's likely a dynamically generated class
            }

            // It's from a custom class loader, so it may exist in the classpath.
            String classFileName = className.replace('.', '/') + ".class";
            location = THIS_CLASS_LOADER.getResource(classFileName);
        }

        return location;
    }

    private boolean isClassExcludedFromCoverage(@NonNull String className) {
        return classesToExclude != null && classesToExclude.reset(className).matches()
                || testCode.reset(className).matches();
    }

    private static boolean isClassFromExternalLibrary(@NonNull URL location) {
        if ("jar".equals(location.getProtocol())) {
            return true;
        }

        String path = location.getPath();
        return path.endsWith(".jar") || path.endsWith("/.cp/") || path.endsWith("/test-classes/");
    }

    private void readConfiguration() {
        String classes = Configuration.getProperty("classes", "");
        loadedOnly = "loaded".equals(classes);
        classesToInclude = loadedOnly ? null : newMatcherForClassSelection(classes);

        String excludes = Configuration.getProperty("excludes", "");
        classesToExclude = newMatcherForClassSelection(excludes);

        configurationRead = true;
    }
}
