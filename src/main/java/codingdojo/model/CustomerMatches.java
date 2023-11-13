package codingdojo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class CustomerMatches {
    private List<Customer> duplicates = new ArrayList<>();
    private String matchTerm;
    private Customer customer;

    public void addDuplicate(@NonNull Customer duplicate) {
        ArrayList<Customer> newList = new ArrayList<>(this.duplicates);
        newList.add(duplicate);
        this.setDuplicates(newList);
    }

    public List<Customer> getDuplicates() {
        return Collections.unmodifiableList(duplicates);
    }
}
