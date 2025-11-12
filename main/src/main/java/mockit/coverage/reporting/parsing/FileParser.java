/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage.reporting.parsing;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FileParser {
    private static final class PendingClass {
        @NonNull
        final String className;
        int braceBalance;

        PendingClass(@NonNull String className) {
            this.className = className;
        }
    }

    @NonNull
    public final LineParser lineParser = new LineParser();
    @NonNull
    public final List<PendingClass> currentClasses = new ArrayList<>(2);

    @Nullable
    private PendingClass currentClass;
    private boolean openingBraceForClassFound;
    private int currentBraceBalance;

    public boolean parseCurrentLine(@NonNull String line) {
        if (!lineParser.parse(line)) {
            return false;
        }

        LineElement firstElement = lineParser.getInitialElement();
        LineElement classDeclaration = findClassNameInNewClassDeclaration();

        if (classDeclaration != null) {
            firstElement = classDeclaration;
            registerStartOfClassDeclaration(classDeclaration);
        }

        if (currentClass != null) {
            detectPotentialEndOfClassDeclaration(firstElement);
        }

        return true;
    }

    @Nullable
    private LineElement findClassNameInNewClassDeclaration() {
        LineElement previous = null;

        for (LineElement element : lineParser.getInitialElement()) {
            if (element.isKeyword("class") && (previous == null || !previous.isDotSeparator())) {
                return element.getNextCodeElement();
            }

            previous = element;
        }

        return null;
    }

    private void registerStartOfClassDeclaration(@NonNull LineElement elementWithClassName) {
        String className = elementWithClassName.getText();

        if (currentClass != null) {
            currentClass.braceBalance = currentBraceBalance;
        }

        currentClass = new PendingClass(className);
        currentClasses.add(currentClass);
        currentBraceBalance = 0;
    }

    private void detectPotentialEndOfClassDeclaration(@NonNull LineElement firstElement) {
        // TODO: how to deal with classes defined entirely in one line?
        currentBraceBalance += firstElement.getBraceBalanceUntilEndOfLine();

        if (!openingBraceForClassFound && currentBraceBalance > 0) {
            openingBraceForClassFound = true;
        } else if (openingBraceForClassFound && currentBraceBalance == 0) {
            restorePreviousPendingClassIfAny();
        }
    }

    private void restorePreviousPendingClassIfAny() {
        currentClasses.remove(currentClass);

        if (currentClasses.isEmpty()) {
            currentClass = null;
        } else {
            currentClass = currentClasses.get(currentClasses.size() - 1);
            currentBraceBalance = currentClass.braceBalance;
        }
    }

    @Nullable
    public String getCurrentlyPendingClass() {
        return currentClass == null ? null : currentClass.className;
    }
}
