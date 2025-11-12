/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.state;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import mockit.internal.startup.Startup;

/**
 * Holds a map of internal class names to the corresponding class files (bytecode arrays), for the classes that have
 * already been loaded during the test run. These classfiles are not necessarily the same as those stored in the
 * corresponding ".class" files available from the runtime classpath. If any third-party {@link ClassFileTransformer}s
 * are active, those original classfiles may have been modified before being loaded by the JVM. JMockit installs a
 * <code>ClassFileTransformer</code> of its own which saves all potentially modified classfiles here.
 * <p>
 * This bytecode cache allows classes to be mocked and un-mocked correctly, even in the presence of other bytecode
 * modification agents such as the AspectJ load-time weaver.
 */
public final class CachedClassfiles implements ClassFileTransformer {
    @NonNull
    public static final CachedClassfiles INSTANCE = new CachedClassfiles();

    @NonNull
    private final Map<ClassLoader, Map<String, byte[]>> classLoadersAndClassfiles;
    @Nullable
    private Class<?> classBeingCached;

    private CachedClassfiles() {
        classLoadersAndClassfiles = new WeakHashMap<>(2);
    }

    @Nullable
    @Override
    public byte[] transform(@Nullable ClassLoader loader, String classDesc,
            @Nullable Class<?> classBeingRedefinedOrRetransformed, @Nullable ProtectionDomain protectionDomain,
            @NonNull byte[] classfileBuffer) {
        // can be null for Java 8 lambdas
        if (classDesc != null && classBeingRedefinedOrRetransformed != null
                && classBeingRedefinedOrRetransformed == classBeingCached) {
            addClassfile(loader, classDesc, classfileBuffer);
            classBeingCached = null;
        }

        return null;
    }

    private void addClassfile(@Nullable ClassLoader loader, @NonNull String classDesc, @NonNull byte[] classfile) {
        Map<String, byte[]> classfiles = getClassfiles(loader);
        classfiles.put(classDesc, classfile);
    }

    @NonNull
    private Map<String, byte[]> getClassfiles(@Nullable ClassLoader loader) {
        return classLoadersAndClassfiles.computeIfAbsent(loader, k -> new HashMap<>(100));
    }

    @Nullable
    private byte[] findClassfile(@NonNull Class<?> aClass) {
        String className = aClass.getName();

        // Discards an invalid numerical suffix from a synthetic Java 8 class, if detected.
        int p = className.indexOf('/');
        if (p > 0) {
            className = className.substring(0, p);
        }

        Map<String, byte[]> classfiles = getClassfiles(aClass.getClassLoader());
        return classfiles.get(className.replace('.', '/'));
    }

    @Nullable
    public static synchronized byte[] getClassfile(@NonNull String classDesc) {
        return INSTANCE.findClassfile(classDesc);
    }

    @Nullable
    private byte[] findClassfile(@NonNull String classDesc) {
        byte[] classfile = null;

        for (Map<String, byte[]> classfiles : classLoadersAndClassfiles.values()) {
            classfile = classfiles.get(classDesc);

            if (classfile != null) {
                return classfile;
            }
        }

        Class<?> desiredClass = Startup.getClassIfLoaded(classDesc);

        if (desiredClass != null) {
            classBeingCached = desiredClass;
            Startup.retransformClass(desiredClass);
            ClassLoader classLoader = desiredClass.getClassLoader();
            classfile = INSTANCE.findClassfile(classLoader, classDesc);
        }

        return classfile;
    }

    @Nullable
    private synchronized byte[] findClassfile(@Nullable ClassLoader loader, @NonNull String classDesc) {
        Map<String, byte[]> classfiles = getClassfiles(loader);
        return classfiles.get(classDesc);
    }

    @Nullable
    public static synchronized byte[] getClassfile(@NonNull Class<?> aClass) {
        byte[] cached = INSTANCE.findClassfile(aClass);
        if (cached != null) {
            return cached;
        }

        INSTANCE.classBeingCached = aClass;
        Startup.retransformClass(aClass);
        return INSTANCE.findClassfile(aClass);
    }

    @Nullable
    public static byte[] getClassfile(@Nullable ClassLoader loader, @NonNull String internalClassName) {
        return INSTANCE.findClassfile(loader, internalClassName);
    }

    public static void addClassfile(@NonNull Class<?> aClass, @NonNull byte[] classfile) {
        INSTANCE.addClassfile(aClass.getClassLoader(), aClass.getName().replace('.', '/'), classfile);
    }
}
