package petclinic.visits;

import edu.umd.cs.findbugs.annotations.Nullable;

import jakarta.faces.view.ViewScoped;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import petclinic.pets.Pet;
import petclinic.pets.PetMaintenance;

/**
 * An application service class that handles {@link Visit}-related operations from the visit screen.
 */
@Named
@Transactional
@ViewScoped
public class VisitScreen {
    @Inject
    private VisitMaintenance visitMaintenance;
    @Inject
    private PetMaintenance petMaintenance;
    @Nullable
    private Pet pet;
    @Nullable
    private Visit visit;
    @Nullable
    private List<Visit> visits;

    @Nullable
    public Pet getPet() {
        return pet;
    }

    @Nullable
    public Visit getVisit() {
        return visit;
    }

    @Nullable
    public List<Visit> getVisits() {
        return visits;
    }

    public void selectPet(int petId) {
        pet = petMaintenance.findById(petId);
    }

    public void selectVisit(int visitId) {
        visit = visitMaintenance.findById(visitId);
        pet = visit == null ? null : visit.getPet();
    }

    public void requestNewVisit() {
        visit = new Visit();
    }

    public void createOrUpdateVisit() {
        if (pet != null && visit != null) {
            visitMaintenance.create(pet, visit);
        }
    }

    public void showVisits() {
        if (pet != null) {
            visits = visitMaintenance.findByPetId(pet.getId());
        }
    }
}
