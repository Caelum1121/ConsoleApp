package com.university.model;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * @author GroupHDGs
 */
public class Equipment {
    public enum Status {
        AVAILABLE, BORROWED
    }

    public enum Condition {
        BRAND_NEW, GOOD, NEEDS_MAINTENANCE, DAMAGED, OUT_OF_SERVICE
    }

    private String equipmentId;
    private String name;
    private Status status;
    private Date purchaseDate;
    private Condition condition;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public Equipment(String equipmentId, String name, Date purchaseDate, Condition condition) {
        if (equipmentId == null || equipmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Equipment ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.equipmentId = equipmentId;
        this.name = name;
        this.status = Status.AVAILABLE;
        this.purchaseDate = purchaseDate;
        this.condition = condition;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getFormattedPurchaseDate() {
        return dateFormat.format(purchaseDate);
    }

    @Override
    public String toString() {
        return "Equipment{id='" + equipmentId + "', name='" + name + "', status=" + status + "}";
    }
}