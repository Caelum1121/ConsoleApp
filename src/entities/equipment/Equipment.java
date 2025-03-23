package entities.equipment;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * @author Chang Fang Cih - s4073761
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

    /**
     * Constructor for creating equipment objects
     * @param equipmentId Equipment's ID
     * @param name Equipment's name
     * @param purchaseDate Equipment's purchase date
     * @param condition Equipment's condition
     */
    public Equipment(String equipmentId, String name, Date purchaseDate, Condition condition) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.status = Status.AVAILABLE;
        this.purchaseDate = purchaseDate;
        this.condition = condition;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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

    // Method to get formatted purchase date
    public String getFormattedPurchaseDate() {
        return dateFormat.format(purchaseDate);
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "equipmentId='" + equipmentId + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", purchaseDate=" + getFormattedPurchaseDate() +
                ", condition=" + condition +
                '}';
    }
}
