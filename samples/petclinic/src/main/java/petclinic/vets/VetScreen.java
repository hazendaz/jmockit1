package petclinic.vets;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An application service class that handles {@link Vet}-related operations from the vet screen.
 */
@Named
@Transactional
@ViewScoped
public class VetScreen {
    @Inject
    private VetMaintenance vetMaintenance;
    @Nullable
    private List<Vet> vets;

    @Nullable
    public List<Vet> getVets() {
        return vets;
    }

    public void showVetList() {
        vets = vetMaintenance.findAll();
    }
}
