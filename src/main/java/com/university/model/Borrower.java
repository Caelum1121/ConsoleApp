package com.university.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for users who can borrow equipment in the university equipment lending system.
 * @author GroupHDGs
 */
@Entity
@Table(name = "Borrower")
public abstract class Borrower extends User {
    @OneToOne
    @JoinColumn(name = "entity_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Person personDetails;

    @OneToMany(mappedBy = "borrower", cascade = CascadeType.ALL)
    private List<LendingRecord> lendingRecords = new ArrayList<>();

    protected Borrower() {}

    public Borrower(String username, String password, String entityId, Person personDetails) {
        super();
        if (personDetails == null) {
            throw new IllegalArgumentException("Person details cannot be null");
        }
        if (!entityId.equals(personDetails.getId())) {
            throw new IllegalArgumentException("Entity ID must match Person ID");
        }
        this.personDetails = personDetails;
    }

    public Person getPersonDetails() {
        return personDetails;
    }

    public void setPersonDetails(Person personDetails) {
        if (personDetails == null) {
            throw new IllegalArgumentException("Person details cannot be null");
        }
        this.personDetails = personDetails;
    }

    public List<LendingRecord> getLendingRecords() {
        return new ArrayList<>(lendingRecords);
    }

    public void addLendingRecord(LendingRecord record) {
        if (record != null && !lendingRecords.contains(record)) {
            lendingRecords.add(record);
            record.setBorrower(this);
        }
    }

    public abstract List<LendingRecord> getLendingHistory();

    @Override
    public String toString() {
        return "Borrower{username='" + getUsername() + "', role='" + getRole() + "'}";
    }
}