package codingdojo.service;

import codingdojo.mapper.CustomerMapper;
import codingdojo.model.Customer;
import codingdojo.model.CustomerMatches;
import codingdojo.model.ExternalCustomer;
import codingdojo.repository.CustomerDataAccess;
import codingdojo.repository.CustomerDataLayer;

public class CustomerSync {
    private final CustomerMapper customerMapper= new CustomerMapper();
    private final CustomerDataAccessService customerDatabaseService;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    public CustomerSync(CustomerDataAccess db) {
        this.customerDatabaseService = new CustomerDataAccessService(db);
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {
        CustomerMatches customerMatches = customerDatabaseService.loadCustomer(externalCustomer);
        Customer customer = customerMapper.toCustomerWithExternalData(externalCustomer, customerMatches.getCustomer());

        customerMatches.getDuplicates().forEach(
            duplicate -> updateDuplicate(externalCustomer, duplicate)
        );

        customerDatabaseService.updateConsumerShoppingList(externalCustomer);
        return customerDatabaseService.isCustomerCreated(customer);
    }

    private void updateDuplicate(ExternalCustomer externalCustomer, Customer duplicate) {
        customerDatabaseService.createOrUpdate(customerMapper.toDuplicateCustomer(externalCustomer, duplicate));
    }
}
