/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.mocking;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.reflect.TypeVariable;

import mockit.internal.state.TestRun;

final class FieldTypeRedefinition extends TypeRedefinition {
    private boolean usePartialMocking;

    FieldTypeRedefinition(@NonNull MockedType typeMetadata) {
        super(typeMetadata);
    }

    boolean redefineTypeForTestedField() {
        usePartialMocking = true;
        return redefineTypeForFieldNotSet();
    }

    @Override
    void configureClassModifier(@NonNull MockedClassModifier modifier) {
        if (usePartialMocking) {
            modifier.useDynamicMocking();
        }
    }

    @SuppressWarnings("ConstantConditions")
    boolean redefineTypeForFinalField() {
        if (targetClass == TypeVariable.class || !typeMetadata.injectable && targetClass.isInterface()) {
            String mockFieldName = typeMetadata.getName();
            throw new IllegalArgumentException("Final mock field \"" + mockFieldName + "\" must be of a class type");
        }

        return redefineTypeForFieldNotSet();
    }

    private boolean redefineTypeForFieldNotSet() {
        boolean redefined = redefineMethodsAndConstructorsInTargetType();

        if (redefined) {
            TestRun.mockFixture().registerMockedClass(targetClass);
        }

        return redefined;
    }
}
