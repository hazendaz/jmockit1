/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class ObjectMethods {
    private ObjectMethods() {
    }

    @NonNull
    public static String objectIdentity(@NonNull Object obj) {
        return obj.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(obj));
    }

    @Nullable
    public static Object evaluateOverride(@NonNull Object obj, @NonNull String methodNameAndDesc,
            @NonNull Object[] args) {
        if ("equals(Ljava/lang/Object;)Z".equals(methodNameAndDesc)) {
            return obj == args[0];
        }
        if ("hashCode()I".equals(methodNameAndDesc)) {
            return System.identityHashCode(obj);
        }
        if ("toString()Ljava/lang/String;".equals(methodNameAndDesc)) {
            return objectIdentity(obj);
        }
        if (args.length == 1 && methodNameAndDesc.startsWith("compareTo(L") && methodNameAndDesc.endsWith(";)I")
                && obj instanceof Comparable<?>) {
            Object arg = args[0];

            if (obj == arg) {
                return 0;
            }

            return System.identityHashCode(obj) > System.identityHashCode(arg) ? 1 : -1;
        }

        return null;
    }

    public static boolean isMethodFromObject(@NonNull String name, @NonNull String desc) {
        return "equals".equals(name) && "(Ljava/lang/Object;)Z".equals(desc)
                || "hashCode".equals(name) && "()I".equals(desc)
                || "toString".equals(name) && "()Ljava/lang/String;".equals(desc)
                || "finalize".equals(name) && "()V".equals(desc);
    }
}
