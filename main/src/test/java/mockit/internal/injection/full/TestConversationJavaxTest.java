/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.injection.full;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class TestConversationJavaxTest {

    private TestConversationJavax conversation;

    @BeforeEach
    void setUp() {
        conversation = new TestConversationJavax();
    }

    @Test
    void initialStateIsTransient() {
        assertTrue(conversation.isTransient());
    }

    @Test
    void initialIdIsNull() {
        assertNull(conversation.getId());
    }

    @Test
    void initialTimeoutIsZero() {
        assertEquals(0L, conversation.getTimeout());
    }

    @Test
    void beginMakesConversationNonTransient() {
        conversation.begin();
        // After begin, conversation is non-transient
        assertEquals(false, conversation.isTransient());
    }

    @Test
    void beginSetsIdToCounter() {
        conversation.begin();
        assertEquals("1", conversation.getId());
    }

    @Test
    void beginTwiceIncreasesCounter() {
        conversation.end();
        conversation.begin();
        conversation.end();
        conversation.begin();
        assertEquals("2", conversation.getId());
    }

    @Test
    void beginWithIdUsesGivenId() {
        conversation.begin("my-conversation-id");
        assertEquals("my-conversation-id", conversation.getId());
        assertEquals(false, conversation.isTransient());
    }

    @Test
    void endMakesConversationTransient() {
        conversation.begin();
        conversation.end();
        assertTrue(conversation.isTransient());
    }

    @Test
    void endClearsId() {
        conversation.begin("test-id");
        conversation.end();
        assertNull(conversation.getId());
    }

    @Test
    void setAndGetTimeout() {
        conversation.setTimeout(5000L);
        assertEquals(5000L, conversation.getTimeout());
    }

    @Test
    void setTimeoutToZero() {
        conversation.setTimeout(1000L);
        conversation.setTimeout(0L);
        assertEquals(0L, conversation.getTimeout());
    }
}
