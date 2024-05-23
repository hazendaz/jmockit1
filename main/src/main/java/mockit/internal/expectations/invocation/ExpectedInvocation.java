/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import static mockit.internal.util.TypeDescriptor.getClassForType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import mockit.asm.types.JavaType;
import mockit.internal.expectations.argumentMatching.ArgumentMatcher;
import mockit.internal.expectations.state.MockedTypeCascade;
import mockit.internal.reflection.GenericTypeReflection;
import mockit.internal.reflection.GenericTypeReflection.GenericSignature;
import mockit.internal.state.TestRun;
import mockit.internal.util.ClassLoad;
import mockit.internal.util.DefaultValues;
import mockit.internal.util.ObjectMethods;
import mockit.internal.util.StackTrace;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("OverlyComplexClass")
public final class ExpectedInvocation {
    @NonNull
    private static final Object UNDEFINED_DEFAULT_RETURN = new Object();

    @Nullable
    public final Object instance;
    @Nullable
    public Object replacementInstance;
    public boolean matchInstance;
    @NonNull
    public final InvocationArguments arguments;
    @Nullable
    private final ExpectationError invocationCause;
    @Nullable
    Object defaultReturnValue;

    public ExpectedInvocation(@Nullable Object mock, @NonNull String mockedClassDesc, @NonNull String mockNameAndDesc,
            @Nullable String genericSignature, @NonNull Object[] args) {
        instance = mock;
        arguments = new InvocationArguments(0, mockedClassDesc, mockNameAndDesc, genericSignature, args);
        invocationCause = null;
        defaultReturnValue = determineDefaultReturnValueFromMethodSignature();
    }

    public ExpectedInvocation(@Nullable Object mock, int access, @NonNull String mockedClassDesc,
            @NonNull String mockNameAndDesc, boolean matchInstance, @Nullable String genericSignature,
            @NonNull Object[] args) {
        instance = mock;
        this.matchInstance = matchInstance;
        arguments = new InvocationArguments(access, mockedClassDesc, mockNameAndDesc, genericSignature, args);
        invocationCause = new ExpectationError();
        defaultReturnValue = determineDefaultReturnValueFromMethodSignature();
    }

    @Nullable
    public AssertionError getInvocationCause() {
        return invocationCause;
    }

    @NonNull
    private Object determineDefaultReturnValueFromMethodSignature() {
        if (instance != null) {
            Object rv = ObjectMethods.evaluateOverride(instance, getMethodNameAndDescription(), getArgumentValues());

            if (rv != null) {
                return rv;
            }
        }

        return UNDEFINED_DEFAULT_RETURN;
    }

    // Simple getters
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @NonNull
    public String getClassDesc() {
        return arguments.classDesc;
    }

    @NonNull
    public String getClassName() {
        return arguments.getClassName();
    }

    @NonNull
    public String getMethodNameAndDescription() {
        return arguments.methodNameAndDesc;
    }

    @NonNull
    public Object[] getArgumentValues() {
        return arguments.getValues();
    }

    public boolean isConstructor() {
        return arguments.isForConstructor();
    }

    @NonNull
    public String getSignatureWithResolvedReturnType() {
        String signature = arguments.genericSignature;

        if (signature != null) {
            // TODO: cache it for use in return type conversion, cascading, etc.
            String classDesc = getClassDesc();
            Class<?> mockedClass = instance != null ? instance.getClass() : ClassLoad.loadByInternalName(classDesc);
            GenericTypeReflection reflection = new GenericTypeReflection(mockedClass, null);
            signature = reflection.resolveSignature(classDesc, signature);

            char firstTypeChar = signature.charAt(signature.indexOf(')') + 1);

            if (firstTypeChar != 'T' && firstTypeChar != '[') {
                return signature;
            }
        }

        return arguments.methodNameAndDesc;
    }

    // Matching based on instance or mocked type
    // ///////////////////////////////////////////////////////////////////////////////////////////

    public boolean isMatch(@Nullable Object mock, @NonNull String invokedClassDesc, @NonNull String invokedMethod) {
        return (invokedClassDesc.equals(getClassDesc()) || mock != null && TestRun.mockFixture().isCaptured(mock))
                && (isMatchingGenericMethod(mock, invokedMethod) || isMatchingMethod(invokedMethod));
    }

    private boolean isMatchingGenericMethod(@Nullable Object mock, @NonNull String invokedMethod) {
        if (mock != null && instance != null) {
            String genericSignature = arguments.genericSignature;

            if (genericSignature != null) {
                Class<?> mockedClass = mock.getClass();

                if (mockedClass != instance.getClass()) {
                    GenericTypeReflection typeReflection = new GenericTypeReflection(mockedClass, null);
                    GenericSignature parsedSignature = typeReflection.parseSignature(genericSignature);
                    return parsedSignature.satisfiesSignature(invokedMethod) && isMatchingMethodName(invokedMethod);
                }
            }
        }

        return false;
    }

    private boolean isMatchingMethod(@NonNull String invokedMethod) {
        int returnTypeStartPos = getReturnTypePosition(invokedMethod);

        if (returnTypeStartPos < 0) {
            return false;
        }

        if (haveSameReturnTypes(invokedMethod, returnTypeStartPos)) {
            return true;
        }

        // At this point the methods are known to differ only in return type, so check if the return type of
        // the recorded one is assignable to the return type of the one invoked:
        return isReturnTypeOfRecordedMethodAssignableToReturnTypeOfInvokedMethod(invokedMethod, returnTypeStartPos);
    }

    private boolean isMatchingMethodName(@NonNull String invokedMethod) {
        int methodNameEndPos = invokedMethod.indexOf('(');
        String methodName = invokedMethod.substring(0, methodNameEndPos + 1);
        return getMethodNameAndDescription().startsWith(methodName);
    }

    // Returns -1 if the method names or parameters are different.
    private int getReturnTypePosition(@NonNull String invokedMethod) {
        String recordedMethod = getMethodNameAndDescription();
        int i = 0;

        while (true) {
            char c = recordedMethod.charAt(i);

            if (c != invokedMethod.charAt(i)) {
                return -1;
            }

            i++;

            if (c == ')') {
                return i;
            }
        }
    }

    private boolean haveSameReturnTypes(@NonNull String invokedMethod, @NonNegative int returnTypeStartPos) {
        String recordedMethod = getMethodNameAndDescription();
        int n = invokedMethod.length();

        if (n != recordedMethod.length()) {
            return false;
        }

        int j = returnTypeStartPos;

        while (true) {
            char c = recordedMethod.charAt(j);

            if (c != invokedMethod.charAt(j)) {
                return false;
            }

            j++;

            if (j == n) {
                return true;
            }
        }
    }

    private boolean isReturnTypeOfRecordedMethodAssignableToReturnTypeOfInvokedMethod(@NonNull String invokedMethod,
            @NonNegative int returnTypeStartPos) {
        String recordedMethod = getMethodNameAndDescription();
        JavaType recordedRT = JavaType.getType(recordedMethod.substring(returnTypeStartPos));
        JavaType invokedRT = JavaType.getType(invokedMethod.substring(returnTypeStartPos));

        return getClassForType(invokedRT).isAssignableFrom(getClassForType(recordedRT));
    }

    public boolean isMatch(@NonNull ExpectedInvocation other) {
        return isMatch(other.instance, other.getClassDesc(), other.getMethodNameAndDescription(), null);
    }

    public boolean isMatch(@Nullable Object replayInstance, @NonNull String invokedClassDesc,
            @NonNull String invokedMethod, @Nullable Map<Object, Object> replacementMap) {
        return isMatch(replayInstance, invokedClassDesc, invokedMethod) && (arguments.isForConstructor()
                || !matchInstance || isEquivalentInstance(replayInstance, replacementMap));
    }

    private boolean isEquivalentInstance(@Nullable Object mockedInstance,
            @Nullable Map<Object, Object> replacementMap) {
        return mockedInstance == instance || mockedInstance != null && instance != null && replacementMap != null
                && replacementMap.get(mockedInstance) == instance;
    }

    // Creation of Error instances for invocation mismatch reporting
    // ///////////////////////////////////////////////////////////////////////

    @NonNull
    public UnexpectedInvocation errorForUnexpectedInvocation() {
        String initialMessage = "Unexpected invocation of " + this;
        return newUnexpectedInvocationWithCause("Unexpected invocation", initialMessage);
    }

    @NonNull
    private UnexpectedInvocation newUnexpectedInvocationWithCause(@NonNull String titleForCause,
            @NonNull String initialMessage) {
        UnexpectedInvocation error = new UnexpectedInvocation(initialMessage);
        setErrorAsInvocationCause(titleForCause, error);
        return error;
    }

    private void setErrorAsInvocationCause(@NonNull String titleForCause, @NonNull Throwable error) {
        if (invocationCause != null) {
            invocationCause.defineCause(titleForCause, error);
        }
    }

    @NonNull
    public MissingInvocation errorForMissingInvocation(@NonNull List<ExpectedInvocation> nonMatchingInvocations) {
        StringBuilder errorMessage = new StringBuilder(200);
        errorMessage.append("Missing invocation to:\n").append(this);
        appendNonMatchingInvocations(errorMessage, nonMatchingInvocations);

        return newMissingInvocationWithCause("Missing invocation", errorMessage.toString());
    }

    @NonNull
    public MissingInvocation errorForMissingInvocations(@NonNegative int missingInvocations,
            @NonNull List<ExpectedInvocation> nonMatchingInvocations) {
        StringBuilder errorMessage = new StringBuilder(200);
        errorMessage.append("Missing ").append(missingInvocations).append(invocationsTo(missingInvocations))
                .append(this);
        appendNonMatchingInvocations(errorMessage, nonMatchingInvocations);

        return newMissingInvocationWithCause("Missing invocations", errorMessage.toString());
    }

    private void appendNonMatchingInvocations(@NonNull StringBuilder errorMessage,
            @NonNull List<ExpectedInvocation> nonMatchingInvocations) {
        if (!nonMatchingInvocations.isEmpty()) {
            errorMessage.append("\ninstead got:\n");
            String sep = "";

            for (ExpectedInvocation nonMatchingInvocation : nonMatchingInvocations) {
                String invocationDescription = nonMatchingInvocation.toString(instance);
                errorMessage.append(sep).append(invocationDescription);
                sep = "\n";
                nonMatchingInvocation.printCause(errorMessage);
            }
        }
    }

    @NonNull
    private MissingInvocation newMissingInvocationWithCause(@NonNull String titleForCause,
            @NonNull String initialMessage) {
        MissingInvocation error = new MissingInvocation(initialMessage);
        setErrorAsInvocationCause(titleForCause, error);
        return error;
    }

    @NonNull
    private static String invocationsTo(@NonNegative int invocations) {
        return invocations == 1 ? " invocation to:\n" : " invocations to:\n";
    }

    @NonNull
    public UnexpectedInvocation errorForUnexpectedInvocation(@NonNull Object[] replayArgs) {
        String message = "Unexpected invocation to:\n" + toString(replayArgs);
        return newUnexpectedInvocationWithCause("Unexpected invocation", message);
    }

    @NonNull
    public UnexpectedInvocation errorForUnexpectedInvocations(@NonNull Object[] replayArgs, int numUnexpected) {
        String message = numUnexpected + " unexpected" + invocationsTo(numUnexpected) + toString(replayArgs);
        String titleForCause = numUnexpected == 1 ? "Unexpected invocation" : "Unexpected invocations";
        return newUnexpectedInvocationWithCause(titleForCause, message);
    }

    @NonNull
    @Override
    public String toString() {
        return toString((Object) null);
    }

    @NonNull
    public String toString(@Nullable Object otherInstance) {
        StringBuilder desc = new StringBuilder().append(arguments.toString());

        if (instance != otherInstance && instance != null) {
            desc.append("\n   on mock instance: ").append(ObjectMethods.objectIdentity(instance));
        }

        return desc.toString();
    }

    @NonNull
    String toString(@NonNull Object[] actualInvocationArguments) {
        Object[] invocationArgs = arguments.getValues();
        List<ArgumentMatcher<?>> matchers = arguments.getMatchers();
        arguments.setValues(actualInvocationArguments);
        arguments.setMatchers(null);
        String description = toString();
        arguments.setMatchers(matchers);
        arguments.setValues(invocationArgs);
        return description;
    }

    private void printCause(@NonNull Appendable errorMessage) {
        if (invocationCause != null) {
            try {
                errorMessage.append('\n');
            } catch (IOException ignore) {
            }

            StackTrace st = new StackTrace(invocationCause);
            st.filter();
            st.print(errorMessage);
        }
    }

    // Default result
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Nullable
    public Object getDefaultValueForReturnType() {
        if (defaultReturnValue == UNDEFINED_DEFAULT_RETURN) {
            Class<?> resolvedReturnType = getReturnTypeAsResolvedFromClassArgument();

            if (resolvedReturnType != null) {
                defaultReturnValue = DefaultValues.computeForType(resolvedReturnType);

                if (defaultReturnValue == null) {
                    String returnTypeDesc = 'L' + resolvedReturnType.getName().replace('.', '/') + ';';
                    String mockedTypeDesc = getClassDesc();
                    defaultReturnValue = MockedTypeCascade.getMock(mockedTypeDesc, arguments.methodNameAndDesc,
                            instance, returnTypeDesc, resolvedReturnType);
                }

                return defaultReturnValue;
            }

            String returnTypeDesc = DefaultValues.getReturnTypeDesc(arguments.methodNameAndDesc);

            if ("V".equals(returnTypeDesc)) {
                return null;
            }

            defaultReturnValue = DefaultValues.computeForType(returnTypeDesc);

            if (defaultReturnValue == null) {
                String mockedTypeDesc = getClassDesc();
                defaultReturnValue = MockedTypeCascade.getMock(mockedTypeDesc, arguments.methodNameAndDesc, instance,
                        returnTypeDesc, arguments.genericSignature);
            }
        }

        return defaultReturnValue;
    }

    @Nullable
    private Class<?> getReturnTypeAsResolvedFromClassArgument() {
        String genericSignature = arguments.genericSignature;

        if (genericSignature != null) {
            int returnTypePos = genericSignature.lastIndexOf(')') + 1;
            char c = genericSignature.charAt(returnTypePos);

            if (c == 'T') {
                for (Object arg : arguments.getValues()) {
                    if (arg instanceof Class<?>) {
                        return (Class<?>) arg;
                    }
                }
            }
        }

        return null;
    }

    public void copyDefaultReturnValue(@NonNull ExpectedInvocation other) {
        defaultReturnValue = other.defaultReturnValue;
    }
}
