/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.injection.full;

import jakarta.enterprise.context.Conversation;

final class TestConversationJakarta implements Conversation {
    private boolean currentlyTransient;
    private int counter;
    private String currentId;
    private long currentTimeout;

    TestConversationJakarta() {
        currentlyTransient = true;
    }

    @Override
    public void begin() {
        counter++;
        currentId = String.valueOf(counter);
        currentlyTransient = false;
    }

    @Override
    public void begin(String id) {
        counter++;
        currentId = id;
        currentlyTransient = false;
    }

    @Override
    public void end() {
        currentlyTransient = true;
        currentId = null;
    }

    @Override
    public String getId() {
        return currentId;
    }

    @Override
    public long getTimeout() {
        return currentTimeout;
    }

    @Override
    public void setTimeout(long milliseconds) {
        currentTimeout = milliseconds;
    }

    @Override
    public boolean isTransient() {
        return currentlyTransient;
    }
}
