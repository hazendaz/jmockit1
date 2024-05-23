/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.util.List;
import java.util.Map;

import mockit.internal.expectations.argumentMatching.AlwaysTrueMatcher;
import mockit.internal.expectations.argumentMatching.ArgumentMatcher;
import mockit.internal.expectations.argumentMatching.ArgumentMismatch;
import mockit.internal.expectations.argumentMatching.EqualityMatcher;
import mockit.internal.expectations.argumentMatching.HamcrestAdapter;
import mockit.internal.expectations.argumentMatching.ReflectiveMatcher;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class ArgumentValuesAndMatchers {
    @NonNull
    final InvocationArguments signature;
    @NonNull
    Object[] values;
    @Nullable
    List<ArgumentMatcher<?>> matchers;

    ArgumentValuesAndMatchers(@NonNull InvocationArguments signature, @NonNull Object[] values) {
        this.signature = signature;
        this.values = values;
    }

    final void setValuesWithNoMatchers(@NonNull Object[] argsToVerify) {
        setValuesAndMatchers(argsToVerify, null);
    }

    @NonNull
    final Object[] prepareForVerification(@NonNull Object[] argsToVerify,
            @Nullable List<ArgumentMatcher<?>> matchersToUse) {
        Object[] replayArgs = values;
        setValuesAndMatchers(argsToVerify, matchersToUse);
        return replayArgs;
    }

    final void setValuesAndMatchers(@NonNull Object[] argsToVerify, @Nullable List<ArgumentMatcher<?>> matchersToUse) {
        values = argsToVerify;
        matchers = matchersToUse;
    }

    @Nullable
    final ArgumentMatcher<?> getArgumentMatcher(@NonNegative int parameterIndex) {
        if (matchers == null) {
            return null;
        }

        ArgumentMatcher<?> matcher = parameterIndex < matchers.size() ? matchers.get(parameterIndex) : null;

        if (matcher == null && parameterIndex < values.length && values[parameterIndex] == null) {
            matcher = AlwaysTrueMatcher.ANY_VALUE;
        }

        return matcher;
    }

    abstract boolean isMatch(@NonNull Object[] replayArgs, @NonNull Map<Object, Object> instanceMap);

    static boolean areEqual(@NonNull Object[] expectedValues, @NonNull Object[] actualValues, @NonNegative int count,
            @NonNull Map<Object, Object> instanceMap) {
        for (int i = 0; i < count; i++) {
            if (isNotEqual(expectedValues[i], actualValues[i], instanceMap)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isNotEqual(@Nullable Object expected, @Nullable Object actual,
            @NonNull Map<Object, Object> instanceMap) {
        return actual == null == (expected != null) || actual != null && actual != expected
                && expected != instanceMap.get(actual) && !EqualityMatcher.areEqualWhenNonNull(actual, expected);
    }

    abstract boolean hasEquivalentMatchers(@NonNull ArgumentValuesAndMatchers other);

    private static boolean equivalentMatches(@NonNull ArgumentMatcher<?> matcher1, @Nullable Object arg1,
            @NonNull ArgumentMatcher<?> matcher2, @Nullable Object arg2) {
        boolean matcher1MatchesArg2 = matcher1.matches(arg2);
        boolean matcher2MatchesArg1 = matcher2.matches(arg1);

        if (arg1 != null && arg2 != null && matcher1MatchesArg2 && matcher2MatchesArg1) {
            return true;
        }

        if (arg1 == arg2 && matcher1MatchesArg2 == matcher2MatchesArg1) { // both matchers fail
            ArgumentMismatch desc1 = new ArgumentMismatch();
            matcher1.writeMismatchPhrase(desc1);
            ArgumentMismatch desc2 = new ArgumentMismatch();
            matcher2.writeMismatchPhrase(desc2);
            return desc1.toString().equals(desc2.toString());
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    final <M1 extends ArgumentMatcher<M1>, M2 extends ArgumentMatcher<M2>> int indexOfFirstValueAfterEquivalentMatchers(
            @NonNull ArgumentValuesAndMatchers other) {
        List<ArgumentMatcher<?>> otherMatchers = other.matchers;

        if (hasDifferentAmountOfMatchers(otherMatchers)) {
            return -1;
        }

        // noinspection ConstantConditions
        int m = matchers.size();
        int i;

        for (i = 0; i < m; i++) {
            M1 matcher1 = (M1) matchers.get(i);
            M2 matcher2 = (M2) otherMatchers.get(i);

            if (matcher1 == null || matcher2 == null) {
                if (!EqualityMatcher.areEqual(values[i], other.values[i])) {
                    return -1;
                }
            } else if (matcher1 != matcher2 && (matcher1.getClass() != matcher2.getClass()
                    || !matcher1.same((M1) matcher2) && areNonEquivalentMatches(other, matcher1, matcher2, i))) {
                return -1;
            }
        }

        return i;
    }

    private boolean hasDifferentAmountOfMatchers(@Nullable List<ArgumentMatcher<?>> otherMatchers) {
        return otherMatchers == null || matchers == null || otherMatchers.size() != matchers.size();
    }

    private boolean areNonEquivalentMatches(@NonNull ArgumentValuesAndMatchers other,
            @NonNull ArgumentMatcher<?> matcher1, @NonNull ArgumentMatcher<?> matcher2, @NonNegative int matcherIndex) {
        Class<?> matcherClass = matcher1.getClass();
        return matcherClass == ReflectiveMatcher.class || matcherClass == HamcrestAdapter.class
                || !equivalentMatches(matcher1, values[matcherIndex], matcher2, other.values[matcherIndex]);
    }

    @NonNull
    final String toString(@NonNull List<String> parameterTypes) {
        ArgumentMismatch desc = new ArgumentMismatch();
        int parameterCount = values.length;

        if (parameterCount > 0) {
            if (matchers == null) {
                desc.appendFormatted(values);
            } else {
                String sep = "";

                for (int i = 0; i < parameterCount; i++) {
                    ArgumentMatcher<?> matcher = getArgumentMatcher(i);
                    String parameterType = parameterTypes.get(i);
                    desc.append(sep).appendFormatted(parameterType, values[i], matcher);
                    sep = ", ";
                }
            }

            desc.append(')');
        }

        return desc.toString();
    }
}
