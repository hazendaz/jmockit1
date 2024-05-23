package tutorial.domain;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tutorial.persistence.Database.persist;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.junit.jupiter.api.Test;

/**
 * The Class MyBusinessServiceTest.
 */
class MyBusinessServiceTest {

    /** The data. */
    @Tested
    EntityX data = new EntityX(1, "abc", "someone@somewhere.com");

    /** The business service. */
    @Tested(fullyInitialized = true)
    MyBusinessService businessService;

    /** The any email. */
    @Mocked
    SimpleEmail anyEmail;

    /**
     * Do business operation xyz.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void doBusinessOperationXyz() throws Exception {
        EntityX existingItem = new EntityX(1, "AX5", "abc@xpta.net");
        persist(existingItem);

        businessService.doBusinessOperationXyz();

        // implies "data" was persisted
        assertNotEquals(0, data.getId());
        new Verifications() {
            {
                anyEmail.send();
                times = 1;
            }
        };
    }

    /**
     * Do business operation xyz with invalid email address.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void doBusinessOperationXyzWithInvalidEmailAddress() throws Exception {
        String email = "invalid address";
        data.setCustomerEmail(email);
        new Expectations() {
            {
                anyEmail.addTo(email);
                result = new EmailException();
            }
        };

        assertThrows(EmailException.class, () -> {
            businessService.doBusinessOperationXyz();
        });
    }
}
