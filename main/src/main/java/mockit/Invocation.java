/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Member;

import mockit.internal.BaseInvocation;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A context object representing the current invocation to a mocked or faked method/constructor, to be passed as the
 * <em>first</em> parameter of the corresponding delegate/fake method implementation.
 * <p>
 * With the <em>Mocking</em> API, this parameter can appear in delegate methods implemented in {@link Delegate} classes.
 * With the <em>Faking</em> API, it can appear in {@link Mock @Mock} methods.
 *
 * @see #getInvokedInstance()
 * @see #getInvokedArguments()
 * @see #getInvocationCount()
 * @see #getInvocationIndex()
 * @see #proceed(Object...)
 * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#delegates" target="tutorial">Tutorial (mocking)</a>
 * @see <a href="http://jmockit.github.io/tutorial/Faking.html#invocation" target="tutorial">Tutorial (faking)</a>
 */
public class Invocation {

    /** The invoked instance. */
    @Nullable
    private final Object invokedInstance;

    /** The invoked arguments. */
    @NonNull
    private final Object[] invokedArguments;

    /** The invocation count. */
    @NonNegative
    private final int invocationCount;

    /**
     * For internal use only.
     *
     * @param invokedInstance
     *            the invoked instance
     * @param invokedArguments
     *            the invoked arguments
     * @param invocationCount
     *            the invocation count
     */
    protected Invocation(@Nullable Object invokedInstance, @NonNull Object[] invokedArguments,
            @NonNegative int invocationCount) {
        this.invokedInstance = invokedInstance;
        this.invokedArguments = invokedArguments;
        this.invocationCount = invocationCount;
    }

    /**
     * Returns the instance on which the current invocation was made, or <code>null</code> for a <code>static</code>
     * method invocation.
     *
     * @param <T>
     *            the generic type
     *
     * @return the invoked instance
     */
    @Nullable
    public final <T> T getInvokedInstance() {
        // noinspection unchecked
        return (T) invokedInstance;
    }

    /**
     * Returns the <code>Method</code> or <code>Constructor</code> object corresponding to the target method or
     * constructor, respectively.
     *
     * @param <M>
     *            the generic type
     *
     * @return the invoked member
     */
    @NonNull
    public final <M extends Member> M getInvokedMember() {
        // noinspection unchecked,ClassReferencesSubclass
        return (M) ((BaseInvocation) this).getRealMember();
    }

    /**
     * Returns the actual argument values passed in the invocation to the target method/constructor.
     *
     * @return the invoked arguments
     */
    @NonNull
    public final Object[] getInvokedArguments() {
        return invokedArguments;
    }

    /**
     * Returns the current invocation count. The first invocation starts at 1 (one).
     *
     * @return the invocation count
     */
    @NonNegative
    public final int getInvocationCount() {
        return invocationCount;
    }

    /**
     * Returns the index for the current invocation. The first invocation starts at 0 (zero). Note that this is
     * equivalent to {@link #getInvocationCount()} - 1.
     *
     * @return the invocation index
     */
    public final int getInvocationIndex() {
        return invocationCount - 1;
    }

    /**
     * Allows execution to proceed into the real implementation of the target method/constructor.
     * <p>
     * In the case of a method, the real implementation is executed with the argument values originally received or
     * explicitly given as replacement. Whatever comes out (either a return value or a thrown exception/error) becomes
     * the result for this execution of the method.
     * <p>
     * In the case of a constructor, the real constructor implementation code which comes after the necessary call to
     * "<code>super</code>" is executed, using the original argument values; replacement arguments are not supported. If
     * the execution of said code throws an exception or error, it is propagated out to the caller of the target
     * constructor. Contrary to proceeding into a method, it's not possible to actually execute test code inside the
     * delegate or fake method after proceeding into the real constructor, nor to proceed into it more than once.
     *
     * @param <T>
     *            the return type of the target method
     * @param replacementArguments
     *            the argument values to be passed to the real method, as replacement for the values received by the
     *            delegate or fake method; if those received values should be passed without replacement, then this
     *            method should be called with no values
     *
     * @return the same value returned by the target method, if any
     *
     * @throws UnsupportedOperationException
     *             if attempting to proceed into a target method which does not belong to an {@linkplain Injectable
     *             injectable mocked type} nor to a {@linkplain Expectations#Expectations(Object...) partially mocked
     *             object}, into a <code>native</code> method, or into an interface or abstract method
     *
     * @see <a href="http://jmockit.github.io/tutorial/Faking.html#proceed" target="tutorial">Tutorial</a>
     */
    @Nullable
    public final <T> T proceed(@Nullable Object... replacementArguments) {
        // noinspection ClassReferencesSubclass
        return ((BaseInvocation) this).doProceed(replacementArguments);
    }
}
