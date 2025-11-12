/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests.other.control.structures;

public final class SynchronizedBlocks {
    private final Object LOCK;

    public SynchronizedBlocks() {
        LOCK = new Object();
    }

    public void doInSynchronizedBlock() {
        synchronized (LOCK) {
            // Do something.
            LOCK.toString();
        }
    }

    public void doInSynchronizedBlockWithParameter(boolean b) {
        synchronized (LOCK) {
            if (!b) {
                throw new RuntimeException();
            }
        }
    }
}
