/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.sourceFiles;

import java.io.PrintWriter;

import mockit.coverage.reporting.parsing.LineElement;
import mockit.coverage.reporting.parsing.LineParser;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class NeutralOutput {
    @NonNull
    private final PrintWriter output;
    private boolean previousLineInImports;
    private boolean previousLineInComments;
    @Nullable
    private String lineIndentation;
    private boolean blankLinesPending;

    NeutralOutput(@NonNull PrintWriter output) {
        this.output = output;
    }

    boolean writeLineWithoutCoverageInfo(@NonNull LineParser lineParser) {
        if (previousLineInComments || !previousLineInImports) {
            if (writeLineInComments(lineParser) || writeLineInImports(lineParser)) {
                return true;
            }
        } else if (writeLineInImports(lineParser) || writeLineInComments(lineParser)) {
            return true;
        }

        if (lineParser.isBlankLine()) {
            blankLinesPending = true;
            return true;
        }

        writeBlankLineIfPending();
        return false;
    }

    private boolean writeLineInComments(@NonNull LineParser lineParser) {
        LineElement initialElement = lineParser.getInitialElement();

        if (lineParser.isInComments()
                || previousLineInComments && initialElement.isComment() && initialElement.getNext() == null) {
            String lineText = initialElement.toString();

            if (previousLineInComments) {
                output.println();
            } else {
                writeOpeningForCollapsibleBlockOfLines();
                output.write("      <td class='cm' onclick='shLns(this)'><div>");
                extractLineIndentation(lineText);
                previousLineInComments = true;
            }

            output.write(lineText);
            return true;
        }
        if (previousLineInComments) {
            output.append("</div><span>").append(lineIndentation).println("/*...*/</span></td>");
            output.println("    </tr>");
            previousLineInComments = false;
        }

        return false;
    }

    private void writeOpeningForCollapsibleBlockOfLines() {
        writeBlankLineIfPending();
        output.println("    <tr>");
        output.println("      <td></td><td></td>");
    }

    private void writeBlankLineIfPending() {
        if (blankLinesPending) {
            output.println("    <tr><td></td><td></td><td></td></tr>");
            blankLinesPending = false;
        }
    }

    private void extractLineIndentation(@NonNull String lineText) {
        int indentationSize = 0;

        for (int i = 0; i < lineText.length(); i++, indentationSize++) {
            if (lineText.charAt(i) > ' ') {
                break;
            }
        }

        lineIndentation = lineText.substring(0, indentationSize);
    }

    private boolean writeLineInImports(@NonNull LineParser lineParser) {
        LineElement initialElement = lineParser.getInitialElement();
        boolean isImport = initialElement.isKeyword("import");

        if (!previousLineInImports && isImport || previousLineInImports && (isImport || lineParser.isBlankLine())) {
            String lineText = initialElement.toString();

            if (previousLineInImports) {
                output.println();
                blankLinesPending = !isImport;
            } else {
                writeOpeningForCollapsibleBlockOfLines();
                output.write("      <td><pre class='ims pp' onclick='shLns(this)'><div>");
                previousLineInImports = true;
            }

            output.write(lineText);
            return true;
        }
        if (previousLineInImports) {
            output.println("</div><span>import ...</span></pre></td>");
            output.println("    </tr>");
            previousLineInImports = false;
        }

        return false;
    }
}
