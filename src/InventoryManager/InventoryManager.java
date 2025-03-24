package InventoryManager;

import entities.equipment.Equipment;

import java.util.List;

/**
 * @author Chang Fang Cih - s4073761
 */
public interface InventoryManager {
    /**
     * Add a new equipment to the inventory
     * @param equipment The equipment to add
     */
    void addEquipment(Equipment equipment);

    /**
     * Update existing equipment details
     * @param equipment The updated equipment
     */
    void updateEquipment(Equipment equipment);

    /**
     * Remove equipment from inventory by ID
     * @param equipmentId The ID of the equipment to remove
     * @return true if removal was successful, false otherwise
     */
    boolean removeEquipment(String equipmentId);

    /**
     * Retrieve all equipment in the inventory
     * @return List of all equipment
     */
    List<Equipment> getAllEquipment();

    /**
     * Retrieve all equipment with AVAILABLE status
     * @return List of available equipment
     */
    List<Equipment> getAvailableEquipment();
}
