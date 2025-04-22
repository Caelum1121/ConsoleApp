package com.university.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author GroupHDGs
 */
public class Professional extends Borrower {
    private String staffId;
    private String department;
    private List<LendingRecord> lendingRecords;

    public Professional(String id, String fullName, Date dateOfBirth, String phoneNumber, String email,
                        String staffId, String department, String username, String password) {
        super(username, password, new Person(id, fullName, dateOfBirth, phoneNumber, email));
        if (staffId == null || staffId.trim().isEmpty()) {
            throw new IllegalArgumentException("Staff ID cannot be null or empty");
        }
        this.staffId = staffId;
        this.department = department;
        this.lendingRecords = new ArrayList<>();
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        if (staffId == null || staffId.trim().isEmpty()) {
            throw new IllegalArgumentException("Staff ID cannot be null or empty");
        }
        this.staffId = staffId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void addLendingRecord(LendingRecord record) {
        if (record != null) {
            lendingRecords.add(record);
        }
    }

    @Override
    public List<LendingRecord> getLendingHistory() {
        return new ArrayList<>(lendingRecords);
    }

    public List<LendingRecord> searchLendingHistory(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getLendingHistory();
        }
        return lendingRecords.stream()
                .filter(r -> r.getEquipment().stream()
                        .anyMatch(e -> e.getName().toLowerCase().contains(searchTerm.toLowerCase())) ||
                        r.getPurpose().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public void updatePersonalInfo(ContactInfo contactInfo) {
        if (contactInfo == null) {
            throw new IllegalArgumentException("Contact info cannot be null");
        }
        getPersonDetails().setContactInfo(contactInfo);
    }

    @Override
    public String toString() {
        return "Professional{staffId='" + staffId + "', department='" + department + "', " + getPersonDetails().toString() + "}";
    }
}
