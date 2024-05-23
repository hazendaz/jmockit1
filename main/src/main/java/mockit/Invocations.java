/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import mockit.internal.expectations.TestOnlyPhase;
import mockit.internal.expectations.argumentMatching.AlwaysTrueMatcher;
import mockit.internal.expectations.argumentMatching.ArgumentMatcher;
import mockit.internal.expectations.argumentMatching.CaptureMatcher;
import mockit.internal.expectations.argumentMatching.ClassMatcher;
import mockit.internal.expectations.argumentMatching.HamcrestAdapter;
import mockit.internal.expectations.argumentMatching.InequalityMatcher;
import mockit.internal.expectations.argumentMatching.LenientEqualityMatcher;
import mockit.internal.expectations.argumentMatching.NonNullityMatcher;
import mockit.internal.expectations.argumentMatching.NullityMatcher;
import mockit.internal.expectations.argumentMatching.NumericEqualityMatcher;
import mockit.internal.expectations.argumentMatching.PatternMatcher;
import mockit.internal.expectations.argumentMatching.ReflectiveMatcher;
import mockit.internal.expectations.argumentMatching.SamenessMatcher;
import mockit.internal.expectations.argumentMatching.StringContainmentMatcher;
import mockit.internal.expectations.argumentMatching.StringPrefixMatcher;
import mockit.internal.expectations.argumentMatching.StringSuffixMatcher;
import mockit.internal.startup.Startup;
import mockit.internal.util.DefaultValues;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hamcrest.Matcher;

/**
 * Provides common API for use inside {@linkplain Expectations expectation} and {@linkplain Verifications verification}
 * blocks.
 */
@SuppressWarnings("ClassWithTooManyFields")
class Invocations {
    static {
        Startup.verifyInitialization();
    }

    /**
     * Matches any <code>Object</code> reference received by a parameter of a reference type.
     * <p>
     * This field can only be used as the argument value at the proper parameter position in a method/constructor
     * invocation, when recording or verifying an expectation; it cannot be used anywhere else.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     * <p>
     * Notice the use of this field will usually require a cast to the specific parameter type. However, if there is any
     * other parameter for which an argument matching constraint is specified, passing the <code>null</code> reference
     * instead will have the same effect.
     * <p>
     * To match an entire <em>varargs</em> parameter of element type <code>V</code> (ie, all arguments in the array),
     * cast it to the parameter type: "<code>(V[]) any</code>".
     *
     * @see #anyBoolean
     * @see #anyByte
     * @see #anyChar
     * @see #anyDouble
     * @see #anyFloat
     * @see #anyInt
     * @see #anyLong
     * @see #anyShort
     * @see #anyString
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#matcherFields" target="tutorial">Tutorial</a>
     */
    @Nullable
    protected final Object any = null;

    /**
     * Matches any <code>String</code> value received by a parameter of this type.
     * <p>
     * This field can only be used as the argument value at the proper parameter position in a method/constructor
     * invocation, when recording or verifying an expectation; it cannot be used anywhere else.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @see #anyBoolean
     * @see #anyByte
     * @see #anyChar
     * @see #anyDouble
     * @see #anyFloat
     * @see #anyInt
     * @see #anyLong
     * @see #anyShort
     * @see #any
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#matcherFields" target="tutorial">Tutorial</a>
     */
    // This is intentional: the empty string causes the compiler to not generate a field read, while the null reference
    // is inconvenient with
    // the invoke(...) methods:
    @NonNull
    protected final String anyString = new String();

    /**
     * Matches any <code>long</code> or <code>Long</code> value received by a parameter of that type.
     * <p>
     * This field can only be used as the argument value at the proper parameter position in a method/constructor
     * invocation, when recording or verifying an expectation; it cannot be used anywhere else.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @see #anyBoolean
     * @see #anyByte
     * @see #anyChar
     * @see #anyDouble
     * @see #anyFloat
     * @see #anyInt
     * @see #anyShort
     * @see #anyString
     * @see #any
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#matcherFields" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final Long anyLong = 0L;

    /**
     * Matches any <code>int</code> or <code>Integer</code> value received by a parameter of that type.
     * <p>
     * This field can only be used as the argument value at the proper parameter position in a method/constructor
     * invocation, when recording or verifying an expectation; it cannot be used anywhere else.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @see #anyBoolean
     * @see #anyByte
     * @see #anyChar
     * @see #anyDouble
     * @see #anyFloat
     * @see #anyLong
     * @see #anyShort
     * @see #anyString
     * @see #any
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#matcherFields" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final Integer anyInt = 0;

    /**
     * Matches any <code>short</code> or <code>Short</code> value received by a parameter of that type.
     * <p>
     * This field can only be used as the argument value at the proper parameter position in a method/constructor
     * invocation, when recording or verifying an expectation; it cannot be used anywhere else.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @see #anyBoolean
     * @see #anyByte
     * @see #anyChar
     * @see #anyDouble
     * @see #anyFloat
     * @see #anyInt
     * @see #anyLong
     * @see #anyString
     * @see #any
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#matcherFields" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final Short anyShort = 0;

    /**
     * Matches any <code>byte</code> or <code>Byte</code> value received by a parameter of that type.
     * <p>
     * This field can only be used as the argument value at the proper parameter position in a method/constructor
     * invocation, when recording or verifying an expectation; it cannot be used anywhere else.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @see #anyBoolean
     * @see #anyChar
     * @see #anyDouble
     * @see #anyFloat
     * @see #anyInt
     * @see #anyLong
     * @see #anyShort
     * @see #anyString
     * @see #any
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#matcherFields" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final Byte anyByte = 0;

    /**
     * Matches any <code>boolean</code> or <code>Boolean</code> value received by a parameter of that type.
     * <p>
     * This field can only be used as the argument value at the proper parameter position in a method/constructor
     * invocation, when recording or verifying an expectation; it cannot be used anywhere else.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @see #anyByte
     * @see #anyChar
     * @see #anyDouble
     * @see #anyFloat
     * @see #anyInt
     * @see #anyLong
     * @see #anyShort
     * @see #anyString
     * @see #any
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#matcherFields" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final Boolean anyBoolean = false;

    /**
     * Matches any <code>char</code> or <code>Character</code> value received by a parameter of that type.
     * <p>
     * This field can only be used as the argument value at the proper parameter position in a method/constructor
     * invocation, when recording or verifying an expectation; it cannot be used anywhere else.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @see #anyBoolean
     * @see #anyByte
     * @see #anyDouble
     * @see #anyFloat
     * @see #anyInt
     * @see #anyLong
     * @see #anyShort
     * @see #anyString
     * @see #any
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#matcherFields" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final Character anyChar = '\0';

    /**
     * Matches any <code>double</code> or <code>Double</code> value received by a parameter of that type.
     * <p>
     * This field can only be used as the argument value at the proper parameter position in a method/constructor
     * invocation, when recording or verifying an expectation; it cannot be used anywhere else.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @see #anyBoolean
     * @see #anyByte
     * @see #anyChar
     * @see #anyFloat
     * @see #anyInt
     * @see #anyLong
     * @see #anyShort
     * @see #anyString
     * @see #any
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#matcherFields" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final Double anyDouble = 0.0;

    /**
     * Matches any <code>float</code> or <code>Float</code> value received by a parameter of that type.
     * <p>
     * This field can only be used as the argument value at the proper parameter position in a method/constructor
     * invocation, when recording or verifying an expectation; it cannot be used anywhere else.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @see #anyBoolean
     * @see #anyByte
     * @see #anyChar
     * @see #anyDouble
     * @see #anyInt
     * @see #anyLong
     * @see #anyString
     * @see #anyShort
     * @see #any
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#matcherFields" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final Float anyFloat = 0.0F;

    /**
     * A non-negative value assigned to this field will be taken as the exact number of times that invocations matching
     * the current expectation should occur during replay.
     *
     * @see #minTimes
     * @see #maxTimes
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#constraints" target="tutorial">Tutorial</a>
     */
    @NonNegative
    protected int times;

    /**
     * A positive value assigned to this field will be taken as the minimum number of times that invocations matching
     * the current expectation should occur during replay. <em>Zero</em> or a <em>negative</em> value means there is no
     * lower limit.
     * <p>
     * If not specified, the default value of <code>1</code> (one) is used.
     * <p>
     * The <em>maximum</em> number of times is automatically adjusted to allow any number of invocations. Both
     * <code>minTimes</code> and <code>maxTimes</code> can be specified for the same expectation, provided
     * <code>minTimes</code> is assigned first.
     *
     * @see #times
     * @see #maxTimes
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#constraints" target="tutorial">Tutorial</a>
     */
    protected int minTimes;

    /**
     * A non-negative value assigned to this field will be taken as the maximum number of times that invocations
     * matching the current expectation should occur during replay. A <em>negative</em> value implies there is no upper
     * limit (the default).
     * <p>
     * Both <code>minTimes</code> and <code>maxTimes</code> can be specified for the same expectation, provided
     * <code>minTimes</code> is assigned first.
     *
     * @see #times
     * @see #minTimes
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#constraints" target="tutorial">Tutorial</a>
     */
    protected int maxTimes;

    /** The current phase. */
    @SuppressWarnings("NullableProblems")
    @NonNull
    TestOnlyPhase currentPhase;

    /**
     * Applies a <em>Hamcrest</em> argument matcher for a parameter in the current expectation.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param argumentMatcher
     *            any <em>org.hamcrest.Matcher</em> object
     *
     * @return the value recorded inside the given Hamcrest matcher, or <em>null</em> if there is no such value to be
     *         found
     *
     * @see #with(Delegate) Use {@linkplain #withCapture(List) argument capturing} or {@link #with(Delegate)} instead.
     */
    @Nullable
    protected final <T> T withArgThat(@NonNull Matcher<? super T> argumentMatcher) {
        HamcrestAdapter matcher = new HamcrestAdapter(argumentMatcher);
        addMatcher(matcher);

        @SuppressWarnings("unchecked")
        T argValue = (T) matcher.getInnerValue();
        return argValue;
    }

    /**
     * Applies a custom argument matcher for a parameter in the current expectation.
     * <p>
     * The class of the given delegate object should define a single non-<code>private</code> <em>delegate</em> method
     * (plus any number of helper <code>private</code> methods). The name of the delegate method doesn't matter, but it
     * must have a single parameter capable of receiving the relevant argument values.
     * <p>
     * The return type of the delegate method should be <code>boolean</code> or <code>void</code>. In the first case, a
     * return value of <code>true</code> will indicate a successful match for the actual invocation argument at replay
     * time, while a return of <code>false</code> will fail to match the invocation. In the case of a <code>void</code>
     * return type, the actual invocation argument should be validated through a suitable JUnit/TestNG assertion.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param objectWithDelegateMethod
     *            an instance of a class defining a single non-<code>private</code> delegate method
     *
     * @return the default primitive value corresponding to <code>T</code> if it's a primitive wrapper type, or
     *         <code>null</code> otherwise
     *
     * @see #withArgThat(org.hamcrest.Matcher)
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @Nullable
    protected final <T> T with(@NonNull Delegate<? super T> objectWithDelegateMethod) {
        addMatcher(new ReflectiveMatcher(objectWithDelegateMethod));

        Class<?> delegateClass = objectWithDelegateMethod.getClass();
        Type[] genericInterfaces = delegateClass.getGenericInterfaces();

        while (genericInterfaces.length == 0) {
            delegateClass = delegateClass.getSuperclass();
            genericInterfaces = delegateClass.getGenericInterfaces();
        }

        Type typeFromDelegateImplementationClass = genericInterfaces[0];

        if (!(typeFromDelegateImplementationClass instanceof ParameterizedType)) {
            return null;
        }

        Type parameterType = ((ParameterizedType) typeFromDelegateImplementationClass).getActualTypeArguments()[0];
        return DefaultValues.computeForWrapperType(parameterType);
    }

    /**
     * Adds the matcher.
     *
     * @param matcher
     *            the matcher
     */
    private void addMatcher(@NonNull ArgumentMatcher<?> matcher) {
        currentPhase.addArgMatcher(matcher);
    }

    /**
     * Same as {@link #withEqual(Object)}, but matching any argument value of the appropriate type (<code>null</code>
     * included).
     * <p>
     * Consider using instead the "anyXyz" field appropriate to the parameter type: {@link #anyBoolean},
     * {@link #anyByte}, {@link #anyChar}, {@link #anyDouble}, {@link #anyFloat}, {@link #anyInt}, {@link #anyLong},
     * {@link #anyShort}, {@link #anyString}, or {@link #any} for other reference types.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param arg
     *            an arbitrary value which will match any argument value in the replay phase
     *
     * @return the input argument
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final <T> T withAny(@NonNull T arg) {
        ArgumentMatcher<?> matcher;
        if (arg instanceof String) {
            matcher = AlwaysTrueMatcher.ANY_STRING;
        } else if (arg instanceof Integer) {
            matcher = AlwaysTrueMatcher.ANY_INT;
        } else if (arg instanceof Boolean) {
            matcher = AlwaysTrueMatcher.ANY_BOOLEAN;
        } else if (arg instanceof Character) {
            matcher = AlwaysTrueMatcher.ANY_CHAR;
        } else if (arg instanceof Double) {
            matcher = AlwaysTrueMatcher.ANY_DOUBLE;
        } else if (arg instanceof Float) {
            matcher = AlwaysTrueMatcher.ANY_FLOAT;
        } else if (arg instanceof Long) {
            matcher = AlwaysTrueMatcher.ANY_LONG;
        } else if (arg instanceof Byte) {
            matcher = AlwaysTrueMatcher.ANY_BYTE;
        } else if (arg instanceof Short) {
            matcher = AlwaysTrueMatcher.ANY_SHORT;
        } else {
            matcher = AlwaysTrueMatcher.ANY_VALUE;
        }

        addMatcher(matcher);
        return arg;
    }

    /**
     * Captures the argument value passed into the associated expectation parameter, for each invocation that matches
     * the expectation when the tested code is exercised. As each such value is captured, it gets added to the given
     * list so that it can be inspected later. Apart from capturing received argument values, this method has the same
     * effect as the {@link #any} argument matcher.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param valueHolderForMultipleInvocations
     *            list into which the arguments received by matching invocations will be added
     *
     * @return the default value for type <code>T</code>
     *
     * @see Verifications#withCapture()
     * @see Verifications#withCapture(Object)
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withCapture" target="tutorial">Tutorial</a>
     */
    @Nullable
    protected final <T> T withCapture(@NonNull List<T> valueHolderForMultipleInvocations) {
        addMatcher(new CaptureMatcher<>(valueHolderForMultipleInvocations));
        return null;
    }

    /**
     * When passed as argument for an expectation, creates a new matcher that will check if the given value is
     * {@link Object#equals(Object) equal} to the corresponding argument received by a matching invocation.
     * <p>
     * The matcher is added to the end of the list of argument matchers for the invocation being recorded/verified. It
     * cannot be reused for a different parameter.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if <code>withEqual(value)</code> had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     * <p>
     * Usually, this particular method should <em>not</em> be used. Instead, simply pass the desired argument value
     * directly, without any matcher. Only when specifying values for a <em>varargs</em> method it's useful, and even
     * then only when some other argument matcher is also used.
     *
     * @param <T>
     *            the generic type
     * @param arg
     *            the expected argument value
     *
     * @return the given argument
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final <T> T withEqual(@NonNull T arg) {
        Map<Object, Object> instanceMap = currentPhase.getInstanceMap();
        addMatcher(new LenientEqualityMatcher(arg, instanceMap));
        return arg;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that a numeric invocation argument in the replay phase is
     * sufficiently close to the given value.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param value
     *            the center value for range comparison
     * @param delta
     *            the tolerance around the center value, for a range of [value - delta, value + delta]
     *
     * @return the given <code>value</code>
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    protected final double withEqual(double value, double delta) {
        addMatcher(new NumericEqualityMatcher(value, delta));
        return value;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that a numeric invocation argument in the replay phase is
     * sufficiently close to the given value.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param value
     *            the center value for range comparison
     * @param delta
     *            the tolerance around the center value, for a range of [value - delta, value + delta]
     *
     * @return the given <code>value</code>
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    protected final float withEqual(float value, double delta) {
        addMatcher(new NumericEqualityMatcher(value, delta));
        return value;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that an invocation argument in the replay phase is an instance
     * of the same class as the given object.
     * <p>
     * Equivalent to a <code>withInstanceOf(object.getClass())</code> call, except that it returns <code>object</code>
     * instead of <code>null</code>.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param object
     *            an instance of the desired class
     *
     * @return the given instance
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final <T> T withInstanceLike(@NonNull T object) {
        addMatcher(ClassMatcher.create(object.getClass()));
        return object;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that an invocation argument in the replay phase is an instance
     * of the given class.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param argClass
     *            the desired class
     *
     * @return always <code>null</code>; if you need a specific return value, use {@link #withInstanceLike(Object)}
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @Nullable
    protected final <T> T withInstanceOf(@NonNull Class<T> argClass) {
        addMatcher(ClassMatcher.create(argClass));
        return null;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that the invocation argument in the replay phase is different
     * from the given value.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param arg
     *            an arbitrary value, but different from the ones expected to occur during replay
     *
     * @return the given argument value
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final <T> T withNotEqual(@NonNull T arg) {
        addMatcher(new InequalityMatcher(arg));
        return arg;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that an invocation argument in the replay phase is
     * <code>null</code>.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     *
     * @return always <code>null</code>
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @Nullable
    protected final <T> T withNull() {
        addMatcher(NullityMatcher.INSTANCE);
        return null;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that an invocation argument in the replay phase is not
     * <code>null</code>.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     *
     * @return always <code>null</code>
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @Nullable
    protected final <T> T withNotNull() {
        addMatcher(NonNullityMatcher.INSTANCE);
        return null;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that an invocation argument in the replay phase is the exact
     * same instance as the one in the recorded/verified invocation.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param object
     *            the desired instance
     *
     * @return the given object
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final <T> T withSameInstance(@NonNull T object) {
        addMatcher(new SamenessMatcher(object));
        return object;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that a textual invocation argument in the replay phase contains
     * the given text as a substring.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param text
     *            an arbitrary non-null textual value
     *
     * @return the given text
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final <T extends CharSequence> T withSubstring(@NonNull T text) {
        addMatcher(new StringContainmentMatcher(text));
        return text;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that a textual invocation argument in the replay phase starts
     * with the given text.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param text
     *            an arbitrary non-null textual value
     *
     * @return the given text
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final <T extends CharSequence> T withPrefix(@NonNull T text) {
        addMatcher(new StringPrefixMatcher(text));
        return text;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that a textual invocation argument in the replay phase ends with
     * the given text.
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param text
     *            an arbitrary non-null textual value
     *
     * @return the given text
     *
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final <T extends CharSequence> T withSuffix(@NonNull T text) {
        addMatcher(new StringSuffixMatcher(text));
        return text;
    }

    /**
     * Same as {@link #withEqual(Object)}, but checking that a textual invocation argument in the replay phase matches
     * the given {@link Pattern regular expression}.
     * <p>
     * Note that this can be used for any string comparison, including case insensitive ones (with <code>"(?i)"</code>
     * in the regex).
     * <p>
     * When an argument matcher is used for a regular (ie, non-varargs) parameter in a call to a mocked
     * method/constructor, it's <em>not</em> necessary to also use matchers for the other parameters. So, it's valid to
     * mix the use of matchers with given values. Any arguments given as literals, local variables, or fields will be
     * implicitly matched as if {@link #withEqual(Object)} had been used. In the special case of a varargs method,
     * however, this flexibility is not available: if a matcher is used for any regular parameter, or for any element in
     * the varargs array, then a matcher <em>must</em> be used for every other parameter and varargs element.
     *
     * @param <T>
     *            the generic type
     * @param regex
     *            an arbitrary (non-null) regular expression against which textual argument values will be matched
     *
     * @return the given regex
     *
     * @see Pattern#compile(String, int)
     * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#withMethods" target="tutorial">Tutorial</a>
     */
    @NonNull
    protected final <T extends CharSequence> T withMatch(@NonNull T regex) {
        addMatcher(new PatternMatcher(regex.toString()));
        return regex;
    }
}
