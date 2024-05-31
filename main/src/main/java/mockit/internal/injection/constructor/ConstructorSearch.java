/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection.constructor;

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PROTECTED;
import static java.lang.reflect.Modifier.PUBLIC;

import static mockit.internal.injection.InjectionPoint.getQualifiedName;
import static mockit.internal.injection.InjectionPoint.getTypeOfInjectionPointFromVarargsParameter;
import static mockit.internal.injection.InjectionPoint.kindOfInjectionPoint;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import mockit.asm.types.JavaType;
import mockit.internal.injection.InjectionPoint;
import mockit.internal.injection.InjectionPoint.KindOfInjectionPoint;
import mockit.internal.injection.InjectionProvider;
import mockit.internal.injection.InjectionProviders;
import mockit.internal.injection.InjectionState;
import mockit.internal.injection.TestedClass;
import mockit.internal.state.ParameterNames;
import mockit.internal.util.ParameterNameExtractor;

public final class ConstructorSearch {
    private static final int CONSTRUCTOR_ACCESS = PUBLIC + PROTECTED + PRIVATE;

    @NonNull
    private final InjectionState injectionState;
    @NonNull
    private final TestedClass testedClass;
    @NonNull
    private final String testedClassDesc;
    @NonNull
    public List<InjectionProvider> parameterProviders;
    private final boolean withFullInjection;
    @Nullable
    private Constructor<?> constructor;
    @Nullable
    private StringBuilder searchResults;

    public ConstructorSearch(@NonNull InjectionState injectionState, @NonNull TestedClass testedClass,
            boolean withFullInjection) {
        this.injectionState = injectionState;
        this.testedClass = testedClass;
        Class<?> declaredClass = testedClass.getDeclaredClass();
        testedClassDesc = ParameterNameExtractor.extractNames(declaredClass);
        parameterProviders = new ArrayList<>();
        this.withFullInjection = withFullInjection;
    }

    @Nullable
    public Constructor<?> findConstructorToUse() {
        constructor = null;
        Class<?> declaredClass = testedClass.targetClass;
        Constructor<?>[] constructors = declaredClass.getDeclaredConstructors();

        if (!findSingleAnnotatedConstructor(constructors)) {
            findSatisfiedConstructorWithMostParameters(constructors);
        }

        return constructor;
    }

    private boolean findSingleAnnotatedConstructor(@NonNull Constructor<?>[] constructors) {
        for (Constructor<?> c : constructors) {
            if (kindOfInjectionPoint(c) != KindOfInjectionPoint.NotAnnotated) {
                List<InjectionProvider> providersFound = findParameterProvidersForConstructor(c);

                if (providersFound != null) {
                    parameterProviders = providersFound;
                    constructor = c;
                }

                return true;
            }
        }

        return false;
    }

    private void findSatisfiedConstructorWithMostParameters(@NonNull Constructor<?>[] constructors) {
        sortConstructorsWithMostAccessibleFirst(constructors);

        Constructor<?> unresolvedConstructor = null;
        List<InjectionProvider> incompleteProviders = null;

        for (Constructor<?> candidateConstructor : constructors) {
            List<InjectionProvider> providersFound = findParameterProvidersForConstructor(candidateConstructor);

            if (providersFound != null) {
                if (withFullInjection && containsUnresolvedProvider(providersFound)) {
                    if (unresolvedConstructor == null || isLargerConstructor(candidateConstructor, providersFound,
                            unresolvedConstructor, incompleteProviders)) {
                        unresolvedConstructor = candidateConstructor;
                        incompleteProviders = providersFound;
                    }
                } else if (constructor == null
                        || isLargerConstructor(candidateConstructor, providersFound, constructor, parameterProviders)) {
                    constructor = candidateConstructor;
                    parameterProviders = providersFound;
                }
            }
        }

        selectConstructorWithUnresolvedParameterIfMoreAccessible(unresolvedConstructor, incompleteProviders);
    }

    private static void sortConstructorsWithMostAccessibleFirst(@NonNull Constructor<?>[] constructors) {
        if (constructors.length > 1) {
            Arrays.sort(constructors, CONSTRUCTOR_COMPARATOR);
        }
    }

    private static final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = ConstructorSearch::compareAccessibility;

    private static int compareAccessibility(@NonNull Constructor<?> c1, @NonNull Constructor<?> c2) {
        int m1 = getModifiers(c1);
        int m2 = getModifiers(c2);
        if (m1 == m2) {
            return 0;
        }
        if (m1 == PUBLIC) {
            return -1;
        }
        if (m2 == PUBLIC) {
            return 1;
        }
        if (m1 == PROTECTED) {
            return -1;
        }
        if (m2 == PROTECTED) {
            return 1;
        }
        if (m2 == PRIVATE) {
            return -1;
        }
        return 1;
    }

    private static boolean containsUnresolvedProvider(@NonNull List<InjectionProvider> providersFound) {
        for (InjectionProvider provider : providersFound) {
            if (provider instanceof ConstructorParameter && provider.getValue(null) == null) {
                return true;
            }
        }

        return false;
    }

    private static boolean isLargerConstructor(@NonNull Constructor<?> candidateConstructor,
            @NonNull List<InjectionProvider> providersFound, @NonNull Constructor<?> previousSatisfiableConstructor,
            @NonNull List<InjectionProvider> previousProviders) {
        return getModifiers(candidateConstructor) == getModifiers(previousSatisfiableConstructor)
                && providersFound.size() >= previousProviders.size();
    }

    private static int getModifiers(@NonNull Constructor<?> c) {
        return CONSTRUCTOR_ACCESS & c.getModifiers();
    }

    @Nullable
    private List<InjectionProvider> findParameterProvidersForConstructor(@NonNull Constructor<?> candidate) {
        Type[] parameterTypes = candidate.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = candidate.getParameterAnnotations();
        int n = parameterTypes.length;
        List<InjectionProvider> providersFound = new ArrayList<>(n);
        boolean varArgs = candidate.isVarArgs();

        if (varArgs) {
            n--;
        }

        printCandidateConstructorNameIfRequested(candidate);

        String constructorDesc = "<init>" + JavaType.getConstructorDescriptor(candidate);
        InjectionProviders injectionProviders = injectionState.injectionProviders;
        KindOfInjectionPoint kindOfInjectionPoint = kindOfInjectionPoint(candidate);

        for (int i = 0; i < n; i++) {
            Type parameterType = parameterTypes[i];
            injectionProviders.setTypeOfInjectionPoint(parameterType, kindOfInjectionPoint);

            String parameterName = ParameterNames.getName(testedClassDesc, constructorDesc, i);
            Annotation[] appliedAnnotations = parameterAnnotations[i];
            InjectionProvider provider = findOrCreateInjectionProvider(parameterType, parameterName,
                    appliedAnnotations);

            if (provider == null || providersFound.contains(provider)) {
                printParameterOfCandidateConstructorIfRequested(parameterName, provider);
                return null;
            }

            providersFound.add(provider);
        }

        if (varArgs) {
            Type parameterType = parameterTypes[n];
            InjectionProvider injectable = hasInjectedValuesForVarargsParameter(parameterType, kindOfInjectionPoint,
                    injectionProviders);

            if (injectable != null) {
                providersFound.add(injectable);
            }
        }

        return providersFound;
    }

    @Nullable
    private InjectionProvider findOrCreateInjectionProvider(@NonNull Type parameterType, @Nullable String parameterName,
            @NonNull Annotation[] parameterAnnotations) {
        String qualifiedName = getQualifiedName(parameterAnnotations);

        if (parameterName == null && qualifiedName == null) {
            return null;
        }

        boolean qualified = qualifiedName != null;
        String targetName = qualified ? qualifiedName : parameterName;
        InjectionProvider provider = injectionState.injectionProviders.getProviderByTypeAndOptionallyName(targetName,
                testedClass);

        if (provider != null) {
            return provider;
        }

        InjectionPoint injectionPoint = new InjectionPoint(parameterType, targetName, qualifiedName);
        Object valueForParameter = injectionState.getTestedValue(testedClass, injectionPoint);

        if (valueForParameter == null && !withFullInjection) {
            return null;
        }

        return new ConstructorParameter(parameterType, parameterAnnotations, targetName, valueForParameter);
    }

    @Nullable
    private InjectionProvider hasInjectedValuesForVarargsParameter(@NonNull Type parameterType,
            @NonNull KindOfInjectionPoint kindOfInjectionPoint, @NonNull InjectionProviders injectionProviders) {
        Type varargsElementType = getTypeOfInjectionPointFromVarargsParameter(parameterType);
        injectionProviders.setTypeOfInjectionPoint(varargsElementType, kindOfInjectionPoint);
        return injectionProviders.findNextInjectableForInjectionPoint(testedClass);
    }

    private void selectConstructorWithUnresolvedParameterIfMoreAccessible(
            @Nullable Constructor<?> unresolvedConstructor, List<InjectionProvider> incompleteProviders) {
        if (unresolvedConstructor != null
                && (constructor == null || compareAccessibility(unresolvedConstructor, constructor) < 0)) {
            constructor = unresolvedConstructor;
            parameterProviders = incompleteProviders;
        }
    }

    // Methods used only when no satisfiable constructor is found //////////////////////////////////////////////////////

    @NonNull
    public String getDescription() {
        searchResults = new StringBuilder();
        findConstructorToUse();
        String contents = searchResults.toString();
        searchResults = null;
        return contents;
    }

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    private void printCandidateConstructorNameIfRequested(@NonNull Constructor<?> candidate) {
        if (searchResults != null) {
            String constructorDesc = candidate.toGenericString().replace("java.lang.", "").replace(",", ", ");
            searchResults.append("\r\n  ").append(constructorDesc).append("\r\n");
        }
    }

    private void printParameterOfCandidateConstructorIfRequested(@Nullable String parameterName,
            @Nullable InjectionProvider injectableFound) {
        if (searchResults != null) {
            searchResults.append("    disregarded because ");

            if (parameterName == null) {
                searchResults.append("parameter names are not available");
            } else {
                searchResults.append("no tested/injectable value was found for parameter \"").append(parameterName)
                        .append('"');

                if (injectableFound != null) {
                    searchResults.append(" that hadn't been used already");
                }
            }
        }
    }
}
