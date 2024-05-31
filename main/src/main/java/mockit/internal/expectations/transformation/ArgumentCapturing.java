/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.transformation;

import static mockit.asm.jvmConstants.Opcodes.ASTORE;
import static mockit.asm.jvmConstants.Opcodes.CHECKCAST;
import static mockit.asm.jvmConstants.Opcodes.ISTORE;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.asm.types.ReferenceType;

import org.checkerframework.checker.index.qual.NonNegative;

public final class ArgumentCapturing {
    private static final Map<Integer, String> varIndexToTypeDesc = new HashMap<>();

    @NonNull
    private final InvocationBlockModifier modifier;
    @Nullable
    private List<Capture> captures;
    private boolean parameterForCapture;
    @Nullable
    private String capturedTypeDesc;

    ArgumentCapturing(@NonNull InvocationBlockModifier modifier) {
        this.modifier = modifier;
    }

    boolean registerMatcher(boolean withCaptureMethod, @NonNull String methodDesc,
            @NonNegative int lastLoadedVarIndex) {
        if (withCaptureMethod && "(Ljava/lang/Object;)Ljava/util/List;".equals(methodDesc)) {
            return false;
        }

        if (withCaptureMethod) {
            if (methodDesc.contains("List")) {
                if (lastLoadedVarIndex > 0) {
                    int parameterIndex = modifier.argumentMatching.getMatcherCount();
                    Capture capture = new Capture(modifier, lastLoadedVarIndex, parameterIndex);
                    addCapture(capture);
                }

                parameterForCapture = false;
            } else {
                parameterForCapture = true;
            }
        } else {
            parameterForCapture = false;
        }

        return true;
    }

    void registerTypeToCaptureIfApplicable(@NonNegative int opcode, @NonNull String typeDesc) {
        if (opcode == CHECKCAST && parameterForCapture) {
            capturedTypeDesc = typeDesc;
        }
    }

    static void registerTypeToCaptureIntoListIfApplicable(@NonNegative int varIndex, @NonNull String signature) {
        if (signature.startsWith("Ljava/util/List<")) {
            String typeDesc = signature.substring(16, signature.length() - 2);
            int p = typeDesc.indexOf('<');

            if (p > 0) {
                typeDesc = typeDesc.substring(0, p) + ';';
            }

            ReferenceType type = ReferenceType.createFromTypeDescriptor(typeDesc);
            varIndexToTypeDesc.put(varIndex, type.getInternalName());
        }
    }

    void registerAssignmentToCaptureVariableIfApplicable(@NonNegative int opcode, @NonNegative int varIndex) {
        if (opcode >= ISTORE && opcode <= ASTORE && parameterForCapture) {
            int parameterIndex = modifier.argumentMatching.getMatcherCount() - 1;
            Capture capture = new Capture(modifier, opcode, varIndex, capturedTypeDesc, parameterIndex);
            addCapture(capture);
            parameterForCapture = false;
            capturedTypeDesc = null;
        }
    }

    private void addCapture(@NonNull Capture capture) {
        if (captures == null) {
            captures = new ArrayList<>();
        }

        captures.add(capture);
    }

    void updateCaptureIfAny(@NonNegative int originalIndex, @NonNegative int newIndex) {
        if (captures != null) {
            for (int i = captures.size() - 1; i >= 0; i--) {
                Capture capture = captures.get(i);

                if (capture.fixParameterIndex(originalIndex, newIndex)) {
                    break;
                }
            }
        }
    }

    void generateCallsToSetArgumentTypesToCaptureIfAny() {
        if (captures != null) {
            for (Capture capture : captures) {
                capture.generateCallToSetArgumentTypeIfNeeded();
            }
        }
    }

    void generateCallsToCaptureMatchedArgumentsIfPending() {
        if (captures != null) {
            for (Capture capture : captures) {
                capture.generateCodeToStoreCapturedValue();
            }

            captures = null;
        }
    }

    @Nullable
    public static String extractArgumentType(@NonNegative int varIndex) {
        return varIndexToTypeDesc.remove(varIndex);
    }
}
