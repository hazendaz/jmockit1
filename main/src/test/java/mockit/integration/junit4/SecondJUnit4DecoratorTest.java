/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.integration.junit4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import mockit.Mock;
import mockit.MockUp;

import org.junit.BeforeClass;
import org.junit.Test;

public final class SecondJUnit4DecoratorTest {
    public static final class RealClass3 {
        public String getValue() {
            return "REAL3";
        }
    }

    public static final class FakeClass3 extends MockUp<RealClass3> {
        @Mock
        public String getValue() {
            return "TEST3";
        }
    }

    @BeforeClass
    public static void setUpFakes() {
        new FakeClass3();
    }

    @Test
    public void realClassesFakedInPreviousTestClassMustNoLongerBeFaked() {
        assertEquals("REAL0", new BaseJUnit4DecoratorTest.RealClass0().getValue());
        assertEquals("REAL1", new BaseJUnit4DecoratorTest.RealClass1().getValue());
        assertEquals("REAL2", new JUnit4DecoratorTest.RealClass2().getValue());
    }

    @Test
    public void useClassScopedFakeDefinedForThisClass() {
        assertEquals("TEST3", new RealClass3().getValue());
    }
}
