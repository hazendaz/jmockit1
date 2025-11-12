/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import static java.lang.reflect.Modifier.isPublic;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import mockit.internal.util.StackTrace;

import org.checkerframework.checker.index.qual.NonNegative;

public final class CallPoint implements Serializable {
    private static final long serialVersionUID = 362727169057343840L;
    private static final Map<StackTraceElement, Boolean> steCache = new HashMap<>();
    private static final Class<? extends Annotation> testAnnotation;
    private static final boolean checkTestAnnotationOnClass;
    private static final boolean checkIfTestCaseSubclass;

    static {
        Class<?> annotation = getJUnitAnnotationIfAvailable();
        boolean checkOnClassAlso = false;

        if (annotation == null) {
            annotation = getTestNGAnnotationIfAvailable();
            checkOnClassAlso = true;
        }

        // noinspection unchecked
        testAnnotation = (Class<? extends Annotation>) annotation;
        checkTestAnnotationOnClass = checkOnClassAlso;
        checkIfTestCaseSubclass = checkForJUnit3Availability();
    }

    @Nullable
    private static Class<?> getJUnitAnnotationIfAvailable() {
        try {
            // JUnit 5:
            return Class.forName("org.junit.jupiter.api.Test");
        } catch (ClassNotFoundException ignore) {
            // JUnit 4:
            try {
                return Class.forName("org.junit.Test");
            } catch (ClassNotFoundException ignored) {
                return null;
            }
        }
    }

    @Nullable
    private static Class<?> getTestNGAnnotationIfAvailable() {
        try {
            return Class.forName("org.testng.annotations.Test");
        } catch (ClassNotFoundException ignore) {
            // For older versions of TestNG:
            try {
                return Class.forName("org.testng.Test");
            } catch (ClassNotFoundException ignored) {
                return null;
            }
        }
    }

    private static boolean checkForJUnit3Availability() {
        try {
            Class.forName("junit.framework.TestCase");
            return true;
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }

    @NonNull
    private final StackTraceElement ste;
    @NonNegative
    private int repetitionCount;

    private CallPoint(@NonNull StackTraceElement ste) {
        this.ste = ste;
    }

    @NonNull
    public StackTraceElement getStackTraceElement() {
        return ste;
    }

    @NonNegative
    public int getRepetitionCount() {
        return repetitionCount;
    }

    public void incrementRepetitionCount() {
        repetitionCount++;
    }

    public boolean isSameTestMethod(@NonNull CallPoint other) {
        StackTraceElement thisSTE = ste;
        StackTraceElement otherSTE = other.ste;
        return thisSTE == otherSTE || thisSTE.getClassName().equals(otherSTE.getClassName())
                && thisSTE.getMethodName().equals(otherSTE.getMethodName());
    }

    public boolean isSameLineInTestCode(@NonNull CallPoint other) {
        return isSameTestMethod(other) && ste.getLineNumber() == other.ste.getLineNumber();
    }

    @Nullable
    static CallPoint create(@NonNull Throwable newThrowable) {
        StackTrace st = new StackTrace(newThrowable);
        int n = st.getDepth();

        for (int i = 2; i < n; i++) {
            StackTraceElement ste = st.getElement(i);

            if (isTestMethod(ste)) {
                return new CallPoint(ste);
            }
        }

        return null;
    }

    private static boolean isTestMethod(@NonNull StackTraceElement ste) {
        if (steCache.containsKey(ste)) {
            return steCache.get(ste);
        }

        boolean isTestMethod = false;

        if (ste.getFileName() != null && ste.getLineNumber() >= 0) {
            String className = ste.getClassName();

            if (!isClassInExcludedPackage(className)) {
                Class<?> aClass = loadClass(className);

                if (aClass != null) {
                    isTestMethod = isTestMethod(aClass, ste.getMethodName());
                }
            }
        }

        steCache.put(ste, isTestMethod);
        return isTestMethod;
    }

    private static boolean isClassInExcludedPackage(@NonNull String className) {
        return className.startsWith("jakarta.") || className.startsWith("java.") || className.startsWith("javax.")
                || className.startsWith("sun.") || className.startsWith("org.junit.")
                || className.startsWith("org.testng.") || className.startsWith("mockit.");
    }

    @Nullable
    private static Class<?> loadClass(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException | LinkageError ignore) {
            return null;
        }
    }

    private static boolean isTestMethod(@NonNull Class<?> testClass, @NonNull String methodName) {
        if (checkTestAnnotationOnClass && testClass.isAnnotationPresent(testAnnotation)) {
            return true;
        }

        Method method = findMethod(testClass, methodName);

        return method != null && (containsATestFrameworkAnnotation(method.getDeclaredAnnotations())
                || checkIfTestCaseSubclass && isJUnit3xTestMethod(testClass, method));
    }

    @Nullable
    private static Method findMethod(@NonNull Class<?> aClass, @NonNull String name) {
        try {
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.getReturnType() == void.class && name.equals(method.getName())) {
                    return method;
                }
            }
        } catch (NoClassDefFoundError ignore) {
        }

        return null;
    }

    private static boolean containsATestFrameworkAnnotation(@NonNull Annotation[] methodAnnotations) {
        for (Annotation annotation : methodAnnotations) {
            String annotationName = annotation.annotationType().getName();

            if (annotationName.startsWith("org.junit.") || annotationName.startsWith("org.testng.")) {
                return true;
            }
        }

        return false;
    }

    private static boolean isJUnit3xTestMethod(@NonNull Class<?> aClass, @NonNull Method method) {
        if (!isPublic(method.getModifiers()) || !method.getName().startsWith("test")) {
            return false;
        }

        Class<?> superClass = aClass.getSuperclass();

        while (superClass != Object.class) {
            if ("junit.framework.TestCase".equals(superClass.getName())) {
                return true;
            }

            superClass = superClass.getSuperclass();
        }

        return false;
    }
}
