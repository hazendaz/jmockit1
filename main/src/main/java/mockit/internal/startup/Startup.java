/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.startup;

import static mockit.internal.startup.ClassLoadingBridgeFields.createSyntheticFieldsInJREClassToHoldClassLoadingBridges;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import mockit.internal.ClassIdentification;
import mockit.internal.expectations.transformation.ExpectationsTransformer;
import mockit.internal.state.CachedClassfiles;

/**
 * This is the "agent class" that initializes the JMockit "Java agent", provided the JVM is initialized with
 *
 * <pre>{@code -javaagent:&lt;properPathTo>/jmockit-1-x.jar }</pre>
 *
 * .
 *
 * @see #premain(String, Instrumentation)
 */
public final class Startup {
    @Nullable
    private static Instrumentation instrumentation;
    public static boolean initializing;

    private static final String UNMOCKABLE_CLASS_SUGGESTION = "This typically occurs with classes from restricted JDK"
            + " modules (such as java.net, java.nio) in JDK 9+. Consider wrapping the class in a testable abstraction"
            + " or interface that can be mocked instead. For example, define an interface for the operations you need"
            + " to test and inject it into the class under test.";

    private Startup() {
    }

    /**
     * User-specified fakes will applied at this time, if the "fakes" system property is set to the fully qualified
     * class names.
     *
     * @param agentArgs
     *            if "coverage", the coverage tool is activated
     * @param inst
     *            the instrumentation service provided by the JVM
     */
    public static void premain(@Nullable String agentArgs, @NonNull Instrumentation inst) {
        createSyntheticFieldsInJREClassToHoldClassLoadingBridges(inst);

        instrumentation = inst;
        inst.addTransformer(CachedClassfiles.INSTANCE, true);

        initializing = true;
        try {
            JMockitInitialization.initialize(inst, "coverage".equals(agentArgs));
        } finally {
            initializing = false;
        }

        inst.addTransformer(new ExpectationsTransformer());
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public static Instrumentation instrumentation() {
        return instrumentation;
    }

    public static void verifyInitialization() {
        if (instrumentation == null) {
            throw new IllegalStateException(
                    "JMockit didn't get initialized; please check the -javaagent JVM initialization parameter was used");
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void retransformClass(@NonNull Class<?> aClass) {
        try {
            instrumentation.retransformClasses(aClass);
        } catch (UnmodifiableClassException ignore) {
        }
    }

    public static void redefineMethods(@NonNull ClassIdentification classToRedefine,
            @NonNull byte[] modifiedClassfile) {
        Class<?> loadedClass = classToRedefine.getLoadedClass();
        redefineMethods(loadedClass, modifiedClassfile);
    }

    public static void redefineMethods(@NonNull Class<?> classToRedefine, @NonNull byte[] modifiedClassfile) {
        redefineMethods(new ClassDefinition(classToRedefine, modifiedClassfile));
    }

    public static void redefineMethods(@NonNull ClassDefinition... classDefs) {
        for (ClassDefinition classDef : classDefs) {
            checkClassIsModifiable(classDef.getDefinitionClass());
        }

        try {
            // noinspection ConstantConditions
            instrumentation.redefineClasses(classDefs);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e); // should never happen
        } catch (UnmodifiableClassException e) {
            // This can happen if a class is in a restricted JDK module that the JVM does not allow to be modified.
            // Provide a clear error to help the user understand what to do.
            throw new IllegalArgumentException(buildUnmockableErrorMessage(classDefs), e);
        } catch (InternalError ignore) {
            // If a class to be redefined hasn't been loaded yet, the JVM may get a NoClassDefFoundError during
            // redefinition. Unfortunately, it then throws a plain InternalError instead.
            for (ClassDefinition classDef : classDefs) {
                detectMissingDependenciesIfAny(classDef.getDefinitionClass());
            }

            // If the above didn't throw upon detecting a NoClassDefFoundError, then ignore the original error and
            // continue, in order to prevent secondary failures.
        }
    }

    private static void checkClassIsModifiable(@NonNull Class<?> classToRedefine) {
        // noinspection ConstantConditions
        if (!instrumentation.isModifiableClass(classToRedefine)) {
            throw new IllegalArgumentException("Class " + classToRedefine.getName()
                    + " cannot be mocked/faked because the JVM does not allow it to be modified. "
                    + UNMOCKABLE_CLASS_SUGGESTION);
        }
    }

    @NonNull
    private static String buildUnmockableErrorMessage(@NonNull ClassDefinition[] classDefs) {
        StringBuilder sb = new StringBuilder("The JVM prevented modification of class(es):");
        for (ClassDefinition classDef : classDefs) {
            sb.append(' ').append(classDef.getDefinitionClass().getName());
        }
        sb.append(". ").append(UNMOCKABLE_CLASS_SUGGESTION);
        return sb.toString();
    }

    private static void detectMissingDependenciesIfAny(@NonNull Class<?> mockedClass) {
        try {
            Class.forName(mockedClass.getName(), false, mockedClass.getClassLoader());
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("Unable to mock " + mockedClass + " due to a missing dependency", e);
        } catch (ClassNotFoundException ignore) {
            // Shouldn't happen since the mocked class would already have been found in the classpath.
        }
    }

    @Nullable
    public static Class<?> getClassIfLoaded(@NonNull String classDescOrName) {
        String className = classDescOrName.replace('/', '.');
        @SuppressWarnings("ConstantConditions")
        Class<?>[] loadedClasses = instrumentation.getAllLoadedClasses();

        for (Class<?> aClass : loadedClasses) {
            if (aClass.getName().equals(className)) {
                return aClass;
            }
        }

        return null;
    }
}
