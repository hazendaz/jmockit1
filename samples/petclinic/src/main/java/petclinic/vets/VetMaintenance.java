/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package petclinic.vets;

import edu.umd.cs.findbugs.annotations.NonNull;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

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
