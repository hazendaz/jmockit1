/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection.full;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.sql.DataSourceDefinition;
import javax.annotation.sql.DataSourceDefinitions;
import javax.sql.CommonDataSource;

import mockit.internal.injection.InjectionPoint;
import mockit.internal.injection.TestedClass;

final class TestDataSource {
    @Nullable
    private final String dsName;
    private Class<? extends CommonDataSource> dsClass;
    private CommonDataSource ds;

    TestDataSource(@NonNull InjectionPoint injectionPoint) {
        dsName = injectionPoint.name;
    }

    @Nullable
    CommonDataSource createIfDataSourceDefinitionAvailable(@NonNull TestedClass testedClass) {
        TestedClass testedClassWithDataSource = testedClass.parent;

        if (testedClassWithDataSource == null || dsName == null) {
            return null;
        }

        Class<?> testClass = testedClassWithDataSource.testClass;

        if (testClass != null) {
            createFromTestedClassOrASuperclass(testClass);
        }

        if (ds != null) {
            return ds;
        }

        TestedClass testedClassToBeSearched = testedClassWithDataSource;

        do {
            createFromTestedClassOrASuperclass(testedClassToBeSearched.targetClass);

            if (ds != null) {
                return ds;
            }

            testedClassToBeSearched = testedClassToBeSearched.parent;
        } while (testedClassToBeSearched != null);

        throw new IllegalStateException("Missing @DataSourceDefinition of name \"" + dsName + "\" on "
                + testedClassWithDataSource.nameOfTestedClass + " or on a super/parent class");
    }

    private void createFromTestedClassOrASuperclass(@NonNull Class<?> targetClass) {
        do {
            createDataSource(targetClass);

            if (ds != null) {
                return;
            }

            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
    }

    private void createDataSource(@NonNull Class<?> targetClass) {
        for (Annotation annotation : targetClass.getDeclaredAnnotations()) {
            String annotationName = annotation.annotationType().getName();

            if ("javax.annotation.sql.DataSourceDefinitions".equals(annotationName)) {
                createDataSource((DataSourceDefinitions) annotation);
            } else if ("javax.annotation.sql.DataSourceDefinition".equals(annotationName)) {
                createDataSource((DataSourceDefinition) annotation);
            }

            if (ds != null) {
                return;
            }
        }
    }

    private void createDataSource(@NonNull DataSourceDefinitions dsDefs) {
        for (DataSourceDefinition dsDef : dsDefs.value()) {
            createDataSource(dsDef);

            if (ds != null) {
                return;
            }
        }
    }

    private void createDataSource(@NonNull DataSourceDefinition dsDef) {
        String configuredDataSourceName = InjectionPoint.getNameFromJNDILookup(dsDef.name());

        if (configuredDataSourceName.equals(dsName)) {
            instantiateConfiguredDataSourceClass(dsDef);
            setDataSourcePropertiesFromConfiguredValues(dsDef);
        }
    }

    private void instantiateConfiguredDataSourceClass(@NonNull DataSourceDefinition dsDef) {
        String className = dsDef.className();

        try {
            // noinspection unchecked
            dsClass = (Class<? extends CommonDataSource>) Class.forName(className);
            // noinspection ClassNewInstance
            ds = dsClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e.getCause());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private void setDataSourcePropertiesFromConfiguredValues(@NonNull DataSourceDefinition dsDef) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(dsClass, Object.class);
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

            setProperty(properties, "url", dsDef.url());
            setProperty(properties, "user", dsDef.user());
            setProperty(properties, "password", dsDef.password());
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void setProperty(@NonNull PropertyDescriptor[] properties, @NonNull String name, @NonNull String value)
            throws InvocationTargetException, IllegalAccessException {
        for (PropertyDescriptor property : properties) {
            if (property.getName().equals(name)) {
                Method writeMethod = property.getWriteMethod();

                if (writeMethod != null) {
                    writeMethod.invoke(ds, value);
                }

                return;
            }
        }
    }
}
