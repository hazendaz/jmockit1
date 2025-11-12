/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package otherTests.multicast;

public interface StatusListener {
    void messageSent(Client toClient);

    void messageDisplayedByClient(Client client);

    void messageReadByClient(Client client);
}
