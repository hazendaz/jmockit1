/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates a mock field or a mock parameter for which all classes extending/implementing the {@linkplain Mocked
 * mocked} type will <em>also</em> get mocked.
 * <p>
 * <em>Future</em> instances of a capturing mocked type (ie, instances created sometime later during the test) will
 * become associated with the mock field/parameter. When recording or verifying expectations on the mock
 * field/parameter, these associated instances are regarded as equivalent to the original mocked instance created for
 * the mock field/parameter.
 *
 * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#capturing" target="tutorial">Tutorial</a>
 */
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER })
public @interface Capturing {

    /**
     * Max instances.
     *
     * @return the int
     */
    int maxInstances() default Integer.MAX_VALUE;

}
