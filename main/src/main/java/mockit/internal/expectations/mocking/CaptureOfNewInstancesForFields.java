/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

final class CaptureOfNewInstancesForFields extends CaptureOfNewInstances {
    void resetCaptureCount(@NonNull Field mockField) {
        Collection<List<Capture>> capturesForAllBaseTypes = getCapturesForAllBaseTypes();

        for (List<Capture> fieldsWithCapture : capturesForAllBaseTypes) {
            resetCaptureCount(mockField, fieldsWithCapture);
        }
    }

    private static void resetCaptureCount(@NonNull Field mockField, @NonNull List<Capture> fieldsWithCapture) {
        for (Capture fieldWithCapture : fieldsWithCapture) {
            if (fieldWithCapture.typeMetadata.field == mockField) {
                fieldWithCapture.reset();
            }
        }
    }
}
