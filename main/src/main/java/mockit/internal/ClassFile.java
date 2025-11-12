/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mockit.asm.classes.ClassReader;
import mockit.internal.state.CachedClassfiles;
import mockit.internal.state.TestRun;

public final class ClassFile {
    private static final Map<String, ClassReader> CLASS_FILES = new ConcurrentHashMap<>();

    private ClassFile() {
    }

    public static final class NotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private NotFoundException(@NonNull String classNameOrDesc) {
            super("Unable to find class file for " + classNameOrDesc.replace('/', '.'));
        }
    }

    private static void verifyClassFileFound(@Nullable InputStream classFile, @NonNull String classNameOrDesc) {
        if (classFile == null) {
            throw new NotFoundException(classNameOrDesc);
        }
    }

    @Nullable
    public static ClassReader createClassReader(@NonNull ClassLoader cl, @NonNull String internalClassName) {
        String classFileName = internalClassName + ".class";
        InputStream classFile = cl.getResourceAsStream(classFileName);

        if (classFile != null) { // ignore the class if the ".class" file wasn't located
            try {
                byte[] bytecode = readClass(classFile);
                return new ClassReader(bytecode);
            } catch (IOException ignore) {
            }
        }

        return null;
    }

    @NonNull
    private static byte[] readClass(@NonNull InputStream is) throws IOException {
        try {
            byte[] bytecode = new byte[is.available()];
            int len = 0;

            while (true) {
                int n = is.read(bytecode, len, bytecode.length - len);

                if (n == -1) {
                    if (len < bytecode.length) {
                        byte[] truncatedCopy = new byte[len];
                        System.arraycopy(bytecode, 0, truncatedCopy, 0, len);
                        bytecode = truncatedCopy;
                    }

                    return bytecode;
                }

                len += n;

                if (len == bytecode.length) {
                    int last = is.read();

                    if (last < 0) {
                        return bytecode;
                    }

                    byte[] lengthenedCopy = new byte[bytecode.length + 1000];
                    System.arraycopy(bytecode, 0, lengthenedCopy, 0, len);
                    // noinspection NumericCastThatLosesPrecision
                    lengthenedCopy[len] = (byte) last;
                    len++;
                    bytecode = lengthenedCopy;
                }
            }
        } finally {
            is.close();
        }
    }

    @NonNull
    public static ClassReader createReaderOrGetFromCache(@NonNull Class<?> aClass) {
        byte[] cachedClassfile = CachedClassfiles.getClassfile(aClass);

        if (cachedClassfile != null) {
            return new ClassReader(cachedClassfile);
        }

        String classDesc = aClass.getName().replace('.', '/');
        ClassReader reader = CLASS_FILES.get(classDesc);

        if (reader == null) {
            reader = readFromFileSavingInCache(classDesc);
        }

        return reader;
    }

    @NonNull
    private static ClassReader readFromFileSavingInCache(@NonNull String classDesc) {
        byte[] classfileBytes = readBytesFromClassFile(classDesc);
        ClassReader cr = new ClassReader(classfileBytes);
        CLASS_FILES.put(classDesc, cr);
        return cr;
    }

    @NonNull
    public static ClassReader createReaderFromLastRedefinitionIfAny(@NonNull Class<?> aClass) {
        byte[] classfile = TestRun.mockFixture().getRedefinedClassfile(aClass);

        if (classfile == null) {
            classfile = CachedClassfiles.getClassfile(aClass);
        }

        if (classfile != null) {
            return new ClassReader(classfile);
        }

        String classDesc = aClass.getName().replace('.', '/');
        return readFromFileSavingInCache(classDesc);
    }

    @NonNull
    public static byte[] getClassFile(@NonNull String internalClassName) {
        byte[] classfileBytes = CachedClassfiles.getClassfile(internalClassName);

        if (classfileBytes == null) {
            classfileBytes = readBytesFromClassFile(internalClassName);
        }

        return classfileBytes;
    }

    @NonNull
    public static byte[] getClassFile(@Nullable ClassLoader loader, @NonNull String internalClassName) {
        byte[] classfileBytes = CachedClassfiles.getClassfile(loader, internalClassName);

        if (classfileBytes == null) {
            classfileBytes = readBytesFromClassFile(internalClassName);
        }

        return classfileBytes;
    }

    @NonNull
    public static byte[] getClassFile(@NonNull Class<?> aClass) {
        byte[] classfileBytes = CachedClassfiles.getClassfile(aClass);

        if (classfileBytes == null) {
            classfileBytes = readBytesFromClassFile(aClass);
        }

        return classfileBytes;
    }

    @NonNull
    public static byte[] readBytesFromClassFile(@NonNull String classDesc) {
        if (classDesc.startsWith("java/") || classDesc.startsWith("javax/") || classDesc.startsWith("jakarta/")) {
            byte[] classfile = CachedClassfiles.getClassfile(classDesc);

            if (classfile != null) {
                return classfile;
            }
        }

        InputStream classFile = readClassFromClasspath(classDesc);

        try {
            return readClass(classFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read class file for " + classDesc.replace('/', '.'), e);
        }
    }

    @NonNull
    public static byte[] readBytesFromClassFile(@NonNull Class<?> aClass) {
        String classDesc = aClass.getName().replace('.', '/');
        return readBytesFromClassFile(classDesc);
    }

    @NonNull
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    private static InputStream readClassFromClasspath(@NonNull String classDesc) {
        String classFileName = classDesc + ".class";
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = null;

        if (contextClassLoader != null) {
            inputStream = contextClassLoader.getResourceAsStream(classFileName);
        }

        if (inputStream == null) {
            ClassLoader thisClassLoader = ClassFile.class.getClassLoader();

            if (thisClassLoader != contextClassLoader) {
                inputStream = thisClassLoader.getResourceAsStream(classFileName);

                if (inputStream == null) {
                    Class<?> testClass = TestRun.getCurrentTestClass();

                    if (testClass != null) {
                        inputStream = testClass.getClassLoader().getResourceAsStream(classFileName);
                    }
                }
            }
        }

        verifyClassFileFound(inputStream, classDesc);
        return inputStream;
    }
}
