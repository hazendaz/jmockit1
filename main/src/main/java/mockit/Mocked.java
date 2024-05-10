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
 * Indicates an instance field of a test class as being a <em>mock field</em>, or a parameter of a test method as a
 * <em>mock parameter</em>; in either case, the declared type of the field/parameter is a <em>mocked type</em>, whose
 * instances are <em>mocked instances</em>.
 * <p>
 * Mocked types can also be introduced by other annotations: {@linkplain Injectable @Injectable},
 * {@link Capturing @Capturing}. Their effect is to <em>constrain</em> or <em>extend</em> the mocking capabilities here
 * specified.
 * <p>
 * Any type can be mocked, except for primitive and array types. A mocked instance of that type is automatically created
 * and assigned to the mock field/parameter, for use when {@linkplain Expectations recording} and/or
 * {@linkplain Verifications verifying} expectations. For a mock <em>field</em>, the test itself can provide the
 * instance by declaring the field as <code>final</code> and assigning it the desired instance (or <code>null</code>).
 * <p>
 * The effect of declaring a <code>@Mocked</code> type, <em>by default</em>, is that all new instances of that type, as
 * well as those previously created, will also be mocked instances; this will last for the duration of each test where
 * the associated mock field/parameter is in scope. All non-<code>private</code> methods of the mocked type will be
 * mocked.
 * <p>
 * When the mocked type is a class, all super-classes up to but not including <code>java.lang.Object</code> are also
 * mocked. Additionally, <em>static methods</em> and <em>constructors</em> are mocked as well, just like instance
 * methods; <em>native</em> methods are also mocked, provided they are <code>public</code> or <code>protected</code>.
 * <p>
 * While a method or constructor is mocked, an invocation does not result in the execution of the original code, but in
 * a (generated) call into JMockit, which then responds with either a default or a <em>recorded</em>
 * {@linkplain Expectations#result result} (or with a {@linkplain Expectations#times constraint} violation, if the
 * invocation is deemed to be unexpected).
 * <p>
 * Mocking will automatically <em>cascade</em> into the return types of all non-<code>void</code> methods belonging to
 * the mocked type, except for non-eligible ones (primitive wrappers, <code>String</code>, and collections/maps). When
 * needed, such cascaded returns can be overridden by explicitly recording a return value for the mocked method. If
 * there is a mock field/parameter with the same type (or a subtype) of some cascaded type, then the original instance
 * from that mock field/parameter will be used as the cascaded instance, rather than a new one being created; this
 * applies to all cascading levels, and even to the type of the mock field/parameter itself (ie, if a method in
 * class/interface "<code>A</code>" has return type <code>A</code>, then it will return itself by default). Finally,
 * when new cascaded instances are created, {@linkplain Injectable @Injectable} semantics apply.
 * <p>
 * Static <em>class initializers</em> (including assignments to <em>static</em> fields) of a mocked class are not
 * affected, unless {@linkplain #stubOutClassInitialization specified otherwise}.
 *
 * @see <a href="http://jmockit.github.io/tutorial/Mocking.html#mocked" target="tutorial">Tutorial</a>
 */
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER })
public @interface Mocked {
    /**
     * Indicates whether <em>static initialization code</em> in the mocked class should be stubbed out or not. Static
     * initialization includes the execution of assignments to static fields of the class and the execution of static
     * initialization blocks, if any. (Note that <em>static final</em> fields initialized with <em>compile-time</em>
     * constants are not assigned at runtime, remaining unaffected whether the class is stubbed out or not.)
     * <p>
     * By default, static initialization code in a mocked class is <em>not</em> stubbed out. The JVM will only perform
     * static initialization of a class <em>once</em>, so stubbing out the initialization code can have unexpected
     * consequences. Stubbing out the static initialization of a class is an unsafe operation, which can cause other
     * tests, executed later in the same test run, to unexpectedly fail; instead of resorting to stubbing out a class's
     * static initializer, the root cause for wanting to stub it out should be eliminated. Caveat Emptor.
     */
    boolean stubOutClassInitialization() default false;
}
