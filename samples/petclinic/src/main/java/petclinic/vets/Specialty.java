package petclinic.vets;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

import petclinic.util.BaseEntity;

/**
 * A {@linkplain Vet Vet's} specialty (for example, "Dentistry").
 */
@Entity
public class Specialty extends BaseEntity {
    private static final long serialVersionUID = 1L;
    @NotNull
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
