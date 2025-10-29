package petclinic.util;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A person having a first and a last name.
 */
@MappedSuperclass
public class Person extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @NotNull
    @Size(min = 1)
    protected String firstName;

    @NotNull
    @Size(min = 1)
    protected String lastName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
