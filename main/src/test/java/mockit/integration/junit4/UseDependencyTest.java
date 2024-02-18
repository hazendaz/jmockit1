package mockit.integration.junit4;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

public final class UseDependencyTest {
    @Test
    public void useMockedDependency() {
        if (AnotherDependency.mockedAtSuiteLevel) {
            assertFalse(AnotherDependency.alwaysTrue());
        } else {
            assertTrue(AnotherDependency.alwaysTrue());
        }
    }

    private static final boolean STATIC_FIELD = Dependency.alwaysTrue();
    private final boolean instanceField = Dependency.alwaysTrue();

    @Test
    public void useFieldSetThroughDirectInstanceInitializationRatherThanBeforeMethod() {
        assertTrue(instanceField, "Dependency still mocked");
    }

    @Test
    public void useFieldSetThroughDirectClassInitializationRatherThanBeforeClassMethod() {
        assertTrue(STATIC_FIELD, "Dependency still mocked");
    }
}
