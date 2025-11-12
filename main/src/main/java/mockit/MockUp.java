/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static java.lang.reflect.Modifier.isAbstract;

import static mockit.internal.util.GeneratedClasses.isGeneratedImplementationClassName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import mockit.internal.classGeneration.ConcreteSubclass;
import mockit.internal.faking.CaptureOfFakedImplementations;
import mockit.internal.faking.FakeClassSetup;
import mockit.internal.faking.FakeClasses;
import mockit.internal.faking.FakedImplementationClass;
import mockit.internal.reflection.ConstructorReflection;
import mockit.internal.reflection.MockInvocationHandler;
import mockit.internal.startup.Startup;
import mockit.internal.state.TestRun;

/**
 * A base class used in the creation of a <em>fake</em> for an <em>external</em> type, which is usually a class from
 * some library or component used from the <em>internal</em> codebase of the system under test (SUT). Such fake classes
 * can be used as <em>fake implementations</em> for use in unit or integration tests. For example:
 *
 * <pre>{@code
 * public final class FakeSystem <strong>extends MockUp&lt;System></strong> {
 *    <strong>&#64;Mock</strong> public static long nanoTime() { return 123L; }
 * }
 * }</pre>
 *
 * One or more <em>fake methods</em> annotated {@linkplain Mock as such} must be defined in the concrete subclass. Each
 * <code>@Mock</code> method should have a matching method or constructor in the faked class. At runtime, the execution
 * of a faked method/constructor will get redirected to the corresponding fake method.
 * <p>
 * When the type to be faked is specified indirectly through a {@linkplain TypeVariable type variable}, then that type
 * is taken as a <em>base</em> type whose concrete implementation classes should <em>also</em> get faked. Example:
 *
 * <pre>{@code
 * &#64;Test
 * public &lt;<strong>BC extends SomeBaseClass</strong>> void someTest() {
 *     new MockUp&lt;<strong>BC</strong>>() {
 *        &#64;Mock int someMethod(int i) { return i + 1; }
 *     };
 *
 *     int i = new AConcreteSubclass().someMethod(1);
 *     assertEquals(2, i);
 * }
 * }</pre>
 *
 * @param <T>
 *            specifies the type to be faked; if a type variable is used, then all implementation classes extending or
 *            implementing that base type are also faked; if the type argument itself is a parameterized type, then only
 *            its raw type is considered
 *
 * @see #MockUp()
 * @see #MockUp(Class)
 * @see #MockUp(Object)
 * @see #getMockInstance()
 * @see #onTearDown()
 * @see #targetType
 * @see <a href="http://jmockit.github.io/tutorial/Faking.html#setUp" target="tutorial">Tutorial</a>
 */
public abstract class MockUp<T> {
    static {
        Startup.verifyInitialization();
    }

    /**
     * Holds the class or generic type targeted by this fake instance.
     */
    protected final Type targetType;

    @Nullable
    private final Class<?> mockedClass;
    @Nullable
    private T mockInstance;
    @Nullable
    private T invokedInstance;

    /**
     * Applies the {@linkplain Mock fake methods} defined in the concrete subclass to the class or interface specified
     * through the type parameter.
     *
     * @see #MockUp(Class)
     * @see #MockUp(Object)
     */
    protected MockUp() {
        MockUp<?> previousMockUp = findPreviouslyFakedClassIfMockUpAlreadyApplied();

        if (previousMockUp != null) {
            targetType = previousMockUp.targetType;
            mockedClass = previousMockUp.mockedClass;
            return;
        }

        targetType = getTypeToFake();
        Class<T> classToMock = null;

        if (targetType instanceof Class<?>) {
            // noinspection unchecked
            classToMock = (Class<T>) targetType;
        } else if (targetType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) targetType;
            // noinspection unchecked
            classToMock = (Class<T>) parameterizedType.getRawType();
        }

        if (classToMock != null) {
            mockedClass = redefineClassOrImplementInterface(classToMock);
        } else {
            Type[] typesToMock = ((TypeVariable<?>) targetType).getBounds();

            mockedClass = typesToMock.length > 1
                    ? new FakedImplementationClass<T>(this).createImplementation(typesToMock)
                    : new CaptureOfFakedImplementations(this, typesToMock[0]).apply();
        }
    }

    @Nullable
    private MockUp<?> findPreviouslyFakedClassIfMockUpAlreadyApplied() {
        FakeClasses mockClasses = TestRun.getFakeClasses();
        FakeClasses.MockUpInstances mockUpInstances = mockClasses.findPreviouslyAppliedMockUps(this);

        if (mockUpInstances != null && mockUpInstances.hasMockUpsForSingleInstances()) {
            return mockUpInstances.initialMockUp;
        }

        return null;
    }

    /**
     * Gets the type to fake.
     *
     * @return the type to fake
     */
    @NonNull
    private Type getTypeToFake() {
        Class<?> currentClass = getClass();

        do {
            Type superclass = currentClass.getGenericSuperclass();

            if (superclass instanceof ParameterizedType) {
                return ((ParameterizedType) superclass).getActualTypeArguments()[0];
            }

            if (superclass == MockUp.class) {
                throw new IllegalArgumentException("No target type");
            }

            currentClass = (Class<?>) superclass;
        } while (true);
    }

    @NonNull
    private Class<?> redefineClassOrImplementInterface(@NonNull Class<T> classToMock) {
        if (classToMock.isInterface()) {
            return createInstanceOfMockedImplementationClass(classToMock, targetType);
        }

        Class<T> realClass = classToMock;

        if (isAbstract(classToMock.getModifiers())) {
            classToMock = new ConcreteSubclass<T>(classToMock).generateClass();
        }

        redefineMethods(realClass, classToMock, targetType);
        return classToMock;
    }

    @NonNull
    private Class<T> createInstanceOfMockedImplementationClass(@NonNull Class<T> classToMock,
            @Nullable Type typeToMock) {
        return new FakedImplementationClass<T>(this).createImplementation(classToMock, typeToMock);
    }

    private void redefineMethods(@NonNull Class<T> realClass, @NonNull Class<T> classToMock,
            @Nullable Type genericMockedType) {
        new FakeClassSetup(realClass, classToMock, genericMockedType, this).redefineMethods();
    }

    /**
     * Applies the {@linkplain Mock mock methods} defined in the mock-up subclass to the given class/interface.
     * <p>
     * In most cases, the constructor with no parameters can be used. This variation should be used only when the type
     * to be faked is not accessible or known from the test code.
     *
     * @param targetClass
     *            the target class
     *
     * @see #MockUp()
     * @see #MockUp(Object)
     */
    protected MockUp(Class<?> targetClass) {
        targetType = targetClass;
        MockUp<?> previousMockUp = findPreviouslyFakedClassIfMockUpAlreadyApplied();

        if (previousMockUp != null) {
            mockedClass = previousMockUp.mockedClass;
            return;
        }

        if (targetClass.isInterface()) {
            // noinspection unchecked
            mockedClass = createInstanceOfMockedImplementationClass((Class<T>) targetClass, targetClass);
        } else {
            mockedClass = targetClass;
            // noinspection unchecked
            Class<T> realClass = (Class<T>) targetClass;
            redefineMethods(realClass, realClass, null);
            mockInstance = null;
        }
    }

    /**
     * Applies the {@linkplain Mock mock methods} defined in the mock-up subclass to the type specified through the type
     * parameter, but only affecting the given instance.
     * <p>
     * In most cases, the constructor with no parameters should be adequate. This variation can be used when mock data
     * or behavior is desired only for a particular instance, with other instances remaining unaffected; or when
     * multiple mock-up objects carrying different states are desired, with one mock-up instance per real instance.
     * <p>
     * If {@link #getMockInstance()} later gets called on this mock-up instance, it will return the instance that was
     * given here.
     *
     * @param targetInstance
     *            a real instance of the type to be faked, meant to be the only one of that type that should be affected
     *            by this mock-up instance
     *
     * @see #MockUp()
     * @see #MockUp(Class)
     */
    protected MockUp(T targetInstance) {
        MockUp<?> previousMockUp = findPreviouslyFakedClassIfMockUpAlreadyApplied();

        if (previousMockUp != null) {
            targetType = previousMockUp.targetType;
            mockedClass = previousMockUp.mockedClass;
            setMockInstance(targetInstance);
            return;
        }

        @SuppressWarnings("unchecked")
        Class<T> classToMock = (Class<T>) targetInstance.getClass();
        targetType = classToMock;
        mockedClass = classToMock;
        redefineMethods(classToMock, classToMock, classToMock);

        setMockInstance(targetInstance);
    }

    private void setMockInstance(@NonNull T mockInstance) {
        TestRun.getFakeClasses().addFake(this, mockInstance);
        this.mockInstance = mockInstance;
    }

    /**
     * Returns the mock instance exclusively associated with this mock-up instance. If the mocked type was an interface,
     * then said instance is the one that was automatically created when the mock-up was applied. If it was a class, and
     * no such instance is currently associated with this (stateful) mock-up object, then a new <em>uninitialized</em>
     * instance of the faked class is created and returned, becoming associated with the mock-up. If a regular
     * <em>initialized</em> instance was desired, then the {@link #MockUp(Object)} constructor should have been used
     * instead.
     * <p>
     * In any case, for a given mock-up instance this method will always return the same mock instance.
     *
     * @return the mock instance
     *
     * @see <a href="http://jmockit.github.io/tutorial/Faking.html#interfaces" target="tutorial">Tutorial</a>
     */
    public final T getMockInstance() {
        if (invokedInstance == Void.class) {
            return null;
        }

        if (invokedInstance != null) {
            return invokedInstance;
        }

        if (mockInstance == null && mockedClass != null) {
            @SuppressWarnings("unchecked")
            T newInstance = (T) createMockInstance(mockedClass);
            setMockInstance(newInstance);
        }

        return mockInstance;
    }

    @NonNull
    private Object createMockInstance(@NonNull Class<?> mockedClass) {
        String mockedClassName = mockedClass.getName();

        if (isGeneratedImplementationClassName(mockedClassName)) {
            return ConstructorReflection.newInstanceUsingPublicDefaultConstructor(mockedClass);
        }

        if (Proxy.isProxyClass(mockedClass)) {
            return MockInvocationHandler.newMockedInstance(mockedClass);
        }

        return ConstructorReflection.newUninitializedInstance(mockedClass);
    }

    /**
     * An empty method that can be overridden in a mock-up subclass that wants to be notified whenever the mock-up is
     * automatically torn down. Tear down happens when the mock-up goes out of scope: at the end of the test when
     * applied inside a test, at the end of the test class when applied before the test class, or at the end of the test
     * run when applied through the "<code>mockups</code>" system property.
     * <p>
     * By default, this method does nothing.
     */
    protected void onTearDown() {
    }
}
