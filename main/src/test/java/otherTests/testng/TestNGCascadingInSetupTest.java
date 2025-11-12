/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package otherTests.testng;

import static org.testng.Assert.assertNotNull;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public final class TestNGCascadingInSetupTest {
    static class Foo {
        Bar getBar() {
            return null;
        }
    }

    static class Bar {
        String getValue() {
            return null;
        }
    }

    @Mocked
    Foo foo;

    @BeforeMethod
    public void recordValueForCascadedInstance() {
        new Expectations() {
            {
                foo.getBar().getValue();
                result = "test";
            }
        };
    }

    @Test
    public void useExpectationResultRecordedOnCascadedInstanceFromSetupMethod() {
        String value = foo.getBar().getValue();

        assertNotNull(value);
    }
}
