/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection.full;

import static java.lang.reflect.Modifier.isStatic;

import static mockit.internal.injection.InjectionPoint.JAKARTA_CONVERSATION_CLASS;
import static mockit.internal.injection.InjectionPoint.JAKARTA_INJECT_CLASS;
import static mockit.internal.injection.InjectionPoint.JAKARTA_PERSISTENCE_UNIT_CLASS;
import static mockit.internal.injection.InjectionPoint.JAKARTA_RESOURCE_CLASS;
import static mockit.internal.injection.InjectionPoint.JAKARTA_SERVLET_CLASS;
import static mockit.internal.injection.InjectionPoint.JAVAX_CONVERSATION_CLASS;
import static mockit.internal.injection.InjectionPoint.JAVAX_INJECT_CLASS;
import static mockit.internal.injection.InjectionPoint.JAVAX_PERSISTENCE_UNIT_CLASS;
import static mockit.internal.injection.InjectionPoint.JAVAX_RESOURCE_CLASS;
import static mockit.internal.injection.InjectionPoint.JAVAX_SERVLET_CLASS;
import static mockit.internal.reflection.ConstructorReflection.newInstanceUsingDefaultConstructorIfAvailable;
import static mockit.internal.util.Utilities.getClassType;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.logging.Logger;

import javax.sql.CommonDataSource;

import mockit.asm.jvmConstants.Access;
import mockit.internal.injection.InjectionPoint;
import mockit.internal.injection.InjectionProvider;
import mockit.internal.injection.InjectionState;
import mockit.internal.injection.Injector;
import mockit.internal.injection.TestedClass;
import mockit.internal.injection.TestedObjectCreation;

/**
 * Responsible for recursive injection of dependencies into a <code>@Tested(fullyInitialized = true)</code> object.
 */
public final class FullInjection {
    private static final int INVALID_TYPES = Access.ABSTRACT + Access.ANNOTATION + Access.ENUM;

    @NonNull
    private final InjectionState injectionState;

    @NonNull
    private final String testedClassName;

    @NonNull
    private final String testedName;

    @Nullable
    private final ServletJakartaDependencies servletJakartaDependencies;
    @Nullable
    private final ServletJavaxDependencies servletJavaxDependencies;

    @Nullable
    private final JPAJakartaDependencies jpaJakartaDependencies;
    @Nullable
    private final JPAJavaxDependencies jpaJavaxDependencies;

    @Nullable
    private Class<?> dependencyClass;

    @Nullable
    private InjectionProvider parentInjectionProvider;

    public FullInjection(@NonNull InjectionState injectionState, @NonNull Class<?> testedClass,
            @NonNull String testedName) {
        this.injectionState = injectionState;
        testedClassName = testedClass.getSimpleName();
        this.testedName = testedName;
        servletJakartaDependencies = JAKARTA_SERVLET_CLASS == null ? null
                : new ServletJakartaDependencies(injectionState);
        servletJavaxDependencies = JAVAX_SERVLET_CLASS == null ? null : new ServletJavaxDependencies(injectionState);
        jpaJakartaDependencies = JAKARTA_PERSISTENCE_UNIT_CLASS == null ? null
                : new JPAJakartaDependencies(injectionState);
        jpaJavaxDependencies = JAVAX_PERSISTENCE_UNIT_CLASS == null ? null : new JPAJavaxDependencies(injectionState);
    }

    @Nullable
    public Object createOrReuseInstance(@NonNull TestedClass testedClass, @NonNull Injector injector,
            @Nullable InjectionProvider injectionProvider, @Nullable String qualifiedName) {
        setInjectionProvider(injectionProvider);

        InjectionPoint injectionPoint = getInjectionPoint(testedClass, injectionProvider, qualifiedName);
        Object dependency = injectionState.getInstantiatedDependency(testedClass, injectionPoint);

        if (dependency != null) {
            return dependency;
        }

        Class<?> typeToInject = dependencyClass;

        if (typeToInject == Logger.class) {
            return createLogger(testedClass);
        }

        if (typeToInject == null || !isInstantiableType(typeToInject)) {
            return null;
        }

        return createInstance(testedClass, injector, injectionProvider, injectionPoint);
    }

    public void setInjectionProvider(@Nullable InjectionProvider injectionProvider) {
        if (injectionProvider != null) {
            injectionProvider.parent = parentInjectionProvider;
        }

        parentInjectionProvider = injectionProvider;
    }

    @NonNull
    private InjectionPoint getInjectionPoint(@NonNull TestedClass testedClass,
            @Nullable InjectionProvider injectionProvider, @Nullable String qualifiedName) {
        if (injectionProvider == null) {
            dependencyClass = testedClass.targetClass;
            return new InjectionPoint(dependencyClass, qualifiedName, true);
        }

        Type dependencyType = injectionProvider.getDeclaredType();

        if (dependencyType instanceof TypeVariable<?>) {
            dependencyType = testedClass.reflection.resolveTypeVariable((TypeVariable<?>) dependencyType);
            dependencyClass = getClassType(dependencyType);
        } else {
            dependencyClass = injectionProvider.getClassOfDeclaredType();
        }

        if (qualifiedName != null && !qualifiedName.isEmpty()) {
            return new InjectionPoint(dependencyClass, qualifiedName, true);
        }

        if (jpaJakartaDependencies != null && JPAJakartaDependencies.isApplicable(dependencyClass)) {
            for (Annotation annotation : injectionProvider.getAnnotations()) {
                InjectionPoint injectionPoint = jpaJakartaDependencies.getInjectionPointIfAvailable(annotation);

                if (injectionPoint != null) {
                    return injectionPoint;
                }
            }
        }

        if (jpaJavaxDependencies != null && JPAJavaxDependencies.isApplicable(dependencyClass)) {
            for (Annotation annotation : injectionProvider.getAnnotations()) {
                InjectionPoint injectionPoint = jpaJavaxDependencies.getInjectionPointIfAvailable(annotation);

                if (injectionPoint != null) {
                    return injectionPoint;
                }
            }
        }

        return new InjectionPoint(dependencyType, injectionProvider.getName(), false);
    }

    @NonNull
    private static Object createLogger(@NonNull TestedClass testedClass) {
        TestedClass testedClassWithLogger = testedClass.parent;
        assert testedClassWithLogger != null;
        return Logger.getLogger(testedClassWithLogger.nameOfTestedClass);
    }

    public static boolean isInstantiableType(@NonNull Class<?> type) {
        if (type.isPrimitive() || type.isArray() || type.isAnnotation()) {
            return false;
        }

        if (type.isInterface()) {
            return true;
        }

        int typeModifiers = type.getModifiers();

        if ((typeModifiers & INVALID_TYPES) != 0 || !isStatic(typeModifiers) && type.isMemberClass()) {
            return false;
        }

        return type.getClassLoader() != null;
    }

    @Nullable
    private Object createInstance(@NonNull TestedClass testedClass, @NonNull Injector injector,
            @Nullable InjectionProvider injectionProvider, @NonNull InjectionPoint injectionPoint) {
        @SuppressWarnings("ConstantConditions")
        @NonNull
        Class<?> typeToInject = dependencyClass;
        Object dependency = null;

        if (typeToInject.isInterface()) {
            dependency = createInstanceOfSupportedInterfaceIfApplicable(testedClass, typeToInject, injectionPoint,
                    injectionProvider);

            if (dependency == null && typeToInject.getClassLoader() != null) {
                Class<?> resolvedType = injectionState.resolveInterface(typeToInject);

                if (resolvedType != null && !resolvedType.isInterface()) {
                    // noinspection AssignmentToMethodParameter
                    testedClass = new TestedClass(resolvedType, resolvedType);
                    typeToInject = resolvedType;
                }
            }
        }

        if (dependency == null) {
            dependency = createAndRegisterNewInstance(typeToInject, testedClass, injector, injectionPoint,
                    injectionProvider);
        }

        return dependency;
    }

    @Nullable
    private Object createInstanceOfSupportedInterfaceIfApplicable(@NonNull TestedClass testedClass,
            @NonNull Class<?> typeToInject, @NonNull InjectionPoint injectionPoint,
            @Nullable InjectionProvider injectionProvider) {
        Object dependency = null;

        if (CommonDataSource.class.isAssignableFrom(typeToInject)) {
            dependency = createAndRegisterDataSource(testedClass, injectionPoint, injectionProvider);
        } else if (JAKARTA_INJECT_CLASS != null && typeToInject == jakarta.inject.Provider.class) {
            assert injectionProvider != null;
            dependency = createProviderJakartaInstance(injectionProvider);
        } else if (JAVAX_INJECT_CLASS != null && typeToInject == javax.inject.Provider.class) {
            assert injectionProvider != null;
            dependency = createProviderJavaxInstance(injectionProvider);
        } else if (JAKARTA_CONVERSATION_CLASS != null
                && typeToInject == jakarta.enterprise.context.Conversation.class) {
            dependency = createAndRegisterConversationJakartaInstance();
        } else if (JAVAX_CONVERSATION_CLASS != null && typeToInject == javax.enterprise.context.Conversation.class) {
            dependency = createAndRegisterConversationJavaxInstance();
        } else if (servletJakartaDependencies != null && ServletJakartaDependencies.isApplicable(typeToInject)) {
            dependency = servletJakartaDependencies.createAndRegisterDependency(typeToInject);
        } else if (servletJavaxDependencies != null && ServletJavaxDependencies.isApplicable(typeToInject)) {
            dependency = servletJavaxDependencies.createAndRegisterDependency(typeToInject);
        } else if (jpaJakartaDependencies != null && JPAJakartaDependencies.isApplicable(typeToInject)) {
            dependency = jpaJakartaDependencies.createAndRegisterDependency(typeToInject, injectionPoint,
                    injectionProvider);
        } else if (jpaJavaxDependencies != null && JPAJavaxDependencies.isApplicable(typeToInject)) {
            dependency = jpaJavaxDependencies.createAndRegisterDependency(typeToInject, injectionPoint,
                    injectionProvider);
        }

        return dependency;
    }

    @Nullable
    private Object createAndRegisterDataSource(@NonNull TestedClass testedClass, @NonNull InjectionPoint injectionPoint,
            @Nullable InjectionProvider injectionProvider) {
        if (injectionProvider == null) {
            return null;
        }

        // Check annotation is present (both jars)
        if ((JAKARTA_RESOURCE_CLASS != null && injectionProvider.hasAnnotation(jakarta.annotation.Resource.class))
                || (JAVAX_RESOURCE_CLASS != null && injectionProvider.hasAnnotation(javax.annotation.Resource.class))) {
            TestDataSource dsCreation = new TestDataSource(injectionPoint);
            CommonDataSource dataSource = dsCreation.createIfDataSourceDefinitionAvailable(testedClass);

            if (dataSource != null) {
                injectionState.saveInstantiatedDependency(injectionPoint, dataSource);
            }

            return dataSource;
        }

        return null;
    }

    @NonNull
    private Object createProviderJakartaInstance(@NonNull InjectionProvider injectionProvider) {
        ParameterizedType genericType = (ParameterizedType) injectionProvider.getDeclaredType();
        final Class<?> providedClass = (Class<?>) genericType.getActualTypeArguments()[0];

        if (providedClass.isAnnotationPresent(jakarta.inject.Singleton.class)) {
            return new jakarta.inject.Provider<Object>() {
                private Object dependency;

                @Override
                public synchronized Object get() {
                    if (dependency == null) {
                        dependency = createNewInstance(providedClass, true);
                    }

                    return dependency;
                }
            };
        }

        return (jakarta.inject.Provider<Object>) () -> createNewInstance(providedClass, false);
    }

    @NonNull
    private Object createProviderJavaxInstance(@NonNull InjectionProvider injectionProvider) {
        ParameterizedType genericType = (ParameterizedType) injectionProvider.getDeclaredType();
        final Class<?> providedClass = (Class<?>) genericType.getActualTypeArguments()[0];

        if (providedClass.isAnnotationPresent(javax.inject.Singleton.class)) {
            return new javax.inject.Provider<Object>() {
                private Object dependency;

                @Override
                public synchronized Object get() {
                    if (dependency == null) {
                        dependency = createNewInstance(providedClass, true);
                    }

                    return dependency;
                }
            };
        }

        return (javax.inject.Provider<Object>) () -> createNewInstance(providedClass, false);
    }

    @Nullable
    private Object createNewInstance(@NonNull Class<?> classToInstantiate, boolean required) {
        if (classToInstantiate.isInterface()) {
            return null;
        }

        if (classToInstantiate.getClassLoader() == null) {
            return newInstanceUsingDefaultConstructorIfAvailable(classToInstantiate);
        }

        return new TestedObjectCreation(injectionState, this, classToInstantiate).create(required, false);
    }

    @NonNull
    private Object createAndRegisterConversationJakartaInstance() {
        jakarta.enterprise.context.Conversation conversation = new TestConversationJakarta();

        InjectionPoint injectionPoint = new InjectionPoint(jakarta.enterprise.context.Conversation.class);
        injectionState.saveInstantiatedDependency(injectionPoint, conversation);
        return conversation;
    }

    @NonNull
    private Object createAndRegisterConversationJavaxInstance() {
        javax.enterprise.context.Conversation conversation = new TestConversationJavax();

        InjectionPoint injectionPoint = new InjectionPoint(javax.enterprise.context.Conversation.class);
        injectionState.saveInstantiatedDependency(injectionPoint, conversation);
        return conversation;
    }

    @Nullable
    private Object createAndRegisterNewInstance(@NonNull Class<?> typeToInstantiate, @NonNull TestedClass testedClass,
            @NonNull Injector injector, @NonNull InjectionPoint injectionPoint,
            @Nullable InjectionProvider injectionProvider) {
        Object dependency = createNewInstance(typeToInstantiate,
                injectionProvider != null && injectionProvider.isRequired());

        if (dependency != null) {
            if (injectionPoint.name == null) {
                assert injectionProvider != null;
                injectionPoint = new InjectionPoint(injectionPoint.type, injectionProvider.getName());
            }

            registerNewInstance(testedClass, injector, injectionPoint, dependency);
        }

        return dependency;
    }

    private void registerNewInstance(@NonNull TestedClass testedClass, @NonNull Injector injector,
            @NonNull InjectionPoint injectionPoint, @NonNull Object dependency) {
        injectionState.saveInstantiatedDependency(injectionPoint, dependency);

        Class<?> instantiatedClass = dependency.getClass();

        if (testedClass.isClassFromSameModuleOrSystemAsTestedClass(instantiatedClass)) {
            injector.fillOutDependenciesRecursively(dependency, testedClass);
            injectionState.lifecycleMethods.findLifecycleMethods(instantiatedClass);
            injectionState.lifecycleMethods.executeInitializationMethodsIfAny(instantiatedClass, dependency);
        }
    }

    @Override
    public String toString() {
        String description = "@Tested object \"" + testedClassName + ' ' + testedName + '"';

        if (parentInjectionProvider != null) {
            InjectionProvider injectionProvider = parentInjectionProvider.parent;

            if (injectionProvider != null) {
                description = injectionProvider + "\r\n  of " + description;
            }
        }

        return description;
    }

    public void clear() {
        parentInjectionProvider = null;
    }
}
