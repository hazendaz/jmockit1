/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.integration.junit5;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import mockit.Capturing;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.TestRunnerDecorator;
import mockit.internal.expectations.RecordAndReplayExecution;
import mockit.internal.faking.FakeStates;
import mockit.internal.state.SavePoint;
import mockit.internal.state.TestRun;
import mockit.internal.util.StackTrace;
import mockit.internal.util.Utilities;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.opentest4j.TestAbortedException;

/**
 * JMockitExtension rewritten to avoid per-extension-instance mutable fields and to use ExtensionContext.Store for
 * per-class / per-test / per-method state.
 */
public final class JMockitExtension extends TestRunnerDecorator implements BeforeAllCallback, AfterAllCallback,
        TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback,
        AfterTestExecutionCallback, ParameterResolver, TestExecutionExceptionHandler {

    // keys for store maps (simple strings)
    private static final String KEY_SAVEPOINT_CLASS = "savePointForTestClass";
    private static final String KEY_SAVEPOINT_TEST = "savePointForTest";
    private static final String KEY_SAVEPOINT_METHOD = "savePointForTestMethod";
    private static final String KEY_THROWN = "thrownByTest";
    private static final String KEY_PARAM_VALUES = "parameterValues";
    private static final String KEY_INIT_CONTEXT = "initContext";
    private static final String KEY_TEST_INSTANCE = "testInstance";

    private static boolean isRegularTestClass(@NonNull ExtensionContext context) {
        Class<?> testClass = context.getTestClass().orElse(null);
        return testClass != null && !testClass.isAnnotationPresent(Nested.class);
    }

    private static ExtensionContext.Store classStore(ExtensionContext context) {
        Class<?> testClass = context.getTestClass().orElse(null);
        String classKey = testClass == null ? "unknown-class" : testClass.getName();
        return context.getRoot().getStore(ExtensionContext.Namespace.create(JMockitExtension.class, classKey));
    }

    private static ExtensionContext.Store testStore(ExtensionContext context) {
        String testKey = context.getUniqueId() + "/test";
        return context.getStore(ExtensionContext.Namespace.create(JMockitExtension.class, testKey));
    }

    private static ExtensionContext.Store methodStore(ExtensionContext context) {
        String methodKey = context.getUniqueId() + "/method";
        return context.getStore(ExtensionContext.Namespace.create(JMockitExtension.class, methodKey));
    }

    @Override
    public void beforeAll(@NonNull ExtensionContext context) {
        if (!isRegularTestClass(context)) {
            return;
        }

        Class<?> testClass = context.getTestClass().orElse(null);
        ExtensionContext.Store clsStore = classStore(context);

        SavePoint sp = new SavePoint();
        clsStore.put(KEY_SAVEPOINT_CLASS, sp);
        // Ensure JMockit state and test class logic is handled before any test instance is created
        if (testClass != null) {
            updateTestClassState(null, testClass);
        }

        if (testClass == null) {
            clsStore.put(KEY_INIT_CONTEXT,
                    new ParamValueInitContext(null, null, null, "@BeforeAll setup failed to acquire 'Class' of test"));
            return;
        }

        Object testInstance = context.getTestInstance().orElse(null);
        Method beforeAllMethod = Utilities.getAnnotatedDeclaredMethod(testClass, BeforeAll.class);

        if (testInstance == null && beforeAllMethod != null) {
            clsStore.put(KEY_INIT_CONTEXT, new ParamValueInitContext(null, testClass, beforeAllMethod,
                    "@BeforeAll setup failed to acquire instance of test class"));
            return;
        }

        if (beforeAllMethod != null) {
            Object[] paramValues = createInstancesForAnnotatedParameters(testInstance, beforeAllMethod, null);
            clsStore.put(KEY_PARAM_VALUES, paramValues);
            clsStore.put(KEY_INIT_CONTEXT, new ParamValueInitContext(testInstance, testClass, beforeAllMethod, null));
        }

        if (testInstance != null) {
            clsStore.put(KEY_TEST_INSTANCE, testInstance);
        }
    }

    @Override
    public void postProcessTestInstance(@NonNull Object testInstance, @NonNull ExtensionContext context) {
        if (!isRegularTestClass(context)) {
            return;
        }

        TestRun.enterNoMockingZone();
        try {
            handleMockFieldsForWholeTestClass(testInstance);
        } finally {
            TestRun.exitNoMockingZone();
        }

        classStore(context).put(KEY_TEST_INSTANCE, testInstance);
    }

    @Override
    public void beforeEach(@NonNull ExtensionContext context) {
        Object testInstance = context.getTestInstance().orElse(null);
        Class<?> testClass = context.getTestClass().orElse(null);
        ExtensionContext.Store tStore = testStore(context);

        if (testInstance == null) {
            tStore.put(KEY_INIT_CONTEXT, new ParamValueInitContext(null, null, null,
                    "@BeforeEach setup failed to acquire instance of test class"));
            return;
        }

        TestRun.prepareForNextTest();
        TestRun.enterNoMockingZone();

        try {
            tStore.put(KEY_SAVEPOINT_TEST, new SavePoint());
            createInstancesForTestedFieldsBeforeSetup(testInstance);

            if (testClass == null) {
                tStore.put(KEY_INIT_CONTEXT, new ParamValueInitContext(null, null, null,
                        "@BeforeEach setup failed to acquire Class<?> of test"));
                return;
            }

            Method beforeEachMethod = Utilities.getAnnotatedDeclaredMethod(testClass, BeforeEach.class);
            if (beforeEachMethod != null) {
                Object[] paramValues = createInstancesForAnnotatedParameters(testInstance, beforeEachMethod, null);
                tStore.put(KEY_PARAM_VALUES, paramValues);
                tStore.put(KEY_INIT_CONTEXT,
                        new ParamValueInitContext(testInstance, testClass, beforeEachMethod, null));
            }
        } finally {
            TestRun.exitNoMockingZone();
        }
    }

    @Override
    public void beforeTestExecution(@NonNull ExtensionContext context) {
        Class<?> testClass = context.getTestClass().orElse(null);
        Method testMethod = context.getTestMethod().orElse(null);
        Object testInstance = context.getTestInstance().orElse(null);
        ExtensionContext.Store mStore = methodStore(context);

        if (testMethod == null || testInstance == null) {
            mStore.put(KEY_INIT_CONTEXT, new ParamValueInitContext(testInstance, testClass, testMethod,
                    "@Test failed to acquire instance of test class, or target method"));
            return;
        }

        // Explicitly update test class state using superclass logic to avoid stale context
        updateTestClassState(testInstance, testClass);
        TestRun.setCurrentTestClass(testClass);
        TestRun.setRunningIndividualTest(testInstance);
        System.out
                .println("[JMockitExtension] Setting test class: " + (testClass != null ? testClass.getName() : "null")
                        + ", test instance: " + (testInstance != null ? testInstance.getClass().getName() : "null"));

        TestRun.enterNoMockingZone();
        try {
            mStore.put(KEY_SAVEPOINT_METHOD, new SavePoint());
            createInstancesForTestedFieldsFromBaseClasses(testInstance);
            Object[] paramValues = createInstancesForAnnotatedParameters(testInstance, testMethod, null);
            mStore.put(KEY_PARAM_VALUES, paramValues);
            mStore.put(KEY_INIT_CONTEXT, new ParamValueInitContext(testInstance, testClass, testMethod, null));
            createInstancesForTestedFields(testInstance);
        } catch (Throwable e) {
            if (isExpectedException(context, e)) {
                throw new TestAbortedException("Expected exception occurred in setup: " + e.getMessage());
            }
            throw e;
        } finally {
            TestRun.exitNoMockingZone();
        }

        TestRun.setRunningIndividualTest(testInstance);
    }

    @Override
    public boolean supportsParameter(@NonNull ParameterContext parameterContext,
            @NonNull ExtensionContext extensionContext) {
        return parameterContext.isAnnotated(Tested.class) || parameterContext.isAnnotated(Mocked.class)
                || parameterContext.isAnnotated(Injectable.class) || parameterContext.isAnnotated(Capturing.class);
    }

    @Override
    public Object resolveParameter(@NonNull ParameterContext parameterContext,
            @NonNull ExtensionContext extensionContext) {
        int parameterIndex = parameterContext.getIndex();
        ExtensionContext.Store mStore = methodStore(extensionContext);
        Object[] parameterValues = mStore.get(KEY_PARAM_VALUES, Object[].class);

        ParamValueInitContext initContext = mStore.get(KEY_INIT_CONTEXT, ParamValueInitContext.class);
        if (initContext == null) {
            initContext = testStore(extensionContext).get(KEY_INIT_CONTEXT, ParamValueInitContext.class);
        }

        if (parameterValues == null) {
            String warning = (initContext == null ? "No init context available" : initContext.warning);
            StringBuilder exceptionMessage = new StringBuilder(
                    "JMockit failed to provide parameters to JUnit ParameterResolver.");
            if (!warning.isEmpty()) {
                exceptionMessage.append("\nAdditional info: ").append(warning);
            }
            exceptionMessage.append("\n - Class: ")
                    .append(initContext == null ? "<unknown>" : initContext.displayClass());
            exceptionMessage.append("\n - Method: ")
                    .append(initContext == null ? "<unknown>" : initContext.displayMethod());
            throw new IllegalStateException(exceptionMessage.toString());
        }

        if (parameterIndex < 0 || parameterIndex >= parameterValues.length) {
            throw new IllegalStateException(
                    "Parameter index " + parameterIndex + " out of bounds (" + parameterValues.length + "). Method: "
                            + (initContext == null ? "<unknown>" : initContext.displayMethod()));
        }

        return parameterValues[parameterIndex];
    }

    @Override
    public void handleTestExecutionException(@NonNull ExtensionContext context, @NonNull Throwable throwable)
            throws Throwable {
        if (isExpectedException(context, throwable)) {
            // Expected exception was thrown, suppress it (test passes)
            return;
        }

        methodStore(context).put(KEY_THROWN, throwable);
        throw throwable;
    }

    @Override
    public void afterTestExecution(@NonNull ExtensionContext context) {
        ExtensionContext.Store mStore = methodStore(context);
        SavePoint spMethod = mStore.remove(KEY_SAVEPOINT_METHOD, SavePoint.class);

        if (spMethod != null) {
            TestRun.enterNoMockingZone();
            try {
                spMethod.rollback();

                Throwable thrownByTest = mStore.remove(KEY_THROWN, Throwable.class);
                if (thrownByTest != null)
                    filterStackTrace(thrownByTest);

                Error expectationsFailure = RecordAndReplayExecution.endCurrentReplayIfAny();
                fakeStates.resetExpectations();
                clearTestedObjectsIfAny();

                if (expectationsFailure != null) {
                    StackTrace.filterStackTrace(expectationsFailure);
                    throw expectationsFailure;
                }
            } finally {
                TestRun.finishCurrentTestExecution();
                TestRun.exitNoMockingZone();

                mStore.remove(KEY_PARAM_VALUES);
                mStore.remove(KEY_INIT_CONTEXT);
                mStore.remove(KEY_THROWN);
            }

            Error expectationsFailure = RecordAndReplayExecution.endCurrentReplayIfAny();
            FakeStates fakeStates = TestRun.getFakeStates();

            if (expectationsFailure != null && isExpectedException(context, expectationsFailure)) {
                // Expected JMockit error was thrown, suppress it (test passes)
                clearTestedObjectsIfAny();
                return;
            }

            fakeStates.verifyMissingInvocations();
            clearTestedObjectsIfAny();

            if (expectationsFailure != null) {
                StackTrace.filterStackTrace(expectationsFailure);
                fakeStates.resetExpectations();
                throw expectationsFailure;
            }
        }
    }

    @Override
    public void afterEach(@NonNull ExtensionContext context) {
        ExtensionContext.Store tStore = testStore(context);
        SavePoint spTest = tStore.remove(KEY_SAVEPOINT_TEST, SavePoint.class);
        if (spTest != null) {
            spTest.rollback();
        }

        tStore.remove(KEY_PARAM_VALUES);
        tStore.put(KEY_INIT_CONTEXT, new ParamValueInitContext(null, null, null, "State reset after each test"));

        // Ensure JMockit state is cleared after each test
        TestRun.setRunningIndividualTest(null);
        TestRun.setCurrentTestClass(null);
    }

    @Override
    public void afterAll(@NonNull ExtensionContext context) {
        if (!isRegularTestClass(context)) {
            return;
        }

        ExtensionContext.Store clsStore = classStore(context);
        SavePoint spClass = clsStore.remove(KEY_SAVEPOINT_CLASS, SavePoint.class);
        if (spClass != null) {
            spClass.rollback();
            clearFieldTypeRedefinitions();
            TestRun.setCurrentTestClass(null);
        }

        clsStore.remove(KEY_PARAM_VALUES);
        clsStore.remove(KEY_INIT_CONTEXT);
        clsStore.remove(KEY_TEST_INSTANCE);

        // Ensure JMockit state is cleared after all tests
        TestRun.setRunningIndividualTest(null);
        TestRun.setCurrentTestClass(null);
    }

    private static class ParamValueInitContext {
        final Object instance;
        final Class<?> clazz;
        final Method method;
        final String warning;

        ParamValueInitContext(Object instance, Class<?> clazz, Method method, String warning) {
            this.instance = instance;
            this.clazz = clazz;
            this.method = method;
            this.warning = warning == null ? "" : warning;
        }

        boolean isBeforeAllMethod() {
            return method != null && method.getDeclaredAnnotation(BeforeAll.class) != null;
        }

        boolean isBeforeEachMethod() {
            return method != null && method.getDeclaredAnnotation(BeforeEach.class) != null;
        }

        String displayClass() {
            return clazz == null ? "<no class reference>" : clazz.getName();
        }

        String displayMethod() {
            if (method == null) {
                return "<no method reference>";
            }
            String methodPrefix = isBeforeAllMethod() ? "@BeforeAll " : isBeforeEachMethod() ? "@BeforeEach " : "";
            String args = Arrays.stream(method.getParameterTypes()).map(Class::getName)
                    .collect(Collectors.joining(", "));
            return methodPrefix + method.getName() + "(" + args + ")";
        }

        @Override
        public String toString() {
            return "ParamContext{hasInstance=" + (instance == null ? "false" : "true") + ", class=" + clazz
                    + ", method=" + method + ", warning=" + warning + "}";
        }
    }

    private static boolean isExpectedException(@NonNull ExtensionContext context, @NonNull Throwable throwable) {
        Method testMethod = context.getTestMethod().orElse(null);
        ExpectedException expectedException = testMethod != null ? testMethod.getAnnotation(ExpectedException.class)
                : null;

        if (expectedException == null) {
            return false;
        }

        return expectedException.value().isInstance(throwable) && matchesExpectedMessages(throwable, expectedException);
    }

    private static boolean matchesExpectedMessages(Throwable throwable, ExpectedException expectedException) {
        String[] expectedMessages = expectedException.expectedMessages();
        if (expectedMessages.length == 0) {
            // No message requirement
            return true;
        }

        String actualMessage = throwable.getMessage();
        if (actualMessage == null) {
            return false;
        }

        boolean contains = expectedException.messageContains();
        for (String expected : expectedMessages) {
            if (contains) {
                if (actualMessage.contains(expected)) {
                    return true;
                }
            } else if (actualMessage.equals(expected)) {
                return true;
            }
        }
        return false;
    }

}
