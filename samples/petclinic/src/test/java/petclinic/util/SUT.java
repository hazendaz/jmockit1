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
 * Designates a field containing an object to be tested, which gets automatically created before any @Test method as
 * needed; any fields of the object for other dependencies to be injected are also automatically created or resolved.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Tested(fullyInitialized = true)
public @interface SUT {
}
