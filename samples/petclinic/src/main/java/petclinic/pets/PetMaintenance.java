package petclinic.pets;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ValidationException;

import petclinic.owners.Owner;
import petclinic.util.Database;

/**
 * A domain service class for {@link Pet}-related business operations.
 */
@Transactional
public class PetMaintenance {
    @Inject
    private Database db;

    @Nullable
    public Pet findById(int id) {
        return db.findById(Pet.class, id);
    }

    /**
     * Finds all pet types.
     *
     * @return the types found, in order of name
     */
    @NonNull
    public List<PetType> findPetTypes() {
        return db.find("select t from PetType t order by t.name");
    }

    public void createPet(@NonNull Owner owner, @NonNull Pet data) {
        validate(owner, data);

        data.setOwner(owner);
        owner.addPet(data);
        db.save(data);
    }

    private void validate(@NonNull Owner owner, @NonNull Pet pet) {
        Pet existingPetOfSameName = owner.getPet(pet.getName());

        if (existingPetOfSameName != null) {
            throw new ValidationException("The owner already has a pet with this name.");
        }
    }

    public void updatePet(@NonNull Pet data) {
        db.save(data);
    }
}
