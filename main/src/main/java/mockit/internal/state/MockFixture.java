/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import static java.lang.reflect.Modifier.isAbstract;

import static mockit.internal.util.GeneratedClasses.getMockedClass;
import static mockit.internal.util.GeneratedClasses.getMockedClassOrInterfaceType;
import static mockit.internal.util.GeneratedClasses.isGeneratedImplementationClass;
import static mockit.internal.util.Utilities.getClassType;

import java.lang.instrument.ClassDefinition;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mockit.internal.ClassFile;
import mockit.internal.ClassIdentification;
import mockit.internal.capturing.CaptureTransformer;
import mockit.internal.expectations.mocking.CaptureOfNewInstances;
import mockit.internal.expectations.mocking.InstanceFactory;
import mockit.internal.startup.Startup;
import mockit.internal.util.ClassLoad;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Holds data about redefined/transformed classes, with methods to add/remove and query such data.
 */
public final class MockFixture {
    /**
     * Similar to {@link #redefinedClasses}, but for classes modified by a <code>ClassFileTransformer</code> such as the
     * <code>CaptureTransformer</code>, and containing the pre-transform bytecode instead of the modified one.
     *
     * @see #addTransformedClass(ClassIdentification, byte[])
     * @see #getTransformedClasses()
     * @see #restoreTransformedClasses(Set)
     */
    @NonNull
    private final Map<ClassIdentification, byte[]> transformedClasses;

    /**
     * Real classes currently redefined in the running JVM and their current (modified) bytecodes.
     * <p>
     * The keys in the map allow each redefined real class to be later restored to a previous definition.
     * <p>
     * The modified bytecode arrays in the map allow a new redefinition to be made on top of the current redefinition
     * (in the case of the Faking API), or to restore the class to a previous definition (provided the map is copied
     * between redefinitions of the same class).
     *
     * @see #addRedefinedClass(ClassDefinition)
     * @see #getRedefinedClasses()
     * @see #getRedefinedClassfile(Class)
     * @see #containsRedefinedClass(Class)
     * @see #restoreRedefinedClasses(Map)
     */
    @NonNull
    private final Map<Class<?>, byte[]> redefinedClasses;

    /**
     * Subset of all currently redefined classes which contain one or more native methods.
     * <p/>
     * This is needed because in order to restore such methods it is necessary (for some classes) to re-register them
     * with the JVM.
     *
     * @see #reregisterNativeMethodsForRestoredClass(Class)
     */
    @NonNull
    private final Set<String> redefinedClassesWithNativeMethods;

    /**
     * Maps redefined real classes to the internal name of the corresponding fake classes, when it's the case.
     * <p>
     * This allows any global state associated to a fake class to be discarded when the corresponding real class is
     * later restored to its original definition.
     *
     * @see #addRedefinedClass(String, ClassDefinition)
     */
    @NonNull
    private final Map<Class<?>, String> realClassesToFakeClasses;

    /**
     * A list of classes that are currently mocked. Said classes are also added to {@link #mockedTypesAndInstances}.
     *
     * @see #registerMockedClass(Class)
     * @see #getMockedClasses()
     * @see #isStillMocked(Object, String)
     * @see #isInstanceOfMockedClass(Object)
     * @see #removeMockedClasses(List)
     */
    @NonNull
    private final List<Class<?>> mockedClasses;

    /**
     * A map of mocked types to their corresponding {@linkplain InstanceFactory mocked instance factories}.
     *
     * @see #registerInstanceFactoryForMockedType(Class, InstanceFactory)
     * @see #findInstanceFactory(Type)
     * @see #isStillMocked(Object, String)
     * @see #removeMockedClasses(List)
     */
    @NonNull
    private final Map<Type, InstanceFactory> mockedTypesAndInstances;

    /**
     * A list of "capturing" class file transformers, used by both the mocking and faking APIs.
     *
     * @see #addCaptureTransformer(CaptureTransformer)
     * @see #areCapturedClasses(Class, Class)
     * @see #isCaptured(Object)
     * @see #getCaptureTransformerCount()
     * @see #removeCaptureTransformers(int)
     */
    @NonNull
    private final List<CaptureTransformer<?>> captureTransformers;

    MockFixture() {
        transformedClasses = new HashMap<>(2);
        redefinedClasses = new ConcurrentHashMap<>(8);
        redefinedClassesWithNativeMethods = new HashSet<>();
        realClassesToFakeClasses = new IdentityHashMap<>(8);
        mockedClasses = new ArrayList<>();
        mockedTypesAndInstances = new IdentityHashMap<>();
        captureTransformers = new ArrayList<>();
    }

    // Methods to add/remove transformed/redefined classes /////////////////////////////////////////////////////////////

    public void addTransformedClass(@NonNull ClassIdentification classId, @NonNull byte[] pretransformClassfile) {
        transformedClasses.put(classId, pretransformClassfile);
    }

    // Methods used by both the Mocking and Faking APIs.

    public void addRedefinedClass(@NonNull ClassDefinition newClassDefinition) {
        redefinedClasses.put(newClassDefinition.getDefinitionClass(), newClassDefinition.getDefinitionClassFile());
    }

    public void registerMockedClass(@NonNull Class<?> mockedType) {
        if (!mockedClasses.contains(mockedType)) {
            mockedType = getMockedClassOrInterfaceType(mockedType);
            mockedClasses.add(mockedType);
        }
    }

    // Methods used by the Mocking API.

    public void redefineClasses(@NonNull ClassDefinition... definitions) {
        Startup.redefineMethods(definitions);

        for (ClassDefinition def : definitions) {
            addRedefinedClass(def);
        }
    }

    public void redefineMethods(@NonNull Map<Class<?>, byte[]> modifiedClassfiles) {
        ClassDefinition[] classDefs = new ClassDefinition[modifiedClassfiles.size()];
        int i = 0;

        for (Entry<Class<?>, byte[]> classAndBytecode : modifiedClassfiles.entrySet()) {
            Class<?> modifiedClass = classAndBytecode.getKey();
            byte[] modifiedClassfile = classAndBytecode.getValue();

            ClassDefinition classDef = new ClassDefinition(modifiedClass, modifiedClassfile);
            classDefs[i] = classDef;
            i++;

            addRedefinedClass(classDef);
        }

        Startup.redefineMethods(classDefs);
    }

    public boolean isStillMocked(@Nullable Object instance, @NonNull String classDesc) {
        Class<?> targetClass;

        if (instance == null) {
            targetClass = ClassLoad.loadByInternalName(classDesc);
            return isClassAssignableTo(targetClass);
        }

        targetClass = instance.getClass();
        return mockedTypesAndInstances.containsKey(targetClass) || isInstanceOfMockedClass(instance);
    }

    private boolean isClassAssignableTo(@NonNull Class<?> toClass) {
        for (Class<?> mockedClass : mockedClasses) {
            if (toClass == mockedClass || toClass.isAssignableFrom(mockedClass)) {
                return true;
            }
        }

        return false;
    }

    public boolean isInstanceOfMockedClass(@NonNull Object mockedInstance) {
        Class<?> mockedClass = getMockedClassOrInterfaceType(mockedInstance.getClass());
        Class<?> mockedSuperclass = mockedClass.getSuperclass();

        if (mockedSuperclass != null && mockedSuperclass.isEnum()) {
            return mockedClasses.contains(mockedSuperclass);
        }

        return mockedClasses.contains(mockedClass) || isCaptured(mockedClass);
    }

    public void registerInstanceFactoryForMockedType(@NonNull Class<?> mockedType,
            @NonNull InstanceFactory mockedInstanceFactory) {
        registerMockedClass(mockedType);
        mockedTypesAndInstances.put(mockedType, mockedInstanceFactory);
    }

    @Nullable
    public InstanceFactory findInstanceFactory(@NonNull Type mockedType) {
        InstanceFactory instanceFactory = mockedTypesAndInstances.get(mockedType);

        if (instanceFactory != null) {
            return instanceFactory;
        }

        Class<?> mockedClass = getClassType(mockedType);
        // noinspection ReuseOfLocalVariable
        instanceFactory = mockedTypesAndInstances.get(mockedClass);

        if (instanceFactory != null) {
            return instanceFactory;
        }

        boolean abstractType = mockedClass.isInterface() || isAbstract(mockedClass.getModifiers());

        for (Entry<Type, InstanceFactory> entry : mockedTypesAndInstances.entrySet()) {
            Type registeredMockedType = entry.getKey();
            Class<?> registeredMockedClass = getClassType(registeredMockedType);

            if (abstractType) {
                registeredMockedClass = getMockedClassOrInterfaceType(registeredMockedClass);
            }

            if (mockedClass.isAssignableFrom(registeredMockedClass)) {
                instanceFactory = entry.getValue();
                break;
            }
        }

        return instanceFactory;
    }

    // Methods used by the Faking API.

    public void addRedefinedClass(@NonNull String fakeClassInternalName, @NonNull ClassDefinition classDef) {
        @NonNull
        Class<?> redefinedClass = classDef.getDefinitionClass();
        String previousNames = realClassesToFakeClasses.put(redefinedClass, fakeClassInternalName);

        if (previousNames != null) {
            realClassesToFakeClasses.put(redefinedClass, previousNames + ' ' + fakeClassInternalName);
        }

        addRedefinedClass(classDef);
    }

    // Methods used by test save-points ////////////////////////////////////////////////////////////////////////////////

    void restoreTransformedClasses(@NonNull Set<ClassIdentification> previousTransformedClasses) {
        if (!transformedClasses.isEmpty()) {
            Set<ClassIdentification> classesToRestore;

            if (previousTransformedClasses.isEmpty()) {
                classesToRestore = transformedClasses.keySet();
            } else {
                classesToRestore = getTransformedClasses();
                classesToRestore.removeAll(previousTransformedClasses);
            }

            if (!classesToRestore.isEmpty()) {
                restoreAndRemoveTransformedClasses(classesToRestore);
            }
        }
    }

    @NonNull
    Set<ClassIdentification> getTransformedClasses() {
        return transformedClasses.isEmpty() ? Collections.emptySet() : new HashSet<>(transformedClasses.keySet());
    }

    @NonNull
    Map<Class<?>, byte[]> getRedefinedClasses() {
        return redefinedClasses.isEmpty() ? Collections.emptyMap() : new HashMap<>(redefinedClasses);
    }

    private void restoreAndRemoveTransformedClasses(@NonNull Set<ClassIdentification> classesToRestore) {
        for (ClassIdentification transformedClassId : classesToRestore) {
            byte[] definitionToRestore = transformedClasses.get(transformedClassId);
            Startup.redefineMethods(transformedClassId, definitionToRestore);
        }

        transformedClasses.keySet().removeAll(classesToRestore);
    }

    void restoreRedefinedClasses(@NonNull Map<?, byte[]> previousDefinitions) {
        if (redefinedClasses.isEmpty()) {
            return;
        }

        Iterator<Entry<Class<?>, byte[]>> itr = redefinedClasses.entrySet().iterator();

        while (itr.hasNext()) {
            Entry<Class<?>, byte[]> entry = itr.next();
            Class<?> redefinedClass = entry.getKey();
            byte[] currentDefinition = entry.getValue();
            byte[] previousDefinition = previousDefinitions.get(redefinedClass);

            if (previousDefinition == null) {
                restoreDefinition(redefinedClass);
                itr.remove();
            } else if (currentDefinition != previousDefinition) {
                Startup.redefineMethods(redefinedClass, previousDefinition);
                entry.setValue(previousDefinition);
            }
        }
    }

    private void restoreDefinition(@NonNull Class<?> redefinedClass) {
        if (!isGeneratedImplementationClass(redefinedClass)) {
            byte[] previousDefinition = ClassFile.getClassFile(redefinedClass);
            Startup.redefineMethods(redefinedClass, previousDefinition);
        }
        if (redefinedClassesWithNativeMethods.contains(redefinedClass.getName())) {
            reregisterNativeMethodsForRestoredClass(redefinedClass);
        }

        removeMockedClass(redefinedClass);
        discardStateForCorrespondingFakeClassIfAny(redefinedClass);
    }

    private void removeMockedClass(@NonNull Class<?> mockedClass) {
        mockedTypesAndInstances.remove(mockedClass);
        mockedClasses.remove(mockedClass);
    }

    private void discardStateForCorrespondingFakeClassIfAny(@NonNull Class<?> redefinedClass) {
        String mockClassesInternalNames = realClassesToFakeClasses.remove(redefinedClass);
        TestRun.getFakeStates().removeClassState(redefinedClass, mockClassesInternalNames);
    }

    void removeMockedClasses(@NonNull List<Class<?>> previousMockedClasses) {
        int currentMockedClassCount = mockedClasses.size();

        if (currentMockedClassCount > 0) {
            int previousMockedClassCount = previousMockedClasses.size();

            if (previousMockedClassCount == 0) {
                mockedClasses.clear();
                mockedTypesAndInstances.clear();
            } else if (previousMockedClassCount < currentMockedClassCount) {
                mockedClasses.retainAll(previousMockedClasses);
                mockedTypesAndInstances.keySet().retainAll(previousMockedClasses);
            }
        }
    }

    // Getter methods for the maps and collections of transformed/redefined/mocked classes /////////////////////////////

    @Nullable
    public byte[] getRedefinedClassfile(@NonNull Class<?> redefinedClass) {
        return redefinedClasses.get(redefinedClass);
    }

    public boolean containsRedefinedClass(@NonNull Class<?> redefinedClass) {
        return redefinedClasses.containsKey(redefinedClass);
    }

    @NonNull
    public List<Class<?>> getMockedClasses() {
        return mockedClasses.isEmpty() ? Collections.emptyList() : new ArrayList<>(mockedClasses);
    }

    // Methods dealing with capture transformers ///////////////////////////////////////////////////////////////////////

    public void addCaptureTransformer(@NonNull CaptureTransformer<?> transformer) {
        captureTransformers.add(transformer);
    }

    // The following methods are used by test save-points to discard currently active capture transformers.

    int getCaptureTransformerCount() {
        return captureTransformers.size();
    }

    void removeCaptureTransformers(int previousTransformerCount) {
        int currentTransformerCount = captureTransformers.size();

        for (int i = currentTransformerCount - 1; i >= previousTransformerCount; i--) {
            CaptureTransformer<?> transformer = captureTransformers.get(i);
            transformer.deactivate();
            Startup.instrumentation().removeTransformer(transformer);
            captureTransformers.remove(i);
        }
    }

    // The following methods are only used by the Mocking API.

    public boolean isCaptured(@NonNull Object mockedInstance) {
        if (!captureTransformers.isEmpty()) {
            Class<?> mockedClass = getMockedClass(mockedInstance);
            return isCaptured(mockedClass);
        }

        return false;
    }

    private boolean isCaptured(@NonNull Class<?> mockedClass) {
        for (CaptureTransformer<?> captureTransformer : captureTransformers) {
            CaptureOfNewInstances capture = captureTransformer.getCaptureOfImplementationsIfApplicable(mockedClass);

            if (capture != null) {
                return true;
            }
        }

        return false;
    }

    public boolean areCapturedClasses(@NonNull Class<?> mockedClass1, @NonNull Class<?> mockedClass2) {
        for (CaptureTransformer<?> captureTransformer : captureTransformers) {
            if (captureTransformer.areCapturedClasses(mockedClass1, mockedClass2)) {
                return true;
            }
        }

        return false;
    }

    private static void reregisterNativeMethodsForRestoredClass(@NonNull Class<?> realClass) {
        Method registerNatives = null;

        try {
            registerNatives = realClass.getDeclaredMethod("registerNatives");
        } catch (NoSuchMethodException ignore) {
            try {
                registerNatives = realClass.getDeclaredMethod("initIDs");
            } catch (NoSuchMethodException ignored) {
            } // OK
        }

        if (registerNatives != null) {
            try {
                registerNatives.setAccessible(true);
                registerNatives.invoke(null);
            } catch (IllegalAccessException ignore) {
            } // won't happen
            catch (InvocationTargetException ignore) {
            } // shouldn't happen either
        }

        // OK, although another solution will be required for this particular class if it requires
        // natives to be explicitly registered again (not all do, such as java.lang.Float).
    }

    public void addRedefinedClassWithNativeMethods(@NonNull String redefinedClassInternalName) {
        redefinedClassesWithNativeMethods.add(redefinedClassInternalName.replace('/', '.'));
    }
}
