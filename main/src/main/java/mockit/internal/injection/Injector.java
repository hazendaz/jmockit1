/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isVolatile;
import static java.util.regex.Pattern.compile;

import static mockit.internal.injection.InjectionPoint.JAKARTA_PERSISTENCE_UNIT_CLASS;
import static mockit.internal.injection.InjectionPoint.JAVAX_PERSISTENCE_UNIT_CLASS;
import static mockit.internal.injection.InjectionPoint.convertToLegalJavaIdentifierIfNeeded;
import static mockit.internal.injection.InjectionPoint.getQualifiedName;
import static mockit.internal.injection.InjectionPoint.isJakartaServlet;
import static mockit.internal.injection.InjectionPoint.isJavaxServlet;
import static mockit.internal.injection.InjectionPoint.kindOfInjectionPoint;
import static mockit.internal.injection.InjectionPoint.wrapInProviderIfNeeded;
import static mockit.internal.injection.InjectionProvider.NULL;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import mockit.internal.injection.InjectionPoint.KindOfInjectionPoint;
import mockit.internal.injection.field.FieldToInject;
import mockit.internal.injection.full.FullInjection;
import mockit.internal.reflection.FieldReflection;
import mockit.internal.util.DefaultValues;

public class Injector {
    private static final Pattern TYPE_NAME = compile("class |interface |java\\.lang\\.");

    @NonNull
    protected final InjectionState injectionState;
    @Nullable
    protected final FullInjection fullInjection;

    protected Injector(@NonNull InjectionState state, @Nullable FullInjection fullInjection) {
        injectionState = state;
        this.fullInjection = fullInjection;
    }

    @NonNull
    static List<Field> findAllTargetInstanceFieldsInTestedClassHierarchy(@NonNull Class<?> actualTestedClass,
            @NonNull TestedClass testedClass) {
        List<Field> targetFields = new ArrayList<>();
        Class<?> classWithFields = actualTestedClass;

        do {
            addEligibleFields(targetFields, classWithFields);
            classWithFields = classWithFields.getSuperclass();
        } while (testedClass.isClassFromSameModuleOrSystemAsTestedClass(classWithFields)
                || isJakartaServlet(classWithFields) || isJavaxServlet(classWithFields));

        return targetFields;
    }

    private static void addEligibleFields(@NonNull List<Field> targetFields, @NonNull Class<?> classWithFields) {
        Field[] fields = classWithFields.getDeclaredFields();

        for (Field field : fields) {
            if (isEligibleForInjection(field)) {
                targetFields.add(field);
            }
        }
    }

    private static boolean isEligibleForInjection(@NonNull Field field) {
        int modifiers = field.getModifiers();

        if (isFinal(modifiers)) {
            return false;
        }

        if (kindOfInjectionPoint(field) != KindOfInjectionPoint.NotAnnotated) {
            return true;
        }

        if (JAKARTA_PERSISTENCE_UNIT_CLASS != null
                && field.getType().isAnnotationPresent(jakarta.persistence.Entity.class)) {
            return false;
        }

        if (JAVAX_PERSISTENCE_UNIT_CLASS != null
                && field.getType().isAnnotationPresent(javax.persistence.Entity.class)) {
            return false;
        }

        return !isStatic(modifiers) && !isVolatile(modifiers);
    }

    public final void fillOutDependenciesRecursively(@NonNull Object dependency, @NonNull TestedClass testedClass) {
        Class<?> dependencyClass = dependency.getClass();
        List<Field> targetFields = findAllTargetInstanceFieldsInTestedClassHierarchy(dependencyClass, testedClass);

        if (!targetFields.isEmpty()) {
            List<InjectionProvider> currentlyConsumedInjectables = injectionState.injectionProviders
                    .saveConsumedInjectionProviders();
            injectIntoEligibleFields(targetFields, dependency, testedClass);
            injectionState.injectionProviders.restoreConsumedInjectionProviders(currentlyConsumedInjectables);
        }
    }

    public final void injectIntoEligibleFields(@NonNull List<Field> targetFields, @NonNull Object testedObject,
            @NonNull TestedClass testedClass) {
        for (Field field : targetFields) {
            if (targetFieldWasNotAssignedByConstructor(testedObject, field)) {
                Object injectableValue = getValueForFieldIfAvailable(targetFields, testedClass, field);

                if (injectableValue != null && injectableValue != NULL) {
                    injectableValue = wrapInProviderIfNeeded(field.getGenericType(), injectableValue);
                    FieldReflection.setFieldValue(field, testedObject, injectableValue);
                }
            }
        }
    }

    private static boolean targetFieldWasNotAssignedByConstructor(@NonNull Object testedObject,
            @NonNull Field targetField) {
        if (kindOfInjectionPoint(targetField) != KindOfInjectionPoint.NotAnnotated) {
            return true;
        }

        Object fieldValue = FieldReflection.getFieldValue(targetField, testedObject);

        if (fieldValue == null) {
            return true;
        }

        Class<?> fieldType = targetField.getType();

        if (!fieldType.isPrimitive()) {
            return false;
        }

        Object defaultValue = DefaultValues.defaultValueForPrimitiveType(fieldType);

        return fieldValue.equals(defaultValue);
    }

    @Nullable
    private Object getValueForFieldIfAvailable(@NonNull List<Field> targetFields, @NonNull TestedClass testedClass,
            @NonNull Field targetField) {
        @Nullable
        String qualifiedFieldName = getQualifiedName(targetField.getDeclaredAnnotations());
        InjectionProvider injectable = findAvailableInjectableIfAny(targetFields, qualifiedFieldName, testedClass,
                targetField);

        if (injectable != null) {
            return injectionState.getValueToInject(injectable);
        }

        InjectionProvider fieldToInject = new FieldToInject(targetField);
        Type typeToInject = fieldToInject.getDeclaredType();
        InjectionPoint injectionPoint = new InjectionPoint(typeToInject, fieldToInject.getName(), qualifiedFieldName);
        TestedClass nextTestedClass = typeToInject instanceof TypeVariable<?> ? testedClass
                : new TestedClass(typeToInject, fieldToInject.getClassOfDeclaredType(), testedClass);
        Object testedValue = injectionState.getTestedValue(nextTestedClass, injectionPoint);

        if (testedValue != null) {
            return testedValue;
        }

        if (fullInjection != null) {
            Object newInstance = fullInjection.createOrReuseInstance(nextTestedClass, this, fieldToInject,
                    qualifiedFieldName);

            if (newInstance != null) {
                return newInstance;
            }
        }

        KindOfInjectionPoint kindOfInjectionPoint = kindOfInjectionPoint(targetField);
        throwExceptionIfUnableToInjectRequiredTargetField(kindOfInjectionPoint, targetField, qualifiedFieldName);
        return null;
    }

    @Nullable
    private InjectionProvider findAvailableInjectableIfAny(@NonNull List<Field> targetFields,
            @Nullable String qualifiedTargetFieldName, @NonNull TestedClass testedClass, @NonNull Field targetField) {
        KindOfInjectionPoint kindOfInjectionPoint = kindOfInjectionPoint(targetField);
        InjectionProviders injectionProviders = injectionState.injectionProviders;
        injectionProviders.setTypeOfInjectionPoint(targetField.getGenericType(), kindOfInjectionPoint);

        if (qualifiedTargetFieldName != null && !qualifiedTargetFieldName.isEmpty()) {
            String injectableName = convertToLegalJavaIdentifierIfNeeded(qualifiedTargetFieldName);
            InjectionProvider matchingInjectable = injectionProviders.findInjectableByTypeAndName(injectableName,
                    testedClass);

            if (matchingInjectable != null) {
                return matchingInjectable;
            }
        }

        String targetFieldName = targetField.getName();

        if (withMultipleTargetFieldsOfSameType(targetFields, testedClass, targetField, injectionProviders)) {
            return injectionProviders.findInjectableByTypeAndName(targetFieldName, testedClass);
        }

        return injectionProviders.getProviderByTypeAndOptionallyName(targetFieldName, testedClass);
    }

    private static boolean withMultipleTargetFieldsOfSameType(@NonNull List<Field> targetFields,
            @NonNull TestedClass testedClass, @NonNull Field targetField,
            @NonNull InjectionProviders injectionProviders) {
        for (Field anotherTargetField : targetFields) {
            if (anotherTargetField != targetField && injectionProviders
                    .isAssignableToInjectionPoint(anotherTargetField.getGenericType(), testedClass)) {
                return true;
            }
        }

        return false;
    }

    private void throwExceptionIfUnableToInjectRequiredTargetField(@NonNull KindOfInjectionPoint kindOfInjectionPoint,
            @NonNull Field targetField, @Nullable String qualifiedFieldName) {
        if (kindOfInjectionPoint == KindOfInjectionPoint.Required) {
            Type fieldType = targetField.getGenericType();
            String fieldTypeName = fieldType.toString();
            fieldTypeName = TYPE_NAME.matcher(fieldTypeName).replaceAll("");
            String kindOfInjectable = "@Tested or @Injectable";

            if (fullInjection != null) {
                if (targetField.getType().isInterface()) {
                    kindOfInjectable = "@Tested instance of an implementation class";
                } else {
                    kindOfInjectable = "@Tested object";
                }
            }

            throw new IllegalStateException("Missing " + kindOfInjectable + " for field " + fieldTypeName + ' '
                    + targetField.getName() + (qualifiedFieldName == null ? "" : " (\"" + qualifiedFieldName + "\")")
                    + " in " + targetField.getDeclaringClass().getSimpleName());
        }
    }
}
