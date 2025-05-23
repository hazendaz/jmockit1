package petclinic.pets;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import jakarta.faces.view.ViewScoped;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import petclinic.owners.Owner;
import petclinic.owners.OwnerMaintenance;

/**
 * An application service class that handles {@link Pet}-related operations from the pet screen.
 */
@Named
@Transactional
@ViewScoped
public class PetScreen {
    @Inject
    private PetMaintenance petMaintenance;
    @Inject
    private OwnerMaintenance ownerMaintenance;
    @Nullable
    private List<PetType> types;
    @Nullable
    private Owner owner;
    @Nullable
    private Pet pet;

    @NonNull
    public List<PetType> getTypes() {
        if (types == null) {
            types = petMaintenance.findPetTypes();
        }

        return types;
    }

    @Nullable
    public Pet getPet() {
        return pet;
    }

    public void requestNewPet() {
        pet = new Pet();
    }

    public void selectOwner(int ownerId) {
        owner = ownerMaintenance.findById(ownerId);
        pet = new Pet();
    }

    public void selectPet(int petId) {
        pet = petMaintenance.findById(petId);
    }

    public void createOrUpdatePet() {
        if (pet != null) {
            if (pet.isNew()) {
                // noinspection ConstantConditions
                petMaintenance.createPet(owner, pet);
            } else {
                petMaintenance.updatePet(pet);
            }
        }
    }
}
