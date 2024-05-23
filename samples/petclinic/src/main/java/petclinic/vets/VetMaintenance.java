package petclinic.vets;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import javax.inject.Inject;
import javax.transaction.Transactional;

import petclinic.util.Database;

/**
 * A domain service class for {@link Vet}-related business operations.
 */
@Transactional
public class VetMaintenance {
    @Inject
    private Database db;

    @NonNull
    public List<Vet> findAll() {
        return db.find("select v from Vet v order by v.lastName, v.firstName");
    }
}
