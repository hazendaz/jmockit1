/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the value of a mock field or mock parameter will be an isolated {@linkplain Mocked mocked} instance,
 * intended to be passed or <em>injected</em> into the code under test. Such instances can be said to be proper <em>mock
 * objects</em>, in contrast to the mocked instances of a regular <code>@Mocked</code> type.
 * <p>
 * When the type of the injectable is <code>String</code>, a primitive wrapper, a {@linkplain Number number type}, or an
 * enum, it is <em>not</em> mocked. A non-empty {@link #value} must then be provided, except in the first case where the
 * empty string is used by default.
 * <p>
 * For the duration of each test where the mock field/parameter is in scope, <em>only one</em> injectable instance is
 * mocked; other instances of the same mocked type are not affected. For an injectable mocked <em>class</em>, <em>static
 * methods</em> and <em>constructors</em> are <em>not</em> mocked; only <em>non-native</em> instance methods are.
 * <p>
 * When used in combination with {@linkplain Tested @Tested}, the values of injectable fields and parameters will be
 * used for automatic injection into the tested object. Additionally, this annotation can be applied to non-mocked
 * fields of primitive or array types, which will also be used for injection.
 *
 * @see #value
 * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#injectable" target="tutorial">Tutorial</a>
 */
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER })
public @interface Injectable {

    /**
     * Specifies a literal value when the type of the injectable mock field/parameter is <code>String</code>, a
     * primitive or wrapper type, a number type, or an enum type. For a primitive/wrapper/number type, the value
     * provided must be convertible to it. For an enum type, the given textual value must equal the name of one of the
     * possible enum values.
     *
     * @return the string
     */
    String value() default "";
}
