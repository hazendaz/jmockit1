/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.mocking;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.internal.util.StackTrace;

import org.objenesis.ObjenesisHelper;

/**
 * Factory for the creation of new mocked instances, and for obtaining/clearing the last instance created. There are
 * separate subclasses dedicated to mocked interfaces and mocked classes.
 */
public abstract class InstanceFactory {

    @NonNull
    private final Class<?> concreteClass;
    @Nullable
    Object lastInstance;

    InstanceFactory(@NonNull Class<?> concreteClass) {
        this.concreteClass = concreteClass;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    final <T> T newUninitializedConcreteClassInstance() {
        try {
            return (T) ObjenesisHelper.newInstance(concreteClass);
        } catch (Exception e) {
            StackTrace.filterStackTrace(e);
            e.printStackTrace();
            throw e;
        }
    }

    @NonNull
    public abstract Object create();

    @Nullable
    public final Object getLastInstance() {
        return lastInstance;
    }

    public abstract void clearLastInstance();

    static final class InterfaceInstanceFactory extends InstanceFactory {
        @Nullable
        private Object emptyProxy;

        InterfaceInstanceFactory(@NonNull Object emptyProxy) {
            super(emptyProxy.getClass());
            this.emptyProxy = emptyProxy;
        }

        @NonNull
        @Override
        public Object create() {
            if (emptyProxy == null) {
                emptyProxy = newUninitializedConcreteClassInstance();
            }

            lastInstance = emptyProxy;
            return emptyProxy;
        }

        @Override
        public void clearLastInstance() {
            emptyProxy = null;
            lastInstance = null;
        }
    }

    static final class ClassInstanceFactory extends InstanceFactory {
        ClassInstanceFactory(@NonNull Class<?> concreteClass) {
            super(concreteClass);
        }

        @Override
        @NonNull
        public Object create() {
            lastInstance = newUninitializedConcreteClassInstance();
            return lastInstance;
        }

        @Override
        public void clearLastInstance() {
            lastInstance = null;
        }
    }
}
