/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.integration.cdi;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.util.Set;

final class BeanLookup {
    private BeanLookup() {}

    @NonNull
    static Object getBean(@NonNull BeanManager beanManager, @NonNull String name) {
        Set<Bean<?>> beans = beanManager.getBeans(name);
        Bean<?> bean = beanManager.resolve(beans);
        if (bean == null) {
            throw new NoSuchBeanDefinitionException(name);
        }
        CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
        return beanManager.getReference(bean, bean.getBeanClass(), ctx);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    static <T> T getBean(@NonNull BeanManager beanManager, @NonNull String name, @Nullable Class<T> requiredType) {
        if (requiredType == null) {
            return (T) getBean(beanManager, name);
        }
        Set<Bean<?>> beans = beanManager.getBeans(name);
        Bean<?> bean = beanManager.resolve(beans);
        if (bean != null) {
            CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
            Object ref = beanManager.getReference(bean, requiredType, ctx);
            if (!requiredType.isInstance(ref)) {
                throw new BeanNotOfRequiredTypeException(name, requiredType, ref.getClass());
            }
            return (T) ref;
        } else {
            return getBean(beanManager, requiredType);
        }
    }

    @NonNull
    static <T> T getBean(@NonNull BeanManager beanManager, @NonNull Class<T> requiredType) {
        Set<Bean<?>> beans = beanManager.getBeans(requiredType);
        Bean<?> bean = beanManager.resolve(beans);
        if (bean == null) {
            throw new NoSuchBeanDefinitionException(requiredType);
        }
        CreationalContext<?> ctx = beanManager.createCreationalContext(bean);
        Object ref = beanManager.getReference(bean, requiredType, ctx);
        return requiredType.cast(ref);
    }

    // Exception classes for consistency with Spring
    static class NoSuchBeanDefinitionException extends RuntimeException {
        NoSuchBeanDefinitionException(Object key) {
            super("No bean found for: " + key);
        }
        NoSuchBeanDefinitionException(Class<?> type, String details) {
            super("No bean of type " + type.getName() + " found " + details);
        }
    }

    static class BeanNotOfRequiredTypeException extends RuntimeException {
        BeanNotOfRequiredTypeException(String name, Class<?> requiredType, Class<?> actualType) {
            super("Bean named '" + name + "' is expected to be of type '" + requiredType.getName() + "' but was actually of type '" + actualType.getName() + "'.");
        }
    }
}
