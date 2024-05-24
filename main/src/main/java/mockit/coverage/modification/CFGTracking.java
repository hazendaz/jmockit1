/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.modification;

import static mockit.asm.jvmConstants.Opcodes.ICONST_0;
import static mockit.asm.jvmConstants.Opcodes.ICONST_1;
import static mockit.asm.jvmConstants.Opcodes.INVOKEVIRTUAL;

import java.util.ArrayList;
import java.util.List;

import mockit.asm.controlFlow.Label;
import mockit.coverage.lines.BranchCoverageData;
import mockit.coverage.lines.LineCoverageData;
import mockit.coverage.lines.PerFileLineCoverage;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;

final class CFGTracking {
    @NonNull
    private final PerFileLineCoverage lineCoverageInfo;
    @NonNull
    private final List<Label> visitedLabels;
    @NonNull
    private final List<Label> jumpTargetsForCurrentLine;
    @NonNull
    private final List<Integer> pendingBranches;
    @NonNegative
    private int lineExpectingInstructionAfterJump;
    private boolean assertFoundInCurrentLine;
    private boolean ignoreUntilNextLabel;
    @NonNegative
    private int foundPotentialBooleanExpressionValue;
    @NonNegative
    private int ignoreUntilNextSwitch;

    CFGTracking(@NonNull PerFileLineCoverage lineCoverageInfo) {
        this.lineCoverageInfo = lineCoverageInfo;
        visitedLabels = new ArrayList<>();
        jumpTargetsForCurrentLine = new ArrayList<>(4);
        pendingBranches = new ArrayList<>(6);
    }

    void startNewLine() {
        if (!pendingBranches.isEmpty()) {
            pendingBranches.clear();
        }

        jumpTargetsForCurrentLine.clear();
    }

    void afterNewLabel(@NonNegative int currentLine, @NonNull Label label) {
        if (ignoreUntilNextLabel || ignoreUntilNextSwitch > 0) {
            ignoreUntilNextLabel = false;
            return;
        }

        visitedLabels.add(label);

        int jumpTargetIndex = jumpTargetsForCurrentLine.indexOf(label);

        if (jumpTargetIndex >= 0) {
            label.jumpTargetLine = label.line > 0 ? label.line : currentLine;
            int targetBranchIndex = 2 * jumpTargetIndex + 1;
            pendingBranches.add(targetBranchIndex);
            assertFoundInCurrentLine = false;
        }

        foundPotentialBooleanExpressionValue = 0;
    }

    void afterGoto() {
        assertFoundInCurrentLine = false;

        if (foundPotentialBooleanExpressionValue == 1) {
            foundPotentialBooleanExpressionValue = 2;
        }
    }

    void afterConditionalJump(@NonNull MethodModifier methodModifier, @NonNull Label jumpSource,
            @NonNull Label jumpTarget) {
        int currentLine = methodModifier.currentLine;

        if (currentLine == 0 || ignoreUntilNextLabel || ignoreUntilNextSwitch > 0
                || visitedLabels.contains(jumpTarget)) {
            assertFoundInCurrentLine = false;
            return;
        }

        jumpSource.jumpTargetLine = currentLine;

        if (!jumpTargetsForCurrentLine.contains(jumpTarget)) {
            jumpTargetsForCurrentLine.add(jumpTarget);
        }

        LineCoverageData lineData = lineCoverageInfo.getOrCreateLineData(currentLine);
        int sourceBranchIndex = lineData.addBranchingPoint(jumpSource, jumpTarget);
        pendingBranches.add(sourceBranchIndex);

        if (assertFoundInCurrentLine) {
            BranchCoverageData branchData = lineCoverageInfo.getBranchData(currentLine, sourceBranchIndex + 1);
            branchData.markAsUnreachable();
        }

        lineExpectingInstructionAfterJump = 0;
        generateCallToRegisterBranchTargetExecutionIfPending(methodModifier);
        lineExpectingInstructionAfterJump = currentLine;
    }

    void generateCallToRegisterBranchTargetExecutionIfPending(@NonNull MethodModifier methodModifier) {
        if (ignoreUntilNextLabel || ignoreUntilNextSwitch > 0) {
            return;
        }

        foundPotentialBooleanExpressionValue = 0;

        if (!pendingBranches.isEmpty()) {
            for (Integer pendingBranchIndex : pendingBranches) {
                methodModifier.generateCallToRegisterBranchTargetExecution(pendingBranchIndex);
            }

            pendingBranches.clear();
        }

        if (lineExpectingInstructionAfterJump > 0) {
            if (methodModifier.currentLine > lineExpectingInstructionAfterJump) {
                lineCoverageInfo.markLastLineSegmentAsEmpty(lineExpectingInstructionAfterJump);
            }

            lineExpectingInstructionAfterJump = 0;
        }
    }

    boolean hasOnlyOneLabelBeingVisited() {
        return visitedLabels.size() == 1;
    }

    void registerAssertFoundInCurrentLine() {
        assertFoundInCurrentLine = true;
        ignoreUntilNextLabel = true;
    }

    void beforeNoOperandInstruction(@NonNull MethodModifier methodModifier, @NonNegative int opcode) {
        if ((opcode == ICONST_0 || opcode == ICONST_1) && foundPotentialBooleanExpressionValue == 0) {
            generateCallToRegisterBranchTargetExecutionIfPending(methodModifier);
            foundPotentialBooleanExpressionValue = 1;
        } else {
            generateCallToRegisterBranchTargetExecutionIfPending(methodModifier);
        }
    }

    void afterMethodInstruction(@NonNegative int opcode, @NonNull String owner, @NonNull String name) {
        if (opcode == INVOKEVIRTUAL && "hashCode".equals(name) && "java/lang/String".equals(owner)
                && ignoreUntilNextSwitch == 0) {
            ignoreUntilNextSwitch = 1;
        }
    }

    void beforeLookupSwitchInstruction() {
        if (ignoreUntilNextSwitch == 1) {
            ignoreUntilNextSwitch = 2;
        }
    }
}
