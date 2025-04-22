package com.university.model;

import java.util.List;

/**
 * @author GroupHDGs
 */
public abstract class Borrower extends User {
    private Person personDetails;

    public Borrower(String username, String password, Person personDetails) {
        super(username, password);
        if (personDetails == null) {
            throw new IllegalArgumentException("Person details cannot be null");
        }
        this.personDetails = personDetails;
    }

    public Person getPersonDetails() {
        return personDetails;
    }

    public abstract List<LendingRecord> getLendingHistory();

    public abstract void updatePersonalInfo(ContactInfo contactInfo);
}