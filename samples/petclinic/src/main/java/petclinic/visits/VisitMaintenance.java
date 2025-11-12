/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package petclinic.visits;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

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
