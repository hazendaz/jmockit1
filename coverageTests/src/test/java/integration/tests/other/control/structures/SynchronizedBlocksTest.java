/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests.other.control.structures;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SynchronizedBlocksTest {
    private final SynchronizedBlocks tested = new SynchronizedBlocks();

    @Test
    void doInSynchronizedBlock() {
        tested.doInSynchronizedBlock();
    }

    @Test
    void doInSynchronizedBlockWithTrue() {
        tested.doInSynchronizedBlockWithParameter(true);
    }

    @Test
    void doInSynchronizedBlockWithFalse() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            tested.doInSynchronizedBlockWithParameter(false);
        });
    }
}
