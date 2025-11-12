/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.integration.junit5;

import static mockit.internal.util.StackTrace.filterStackTrace;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import mockit.Capturing;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.TestRunnerDecorator;
import mockit.internal.expectations.RecordAndReplayExecution;
import mockit.internal.state.SavePoint;
import mockit.internal.state.TestRun;
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

public final class JMockitExtension extends TestRunnerDecorator implements BeforeAllCallback, AfterAllCallback,
        TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback,
        AfterTestExecutionCallback, ParameterResolver, TestExecutionExceptionHandler {
    @Nullable
    private SavePoint savePointForTestClass;
    @Nullable
    private SavePoint savePointForTest;
    @Nullable
    private SavePoint savePointForTestMethod;
    @Nullable
    private Throwable thrownByTest;
    private Object[] parameterValues;
    private ParamValueInitContext initContext = new ParamValueInitContext(null, null, null,
            "No callbacks have been processed, preventing parameter population");

    @Override
    public void beforeAll(@NonNull ExtensionContext context) {
        if (!isRegularTestClass(context)) {
            return;
        }

        @Nullable
        Class<?> testClass = context.getTestClass().orElse(null);
        savePointForTestClass = new SavePoint();
        // Ensure JMockit state and test class logic is handled before any test instance is created
        updateTestClassState(null, testClass);

        if (testClass == null) {
            initContext = new ParamValueInitContext(null, null, null,
                    "@BeforeAll setup failed to acquire 'Class' of test");
            return;
        }

        // @BeforeAll can be used on instance methods depending on @TestInstance(PER_CLASS) usage
        Object testInstance = context.getTestInstance().orElse(null);
        Method beforeAllMethod = Utilities.getAnnotatedDeclaredMethod(testClass, BeforeAll.class);
        if (testInstance == null) {
            initContext = new ParamValueInitContext(null, testClass, beforeAllMethod,
                    "@BeforeAll setup failed to acquire instance of test class");
            return;
        }

        if (beforeAllMethod != null) {
            initContext = new ParamValueInitContext(testInstance, testClass, beforeAllMethod, null);
            parameterValues = createInstancesForAnnotatedParameters(testInstance, beforeAllMethod, null);
        }
    }

    private static boolean isRegularTestClass(@NonNull ExtensionContext context) {
        Class<?> testClass = context.getTestClass().orElse(null);
        return testClass != null && !testClass.isAnnotationPresent(Nested.class);
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

        TestRun.setRunningIndividualTest(testInstance);
    }

    @Override
    public void beforeEach(@NonNull ExtensionContext context) {
        Object testInstance = context.getTestInstance().orElse(null);
        Class<?> testClass = context.getTestClass().orElse(null);
        if (testInstance == null) {
            initContext = new ParamValueInitContext(null, null, null,
                    "@BeforeEach setup failed to acquire instance of test class");
            return;
        }

        TestRun.prepareForNextTest();
        TestRun.enterNoMockingZone();

        try {
            savePointForTest = new SavePoint();
            createInstancesForTestedFieldsBeforeSetup(testInstance);

            if (testClass == null) {
                initContext = new ParamValueInitContext(null, null, null,
                        "@BeforeEach setup failed to acquire Class<?> of test");
                return;
            }

            Method beforeEachMethod = Utilities.getAnnotatedDeclaredMethod(testClass, BeforeEach.class);
            if (beforeEachMethod != null) {
                initContext = new ParamValueInitContext(testInstance, testClass, beforeEachMethod, null);
                parameterValues = createInstancesForAnnotatedParameters(testInstance, beforeEachMethod, null);
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

        if (testMethod == null || testInstance == null) {
            initContext = new ParamValueInitContext(testInstance, testClass, testMethod,
                    "@Test failed to acquire instance of test class, or target method");
            return;
        }

        TestRun.enterNoMockingZone();

        try {
            savePointForTestMethod = new SavePoint();
            createInstancesForTestedFieldsFromBaseClasses(testInstance);
            initContext = new ParamValueInitContext(testInstance, testClass, testMethod, null);
            parameterValues = createInstancesForAnnotatedParameters(testInstance, testMethod, null);
            createInstancesForTestedFields(testInstance);
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
        if (parameterValues == null) {
            String warning = initContext.warning;
            StringBuilder exceptionMessage = new StringBuilder(
                    "JMockit failed to provide parameters to JUnit 5 ParameterResolver.");
            if (warning != null) {
                exceptionMessage.append("\nAdditional info: ").append(warning);
            }
            exceptionMessage.append("\n - Class: ").append(initContext.displayClass());
            exceptionMessage.append("\n - Method: ").append(initContext.displayMethod());
            throw new IllegalStateException(exceptionMessage.toString());
        }
        return parameterValues[parameterIndex];
    }

    @Override
    public void handleTestExecutionException(@NonNull ExtensionContext context, @NonNull Throwable throwable)
            throws Throwable {
        thrownByTest = throwable;
        throw throwable;
    }

    @Override
    public void afterTestExecution(@NonNull ExtensionContext context) {
        if (savePointForTestMethod == null) {
            return;
        }

        TestRun.enterNoMockingZone();

        try {
            savePointForTestMethod.rollback();
            savePointForTestMethod = null;

            if (thrownByTest != null) {
                filterStackTrace(thrownByTest);
            }

            Error expectationsFailure = RecordAndReplayExecution.endCurrentReplayIfAny();
            clearTestedObjectsIfAny();

            if (expectationsFailure != null) {
                filterStackTrace(expectationsFailure);
                throw expectationsFailure;
            }
        } finally {
            TestRun.finishCurrentTestExecution();
            TestRun.exitNoMockingZone();
        }
    }

    @Override
    public void afterEach(@NonNull ExtensionContext context) {
        if (savePointForTest != null) {
            savePointForTest.rollback();
            savePointForTest = null;
        }
    }

    @Override
    public void afterAll(@NonNull ExtensionContext context) {
        if (savePointForTestClass != null && isRegularTestClass(context)) {
            savePointForTestClass.rollback();
            savePointForTestClass = null;

            clearFieldTypeRedefinitions();
            TestRun.setCurrentTestClass(null);
        }
    }

    private static class ParamValueInitContext {
        private final Object instance;
        private final Class<?> clazz;
        private final Method method;
        private final String warning;

        ParamValueInitContext(Object instance, Class<?> clazz, Method method, String warning) {
            this.instance = instance;
            this.clazz = clazz;
            this.method = method;
            this.warning = warning;
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
}
