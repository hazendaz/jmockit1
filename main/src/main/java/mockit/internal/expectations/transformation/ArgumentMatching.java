/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.transformation;

import static mockit.asm.jvmConstants.Opcodes.GETFIELD;
import static mockit.asm.jvmConstants.Opcodes.SIPUSH;

import mockit.asm.methods.MethodWriter;
import mockit.asm.types.JavaType;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

final class ArgumentMatching {
    private static final JavaType[] NO_PARAMETERS = {};
    private static final String ANY_FIELDS = "any anyString anyInt anyBoolean anyLong anyDouble anyFloat anyChar anyShort anyByte";
    private static final String WITH_METHODS = "with(Lmockit/Delegate;)Ljava/lang/Object; "
            + "withAny(Ljava/lang/Object;)Ljava/lang/Object; "
            + "withArgThat(Lorg/hamcrest/Matcher;)Ljava/lang/Object; "
            + "withCapture()Ljava/lang/Object; withCapture(Ljava/util/List;)Ljava/lang/Object; "
            + "withCapture(Ljava/lang/Object;)Ljava/util/List; "
            + "withEqual(Ljava/lang/Object;)Ljava/lang/Object; withEqual(DD)D withEqual(FD)F "
            + "withInstanceLike(Ljava/lang/Object;)Ljava/lang/Object; "
            + "withInstanceOf(Ljava/lang/Class;)Ljava/lang/Object; "
            + "withNotEqual(Ljava/lang/Object;)Ljava/lang/Object; "
            + "withNull()Ljava/lang/Object; withNotNull()Ljava/lang/Object; "
            + "withSameInstance(Ljava/lang/Object;)Ljava/lang/Object; "
            + "withSubstring(Ljava/lang/CharSequence;)Ljava/lang/CharSequence; "
            + "withPrefix(Ljava/lang/CharSequence;)Ljava/lang/CharSequence; "
            + "withSuffix(Ljava/lang/CharSequence;)Ljava/lang/CharSequence; "
            + "withMatch(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;";

    @NonNull
    private final InvocationBlockModifier modifier;

    // Helper fields that allow argument matchers to be moved to the correct positions of their corresponding
    // parameters:
    @NonNull
    private final int[] matcherStacks;
    @NonNegative
    private int matcherCount;
    @NonNull
    private JavaType[] parameterTypes;

    static boolean isAnyField(@NonNull String name) {
        return name.startsWith("any") && ANY_FIELDS.contains(name);
    }

    static boolean isCallToArgumentMatcher(@NonNull String name, @NonNull String desc) {
        return name.startsWith("with") && WITH_METHODS.contains(name + desc);
    }

    ArgumentMatching(@NonNull InvocationBlockModifier modifier) {
        this.modifier = modifier;
        matcherStacks = new int[40];
        parameterTypes = NO_PARAMETERS;
    }

    void addMatcher(@NonNegative int stackSize) {
        matcherStacks[matcherCount++] = stackSize;
    }

    @NonNegative
    int getMatcherCount() {
        return matcherCount;
    }

    @NonNull
    JavaType getParameterType(@NonNegative int parameterIndex) {
        return parameterTypes[parameterIndex];
    }

    void generateCodeToAddArgumentMatcherForAnyField(@NonNull String fieldOwner, @NonNull String name,
            @NonNull String desc) {
        MethodWriter mw = modifier.getMethodWriter();
        mw.visitFieldInsn(GETFIELD, fieldOwner, name, desc);
        modifier.generateCallToActiveInvocationsMethod(name);
    }

    boolean handleInvocationParameters(@NonNegative int stackSize, @NonNull String desc) {
        parameterTypes = JavaType.getArgumentTypes(desc);
        int stackAfter = stackSize - getSumOfParameterSizes();
        boolean mockedInvocationUsingTheMatchers = stackAfter < matcherStacks[0];

        if (mockedInvocationUsingTheMatchers) {
            generateCallsToMoveArgMatchers(stackAfter);
            modifier.argumentCapturing.generateCallsToSetArgumentTypesToCaptureIfAny();
            matcherCount = 0;
        }

        return mockedInvocationUsingTheMatchers;
    }

    @NonNegative
    private int getSumOfParameterSizes() {
        @NonNegative
        int sum = 0;

        for (JavaType argType : parameterTypes) {
            sum += argType.getSize();
        }

        return sum;
    }

    private void generateCallsToMoveArgMatchers(@NonNegative int initialStack) {
        @NonNegative
        int stack = initialStack;
        @NonNegative
        int nextMatcher = 0;
        @NonNegative
        int matcherStack = matcherStacks[0];

        for (int i = 0; i < parameterTypes.length && nextMatcher < matcherCount; i++) {
            stack += parameterTypes[i].getSize();

            if (stack == matcherStack || stack == matcherStack + 1) {
                if (nextMatcher < i) {
                    generateCallToMoveArgMatcher(nextMatcher, i);
                    modifier.argumentCapturing.updateCaptureIfAny(nextMatcher, i);
                }

                nextMatcher++;
                matcherStack = matcherStacks[nextMatcher];
            }
        }
    }

    private void generateCallToMoveArgMatcher(@NonNegative int originalMatcherIndex, @NonNegative int toIndex) {
        MethodWriter mw = modifier.getMethodWriter();
        mw.visitIntInsn(SIPUSH, originalMatcherIndex);
        mw.visitIntInsn(SIPUSH, toIndex);
        modifier.generateCallToActiveInvocationsMethod("moveArgMatcher", "(II)V");
    }
}
