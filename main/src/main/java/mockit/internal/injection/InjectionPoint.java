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

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.servlet.Servlet;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class InjectionPoint {
    public enum KindOfInjectionPoint {
        NotAnnotated, Required, Optional
    }

    @Nullable
    public static final Class<? extends Annotation> INJECT_CLASS;
    @Nullable
    private static final Class<? extends Annotation> INSTANCE_CLASS;
    @Nullable
    private static final Class<? extends Annotation> EJB_CLASS;
    @Nullable
    public static final Class<? extends Annotation> PERSISTENCE_UNIT_CLASS;
    @Nullable
    public static final Class<?> SERVLET_CLASS;
    @Nullable
    public static final Class<?> CONVERSATION_CLASS;

    static {
        INJECT_CLASS = searchTypeInClasspath("javax.inject.Inject");
        INSTANCE_CLASS = searchTypeInClasspath("javax.enterprise.inject.Instance");
        EJB_CLASS = searchTypeInClasspath("javax.ejb.EJB");
        SERVLET_CLASS = searchTypeInClasspath("javax.servlet.Servlet");
        CONVERSATION_CLASS = searchTypeInClasspath("javax.enterprise.context.Conversation");

        Class<? extends Annotation> entity = searchTypeInClasspath("javax.persistence.Entity");

        if (entity == null) {
            PERSISTENCE_UNIT_CLASS = null;
        } else {
            PERSISTENCE_UNIT_CLASS = searchTypeInClasspath("javax.persistence.PersistenceUnit");
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

    static boolean isServlet(@NonNull Class<?> aClass) {
        return SERVLET_CLASS != null && Servlet.class.isAssignableFrom(aClass);
    }

    @NonNull
    public static Object wrapInProviderIfNeeded(@NonNull Type type, @NonNull final Object value) {
        if (INJECT_CLASS != null && type instanceof ParameterizedType && !(value instanceof Provider)) {
            Type parameterizedType = ((ParameterizedType) type).getRawType();

            if (parameterizedType == Provider.class) {
                return (Provider<Object>) () -> value;
            }

            if (INSTANCE_CLASS != null && parameterizedType == Instance.class) {
                @SuppressWarnings("unchecked")
                List<Object> values = (List<Object>) value;
                return new Listed(values);
            }
        }

        return value;
    }

    private static final class Listed implements Instance<Object> {
        @NonNull
        private final List<Object> instances;

        Listed(@NonNull List<Object> instances) {
            this.instances = instances;
        }

        @Override
        public Instance<Object> select(Annotation... annotations) {
            return null;
        }

        @Override
        public <U> Instance<U> select(Class<U> uClass, Annotation... annotations) {
            return null;
        }

        @Override
        public <U> Instance<U> select(TypeLiteral<U> tl, Annotation... annotations) {
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

        if (INJECT_CLASS != null && isAnnotated(annotations, Inject.class)) {
            return KindOfInjectionPoint.Required;
        }

        KindOfInjectionPoint kind = isAutowired(annotations);

        if (kind != KindOfInjectionPoint.NotAnnotated || fieldOrConstructor instanceof Constructor) {
            return kind;
        }

        if (isRequired(annotations)) {
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

    private static boolean isRequired(@NonNull Annotation[] annotations) {
        return isAnnotated(annotations, Resource.class) || EJB_CLASS != null && isAnnotated(annotations, EJB.class)
                || PERSISTENCE_UNIT_CLASS != null && (isAnnotated(annotations, PersistenceContext.class)
                        || isAnnotated(annotations, PersistenceUnit.class));
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

            if ("javax.annotation.Resource javax.ejb.EJB".contains(annotationName)) {
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

            if ("javax.inject.Named".equals(annotationName) || annotationName.endsWith(".Qualifier")) {
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
