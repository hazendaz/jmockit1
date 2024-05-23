package petclinic.vets;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;

import java.util.ArrayList;
import java.util.List;

import petclinic.util.Person;

/**
 * A veterinarian.
 */
@Entity
public class Vet extends Person {
    private static final long serialVersionUID = 1L;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(joinColumns = @JoinColumn(name = "vetId"), inverseJoinColumns = @JoinColumn(name = "specialtyId"))
    @OrderBy("name")
    private final List<Specialty> specialties = new ArrayList<>();

    public List<Specialty> getSpecialties() {
        return specialties;
    }

    public int getNrOfSpecialties() {
        return specialties.size();
    }
}
