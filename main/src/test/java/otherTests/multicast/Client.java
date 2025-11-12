/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package otherTests.multicast;

public final class Client {
    private final String address;

    public Client(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
