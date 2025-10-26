/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import static java.lang.Character.toUpperCase;

import static mockit.internal.reflection.AnnotationReflection.readAnnotationAttribute;
import static mockit.internal.reflection.AnnotationReflection.readAnnotationAttributeIfAvailable;
import static mockit.internal.reflection.MethodReflection.invokePublicIfAvailable;
import static mockit.internal.reflection.ParameterReflection.NO_PARAMETERS;
import static mockit.internal.util.ClassLoad.searchTypeInClasspath;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class InjectionPoint {
    public enum KindOfInjectionPoint {
        NotAnnotated, Required, Optional
    }

    @Nullable
    public static final Class<?> JAKARTA_CONVERSATION_CLASS;
    @Nullable
    public static final Class<?> JAVAX_CONVERSATION_CLASS;

    @Nullable
    private static final Class<? extends Annotation> JAKARTA_EJB_CLASS;
    @Nullable
    private static final Class<? extends Annotation> JAVAX_EJB_CLASS;

    @Nullable
    public static final Class<? extends Annotation> JAKARTA_INJECT_CLASS;
    @Nullable
    public static final Class<? extends Annotation> JAVAX_INJECT_CLASS;

    @Nullable
    private static final Class<? extends Annotation> JAKARTA_INSTANCE_CLASS;
    @Nullable
    private static final Class<? extends Annotation> JAVAX_INSTANCE_CLASS;

    @Nullable
    public static final Class<? extends Annotation> JAKARTA_PERSISTENCE_UNIT_CLASS;
    @Nullable
    public static final Class<? extends Annotation> JAVAX_PERSISTENCE_UNIT_CLASS;

    @Nullable
    public static final Class<? extends Annotation> JAKARTA_POST_CONSTRUCT_CLASS;
    @Nullable
    public static final Class<? extends Annotation> JAVAX_POST_CONSTRUCT_CLASS;

    @Nullable
    public static final Class<?> JAKARTA_RESOURCE_CLASS;
    @Nullable
    public static final Class<?> JAVAX_RESOURCE_CLASS;

    @Nullable
    public static final Class<?> JAKARTA_SERVLET_CLASS;
    @Nullable
    public static final Class<?> JAVAX_SERVLET_CLASS;

    static {
        JAKARTA_CONVERSATION_CLASS = searchTypeInClasspath("jakarta.enterprise.context.Conversation");
        JAVAX_CONVERSATION_CLASS = searchTypeInClasspath("javax.enterprise.context.Conversation");

        JAKARTA_EJB_CLASS = searchTypeInClasspath("jakarta.ejb.EJB");
        JAVAX_EJB_CLASS = searchTypeInClasspath("javax.ejb.EJB");

        JAKARTA_INJECT_CLASS = searchTypeInClasspath("jakarta.inject.Inject");
        JAVAX_INJECT_CLASS = searchTypeInClasspath("javax.inject.Inject");

        JAKARTA_INSTANCE_CLASS = searchTypeInClasspath("jakarta.enterprise.inject.Instance");
        JAVAX_INSTANCE_CLASS = searchTypeInClasspath("javax.enterprise.inject.Instance");

        JAKARTA_POST_CONSTRUCT_CLASS = searchTypeInClasspath("jakarta.annotation.PostConstruct");
        JAVAX_POST_CONSTRUCT_CLASS = searchTypeInClasspath("javax.annotation.PostConstruct");

        JAKARTA_RESOURCE_CLASS = searchTypeInClasspath("jakarta.annotation.Resource");
        JAVAX_RESOURCE_CLASS = searchTypeInClasspath("javax.annotation.Resource");

        JAKARTA_SERVLET_CLASS = searchTypeInClasspath("jakarta.servlet.Servlet");
        JAVAX_SERVLET_CLASS = searchTypeInClasspath("javax.servlet.Servlet");

        Class<? extends Annotation> entity = searchTypeInClasspath("jakarta.persistence.Entity");

        if (entity == null) {
            JAKARTA_PERSISTENCE_UNIT_CLASS = null;
        } else {
            JAKARTA_PERSISTENCE_UNIT_CLASS = searchTypeInClasspath("jakarta.persistence.PersistenceUnit");
        }

        entity = searchTypeInClasspath("javax.persistence.Entity");

        if (entity == null) {
            JAVAX_PERSISTENCE_UNIT_CLASS = null;
        } else {
            JAVAX_PERSISTENCE_UNIT_CLASS = searchTypeInClasspath("javax.persistence.PersistenceUnit");
        }
    }

    @NonNull
    public final Type type;

    @Nullable
    public final String name;

    @Nullable
    private final String normalizedName;

    public final boolean qualified;

    public InjectionPoint(@NonNull Type type) {
        this(type, null, false);
    }

    public InjectionPoint(@NonNull Type type, @Nullable String name) {
        this(type, name, false);
    }

    public InjectionPoint(@NonNull Type type, @Nullable String name, boolean qualified) {
        this.type = type;
        this.name = name;
        normalizedName = name == null ? null : convertToLegalJavaIdentifierIfNeeded(name);
        this.qualified = qualified;
    }

    public InjectionPoint(@NonNull Type type, @NonNull String name, @Nullable String qualifiedName) {
        this.type = type;
        this.name = qualifiedName == null ? name : qualifiedName;
        normalizedName = this.name;
        qualified = qualifiedName != null;
    }

    @NonNull
    public static String convertToLegalJavaIdentifierIfNeeded(@NonNull String name) {
        if (name.indexOf('-') < 0 && name.indexOf('.') < 0) {
            return name;
        }

        StringBuilder identifier = new StringBuilder(name);

        for (int i = name.length() - 1; i >= 0; i--) {
            char c = identifier.charAt(i);

            if (c == '-' || c == '.') {
                identifier.deleteCharAt(i);
                char d = identifier.charAt(i);
                identifier.setCharAt(i, toUpperCase(d));
            }
        }

        return identifier.toString();
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        InjectionPoint otherIP = (InjectionPoint) other;

        if (type instanceof TypeVariable<?> || otherIP.type instanceof TypeVariable<?>) {
            return false;
        }

        String thisName = normalizedName;

        return type.equals(otherIP.type) && (thisName == null || thisName.equals(otherIP.normalizedName));
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + (normalizedName == null ? 0 : normalizedName.hashCode());
    }

    boolean hasSameName(InjectionPoint otherIP) {
        String thisName = normalizedName;
        return thisName != null && thisName.equals(otherIP.normalizedName);
    }

    static boolean isJakartaServlet(@NonNull Class<?> aClass) {
        return JAKARTA_SERVLET_CLASS != null && jakarta.servlet.Servlet.class.isAssignableFrom(aClass);
    }

    static boolean isJavaxServlet(@NonNull Class<?> aClass) {
        return JAVAX_SERVLET_CLASS != null && javax.servlet.Servlet.class.isAssignableFrom(aClass);
    }

    @NonNull
    public static Object wrapInProviderIfNeeded(@NonNull Type type, @NonNull final Object value) {
        if (!(type instanceof ParameterizedType)) {
            return value;
        }

        Type rawType = ((ParameterizedType) type).getRawType();

        if (JAKARTA_INJECT_CLASS != null && rawType == jakarta.inject.Provider.class
                && !(value instanceof jakarta.inject.Provider)) {
            return (jakarta.inject.Provider<Object>) () -> value;
        }

        if (JAKARTA_INSTANCE_CLASS != null && rawType == jakarta.enterprise.inject.Instance.class
                && !(value instanceof jakarta.enterprise.inject.Instance)) {
            @SuppressWarnings("unchecked")
            List<Object> values = (List<Object>) value;
            return new ListedJakarta(values);
        }

        if (JAKARTA_INJECT_CLASS != null && rawType == javax.inject.Provider.class
                && !(value instanceof javax.inject.Provider)) {
            return (javax.inject.Provider<Object>) () -> value;
        }
        if (JAVAX_INSTANCE_CLASS != null && rawType == javax.enterprise.inject.Instance.class
                && !(value instanceof javax.enterprise.inject.Instance)) {
            @SuppressWarnings("unchecked")
            List<Object> values = (List<Object>) value;
            return new ListedJavax(values);
        }

        return value;
    }

    private static final class ListedJakarta implements jakarta.enterprise.inject.Instance<Object> {
        @NonNull
        private final List<Object> instances;

        ListedJakarta(@NonNull List<Object> instances) {
            this.instances = instances;
        }

        @Override
        public jakarta.enterprise.inject.Instance<Object> select(Annotation... annotations) {
            return null;
        }

        @Override
        public <U> jakarta.enterprise.inject.Instance<U> select(Class<U> uClass, Annotation... annotations) {
            return null;
        }

        @Override
        public <U> jakarta.enterprise.inject.Instance<U> select(jakarta.enterprise.util.TypeLiteral<U> tl,
                Annotation... annotations) {
            return null;
        }

        @Override
        public boolean isUnsatisfied() {
            return false;
        }

        @Override
        public boolean isAmbiguous() {
            return false;
        }

        @Override
        public void destroy(Object instance) {
        }

        @Override
        public Iterator<Object> iterator() {
            return instances.iterator();
        }

        @Override
        public Object get() {
            throw new RuntimeException("Unexpected");
        }

        @Override
        public Iterable<? extends jakarta.enterprise.inject.Instance.Handle<Object>> handles() {
            class SimpleHandle implements jakarta.enterprise.inject.Instance.Handle<Object> {
                private final Object instance;

                SimpleHandle(Object instance) {
                    this.instance = instance;
                }

                @Override
                public Object get() {
                    return instance;
                }

                @Override
                public void destroy() {
                    // No-op
                }

                @Override
                public void close() {
                    // No-op
                }

                @Override
                public jakarta.enterprise.inject.spi.Bean<Object> getBean() {
                    return null;
                }
            }
            List<SimpleHandle> handleList = new ArrayList<>();
            for (Object obj : instances) {
                handleList.add(new SimpleHandle(obj));
            }
            return handleList;
        }

        @Override
        public jakarta.enterprise.inject.Instance.Handle<Object> getHandle() {
            if (instances.isEmpty()) {
                throw new RuntimeException("No instance available");
            }
            return new jakarta.enterprise.inject.Instance.Handle<Object>() {
                private final Object instance = instances.get(0);

                @Override
                public Object get() {
                    return instance;
                }

                @Override
                public void destroy() {
                    // No-op
                }

                @Override
                public void close() {
                    // No-op
                }

                @Override
                public jakarta.enterprise.inject.spi.Bean<Object> getBean() {
                    return null;
                }
            };
        }
    }

    private static final class ListedJavax implements javax.enterprise.inject.Instance<Object> {
        @NonNull
        private final List<Object> instances;

        ListedJavax(@NonNull List<Object> instances) {
            this.instances = instances;
        }

        @Override
        public javax.enterprise.inject.Instance<Object> select(Annotation... annotations) {
            return null;
        }

        @Override
        public <U> javax.enterprise.inject.Instance<U> select(Class<U> uClass, Annotation... annotations) {
            return null;
        }

        @Override
        public <U> javax.enterprise.inject.Instance<U> select(javax.enterprise.util.TypeLiteral<U> tl,
                Annotation... annotations) {
            return null;
        }

        @Override
        public boolean isUnsatisfied() {
            return false;
        }

        @Override
        public boolean isAmbiguous() {
            return false;
        }

        @Override
        public void destroy(Object instance) {
        }

        @Override
        public Iterator<Object> iterator() {
            return instances.iterator();
        }

        @Override
        public Object get() {
            throw new RuntimeException("Unexpected");
        }
    }

    @NonNull
    public static KindOfInjectionPoint kindOfInjectionPoint(@NonNull AccessibleObject fieldOrConstructor) {
        Annotation[] annotations = fieldOrConstructor.getDeclaredAnnotations();

        if (annotations.length == 0) {
            return KindOfInjectionPoint.NotAnnotated;
        }

        if (JAKARTA_INJECT_CLASS != null && isAnnotated(annotations, jakarta.inject.Inject.class)) {
            return KindOfInjectionPoint.Required;
        }

        if (JAVAX_INJECT_CLASS != null && isAnnotated(annotations, javax.inject.Inject.class)) {
            return KindOfInjectionPoint.Required;
        }

        KindOfInjectionPoint kind = isAutowired(annotations);

        if (kind != KindOfInjectionPoint.NotAnnotated || fieldOrConstructor instanceof Constructor) {
            return kind;
        }

        if (isRequiredJakarta(annotations) || isRequiredJavax(annotations)) {
            return KindOfInjectionPoint.Required;
        }

        return KindOfInjectionPoint.NotAnnotated;
    }

    private static boolean isAnnotated(@NonNull Annotation[] declaredAnnotations,
            @NonNull Class<?> annotationOfInterest) {
        Annotation annotation = getAnnotation(declaredAnnotations, annotationOfInterest);
        return annotation != null;
    }

    @Nullable
    private static Annotation getAnnotation(@NonNull Annotation[] declaredAnnotations,
            @NonNull Class<?> annotationOfInterest) {
        for (Annotation declaredAnnotation : declaredAnnotations) {
            if (declaredAnnotation.annotationType() == annotationOfInterest) {
                return declaredAnnotation;
            }
        }

        return null;
    }

    @NonNull
    private static KindOfInjectionPoint isAutowired(@NonNull Annotation[] declaredAnnotations) {
        for (Annotation declaredAnnotation : declaredAnnotations) {
            Class<?> annotationType = declaredAnnotation.annotationType();

            if (annotationType.getName().endsWith(".Autowired")) {
                Boolean required = invokePublicIfAvailable(annotationType, declaredAnnotation, "required",
                        NO_PARAMETERS);
                return required != null && required ? KindOfInjectionPoint.Required : KindOfInjectionPoint.Optional;
            }
        }

        return KindOfInjectionPoint.NotAnnotated;
    }

    private static boolean isRequiredJakarta(@NonNull Annotation[] annotations) {
        return isAnnotated(annotations, jakarta.annotation.Resource.class)
                || JAVAX_EJB_CLASS != null && isAnnotated(annotations, jakarta.ejb.EJB.class)
                || JAKARTA_PERSISTENCE_UNIT_CLASS != null
                        && (isAnnotated(annotations, jakarta.persistence.PersistenceContext.class)
                                || isAnnotated(annotations, jakarta.persistence.PersistenceUnit.class));
    }

    private static boolean isRequiredJavax(@NonNull Annotation[] annotations) {
        return isAnnotated(annotations, javax.annotation.Resource.class)
                || JAVAX_EJB_CLASS != null && isAnnotated(annotations, javax.ejb.EJB.class)
                || JAVAX_PERSISTENCE_UNIT_CLASS != null
                        && (isAnnotated(annotations, javax.persistence.PersistenceContext.class)
                                || isAnnotated(annotations, javax.persistence.PersistenceUnit.class));
    }

    @NonNull
    public static Type getTypeOfInjectionPointFromVarargsParameter(@NonNull Type parameterType) {
        if (parameterType instanceof Class<?>) {
            return ((Class<?>) parameterType).getComponentType();
        }

        return ((GenericArrayType) parameterType).getGenericComponentType();
    }

    @Nullable
    public static String getQualifiedName(@NonNull Annotation[] annotationsOnInjectionPoint) {
        for (Annotation annotation : annotationsOnInjectionPoint) {
            Class<?> annotationType = annotation.annotationType();
            String annotationName = annotationType.getName();

            if ("jakarta.annotation.Resource jakarta.ejb.EJB".contains(annotationName)
                    || "javax.annotation.Resource javax.ejb.EJB".contains(annotationName)) {
                String name = readAnnotationAttribute(annotation, "name");

                if (name.isEmpty()) {
                    name = readAnnotationAttributeIfAvailable(annotation, "lookup"); // EJB 3.0 has no "lookup"
                    // attribute

                    if (name == null || name.isEmpty()) {
                        name = readAnnotationAttribute(annotation, "mappedName");
                    }

                    name = name.isEmpty() ? null : getNameFromJNDILookup(name);
                }

                return name;
            }

            if ("jakarta.inject.Named".equals(annotationName) || "javax.inject.Named".equals(annotationName)
                    || annotationName.endsWith(".Qualifier")) {
                return readAnnotationAttribute(annotation, "value");
            }
        }

        return null;
    }

    @NonNull
    public static String getNameFromJNDILookup(@NonNull String jndiLookup) {
        int p = jndiLookup.lastIndexOf('/');

        if (p >= 0) {
            jndiLookup = jndiLookup.substring(p + 1);
        }

        return jndiLookup;
    }
}
