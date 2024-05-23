package petclinic.pets;

import static java.util.Arrays.asList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import jakarta.validation.ValidationException;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.Test;

import petclinic.owners.Owner;
import petclinic.owners.OwnerData;
import petclinic.util.SUT;
import petclinic.util.TestUtil;

/**
 * Integration tests for {@link Pet}-related operations, at the application service level. Each test runs in a database
 * transaction that is rolled back at the end of the test.
 */
final class PetScreenTest {
    @TestUtil
    OwnerData ownerData;
    @TestUtil
    PetData petData;
    @SUT
    PetScreen petScreen;

    @Test
    void findAllPetTypes() {
        PetType type1 = petData.createType("type1");
        PetType type2 = petData.createType("Another type");

        List<PetType> petTypes = petScreen.getTypes();
        List<PetType> petTypesAgain = petScreen.getTypes();

        petTypes.retainAll(asList(type1, type2));
        assertSame(type1, petTypes.get(1));
        assertSame(type2, petTypes.get(0));
        assertSame(petTypes, petTypesAgain);
    }

    @Test
    void createPetWithGeneratedId() {
        String petName = "bowser";
        Owner owner = ownerData.create("The Owner");
        assumeTrue(owner.getPet(petName) == null);
        petScreen.selectOwner(owner.getId());

        PetType type = petData.findOrCreatePetType("dog");
        assertEquals("dog", type.getName());

        petScreen.requestNewPet();
        Pet pet = petScreen.getPet();
        pet.setName(petName);
        pet.setType(type);
        pet.setBirthDate(new Date());
        petScreen.createOrUpdatePet();

        assertNotNull(pet.getId());
        assertSame(owner, pet.getOwner());
        assertEquals(1, owner.getPets().size());
        assertSame(pet, owner.getPet(petName));
    }

    @Test
    void attemptToCreatePetWithDuplicateNameForSameOwner() {
        Owner owner = ownerData.create("The Owner");
        petScreen.selectOwner(owner.getId());
        Date birthDate = new GregorianCalendar(2005, Calendar.AUGUST, 6).getTime();
        Pet ownedPet = petData.create(owner, "Buck", birthDate, "dog");

        petScreen.requestNewPet();
        Pet secondPet = petScreen.getPet();
        secondPet.setName(ownedPet.getName());

        ValidationException thrown = assertThrows(ValidationException.class, () -> petScreen.createOrUpdatePet());

        assertTrue(thrown.getMessage().contains("owner already has a pet with this name"));
    }

    @Test
    void attemptToCreatePetWithoutAnOwnerHavingBeenSelected() {
        petScreen.createOrUpdatePet();

        assertNull(petScreen.getPet());
    }

    @Test
    void updatePetName() {
        Date birthDate = new GregorianCalendar(2005, Calendar.AUGUST, 6).getTime();
        Pet pet = petData.create("Pet", birthDate, "cat");
        petScreen.selectPet(pet.getId());

        String oldName = pet.getName();
        String newName = oldName + "X";
        pet.setName(newName);
        petScreen.createOrUpdatePet();

        Pet petUpdated = petScreen.getPet();
        petData.refresh(petUpdated);
        assertEquals(newName, petUpdated.getName());
        assertEquals(pet.getBirthDate(), petUpdated.getBirthDate());
        assertEquals(pet.getType(), petUpdated.getType());
    }
}
