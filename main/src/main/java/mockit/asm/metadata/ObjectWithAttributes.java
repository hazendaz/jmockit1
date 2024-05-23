package mockit.asm.metadata;

import java.util.List;

import mockit.asm.metadata.ClassMetadataReader.AnnotationInfo;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

class ObjectWithAttributes {
    @Nullable
    public List<AnnotationInfo> annotations;

    public final boolean hasAnnotation(@NonNull String annotationName) {
        if (annotations != null) {
            for (AnnotationInfo annotation : annotations) {
                if (annotationName.equals(annotation.name)) {
                    return true;
                }
            }
        }

        return false;
    }
}
