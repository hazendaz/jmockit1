package petclinic.pets;

import static jakarta.persistence.TemporalType.DATE;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Temporal;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import petclinic.owners.Owner;
import petclinic.util.BaseEntity;
import petclinic.visits.Visit;

/**
 * A pet.
 */
@Entity
public class Pet extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "ownerId")
    @NotNull
    private Owner owner;

    @NotNull
    private String name;

    @Temporal(DATE)
    private Date birthDate;

    @ManyToOne
    @JoinColumn(name = "typeId")
    @NotNull
    private PetType type;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pet")
    @OrderBy("date desc")
    private final List<Visit> visits = new ArrayList<>();

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public PetType getType() {
        return type;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void addVisit(Visit visit) {
        visits.add(visit);
        visit.setPet(this);
    }
}
