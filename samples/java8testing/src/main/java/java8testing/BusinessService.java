/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package java8testing;

/**
 * The Class BusinessService.
 */
public final class BusinessService {

    /** The collaborator. */
    private final Collaborator collaborator;

    /**
     * Instantiates a new business service.
     *
     * @param collaborator
     *            the collaborator
     */
    public BusinessService(Collaborator collaborator) {
        this.collaborator = collaborator;
    }

    /**
     * Gets the collaborator.
     *
     * @return the collaborator
     */
    public Collaborator getCollaborator() {
        return collaborator;
    }

    /**
     * Perform business operation.
     *
     * @param value
     *            the value
     *
     * @return the string
     */
    public String performBusinessOperation(int value) {
        return collaborator.doSomething(value + 1);
    }
}
