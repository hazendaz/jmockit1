/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static java.util.Arrays.asList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class InstanceDITest.
 */
@ExtendWith(JMockitExtension.class)
class InstanceDITest {

    /**
     * The Class Collaborator.
     */
    static class Collaborator {
    }

    /**
     * The Class TestedClassWithInstanceInjectionPoints.
     */
    static final class TestedClassWithInstanceInjectionPoints {

        /** The names. */
        final Set<String> names;

        /** The collaborators. */
        @Inject
        Instance<Collaborator> collaborators;

        /**
         * Instantiates a new tested class with instance injection points.
         *
         * @param names
         *            the names
         */
        @Inject
        TestedClassWithInstanceInjectionPoints(Instance<String> names) {
            this.names = new HashSet<>();

            for (String name : names) {
                this.names.add(name);
            }
        }
    }

    /** The tested. */
    @Tested
    TestedClassWithInstanceInjectionPoints tested;

    /** The col 1. */
    @Injectable
    Collaborator col1;

    /** The col 2. */
    @Injectable
    Collaborator col2;

    /** The names. */
    @Injectable
    final Iterable<String> names = asList("Abc", "Test", "123");

    /**
     * Allow multiple injectables of same type to be obtained from instance injection point.
     */
    @Test
    void allowMultipleInjectablesOfSameTypeToBeObtainedFromInstanceInjectionPoint() {
        assertEquals(new HashSet<>(asList("Abc", "Test", "123")), tested.names);

        Instance<Collaborator> collaborators = tested.collaborators;
        assertFalse(collaborators.isAmbiguous());
        assertFalse(collaborators.isUnsatisfied());

        List<Collaborator> collaboratorInstances = toList(collaborators);
        assertEquals(asList(col1, col2), collaboratorInstances);
    }

    /**
     * To list.
     *
     * @param <T>
     *            the generic type
     * @param instances
     *            the instances
     *
     * @return the list
     */
    static <T> List<T> toList(Iterable<T> instances) {
        List<T> list = new ArrayList<>();

        for (T instance : instances) {
            list.add(instance);
        }

        return list;
    }
}
