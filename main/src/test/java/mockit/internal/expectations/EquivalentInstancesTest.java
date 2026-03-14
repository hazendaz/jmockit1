/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class EquivalentInstancesTest {

    private EquivalentInstances equivalentInstances;
    private final Object mock1 = new Object();
    private final Object mock2 = new Object();
    private final Object mock3 = new Object();

    @BeforeEach
    void setUp() {
        equivalentInstances = new EquivalentInstances();
    }

    @Test
    void isEquivalentInstanceWhenSameObject() {
        assertTrue(equivalentInstances.isEquivalentInstance(mock1, mock1));
    }

    @Test
    void isEquivalentInstanceWhenDifferentObjects() {
        assertFalse(equivalentInstances.isEquivalentInstance(mock1, mock2));
    }

    @Test
    void isEquivalentInstanceWhenInReplacementMap() {
        equivalentInstances.replacementMap.put(mock2, mock1);
        assertTrue(equivalentInstances.isEquivalentInstance(mock1, mock2));
    }

    @Test
    void isEquivalentInstanceWhenInInstanceMap() {
        equivalentInstances.instanceMap.put(mock2, mock1);
        assertTrue(equivalentInstances.isEquivalentInstance(mock1, mock2));
    }

    @Test
    void isEquivalentInstanceWhenInInstanceMapReverse() {
        equivalentInstances.instanceMap.put(mock1, mock2);
        assertTrue(equivalentInstances.isEquivalentInstance(mock1, mock2));
    }

    @Test
    void areNonEquivalentInstancesWhenBothNotInMaps() {
        // Neither in maps -> recordedInstanceMatchingAnyInstance=true, invokedInstanceMatchingSpecificInstance=false
        assertFalse(equivalentInstances.areNonEquivalentInstances(mock1, mock2));
    }

    @Test
    void areNonEquivalentInstancesWhenOnlyInvokedIsInMap() {
        equivalentInstances.instanceMap.put(mock2, mock3);
        // invocationInstance (mock1) not in maps -> recordedInstanceMatchingAnyInstance=true
        // invokedInstance (mock2) is in maps -> invokedInstanceMatchingSpecificInstance=true
        assertTrue(equivalentInstances.areNonEquivalentInstances(mock1, mock2));
    }

    @Test
    void areNonEquivalentInstancesWhenInvocationInstanceIsInMap() {
        equivalentInstances.instanceMap.put(mock1, mock3);
        // invocationInstance (mock1) is in maps -> recordedInstanceMatchingAnyInstance=false
        assertFalse(equivalentInstances.areNonEquivalentInstances(mock1, mock2));
    }

    @Test
    void areMatchingInstancesWhenMatchInstanceTrueAndSameObject() {
        assertTrue(equivalentInstances.areMatchingInstances(true, mock1, mock1));
    }

    @Test
    void areMatchingInstancesWhenMatchInstanceFalseAndEmptyMap() {
        assertTrue(equivalentInstances.areMatchingInstances(false, mock1, mock2));
    }

    @Test
    void areMatchingInstancesWhenMatchInstanceFalseAndSameObject() {
        equivalentInstances.instanceMap.put(mock3, mock1);
        // mock1 == mock2 is false, but they're not in different equivalence sets if map is empty for them
        assertTrue(equivalentInstances.areMatchingInstances(false, mock1, mock2));
    }

    @Test
    void areMatchingInstancesWhenInDifferentEquivalenceSets() {
        equivalentInstances.instanceMap.put(mock1, mock3);
        equivalentInstances.instanceMap.put(mock2, new Object());
        // mock1 and mock2 are in different entries -> are in different equivalence sets
        assertFalse(equivalentInstances.areMatchingInstances(false, mock1, mock2));
    }

    @Test
    void areInDifferentEquivalenceSetsWhenMock1EquivalentIsMock2() {
        equivalentInstances.instanceMap.put(mock1, mock2);
        // mock1Equivalent == mock2 -> not in different sets -> areMatchingInstances returns true
        assertTrue(equivalentInstances.areMatchingInstances(false, mock1, mock2));
    }

    @Test
    void areInDifferentEquivalenceSetsWhenMock2EquivalentIsMock1() {
        equivalentInstances.instanceMap.put(mock2, mock1);
        // mock2Equivalent == mock1 -> not in different sets
        assertTrue(equivalentInstances.areMatchingInstances(false, mock1, mock2));
    }

    @Test
    void instanceMapHasMocksInSeparateEntriesReturnsFalse() {
        // Only mock1 is in map, not mock2
        equivalentInstances.instanceMap.put(mock1, mock3);
        // mock1 found in map but mock2 not -> return false
        assertTrue(equivalentInstances.areMatchingInstances(false, mock1, mock2));
    }

    @Test
    void instanceMapHasMocksInSeparateEntriesReturnsTrue() {
        // Both mock1 and mock2 are keys in the map (with different values)
        equivalentInstances.instanceMap.put(mock1, mock3);
        equivalentInstances.instanceMap.put(mock2, new Object());
        assertFalse(equivalentInstances.areMatchingInstances(false, mock1, mock2));
    }

    @Test
    void getReplacementInstanceForMethodInvocationForConstructor() {
        equivalentInstances.replacementMap.put(mock1, mock2);
        // Constructor method name starts with '<'
        assertNull(equivalentInstances.getReplacementInstanceForMethodInvocation(mock1, "<init>()V"));
    }

    @Test
    void getReplacementInstanceForMethodInvocationForRegularMethod() {
        equivalentInstances.replacementMap.put(mock1, mock2);
        assertEquals(mock2, equivalentInstances.getReplacementInstanceForMethodInvocation(mock1, "doSomething()V"));
    }

    @Test
    void getReplacementInstanceForMethodInvocationReturnsNullWhenNotInMap() {
        assertNull(equivalentInstances.getReplacementInstanceForMethodInvocation(mock1, "doSomething()V"));
    }

    @Test
    void isReplacementInstanceForConstructorReturnsFalse() {
        equivalentInstances.replacementMap.put(mock1, mock2);
        assertFalse(equivalentInstances.isReplacementInstance(mock1, "<init>()V"));
    }

    @Test
    void isReplacementInstanceForRegularMethodWhenKeyInMap() {
        equivalentInstances.replacementMap.put(mock1, mock2);
        assertTrue(equivalentInstances.isReplacementInstance(mock1, "doSomething()V"));
    }

    @Test
    void isReplacementInstanceForRegularMethodWhenValueInMap() {
        equivalentInstances.replacementMap.put(mock3, mock1);
        assertTrue(equivalentInstances.isReplacementInstance(mock1, "doSomething()V"));
    }

    @Test
    void isReplacementInstanceReturnsFalseWhenNotInMap() {
        assertFalse(equivalentInstances.isReplacementInstance(mock1, "doSomething()V"));
    }
}
