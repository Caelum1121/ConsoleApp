package com.university.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Map;

/**
 * Represents an administrator user who manages the system.
 * @author GroupHDGs
 */
@Entity
@Table(name = "users")
@DiscriminatorValue("ADMINISTRATOR")
public class Administrator extends User {
    protected Administrator() {}

    public Administrator(String username, String password, String entityId) {
        super(username, password, entityId);
    }

    public Equipment createEquipment(String equipmentId, String name, java.util.Date purchaseDate,
                                     Equipment.Condition condition, byte[] image) {
        return new Equipment(equipmentId, name, purchaseDate, condition, image);
    }

    public void deleteEntity(Object entity) {
        // Deletion logic handled in DAO
    }

    public Map<String, Object> generateSystemStatistics(List<Equipment> equipmentList, List<LendingRecord> lendingRecords) {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalEquipment", equipmentList.size());
        stats.put("totalBorrowers", lendingRecords.stream().map(LendingRecord::getBorrower).distinct().count());
        stats.put("totalLendingRecords", lendingRecords.size());
        stats.put("overdueRecords", lendingRecords.stream().filter(LendingRecord::isOverdue).count());
        return stats;
    }

    @Override
    public String toString() {
        return "Administrator{entityId='" + getEntityId() + "', username='" + getUsername() + "'}";
    }
}