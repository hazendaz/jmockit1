package mockit.asm.metadata;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;

import mockit.asm.metadata.ClassMetadataReader.AnnotationInfo;

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
