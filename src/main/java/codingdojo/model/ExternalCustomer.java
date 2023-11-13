package codingdojo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ExternalCustomer {
    private Address address;
    private String name;
    private String preferredStore;
    @Builder.Default()
    private List<ShoppingList> shoppingLists = new ArrayList<>();
    private String externalId;
    private String companyNumber;
    private Integer bonusPointsBalance;

    public List<ShoppingList> getShoppingLists() {
        return Collections.unmodifiableList(shoppingLists);
    }

    public boolean isCompany() {
        return companyNumber != null;
    }
}
