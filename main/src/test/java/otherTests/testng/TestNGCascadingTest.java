/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package otherTests.testng;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.ITestContext;
import org.testng.annotations.Test;

public final class TestNGCascadingTest {
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

    @Test
    public void useExpectationResultRecordedOnCascadedInstance(ITestContext ctx) {
        new Expectations() {
            {
                foo.getBar().getValue();
                result = "test";
            }
        };

        String value = foo.getBar().getValue();

        assertNotNull(value);
    }

    @Test
    public void getUnrecordedResultFromCascadedInstance() {
        String value = foo.getBar().getValue();

        assertNull(value);
    }
}
