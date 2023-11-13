package codingdojo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import codingdojo.exception.ConflictException;
import codingdojo.model.Address;
import codingdojo.model.Customer;
import codingdojo.model.ExternalCustomer;
import codingdojo.model.ShoppingList;
import codingdojo.model.enums.CustomerType;
import codingdojo.service.CustomerSync;


class CustomerSyncTest {

    /**
     * The external record already exists in the customer db, so no need to create it.
     * There is new data in some fields, which is merged in.
     */
    @Test
    void syncCompanyByExternalId() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    void syncPrivatePersonByExternalId() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalPrivatePerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = Customer.builder()
            .customerType(CustomerType.PERSON)
            .internalId("67576")
            .externalId(externalId)
            .bonusPointsBalance(15)
            .build();

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    void syncShoppingLists() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setShoppingLists(List.of(new ShoppingList("eyeliner", "blusher")));

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    void syncNewCompanyCustomer() {

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId("12345");

        FakeDatabase db = new FakeDatabase();
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertTrue(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    void syncNewPrivateCustomer() {

        ExternalCustomer externalCustomer = createExternalPrivatePerson();
        externalCustomer.setExternalId("12345");

        FakeDatabase db = new FakeDatabase();
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertTrue(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    void conflictExceptionWhenExistingCustomerIsPerson() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = Customer.builder()
            .customerType(CustomerType.PERSON)
            .internalId("45435")
            .externalId(externalId)
            .build();

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncWithDataLayer(externalCustomer);
        }, printAfterState(db, toAssert).toString());

        Approvals.verify(toAssert);
    }

    @Test
    void syncByExternalIdButCompanyNumbersConflict() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setCompanyNumber("000-3234");

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertTrue(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }


    @Test
    void syncByCompanyNumber() {
        String companyNumber = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setCompanyNumber(companyNumber);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setCompanyNumber(companyNumber);
        customer.addShoppingList(new ShoppingList("eyeliner", "mascara", "blue bombe eyeshadow"));

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    void syncByCompanyNumberWithConflictingExternalId() {
        String companyNumber = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setCompanyNumber(companyNumber);
        externalCustomer.setExternalId("45646");

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setCompanyNumber(companyNumber);
        customer.setExternalId("conflicting id");

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncWithDataLayer(externalCustomer);
        }, printAfterState(db, toAssert).toString());

        Approvals.verify(toAssert);
    }

    @Test
    void conflictExceptionWhenExistingCustomerIsCompany() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalPrivatePerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = Customer.builder()
            .customerType(CustomerType.COMPANY)
            .companyNumber("32423-342")
            .internalId("45435")
            .externalId(externalId)
            .build();

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncWithDataLayer(externalCustomer);
        }, printAfterState(db, toAssert).toString());

        Approvals.verify(toAssert);
    }

    @Test
    void syncCompanyByExternalIdWithNonMatchingMasterId() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setName("company 1");

        Customer customer2 = Customer.builder()
            .companyNumber(externalCustomer.getCompanyNumber())
            .customerType(CustomerType.COMPANY)
            .internalId("45435234")
            .masterExternalId(externalId)
            .name("company 2")
            .build();

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        db.addCustomer(customer2);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }


    private ExternalCustomer createExternalPrivatePerson() {
        return ExternalCustomer.builder()
            .externalId("12345")
            .name("Joe Bloggs")
            .address(new Address("123 main st", "Stockholm", "SE-123 45"))
            .preferredStore("Nordstan")
            .shoppingLists(List.of(new ShoppingList("lipstick", "foundation")))
            .bonusPointsBalance(25)
            .build();
    }


    private ExternalCustomer createExternalCompany() {
        return ExternalCustomer.builder()
            .externalId("12345")
            .name("Acme Inc.")
            .address(new Address("123 main st", "Helsingborg", "SE-123 45"))
            .companyNumber("470813-8895")
            .shoppingLists(List.of(new ShoppingList("lipstick", "blusher")))
            .build();
    }

    private Customer createCustomerWithSameCompanyAs(ExternalCustomer externalCustomer) {
        return Customer.builder()
            .companyNumber(externalCustomer.getCompanyNumber())
            .customerType(CustomerType.COMPANY)
            .internalId("45435")
            .build();
    }

    private StringBuilder printBeforeState(ExternalCustomer externalCustomer, FakeDatabase db) {
        StringBuilder toAssert = new StringBuilder();
        toAssert.append("BEFORE:\n");
        toAssert.append(db.printContents());

        toAssert.append("\nSYNCING THIS:\n");
        toAssert.append(ExternalCustomerPrinter.print(externalCustomer, ""));
        return toAssert;
    }

    private StringBuilder printAfterState(FakeDatabase db, StringBuilder toAssert) {
        toAssert.append("\nAFTER:\n");
        toAssert.append(db.printContents());
        return toAssert;
    }
}
