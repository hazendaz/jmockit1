package petclinic.pets;

import java.util.Date;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import javax.inject.Inject;

import petclinic.owners.Owner;
import petclinic.owners.OwnerData;
import petclinic.util.TestDatabase;

/**
 * Utility class for creation of {@link Pet} data in the test database, to be used in integration tests.
 */
public final class PetData extends TestDatabase {
    @Inject
    private OwnerData ownerData;

    @NonNull
    public Pet findOrCreate(@NonNull String name, @Nullable Date birthDate, @NonNull String petType) {
        Pet pet = findOne("select p from Pet p where p.name = ?1", name);

        if (pet == null) {
            pet = create(name, birthDate, petType);
        }

        return pet;
    }

    @NonNull
    public Pet create(@NonNull String name, @Nullable Date birthDate, @NonNull String petType) {
        Owner owner = ownerData.create("Pet Owner");
        return create(owner, name, birthDate, petType);
    }

    @NonNull
    public Pet create(@NonNull Owner owner, @NonNull String name, @Nullable Date birthDate, @NonNull String petType) {
        PetType type = findOrCreatePetType(petType);

        Pet pet = new Pet();
        owner.addPet(pet);
        pet.setName(name);
        pet.setBirthDate(birthDate);
        pet.setType(type);

        db.save(pet);
        return pet;
    }

    @NonNull
    PetType findOrCreatePetType(@NonNull String petType) {
        PetType type = findOne("select t from PetType t where t.name = ?1", petType);

        if (type == null) {
            type = createType(petType);
        }

        return type;
    }

    @NonNull
    PetType createType(@NonNull String name) {
        PetType type = new PetType();
        type.setName(name);
        db.save(type);
        return type;
    }
}
