/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.integration.junit4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import mockit.Mock;
import mockit.MockUp;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public final class JUnit4DecoratorTest extends BaseJUnit4DecoratorTest {
    public static final class RealClass2 {
        public String getValue() {
            return "REAL2";
        }
    }

    public static final class FakeClass2 extends MockUp<RealClass2> {
        @Mock
        public String getValue() {
            return "TEST2";
        }
    }

    @BeforeClass
    public static void beforeClassThatRunsSecond() {
        assertEquals("TEST0", new RealClass0().getValue());
    }

    @BeforeClass
    public static void beforeClassThatRunsFirst() {
        assertEquals("TEST0", new RealClass0().getValue());
    }

    @Test
    public void useClassScopedMockDefinedByBaseClass() {
        assertEquals("TEST0", new RealClass0().getValue());
    }

    @Test
    public void setUpAndUseSomeFakes() {
        assertEquals("TEST1", new RealClass1().getValue());
        assertEquals("REAL2", new RealClass2().getValue());

        new FakeClass2();

        assertEquals("TEST2", new RealClass2().getValue());
        assertEquals("TEST1", new RealClass1().getValue());
    }

    @Test
    public void setUpAndUseFakesAgain() {
        assertEquals("TEST1", new RealClass1().getValue());
        assertEquals("REAL2", new RealClass2().getValue());

        new FakeClass2();

        assertEquals("TEST2", new RealClass2().getValue());
        assertEquals("TEST1", new RealClass1().getValue());
    }

    @After
    public void afterTest() {
        assertEquals("REAL2", new RealClass2().getValue());
    }

    @Test
    public void classFakedInSecondTestClassMustNotBeFakedForThisTestClass() {
        assertEquals("REAL3", new SecondJUnit4DecoratorTest.RealClass3().getValue());
    }
}
