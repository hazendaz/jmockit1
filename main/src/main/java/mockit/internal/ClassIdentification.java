/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal;

import javax.annotation.Nullable;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Identifies a class by its loader and name rather than by the <code>Class</code> object, which isn't available during
 * initial class transformation.
 */
public final class ClassIdentification {
    @Nullable
    public final ClassLoader loader;
    @NonNull
    public final String name;

    public ClassIdentification(@Nullable ClassLoader loader, @NonNull String name) {
        this.loader = loader;
        this.name = name;
    }

    @NonNull
    public Class<?> getLoadedClass() {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClassIdentification other = (ClassIdentification) o;
        return loader == other.loader && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return loader == null ? name.hashCode() : 31 * loader.hashCode() + name.hashCode();
    }
}
