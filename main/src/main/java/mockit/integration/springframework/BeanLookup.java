/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.springframework;

import javax.annotation.Nullable;

import mockit.internal.injection.BeanExporter;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

final class BeanLookup {
    private BeanLookup() {
    }

    @NonNull
    static Object getBean(@NonNull BeanExporter beanExporter, @NonNull String name) {
        Object bean = beanExporter.getBean(name);

        if (bean == null) {
            throw new NoSuchBeanDefinitionException(name);
        }

        return bean;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    static <T> T getBean(@NonNull BeanExporter beanExporter, @NonNull String name, @Nullable Class<T> requiredType) {
        if (requiredType == null) {
            return (T) getBean(beanExporter, name);
        }

        T bean = (T) beanExporter.getBean(name);

        if (bean != null) {
            Class<?> actualType = bean.getClass();

            if (!requiredType.isAssignableFrom(actualType)) {
                throw new BeanNotOfRequiredTypeException(name, requiredType, actualType);
            }
        } else {
            bean = beanExporter.getBean(requiredType);

            if (bean == null) {
                throw new NoSuchBeanDefinitionException(requiredType, "with bean name \"" + name + '"');
            }
        }

        return bean;
    }

    @NonNull
    static <T> T getBean(@NonNull BeanExporter beanExporter, @NonNull Class<T> requiredType) {
        T bean = beanExporter.getBean(requiredType);

        if (bean == null) {
            throw new NoSuchBeanDefinitionException(requiredType);
        }

        return bean;
    }
}
