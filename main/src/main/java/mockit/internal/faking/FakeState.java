/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.faking;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;
import mockit.internal.faking.FakeMethods.FakeMethod;
import mockit.internal.reflection.MethodReflection;
import mockit.internal.reflection.RealMethodOrConstructor;
import mockit.internal.util.ClassLoad;

final class FakeState {
    private static final ClassLoader THIS_CL = FakeState.class.getClassLoader();

    @NonNull
    final FakeMethod fakeMethod;
    @Nullable
    private Method actualFakeMethod;
    @Nullable
    private Member realMethodOrConstructor;
    @Nullable
    private Object realClass;

    // Constraints pulled from the @Mock annotation; negative values indicate "no constraint".
    private int expectedInvocations;
    private int minExpectedInvocations;
    private int maxExpectedInvocations;

    // Current fake invocation state:
    private int invocationCount;
    @Nullable
    private ThreadLocal<FakeInvocation> proceedingInvocation;

    // Helper field just for synchronization:
    @NonNull
    private final Object invocationCountLock;

    FakeState(@NonNull FakeMethod fakeMethod) {
        this.fakeMethod = fakeMethod;
        invocationCountLock = new Object();
        expectedInvocations = -1;
        minExpectedInvocations = 0;
        maxExpectedInvocations = -1;

        if (fakeMethod.canBeReentered()) {
            makeReentrant();
        }
    }

    FakeState(@NonNull FakeState fakeState) {
        fakeMethod = fakeState.fakeMethod;
        actualFakeMethod = fakeState.actualFakeMethod;
        realMethodOrConstructor = fakeState.realMethodOrConstructor;
        invocationCountLock = new Object();
        realClass = fakeState.realClass;
        invocationCount = fakeState.invocationCount;
        expectedInvocations = fakeState.expectedInvocations;
        minExpectedInvocations = fakeState.minExpectedInvocations;
        maxExpectedInvocations = fakeState.maxExpectedInvocations;

        if (fakeState.proceedingInvocation != null) {
            makeReentrant();
        }
    }

    @NonNull
    Class<?> getRealClass() {
        return fakeMethod.getRealClass();
    }

    private void makeReentrant() {
        proceedingInvocation = new ThreadLocal<>();
    }

    boolean isWithExpectations() {
        return expectedInvocations >= 0 || minExpectedInvocations > 0 || maxExpectedInvocations >= 0;
    }

    void setExpectedInvocations(int expectedInvocations) {
        this.expectedInvocations = expectedInvocations;
    }

    void setMinExpectedInvocations(int minExpectedInvocations) {
        this.minExpectedInvocations = minExpectedInvocations;
    }

    void setMaxExpectedInvocations(int maxExpectedInvocations) {
        this.maxExpectedInvocations = maxExpectedInvocations;
    }

    boolean update() {
        if (proceedingInvocation != null) {
            FakeInvocation invocation = proceedingInvocation.get();

            if (invocation != null && invocation.proceeding) {
                invocation.proceeding = false;
                return false;
            }
        }

        int timesInvoked;

        synchronized (invocationCountLock) {
            timesInvoked = ++invocationCount;
        }

        verifyUnexpectedInvocation(timesInvoked);

        return true;
    }

    private void verifyUnexpectedInvocation(int timesInvoked) {
        if (expectedInvocations >= 0 && timesInvoked > expectedInvocations) {
            throw new UnexpectedInvocation(fakeMethod.errorMessage("exactly", expectedInvocations, timesInvoked));
        }

        if (maxExpectedInvocations >= 0 && timesInvoked > maxExpectedInvocations) {
            throw new UnexpectedInvocation(fakeMethod.errorMessage("at most", maxExpectedInvocations, timesInvoked));
        }
    }

    void verifyMissingInvocations() {
        int timesInvoked = getTimesInvoked();

        if (expectedInvocations >= 0 && timesInvoked < expectedInvocations) {
            throw new MissingInvocation(fakeMethod.errorMessage("exactly", expectedInvocations, timesInvoked));
        }

        if (minExpectedInvocations > 0 && timesInvoked < minExpectedInvocations) {
            throw new MissingInvocation(fakeMethod.errorMessage("at least", minExpectedInvocations, timesInvoked));
        }
    }

    int getTimesInvoked() {
        synchronized (invocationCountLock) {
            return invocationCount;
        }
    }

    void reset() {
        synchronized (invocationCountLock) {
            invocationCount = 0;
        }
    }

    @NonNull
    Member getRealMethodOrConstructor(@NonNull String fakedClassDesc, @NonNull String fakedMethodName,
            @NonNull String fakedMethodDesc) {
        Class<?> fakedClass = ClassLoad.loadFromLoader(THIS_CL, fakedClassDesc.replace('/', '.'));
        return getRealMethodOrConstructor(fakedClass, fakedMethodName, fakedMethodDesc);
    }

    @NonNull
    Member getRealMethodOrConstructor(@NonNull Class<?> fakedClass, @NonNull String fakedMethodName,
            @NonNull String fakedMethodDesc) {
        Member member = realMethodOrConstructor;

        if (member == null || !fakedClass.equals(realClass)) {
            String memberName = "$init".equals(fakedMethodName) ? "<init>" : fakedMethodName;

            RealMethodOrConstructor realMember;
            try {
                realMember = new RealMethodOrConstructor(fakedClass, memberName, fakedMethodDesc);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            member = realMember.getMember();

            if (!fakeMethod.isAdvice) {
                realMethodOrConstructor = member;
                realClass = fakedClass;
            }
        }

        return member;
    }

    boolean shouldProceedIntoRealImplementation(@Nullable Object fake, @NonNull String classDesc) {
        if (proceedingInvocation != null) {
            FakeInvocation pendingInvocation = proceedingInvocation.get();

            // noinspection RedundantIfStatement
            if (pendingInvocation != null && pendingInvocation.isMethodInSuperclass(fake, classDesc)) {
                return true;
            }
        }

        return false;
    }

    void prepareToProceed(@NonNull FakeInvocation invocation) {
        if (proceedingInvocation == null) {
            throw new UnsupportedOperationException("Cannot proceed into abstract/interface method");
        }

        if (fakeMethod.isForNativeMethod()) {
            throw new UnsupportedOperationException("Cannot proceed into real implementation of native method");
        }

        FakeInvocation previousInvocation = proceedingInvocation.get();

        if (previousInvocation != null) {
            invocation.setPrevious(previousInvocation);
        }

        proceedingInvocation.set(invocation);
    }

    void prepareToProceedFromNonRecursiveFake(@NonNull FakeInvocation invocation) {
        assert proceedingInvocation != null;
        proceedingInvocation.set(invocation);
    }

    void clearProceedIndicator() {
        assert proceedingInvocation != null;
        FakeInvocation currentInvocation = proceedingInvocation.get();
        FakeInvocation previousInvocation = (FakeInvocation) currentInvocation.getPrevious();
        proceedingInvocation.set(previousInvocation);
    }

    @NonNull
    Method getFakeMethod(@NonNull Class<?> fakeClass, @NonNull Class<?>[] parameterTypes) {
        if (actualFakeMethod == null) {
            actualFakeMethod = MethodReflection.findCompatibleMethod(fakeClass, fakeMethod.name, parameterTypes);
        }

        return actualFakeMethod;
    }
}
