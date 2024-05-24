/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.faking;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import mockit.internal.faking.FakeMethods.FakeMethod;
import mockit.internal.reflection.MethodReflection;
import mockit.internal.reflection.RealMethodOrConstructor;
import mockit.internal.util.ClassLoad;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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

        if (fakeMethod.canBeReentered()) {
            makeReentrant();
        }
    }

    FakeState(@NonNull FakeState fakeState) {
        fakeMethod = fakeState.fakeMethod;
        actualFakeMethod = fakeState.actualFakeMethod;
        realMethodOrConstructor = fakeState.realMethodOrConstructor;
        invocationCountLock = new Object();

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

    boolean update() {
        if (proceedingInvocation != null) {
            FakeInvocation invocation = proceedingInvocation.get();

            if (invocation != null && invocation.proceeding) {
                invocation.proceeding = false;
                return false;
            }
        }

        synchronized (invocationCountLock) {
            invocationCount++;
        }

        return true;
    }

    int getTimesInvoked() {
        synchronized (invocationCountLock) {
            return invocationCount;
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
