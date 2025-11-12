/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.integration.junit4;

import mockit.Mock;
import mockit.MockUp;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@Ignore
@RunWith(Suite.class)
@Suite.SuiteClasses({ MockDependencyTest.class, UseDependencyTest.class })
public final class TestSuiteWithBeforeClass {
    @BeforeClass
    public static void setUpSuiteWideFakes() {
        new MockUp<AnotherDependency>() {
            @Mock
            boolean alwaysTrue() {
                return false;
            }
        };

        AnotherDependency.mockedAtSuiteLevel = true;
    }
}
