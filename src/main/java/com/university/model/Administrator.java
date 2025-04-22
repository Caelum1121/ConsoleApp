package com.university.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author GroupHDGs
 */
public class Administrator extends User {
    private String adminId;

    public Administrator(String username, String password, String adminId) {
        super(username, password);
        if (adminId == null || adminId.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin ID cannot be null or empty");
        }
        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        if (adminId == null || adminId.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin ID cannot be null or empty");
        }
        this.adminId = adminId;
    }

    public Equipment createEquipment(String equipmentId, String name, Date purchaseDate,
                                     Equipment.Condition condition, byte[] image) {
        Equipment equipment = new Equipment(equipmentId, name, purchaseDate, condition);
        return equipment;
    }

    public void deleteEntity(Object entity) {
        // Implement soft delete (e.g., set a 'deleted' flag in the database)
    }

    public Map<String, Object> generateSystemStatistics(List<Equipment> equipmentList, List<LendingRecord> lendingRecords) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEquipment", equipmentList.size());
        stats.put("totalBorrowers", lendingRecords.stream()
                .map(LendingRecord::getBorrower)
                .distinct()
                .count());
        stats.put("totalLendingRecords", lendingRecords.size());
        stats.put("overdueRecords", lendingRecords.stream()
                .filter(LendingRecord::isOverdue)
                .count());
        stats.put("mostBorrowedEquipment", equipmentList.stream()
                .max((e1, e2) -> Integer.compare(
                        Math.toIntExact(lendingRecords.stream().filter(r -> r.getEquipment().contains(e1)).count()),
                        Math.toIntExact(lendingRecords.stream().filter(r -> r.getEquipment().contains(e2)).count())
                ))
                .map(Equipment::getName)
                .orElse("N/A"));
        return stats;
    }

    @Override
    public String toString() {
        return "Administrator{adminId='" + adminId + "', " + super.toString() + "}";
    }
}