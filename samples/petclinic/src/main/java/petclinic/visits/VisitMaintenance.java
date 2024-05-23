package petclinic.visits;

import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.transaction.Transactional;

import petclinic.pets.Pet;
import petclinic.util.Database;

/**
 * A domain service class for {@link Visit}-related business operations.
 */
@Transactional
public class VisitMaintenance {
    @Inject
    private Database db;

    public void create(@NonNull Pet visitedPet, @NonNull Visit visitData) {
        visitData.setPet(visitedPet);
        visitedPet.addVisit(visitData);
        db.save(visitData);
    }

    @Nullable
    public Visit findById(int visitId) {
        return db.findById(Visit.class, visitId);
    }

    @NonNull
    public List<Visit> findByPetId(int petId) {
        return db.find("select v from Visit v where v.pet.id = ?1", petId);
    }
}
