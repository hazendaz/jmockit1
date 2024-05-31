/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;

import mockit.asm.jvmConstants.Access;
import mockit.internal.expectations.argumentMatching.ArgumentMatcher;
import mockit.internal.expectations.state.ExecutingTest;
import mockit.internal.reflection.RealMethodOrConstructor;
import mockit.internal.state.TestRun;
import mockit.internal.util.MethodFormatter;

public final class InvocationArguments {
    @NonNull
    final String classDesc;
    @NonNull
    final String methodNameAndDesc;
    @Nullable
    final String genericSignature;
    @NonNull
    private final ArgumentValuesAndMatchers valuesAndMatchers;
    @Nullable
    private Member realMethodOrConstructor;

    InvocationArguments(int access, @NonNull String classDesc, @NonNull String methodNameAndDesc,
            @Nullable String genericSignature, @NonNull Object[] args) {
        this.classDesc = classDesc;
        this.methodNameAndDesc = methodNameAndDesc;
        this.genericSignature = genericSignature;
        valuesAndMatchers = (access & Access.VARARGS) == 0 ? new ArgumentValuesAndMatchersWithoutVarargs(this, args)
                : new ArgumentValuesAndMatchersWithVarargs(this, args);
    }

    @NonNull
    String getClassName() {
        return classDesc.replace('/', '.');
    }

    boolean isForConstructor() {
        return methodNameAndDesc.charAt(0) == '<';
    }

    @NonNull
    public Object[] getValues() {
        return valuesAndMatchers.values;
    }

    void setValues(@NonNull Object[] values) {
        valuesAndMatchers.values = values;
    }

    public void setValuesWithNoMatchers(@NonNull Object[] argsToVerify) {
        valuesAndMatchers.setValuesWithNoMatchers(argsToVerify);
    }

    public void setValuesAndMatchers(@NonNull Object[] argsToVerify, @Nullable List<ArgumentMatcher<?>> matchers) {
        valuesAndMatchers.setValuesAndMatchers(argsToVerify, matchers);
    }

    @Nullable
    public List<ArgumentMatcher<?>> getMatchers() {
        return valuesAndMatchers.matchers;
    }

    public void setMatchers(@Nullable List<ArgumentMatcher<?>> matchers) {
        valuesAndMatchers.matchers = matchers;
    }

    @NonNull
    public Object[] prepareForVerification(@NonNull Object[] argsToVerify,
            @Nullable List<ArgumentMatcher<?>> matchers) {
        return valuesAndMatchers.prepareForVerification(argsToVerify, matchers);
    }

    public boolean isMatch(@NonNull Object[] replayArgs, @NonNull Map<Object, Object> instanceMap) {
        TestRun.enterNoMockingZone();
        ExecutingTest executingTest = TestRun.getExecutingTest();
        boolean previousFlag = executingTest.setShouldIgnoreMockingCallbacks(true);

        try {
            return valuesAndMatchers.isMatch(replayArgs, instanceMap);
        } finally {
            executingTest.setShouldIgnoreMockingCallbacks(previousFlag);
            TestRun.exitNoMockingZone();
        }
    }

    @Override
    public String toString() {
        MethodFormatter methodFormatter = new MethodFormatter(classDesc, methodNameAndDesc, false);
        List<String> parameterTypes = methodFormatter.getParameterTypes();
        String arguments = valuesAndMatchers.toString(parameterTypes);
        methodFormatter.append(arguments);
        return methodFormatter.toString();
    }

    public boolean hasEquivalentMatchers(@NonNull InvocationArguments other) {
        return valuesAndMatchers.hasEquivalentMatchers(other.valuesAndMatchers);
    }

    @NonNull
    Member getRealMethodOrConstructor() {
        if (realMethodOrConstructor == null) {
            try {
                realMethodOrConstructor = new RealMethodOrConstructor(getClassName(), methodNameAndDesc).getMember();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        return realMethodOrConstructor;
    }
}
