package codingdojo.service;

import codingdojo.exception.ConflictException;
import codingdojo.model.Customer;
import codingdojo.repository.CustomerDataAccess;
import codingdojo.model.CustomerMatches;
import codingdojo.model.enums.CustomerType;
import codingdojo.model.ExternalCustomer;
import codingdojo.model.ShoppingList;

public class CustomerDataAccessService {
    private final CustomerDataAccess customerDataAccess;

    public CustomerDataAccessService(CustomerDataAccess customerDataAccess) {
        this.customerDataAccess = customerDataAccess;
    }

    public void updateConsumerShoppingList(ExternalCustomer externalCustomer) {
        for (ShoppingList consumerShoppingList : externalCustomer.getShoppingLists()) {
            this.customerDataAccess.updateShoppingList(consumerShoppingList);
        }
    }

    public Customer updateCustomer(Customer customer) {
        return this.customerDataAccess.updateCustomerRecord(customer);
    }

    public CustomerMatches loadCustomer(ExternalCustomer externalCustomer) {
        if (externalCustomer.isCompany()) {
            return loadCompany(externalCustomer);
        } else {
            return loadPerson(externalCustomer);
        }
    }

    public boolean isCustomerCreated(Customer customer) {
        boolean created = customer.getInternalId() == null;
        this.createOrUpdate(customer);
        return created;
    }

    public Customer createOrUpdate(Customer customer) {
        if (customer.getInternalId() == null) {
            return createCustomer(customer);
        } else {
            return updateCustomer(customer);
        }
    }

    public Customer createCustomer(Customer customer) {
        return this.customerDataAccess.createCustomerRecord(customer);
    }

    public CustomerMatches loadCompany(ExternalCustomer externalCustomer) {

        final String externalId = externalCustomer.getExternalId();
        final String companyNumber = externalCustomer.getCompanyNumber();

        CustomerMatches customerMatches = customerDataAccess.loadCompanyCustomer(externalId, companyNumber);

        if (customerMatches.getCustomer() != null && !CustomerType.COMPANY.equals(customerMatches.getCustomer().getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
        }

        if ("ExternalId".equals(customerMatches.getMatchTerm())) {
            String customerCompanyNumber = customerMatches.getCustomer().getCompanyNumber();
            if (!companyNumber.equals(customerCompanyNumber)) {
                customerMatches.addDuplicate(customerMatches.getCustomer());
                customerMatches.setCustomer(null);
            }
        } else if ("CompanyNumber".equals(customerMatches.getMatchTerm())) {
            String customerExternalId = customerMatches.getCustomer().getExternalId();
            if (customerExternalId != null && !externalId.equals(customerExternalId)) {
                throw new ConflictException("Existing customer for externalCustomer " + companyNumber + " doesn't match external id " + externalId + " instead found " + customerExternalId );
            }
            Customer customer = customerMatches.getCustomer();
            customer.setExternalId(externalId);
            customer.setMasterExternalId(externalId);
        }

        return customerMatches;
    }

    public CustomerMatches loadPerson(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();

        CustomerMatches customerMatches = customerDataAccess.loadPersonCustomer(externalId);

        if (customerMatches.getCustomer() != null) {
            if (!CustomerType.PERSON.equals(customerMatches.getCustomer().getCustomerType())) {
                throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a person");
            }

            if (!"ExternalId".equals(customerMatches.getMatchTerm())) {
                Customer customer = customerMatches.getCustomer();
                customer.setExternalId(externalId);
                customer.setMasterExternalId(externalId);
            }
        }

        return customerMatches;
    }
}
