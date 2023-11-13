package codingdojo.mapper;

import java.util.Objects;

import codingdojo.model.Customer;
import codingdojo.model.ExternalCustomer;
import codingdojo.model.enums.CustomerType;

public class CustomerMapper {

    public Customer toNewCustomer(ExternalCustomer externalCustomer) {
        return Customer.builder()
            .externalId(externalCustomer.getExternalId())
            .masterExternalId(externalCustomer.getExternalId())
            .build();
    }

    public Customer toDuplicateCustomer(ExternalCustomer externalCustomer, Customer customer) {
        if (customer == null) {
            customer = toNewCustomer(externalCustomer);
        }
        customer.setName(externalCustomer.getName());
        return customer;
    }

    public Customer toCustomerWithExternalData(ExternalCustomer externalCustomer, Customer customer) {
        if (customer == null) {
            customer = toNewCustomer(externalCustomer);
        }
        customer.setName(externalCustomer.getName());
        customer.setPreferredStore(externalCustomer.getPreferredStore());
        customer.setAddress(externalCustomer.getAddress());
        customer.addShoppingLists(externalCustomer.getShoppingLists());

        if (externalCustomer.isCompany()) {
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
            customer.setCustomerType(CustomerType.COMPANY);
        } else {
            if (toUpdateBonusPointsBalance(externalCustomer, customer)) {
                customer.setBonusPointsBalance(externalCustomer.getBonusPointsBalance());
            }
            customer.setCustomerType(CustomerType.PERSON);
        }
        return customer;
    }

    private boolean toUpdateBonusPointsBalance(ExternalCustomer externalCustomer, Customer customer) {
        return customer.getBonusPointsBalance() == null ||
            !Objects.equals(customer.getBonusPointsBalance(), externalCustomer.getBonusPointsBalance());
    }
}
