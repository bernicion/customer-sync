package codingdojo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import codingdojo.model.enums.CustomerType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Customer {
    @EqualsAndHashCode.Include
    private String externalId;
    @EqualsAndHashCode.Include
    private String masterExternalId;
    @EqualsAndHashCode.Include
    private String companyNumber;
    private Address address;
    private String preferredStore;
    @Builder.Default()
    private List<ShoppingList> shoppingLists = new ArrayList<>();
    private String internalId;
    private String name;
    private CustomerType customerType;
    private Integer bonusPointsBalance;

    public List<ShoppingList> getShoppingLists() {
        return Collections.unmodifiableList(shoppingLists);
    }

    public void addShoppingList(ShoppingList consumerShoppingList) {
        ArrayList<ShoppingList> newList = new ArrayList<>(this.shoppingLists);
        newList.add(consumerShoppingList);
        this.setShoppingLists(newList);
    }

    public void addShoppingLists(List<ShoppingList> consumerShoppingLists) {
        ArrayList<ShoppingList> newList = new ArrayList<>(this.shoppingLists);
        newList.addAll(consumerShoppingLists);
        this.setShoppingLists(newList);
    }
}
