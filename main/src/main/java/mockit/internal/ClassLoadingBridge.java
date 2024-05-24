/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationHandler;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import mockit.internal.expectations.mocking.MockedBridge;
import mockit.internal.faking.FakeBridge;
import mockit.internal.faking.FakeMethodBridge;
import mockit.internal.util.ClassLoad;
import mockit.internal.util.StackTrace;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public abstract class ClassLoadingBridge implements InvocationHandler {
    private static final Object[] EMPTY_ARGS = {};
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static boolean fieldsSet;
    public static String hostJREClassName;

    public final String id;

    /**
     * The instance is stored in a place directly accessible through the Java SE API, so that it can be recovered from
     * any class loader.
     */
    protected ClassLoadingBridge(@NonNull String id) {
        this.id = id;
    }

    protected static boolean notToBeMocked(@Nullable Object instance, @NonNull String classDesc) {
        return (instance == null && "java/lang/System".equals(classDesc)
                || instance != null && instanceOfClassThatParticipatesInClassLoading(instance.getClass()))
                && wasCalledDuringClassLoading();
    }

    public static boolean instanceOfClassThatParticipatesInClassLoading(@NonNull Class<?> aClass) {
        return aClass == File.class || aClass == URL.class || aClass == FileInputStream.class
                || aClass == Manifest.class || JarFile.class.isAssignableFrom(aClass)
                || JarEntry.class.isAssignableFrom(aClass) || Vector.class.isAssignableFrom(aClass)
                || Hashtable.class.isAssignableFrom(aClass);
    }

    private static boolean wasCalledDuringClassLoading() {
        if (LOCK.isHeldByCurrentThread()) {
            return true;
        }

        LOCK.lock();

        try {
            StackTrace st = new StackTrace(new Throwable());
            int n = st.getDepth();

            for (int i = 3; i < n; i++) {
                StackTraceElement ste = st.getElement(i);

                if ("ClassLoader.java".equals(ste.getFileName())
                        && "loadClass getResource loadLibrary".contains(ste.getMethodName())) {
                    return true;
                }
            }

            return false;
        } finally {
            LOCK.unlock();
        }
    }

    @NonNull
    protected static Object[] extractArguments(@NonNegative int startingIndex, @NonNull Object[] args) {
        if (args.length > startingIndex) {
            Object[] targetMemberArgs = new Object[args.length - startingIndex];
            System.arraycopy(args, startingIndex, targetMemberArgs, 0, targetMemberArgs.length);
            return targetMemberArgs;
        }

        return EMPTY_ARGS;
    }

    @NonNull
    static String getHostClassName() {
        if (!fieldsSet) {
            setBridgeFields();
            fieldsSet = true;
        }

        return hostJREClassName;
    }

    private static void setBridgeFields() {
        Class<?> hostClass = ClassLoad.loadByInternalName(hostJREClassName);
        setBridgeField(hostClass, MockedBridge.MB);
        setBridgeField(hostClass, FakeBridge.MB);
        setBridgeField(hostClass, FakeMethodBridge.MB);
    }

    private static void setBridgeField(@NonNull Class<?> hostClass, @NonNull ClassLoadingBridge bridge) {
        try {
            hostClass.getDeclaredField(bridge.id).set(null, bridge);
        } catch (NoSuchFieldException ignore) {
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
