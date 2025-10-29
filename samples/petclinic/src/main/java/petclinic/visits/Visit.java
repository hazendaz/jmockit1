package petclinic.visits;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

import petclinic.pets.Pet;
import petclinic.util.BaseEntity;

/**
 * A visit from a pet and its owner to the clinic.
 */
@Entity
public class Visit extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @NotNull
    private String description;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "petId")
    private Pet pet;

    /**
     * Creates a new instance of Visit for the current date.
     */
    public Visit() {
        date = new Date();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }
}
