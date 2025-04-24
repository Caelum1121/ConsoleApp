package com.university.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Represents a piece of equipment in the university equipment lending system.
 * @author GroupHDGs
 */
@Entity
@Table(name = "equipment")
public class Equipment {
    public enum Condition {
        GOOD, FAIR, POOR
    }

    @Id
    @Column(name = "equipment_id")
    private String equipmentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "purchase_date")
    @Temporal(TemporalType.DATE)
    private Date purchaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition")
    private Condition condition;

    @Column(name = "image")
    private byte[] image;

    protected Equipment() {}

    public Equipment(String equipmentId, String name, Date purchaseDate, Condition condition) {
        this(equipmentId, name, purchaseDate, condition, null);
    }

    public Equipment(String equipmentId, String name, Date purchaseDate, Condition condition, byte[] image) {
        if (equipmentId == null || equipmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Equipment ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.equipmentId = equipmentId;
        this.name = name;
        this.purchaseDate = purchaseDate;
        this.condition = condition;
        this.image = image;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Equipment{equipmentId='" + equipmentId + "', name='" + name + "'}";
    }
}