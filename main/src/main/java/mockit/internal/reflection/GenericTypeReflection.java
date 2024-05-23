/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("OverlyComplexClass")
public final class GenericTypeReflection {
    @NonNull
    private final Map<String, Type> typeParametersToTypeArguments;
    @NonNull
    private final Map<String, String> typeParametersToTypeArgumentNames;
    private final boolean withSignatures;

    public GenericTypeReflection(@NonNull Class<?> ownerClass, @Nullable Type genericType) {
        this(ownerClass, genericType, true);
    }

    public GenericTypeReflection(@NonNull Class<?> ownerClass, @Nullable Type genericType, boolean withSignatures) {
        typeParametersToTypeArguments = new HashMap<>(4);
        typeParametersToTypeArgumentNames = withSignatures ? new HashMap<>(4) : Collections.<String, String>emptyMap();
        this.withSignatures = withSignatures;
        discoverTypeMappings(ownerClass, genericType);
    }

    private void discoverTypeMappings(@NonNull Class<?> rawType, @Nullable Type genericType) {
        if (genericType instanceof ParameterizedType) {
            addMappingsFromTypeParametersToTypeArguments(rawType, (ParameterizedType) genericType);
        }

        addGenericTypeMappingsForSuperTypes(rawType);
    }

    private void addGenericTypeMappingsForSuperTypes(@NonNull Class<?> rawType) {
        Type superType = rawType;

        while (superType != null && superType != Object.class) {
            Class<?> superClass = (Class<?>) superType;
            superType = superClass.getGenericSuperclass();

            if (superType != null && superType != Object.class) {
                superClass = addGenericTypeMappingsIfParameterized(superType);
                superType = superClass;
            }

            addGenericTypeMappingsForInterfaces(superClass);
        }
    }

    @NonNull
    private Class<?> addGenericTypeMappingsIfParameterized(@NonNull Type superType) {
        if (superType instanceof ParameterizedType) {
            ParameterizedType genericSuperType = (ParameterizedType) superType;
            Class<?> rawType = (Class<?>) genericSuperType.getRawType();
            addMappingsFromTypeParametersToTypeArguments(rawType, genericSuperType);
            return rawType;
        }

        return (Class<?>) superType;
    }

    private void addGenericTypeMappingsForInterfaces(@NonNull Class<?> classOrInterface) {
        for (Type implementedInterface : classOrInterface.getGenericInterfaces()) {
            Class<?> implementedType = addGenericTypeMappingsIfParameterized(implementedInterface);
            addGenericTypeMappingsForInterfaces(implementedType);
        }
    }

    private void addMappingsFromTypeParametersToTypeArguments(@NonNull Class<?> rawType,
            @NonNull ParameterizedType genericType) {
        String ownerTypeDesc = getOwnerClassDesc(rawType);
        TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
        Type[] typeArguments = genericType.getActualTypeArguments();

        for (int i = 0, n = typeParameters.length; i < n; i++) {
            TypeVariable<?> typeParam = typeParameters[i];
            String typeVarName = typeParam.getName();

            if (typeParametersToTypeArguments.containsKey(ownerTypeDesc + ':' + typeVarName)) {
                continue;
            }

            Type typeArg = typeArguments[i];

            if (typeArg instanceof Class<?>) {
                addMappingForClassType(ownerTypeDesc, typeVarName, typeArg);
            } else if (typeArg instanceof TypeVariable<?>) {
                addMappingForTypeVariable(ownerTypeDesc, typeVarName, typeArg);
            } else if (typeArg instanceof ParameterizedType) {
                addMappingForParameterizedType(ownerTypeDesc, typeVarName, typeArg);
            } else if (typeArg instanceof GenericArrayType) {
                addMappingForArrayType(ownerTypeDesc, typeVarName, typeArg);
            } else {
                addMappingForFirstTypeBound(ownerTypeDesc, typeParam);
            }
        }

        Type outerType = genericType.getOwnerType();

        if (outerType instanceof ParameterizedType) {
            ParameterizedType parameterizedOuterType = (ParameterizedType) outerType;
            Class<?> rawOuterType = (Class<?>) parameterizedOuterType.getRawType();
            addMappingsFromTypeParametersToTypeArguments(rawOuterType, parameterizedOuterType);
        }
    }

    private void addMappingForClassType(@NonNull String ownerTypeDesc, @NonNull String typeName,
            @NonNull Type typeArg) {
        String mappedTypeArgName = null;

        if (withSignatures) {
            Class<?> classArg = (Class<?>) typeArg;
            String ownerClassDesc = getOwnerClassDesc(classArg);
            mappedTypeArgName = classArg.isArray() ? ownerClassDesc : 'L' + ownerClassDesc;
        }

        addTypeMapping(ownerTypeDesc, typeName, typeArg, mappedTypeArgName);
    }

    private void addMappingForTypeVariable(@NonNull String ownerTypeDesc, @NonNull String typeName,
            @NonNull Type typeArg) {
        @Nullable
        String mappedTypeArgName = null;

        if (withSignatures) {
            TypeVariable<?> typeVar = (TypeVariable<?>) typeArg;
            String ownerClassDesc = getOwnerClassDesc(typeVar);
            String intermediateTypeArg = ownerClassDesc + ":T" + typeVar.getName();
            mappedTypeArgName = typeParametersToTypeArgumentNames.get(intermediateTypeArg);
        }

        addTypeMapping(ownerTypeDesc, typeName, typeArg, mappedTypeArgName);
    }

    private void addMappingForParameterizedType(@NonNull String ownerTypeDesc, @NonNull String typeName,
            @NonNull Type typeArg) {
        String mappedTypeArgName = getMappedTypeArgName(typeArg);
        addTypeMapping(ownerTypeDesc, typeName, typeArg, mappedTypeArgName);
    }

    @Nullable
    private String getMappedTypeArgName(@NonNull Type typeArg) {
        if (withSignatures) {
            Class<?> classType = getClassType(typeArg);
            return 'L' + getOwnerClassDesc(classType);
        }

        return null;
    }

    private void addMappingForArrayType(@NonNull String ownerTypeDesc, @NonNull String typeName,
            @NonNull Type typeArg) {
        String mappedTypeArgName = null;

        if (withSignatures) {
            mappedTypeArgName = getMappedTypeArgName((GenericArrayType) typeArg);
        }

        addTypeMapping(ownerTypeDesc, typeName, typeArg, mappedTypeArgName);
    }

    private void addMappingForFirstTypeBound(@NonNull String ownerTypeDesc, @NonNull TypeVariable<?> typeParam) {
        Type typeArg = typeParam.getBounds()[0];
        String mappedTypeArgName = getMappedTypeArgName(typeArg);
        addTypeMapping(ownerTypeDesc, typeParam.getName(), typeArg, mappedTypeArgName);
    }

    @NonNull
    private static String getOwnerClassDesc(@NonNull Class<?> rawType) {
        return rawType.getName().replace('.', '/');
    }

    @NonNull
    private Class<?> getClassType(@NonNull Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class<?>) parameterizedType.getRawType();
        }

        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVar = (TypeVariable<?>) type;
            String typeVarKey = getTypeVariableKey(typeVar);
            @Nullable
            Type typeArg = typeParametersToTypeArguments.get(typeVarKey);

            if (typeArg == null) {
                throw new IllegalArgumentException("Unable to resolve type variable \"" + typeVar.getName() + '"');
            }

            // noinspection TailRecursion
            return getClassType(typeArg);
        }

        return (Class<?>) type;
    }

    @NonNull
    private String getMappedTypeArgName(@NonNull GenericArrayType arrayType) {
        StringBuilder argName = new StringBuilder(20);
        argName.append('[');

        while (true) {
            Type componentType = arrayType.getGenericComponentType();

            if (!(componentType instanceof GenericArrayType)) {
                Class<?> classType = getClassType(componentType);
                argName.append('L').append(getOwnerClassDesc(classType));
                return argName.toString();
            }
            argName.append('[');
            // noinspection AssignmentToMethodParameter
            arrayType = (GenericArrayType) componentType;
        }
    }

    private void addTypeMapping(@NonNull String ownerTypeDesc, @NonNull String typeVarName, @NonNull Type mappedTypeArg,
            @Nullable String mappedTypeArgName) {
        typeParametersToTypeArguments.put(ownerTypeDesc + ':' + typeVarName, mappedTypeArg);

        if (mappedTypeArgName != null) {
            addTypeMapping(ownerTypeDesc, typeVarName, mappedTypeArgName);
        }
    }

    private void addTypeMapping(@NonNull String ownerTypeDesc, @NonNull String typeVarName,
            @NonNull String mappedTypeArgName) {
        String typeMappingKey = ownerTypeDesc + ":T" + typeVarName;
        typeParametersToTypeArgumentNames.put(typeMappingKey, mappedTypeArgName);
    }

    public final class GenericSignature {
        private final List<String> parameters = new ArrayList<>();
        private final String parameterTypeDescs;
        private final int lengthOfParameterTypeDescs;
        private int currentPos;

        GenericSignature(@NonNull String signature) {
            int p = signature.indexOf('(');
            int q = signature.lastIndexOf(')');
            parameterTypeDescs = signature.substring(p + 1, q);
            lengthOfParameterTypeDescs = parameterTypeDescs.length();
            addTypeDescsToList();
        }

        private void addTypeDescsToList() {
            while (currentPos < lengthOfParameterTypeDescs) {
                addNextParameter();
            }
        }

        private void addNextParameter() {
            int startPos = currentPos;
            int endPos;
            char c = parameterTypeDescs.charAt(startPos);

            switch (c) {
                case 'T':
                    endPos = parameterTypeDescs.indexOf(';', startPos);
                    currentPos = endPos;
                    break;
                case 'L':
                    endPos = advanceToEndOfTypeDesc();
                    break;
                case '[': {
                    char elemTypeStart = firstCharacterOfArrayElementType();
                    if (elemTypeStart == 'T') {
                        endPos = parameterTypeDescs.indexOf(';', startPos);
                        currentPos = endPos;
                    } else if (elemTypeStart == 'L') {
                        endPos = advanceToEndOfTypeDesc();
                    } else {
                        endPos = currentPos + 1;
                    }
                    break;
                }
                default:
                    endPos = currentPos + 1;
                    break;
            }

            currentPos++;
            String parameter = parameterTypeDescs.substring(startPos, endPos);
            parameters.add(parameter);
        }

        private int advanceToEndOfTypeDesc() {
            char c = '\0';

            do {
                currentPos++;
                if (currentPos == lengthOfParameterTypeDescs) {
                    break;
                }
                c = parameterTypeDescs.charAt(currentPos);
            } while (c != ';' && c != '<');

            int endPos = currentPos;

            if (c == '<') {
                advancePastTypeArguments();
                currentPos++;
            }

            return endPos;
        }

        private char firstCharacterOfArrayElementType() {
            char c;

            do {
                currentPos++;
                c = parameterTypeDescs.charAt(currentPos);
            } while (c == '[');

            return c;
        }

        private void advancePastTypeArguments() {
            int angleBracketDepth = 1;

            do {
                currentPos++;
                char c = parameterTypeDescs.charAt(currentPos);
                if (c == '>') {
                    angleBracketDepth--;
                } else if (c == '<') {
                    angleBracketDepth++;
                }
            } while (angleBracketDepth > 0);
        }

        public boolean satisfiesGenericSignature(@NonNull String otherSignature) {
            GenericSignature other = new GenericSignature(otherSignature);
            return areMatchingSignatures(other);
        }

        private boolean areMatchingSignatures(@NonNull GenericSignature other) {
            int n = parameters.size();

            if (n != other.parameters.size()) {
                return false;
            }

            for (int i = 0; i < n; i++) {
                String p1 = other.parameters.get(i);
                String p2 = parameters.get(i);

                if (!areParametersOfSameType(p1, p2)) {
                    return false;
                }
            }

            return true;
        }

        @SuppressWarnings("MethodWithMultipleLoops")
        private boolean areParametersOfSameType(@NonNull String param1, @NonNull String param2) {
            if (param1.equals(param2)) {
                return true;
            }

            int i = -1;
            char c;
            do {
                i++;
                c = param1.charAt(i);
            } while (c == '[');
            if (c != 'T') {
                return false;
            }

            String typeVarName1 = param1.substring(i);
            String typeVarName2 = param2.substring(i);
            String typeArg1 = null;

            for (Entry<String, String> typeParamAndArgName : typeParametersToTypeArgumentNames.entrySet()) {
                String typeMappingKey = typeParamAndArgName.getKey();
                String typeVarName = typeMappingKey.substring(typeMappingKey.indexOf(':') + 1);

                if (typeVarName.equals(typeVarName1)) {
                    typeArg1 = typeParamAndArgName.getValue();
                    break;
                }
            }

            return typeVarName2.equals(typeArg1);
        }

        public boolean satisfiesSignature(@NonNull String otherSignature) {
            GenericSignature other = new GenericSignature(otherSignature);
            return other.areMatchingSignatures(this);
        }
    }

    @NonNull
    public GenericSignature parseSignature(@NonNull String genericSignature) {
        return new GenericSignature(genericSignature);
    }

    @NonNull
    public String resolveSignature(@NonNull String ownerTypeDesc, @NonNull String genericSignature) {
        addTypeArgumentsIfAvailable(ownerTypeDesc, genericSignature);

        int p = genericSignature.lastIndexOf(')') + 1;
        int q = genericSignature.length();
        String returnType = genericSignature.substring(p, q);
        String resolvedReturnType = replaceTypeParametersWithActualTypes(ownerTypeDesc, returnType);

        StringBuilder finalSignature = new StringBuilder(genericSignature);
        finalSignature.replace(p, q, resolvedReturnType);
        return finalSignature.toString();
    }

    private void addTypeArgumentsIfAvailable(@NonNull String ownerTypeDesc, @NonNull String signature) {
        int firstParen = signature.indexOf('(');
        if (firstParen == 0) {
            return;
        }

        int p = 1;
        boolean lastMappingFound = false;

        while (!lastMappingFound) {
            int q = signature.indexOf(':', p);
            String typeVar = signature.substring(p, q);

            q++;

            if (signature.charAt(q) == ':') {
                q++; // an unbounded type argument uses ":" as separator, while a bounded one uses "::"
            }

            int r = signature.indexOf(':', q);

            if (r < 0) {
                r = firstParen - 2;
                lastMappingFound = true;
            } else {
                r = signature.lastIndexOf(';', r);
                p = r + 1;
            }

            String typeArg = signature.substring(q, r);
            addTypeMapping(ownerTypeDesc, typeVar, typeArg);
        }
    }

    @NonNull
    private String replaceTypeParametersWithActualTypes(@NonNull String ownerTypeDesc, @NonNull String typeDesc) {
        if (typeDesc.charAt(0) == 'T' && !typeParametersToTypeArgumentNames.isEmpty()) {
            return replaceTypeParameters(ownerTypeDesc, typeDesc);
        }

        int p = typeDesc.indexOf('<');

        if (p < 0) {
            return typeDesc;
        }

        String resolvedTypeDesc = typeDesc;

        for (Entry<String, String> paramAndArg : typeParametersToTypeArgumentNames.entrySet()) {
            String typeMappingKey = paramAndArg.getKey();
            String typeParam = typeMappingKey.substring(typeMappingKey.indexOf(':') + 1) + ';';
            String typeArg = paramAndArg.getValue() + ';';
            resolvedTypeDesc = resolvedTypeDesc.replace(typeParam, typeArg);
        }

        return resolvedTypeDesc;
    }

    @NonNull
    private String replaceTypeParameters(@NonNull String ownerTypeDesc, @NonNull String typeDesc) {
        String typeParameter = typeDesc.substring(0, typeDesc.length() - 1);

        while (true) {
            @Nullable
            String typeArg = typeParametersToTypeArgumentNames.get(ownerTypeDesc + ':' + typeParameter);

            if (typeArg == null) {
                return typeDesc;
            }

            if (typeArg.charAt(0) != 'T') {
                return typeArg + ';';
            }

            typeParameter = typeArg;
        }
    }

    @NonNull
    public Type resolveTypeVariable(@NonNull TypeVariable<?> typeVariable) {
        String typeVarKey = getTypeVariableKey(typeVariable);
        @Nullable
        Type typeArgument = typeParametersToTypeArguments.get(typeVarKey);

        if (typeArgument == null) {
            typeArgument = typeVariable.getBounds()[0];
        }

        if (typeArgument instanceof TypeVariable<?>) {
            typeArgument = resolveTypeVariable((TypeVariable<?>) typeArgument);
        }

        return typeArgument;
    }

    @NonNull
    private static String getTypeVariableKey(@NonNull TypeVariable<?> typeVariable) {
        String ownerClassDesc = getOwnerClassDesc(typeVariable);
        return ownerClassDesc + ':' + typeVariable.getName();
    }

    @NonNull
    private static String getOwnerClassDesc(@NonNull TypeVariable<?> typeVariable) {
        GenericDeclaration owner = typeVariable.getGenericDeclaration();
        Class<?> ownerClass = owner instanceof Member ? ((Member) owner).getDeclaringClass() : (Class<?>) owner;
        return getOwnerClassDesc(ownerClass);
    }

    public boolean areMatchingTypes(@NonNull Type declarationType, @NonNull Type realizationType) {
        if (declarationType.equals(realizationType)) {
            return true;
        }

        if (declarationType instanceof Class<?>) {
            if (realizationType instanceof Class<?>) {
                return ((Class<?>) declarationType).isAssignableFrom((Class<?>) realizationType);
            }
        } else if (declarationType instanceof TypeVariable<?>) {
            if (realizationType instanceof TypeVariable<?>) {
                return false;
            }

            // noinspection RedundantIfStatement
            if (areMatchingTypes((TypeVariable<?>) declarationType, realizationType)) {
                return true;
            }
        } else if (declarationType instanceof ParameterizedType) {
            ParameterizedType parameterizedDeclarationType = (ParameterizedType) declarationType;
            ParameterizedType parameterizedRealizationType = getParameterizedType(realizationType);

            if (parameterizedRealizationType != null) {
                return areMatchingTypes(parameterizedDeclarationType, parameterizedRealizationType);
            }
        }

        return false;
    }

    @Nullable
    private static ParameterizedType getParameterizedType(@NonNull Type realizationType) {
        if (realizationType instanceof ParameterizedType) {
            return (ParameterizedType) realizationType;
        }

        if (realizationType instanceof Class<?>) {
            return findRealizationSupertype((Class<?>) realizationType);
        }

        return null;
    }

    @Nullable
    private static ParameterizedType findRealizationSupertype(@NonNull Class<?> realizationType) {
        Type realizationSuperclass = realizationType.getGenericSuperclass();
        ParameterizedType parameterizedRealizationType = null;

        if (realizationSuperclass instanceof ParameterizedType) {
            parameterizedRealizationType = (ParameterizedType) realizationSuperclass;
        } else {
            for (Type realizationSupertype : realizationType.getGenericInterfaces()) {
                if (realizationSupertype instanceof ParameterizedType) {
                    parameterizedRealizationType = (ParameterizedType) realizationSupertype;
                    break;
                }
            }
        }

        return parameterizedRealizationType;
    }

    private boolean areMatchingTypes(@NonNull TypeVariable<?> declarationType, @NonNull Type realizationType) {
        String typeVarKey = getTypeVariableKey(declarationType);
        @Nullable
        Type resolvedType = typeParametersToTypeArguments.get(typeVarKey);

        return resolvedType != null && (resolvedType.equals(realizationType)
                || typeSatisfiesResolvedTypeVariable(resolvedType, realizationType));
    }

    private boolean areMatchingTypes(@NonNull ParameterizedType declarationType,
            @NonNull ParameterizedType realizationType) {
        return declarationType.getRawType().equals(realizationType.getRawType())
                && haveMatchingActualTypeArguments(declarationType, realizationType);
    }

    private boolean haveMatchingActualTypeArguments(@NonNull ParameterizedType declarationType,
            @NonNull ParameterizedType realizationType) {
        Type[] declaredTypeArguments = declarationType.getActualTypeArguments();
        Type[] concreteTypeArguments = realizationType.getActualTypeArguments();

        for (int i = 0, n = declaredTypeArguments.length; i < n; i++) {
            Type declaredTypeArg = declaredTypeArguments[i];
            Type concreteTypeArg = concreteTypeArguments[i];

            if (declaredTypeArg instanceof TypeVariable<?>) {
                if (areMatchingTypeArguments((TypeVariable<?>) declaredTypeArg, concreteTypeArg)) {
                    continue;
                }
            } else if (areMatchingTypes(declaredTypeArg, concreteTypeArg)) {
                continue;
            }

            return false;
        }

        return true;
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean areMatchingTypeArguments(@NonNull TypeVariable<?> declaredType, @NonNull Type concreteType) {
        String typeVarKey = getTypeVariableKey(declaredType);
        @Nullable
        Type resolvedType = typeParametersToTypeArguments.get(typeVarKey);

        if (resolvedType != null) {
            if (resolvedType.equals(concreteType) || concreteType instanceof Class<?>
                    && typeSatisfiesResolvedTypeVariable(resolvedType, (Class<?>) concreteType)) {
                return true;
            }

            if (concreteType instanceof WildcardType
                    && typeSatisfiesUpperBounds(resolvedType, ((WildcardType) concreteType).getUpperBounds())) {
                return true;
            }
        } else if (typeSatisfiesUpperBounds(concreteType, declaredType.getBounds())) {
            return true;
        }

        return false;
    }

    private boolean typeSatisfiesResolvedTypeVariable(@NonNull Type resolvedType, @NonNull Type realizationType) {
        Class<?> realizationClass = getClassType(realizationType);
        return typeSatisfiesResolvedTypeVariable(resolvedType, realizationClass);
    }

    private boolean typeSatisfiesResolvedTypeVariable(@NonNull Type resolvedType, @NonNull Class<?> realizationType) {
        Class<?> resolvedClass = getClassType(resolvedType);
        return resolvedClass.isAssignableFrom(realizationType);
    }

    private boolean typeSatisfiesUpperBounds(@NonNull Type type, @NonNull Type[] upperBounds) {
        Class<?> classType = getClassType(type);

        for (Type upperBound : upperBounds) {
            Class<?> classBound = getClassType(upperBound);

            if (!classBound.isAssignableFrom(classType)) {
                return false;
            }
        }

        return true;
    }
}
