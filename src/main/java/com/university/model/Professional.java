package com.university.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a professional staff user who can borrow equipment.
 * @author GroupHDGs
 */
@Entity
@Table(name = "users")
@DiscriminatorValue("PROFESSIONAL")
public class Professional extends Borrower {
    protected Professional() {}

    public Professional(String username, String password, String entityId, Person personDetails) {
        super(username, password, entityId, personDetails);
    }

    public List<LendingRecord> searchLendingHistory(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getLendingRecords();
        }
        return getLendingRecords().stream()
                .filter(r -> r.getEquipment().stream()
                        .anyMatch(e -> e.getName().toLowerCase().contains(searchTerm.toLowerCase())) ||
                        r.getPurpose().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void updatePersonalInfo(String phoneNumber, String email) {
        if (getPersonDetails() != null) {
            getPersonDetails().setPhoneNumber(phoneNumber);
            getPersonDetails().setEmail(email);
        }
    }

    @Override
    public List<LendingRecord> getLendingHistory() {
        return getLendingRecords();
    }

    @Override
    public String toString() {
        return "Professional{entityId='" + getEntityId() + "', username='" + getUsername() + "'}";
    }
}
