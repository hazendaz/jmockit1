/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package otherTests.testng;

import static org.testng.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

// Just to make sure no NPEs or other exceptions occur from JMockit-TestNG integration.
public final class ParallelExecutionTest {
    final AtomicInteger counter = new AtomicInteger();

    @Test(threadPoolSize = 4, invocationCount = 10)
    public void parallelExecution() {
        counter.incrementAndGet();
    }

    @AfterClass
    public void checkCounter() {
        assertEquals(counter.get(), 10);
    }
}
