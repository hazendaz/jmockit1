package petclinic.vets;

import edu.umd.cs.findbugs.annotations.NonNull;
import petclinic.util.TestDatabase;

/**
 * Utility class for creation of {@link Vet} data in the test database, to be used in integration tests.
 */
public final class VetData extends TestDatabase {
    @NonNull
    public Vet create(@NonNull String fullName, @NonNull String... specialtyNames) {
        String[] names = fullName.split(" ");

        Vet vet = new Vet();
        vet.setFirstName(names[0]);
        vet.setLastName(names[names.length - 1]);

        for (String specialtyName : specialtyNames) {
            Specialty specialty = new Specialty();
            specialty.setName(specialtyName);

            vet.getSpecialties().add(specialty);
        }

        db.save(vet);
        return vet;
    }
}
