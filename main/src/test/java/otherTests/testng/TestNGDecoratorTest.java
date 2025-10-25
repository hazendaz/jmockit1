package otherTests.testng;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import javax.naming.Reference;

import mockit.Mock;
import mockit.MockUp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class TestNGDecoratorTest extends BaseTestNGDecoratorTest {
    public static class FakeClass2 extends MockUp<Reference> {
        @Mock
        public String getClassName() {
            return "TEST2";
        }
    }

    @Test
    public void applyAndUseSomeFakes() {
        assertEquals(new SimpleComponent().getInfo(), "TEST1");
        assertEquals(new Reference("REAL2").getClassName(), "REAL2");

        new FakeClass2();

        assertEquals(new Reference("").getClassName(), "TEST2");
        assertEquals(new SimpleComponent().getInfo(), "TEST1");
    }

    @Test
    public void applyAndUseFakesAgain() {
        assertEquals(new SimpleComponent().getInfo(), "TEST1");
        assertEquals(new Reference("REAL2").getClassName(), "REAL2");

        new FakeClass2();

        assertEquals(new Reference("").getClassName(), "TEST2");
        assertEquals(new SimpleComponent().getInfo(), "TEST1");
    }

    @AfterMethod
    public void afterTest() {
        assertEquals(new Reference("REAL2").getClassName(), "REAL2");
    }

    public static class Temp {
    }

    private static final Temp temp = new Temp();

    @DataProvider(name = "data")
    public Object[][] createData1() {
        return new Object[][] { { temp } };
    }

    @Test(dataProvider = "data")
    public void checkNoMockingOfParametersWhenUsingDataProvider(Temp t) {
        // noinspection MisorderedAssertEqualsArgumentsTestNG
        assertSame(t, temp);
    }
}
