/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.springframework;

import mockit.internal.injection.BeanExporter;
import mockit.internal.injection.TestedClassInstantiations;
import mockit.internal.state.TestRun;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.web.context.support.StaticWebApplicationContext;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A {@link org.springframework.web.context.WebApplicationContext} implementation which exposes the
 * {@linkplain mockit.Tested @Tested} objects and their injected dependencies declared in the current test class.
 */
public final class TestWebApplicationContext extends StaticWebApplicationContext {
    @Override
    @NonNull
    public Object getBean(@NonNull String name) {
        BeanExporter beanExporter = getBeanExporter();
        return BeanLookup.getBean(beanExporter, name);
    }

    @NonNull
    private static BeanExporter getBeanExporter() {
        TestedClassInstantiations testedClasses = TestRun.getTestedClassInstantiations();

        if (testedClasses == null) {
            throw new BeanDefinitionStoreException("Test class does not define any @Tested fields");
        }

        return testedClasses.getBeanExporter();
    }

    @Override
    @NonNull
    public <T> T getBean(@NonNull String name, @Nullable Class<T> requiredType) {
        BeanExporter beanExporter = getBeanExporter();
        return BeanLookup.getBean(beanExporter, name, requiredType);
    }

    @Override
    @NonNull
    public <T> T getBean(@NonNull Class<T> requiredType) {
        BeanExporter beanExporter = getBeanExporter();
        return BeanLookup.getBean(beanExporter, requiredType);
    }
}
