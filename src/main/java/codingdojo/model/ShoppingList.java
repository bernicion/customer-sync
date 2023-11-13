package codingdojo.model;

import java.util.List;

public record ShoppingList(List<String> products) {

    public ShoppingList(String... products) {
        this(List.of(products));
    }
}
