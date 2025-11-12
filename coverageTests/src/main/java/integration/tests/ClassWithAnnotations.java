/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import java.beans.ConstructorProperties;

import javax.sql.DataSource;

/**
 * The Class ClassWithAnnotations.
 */
@Deprecated
final class ClassWithAnnotations {

    /** The data source. */
    @SuppressWarnings("DefaultAnnotationParam")
    @AnAnnotation(integers = {})
    DataSource dataSource;

    /** The values. */
    @Deprecated
    @AnAnnotation(integers = { 1, 2, 3 })
    int[] values;

    /**
     * Instantiates a new class with annotations.
     */
    @ConstructorProperties({ "Ab", "cde" })
    ClassWithAnnotations() {
    }

    /**
     * A method.
     */
    @AnAnnotation("some text")
    @Deprecated
    void aMethod() {
    }
}
