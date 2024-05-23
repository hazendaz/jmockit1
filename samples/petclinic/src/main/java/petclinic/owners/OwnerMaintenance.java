package petclinic.owners;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.transaction.Transactional;

import petclinic.util.Database;

/**
 * A domain service class for {@link Owner}-related business operations.
 */
@Transactional
public class OwnerMaintenance {
    @Inject
    private Database db;

    @Nullable
    public Owner findById(int ownerId) {
        return db.findById(Owner.class, ownerId);
    }

    /**
     * Finds the owners whose last name <em>starts</em> with the given name.
     *
     * @param lastName
     *            a prefix of the owner's last name to search for
     *
     * @return list of matching owners (empty if none found)
     */
    @NonNull
    public List<Owner> findByLastName(@NonNull String lastName) {
        return db.find("select o from Owner o where o.lastName like ?1", lastName + '%');
    }

    public void createOrUpdate(@NonNull Owner newData) {
        db.save(newData);
    }
}
