/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package petclinic.util;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import mockit.Tested;

/**
 * Designates a field containing a test utility object, which will be initialized before any @Test or @Before method is
 * executed.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Tested(availableDuringSetup = true, fullyInitialized = true)
public @interface TestUtil {
}
