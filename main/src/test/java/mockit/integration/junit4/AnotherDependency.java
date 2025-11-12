/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.integration.junit4;

public final class AnotherDependency {
    static boolean mockedAtSuiteLevel;

    public static boolean alwaysTrue() {
        return true;
    }
}
