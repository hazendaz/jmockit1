/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class ConfigurationTest {
    private static final String SUFFIX = "unitTestProperty";
    private static final String FULL_NAME = "coverage-" + SUFFIX;

    @AfterEach
    void clearTestProperty() {
        System.clearProperty(FULL_NAME);
    }

    @Test
    void getPropertyReturnsNullWhenNotSet() {
        assertNull(Configuration.getProperty(SUFFIX));
    }

    @Test
    void getPropertyWithDefaultReturnsDefaultWhenNotSet() {
        assertEquals("theDefault", Configuration.getProperty(SUFFIX, "theDefault"));
    }

    @Test
    void getPropertyReturnsConfiguredValue() {
        System.setProperty(FULL_NAME, "theValue");

        assertEquals("theValue", Configuration.getProperty(SUFFIX));
        assertEquals("theValue", Configuration.getProperty(SUFFIX, "theDefault"));
    }

    @Test
    void getOrChooseOutputDirectoryReturnsGivenDirectoryWhenNotEmpty() {
        assertEquals("myOutputDir", Configuration.getOrChooseOutputDirectory("myOutputDir"));
    }

    @Test
    void getOrChooseOutputDirectoryWithDefaultReturnsGivenDirectoryWhenNotEmpty() {
        assertEquals("myOutputDir", Configuration.getOrChooseOutputDirectory("myOutputDir", "reports"));
    }

    @Test
    void getOrChooseOutputDirectoryFallsBackBasedOnTargetDirectoryAvailability() {
        boolean targetAvailable = System.getProperty("basedir") != null || Files.exists(Path.of("target"));

        assertEquals(targetAvailable ? "target" : null, Configuration.getOrChooseOutputDirectory(""));
    }

    @Test
    void getOrChooseOutputDirectoryWithDefaultFallsBackBasedOnTargetDirectoryAvailability() {
        boolean targetAvailable = System.getProperty("basedir") != null || Files.exists(Path.of("target"));

        assertEquals(targetAvailable ? "target/reports" : "reports",
                Configuration.getOrChooseOutputDirectory("", "reports"));
    }
}
