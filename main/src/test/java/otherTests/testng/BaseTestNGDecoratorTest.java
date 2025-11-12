/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package otherTests.testng;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import mockit.Mock;
import mockit.MockUp;

import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTestNGDecoratorTest implements IHookable {
    // Makes sure TestNG integration works with test classes which implement IHookable.
    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        callBack.runTestMethod(testResult);
    }

    // Lightweight test-only class to be mocked.
    public static class SimpleComponent {
        public String getInfo() {
            return null;
        }
    }

    public static class FakeClass1 extends MockUp<SimpleComponent> {
        @Mock
        public String getInfo() {
            return "TEST1";
        }
    }

    @BeforeMethod
    public final void beforeBase() {
        assertNull(new SimpleComponent().getInfo());
        new FakeClass1();
        assertEquals(new SimpleComponent().getInfo(), "TEST1");
    }

    @AfterMethod
    public final void afterBase() {
        assertEquals(new SimpleComponent().getInfo(), "TEST1");
    }
}
