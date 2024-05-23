/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mockit.internal.state.TestRun;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class ClassLoad {
    public static final String OBJECT = "java/lang/Object";

    private static final ClassLoader THIS_CL = ClassLoad.class.getClassLoader();
    private static final Map<String, Class<?>> LOADED_CLASSES = new ConcurrentHashMap<>();
    private static final Map<String, String> SUPER_CLASSES = new ConcurrentHashMap<>();

    private ClassLoad() {
    }

    public static void registerLoadedClass(@NonNull Class<?> aClass) {
        LOADED_CLASSES.put(aClass.getName(), aClass);
    }

    @NonNull
    public static <T> Class<T> loadByInternalName(@NonNull String internalClassName) {
        return loadClass(internalClassName.replace('/', '.'));
    }

    @NonNull
    public static <T> Class<T> loadClass(@NonNull String className) {
        @Nullable
        Class<?> loadedClass = LOADED_CLASSES.get(className);

        if (loadedClass == null) {
            try {
                loadedClass = loadClassFromAClassLoader(className);
            } catch (LinkageError e) {
                e.printStackTrace();
                throw e;
            }
        }

        // noinspection unchecked
        return (Class<T>) loadedClass;
    }

    @NonNull
    private static Class<?> loadClassFromAClassLoader(@NonNull String className) {
        Class<?> loadedClass = loadClass(null, className);

        if (loadedClass == null) {
            if (className.startsWith("mockit.")) {
                loadedClass = loadClass(THIS_CL, className);
            }

            if (loadedClass == null) {
                Class<?> testClass = TestRun.getCurrentTestClass();
                loadedClass = testClass == null ? null : loadClass(testClass.getClassLoader(), className);

                if (loadedClass == null) {
                    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
                    loadedClass = loadClass(contextCL, className);

                    if (loadedClass == null) {
                        throw new IllegalArgumentException("No class with name \"" + className + "\" found");
                    }
                }
            }
        }

        return loadedClass;
    }

    @NonNull
    public static <T> Class<T> loadClassAtStartup(@NonNull String className) {
        ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
        Class<?> loadedClass;

        try {
            loadedClass = loadClass(contextCL, className);

            if (loadedClass == null) {
                loadedClass = loadClass(THIS_CL, className);

                if (loadedClass == null) {
                    throw new IllegalArgumentException("No class with name \"" + className + "\" found");
                }
            }
        } catch (LinkageError e) {
            e.printStackTrace();
            throw e;
        }

        // noinspection unchecked
        return (Class<T>) loadedClass;
    }

    @Nullable
    public static Class<?> loadClass(@Nullable ClassLoader loader, @NonNull String className) {
        try {
            return Class.forName(className, false, loader);
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }

    @NonNull
    public static <T> Class<T> loadFromLoader(@Nullable ClassLoader loader, @NonNull String className) {
        try {
            // noinspection unchecked
            return (Class<T>) Class.forName(className, false, loader);
        } catch (ClassNotFoundException ignore) {
            throw new IllegalArgumentException("No class with name \"" + className + "\" found");
        }
    }

    @Nullable
    public static <T> Class<? extends T> searchTypeInClasspath(@NonNull String typeName) {
        return searchTypeInClasspath(typeName, false);
    }

    @Nullable
    public static <T> Class<? extends T> searchTypeInClasspath(@NonNull String typeName, boolean initializeType) {
        // noinspection OverlyBroadCatchBlock
        try {
            // noinspection unchecked
            return (Class<? extends T>) Class.forName(typeName, initializeType, THIS_CL);
        } catch (Throwable ignore) {
            return null;
        }
    }

    public static void addSuperClass(@NonNull String classInternalName, @NonNull String superClassInternalName) {
        SUPER_CLASSES.put(classInternalName.intern(), superClassInternalName.intern());
    }

    @NonNull
    public static String getSuperClass(@NonNull String classInternalName) {
        String classDesc = classInternalName.intern();
        String superName = SUPER_CLASSES.get(classDesc);

        if (superName == null) {
            Class<?> theClass = loadByInternalName(classDesc);
            Class<?> superClass = theClass.getSuperclass();

            if (superClass != null) {
                superName = superClass.getName().replace('.', '/').intern();
                SUPER_CLASSES.put(classDesc, superName);
            }
        }

        return superName == null ? OBJECT : superName;
    }

    @Nullable
    public static String whichIsSuperClass(@NonNull String internalClassName1, @NonNull String internalClassName2) {
        String class1 = actualSuperClass(internalClassName1, internalClassName2);

        if (class1 != null) {
            return class1;
        }

        return actualSuperClass(internalClassName2, internalClassName1);
    }

    @Nullable
    private static String actualSuperClass(@NonNull String candidateSuperClass, @NonNull String candidateSubclass) {
        String subclass = candidateSubclass;

        while (true) {
            String superClass = getSuperClass(subclass);

            if (superClass.equals(OBJECT)) {
                return null;
            }

            if (superClass.equals(candidateSuperClass)) {
                return candidateSuperClass;
            }

            subclass = superClass;
        }
    }

    public static boolean isClassLoaderWithNoDirectAccess(@Nullable ClassLoader classLoader) {
        return classLoader == null || classLoader != THIS_CL && classLoader.getParent() != THIS_CL;
    }

    public static ClassLoader getClassLoaderWithAccess(@NonNull Class<?> classToBeAccessed) {
        ClassLoader cl = classToBeAccessed.getClassLoader();
        return isClassLoaderWithNoDirectAccess(cl) ? THIS_CL : cl;
    }
}
