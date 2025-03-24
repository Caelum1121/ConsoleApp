package InventoryManager;

import entities.equipment.Equipment;
import InventoryManager.InventoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Chang Fang Cih - s4073761
 */

public class InventoryImplement implements InventoryManager {
    private Map<String, Equipment> inventory;

    /**
     * Constructor initializing the inventory
     */
    public InventoryImplement() {
        this.inventory = new HashMap<>();
    }

    /**
     * Constructor with initial data
     * @param initialEquipment List of initial equipment
     */
    public InventoryImplement(List<Equipment> initialEquipment) {
        this.inventory = new HashMap<>();
        for (Equipment equipment : initialEquipment) {
            this.inventory.put(equipment.getEquipmentId(), equipment);
        }
    }

    @Override
    public void addEquipment(Equipment equipment) {
        inventory.put(equipment.getEquipmentId(), equipment);
    }

    @Override
    public void updateEquipment(Equipment equipment) {
        if (inventory.containsKey(equipment.getEquipmentId())) {
            inventory.put(equipment.getEquipmentId(), equipment);
        }
    }

    @Override
    public boolean removeEquipment(String equipmentId) {
        Equipment equipment = inventory.get(equipmentId);
        // Only remove if equipment exists and is out of service
        if (equipment != null && equipment.getCondition() == Equipment.Condition.OUT_OF_SERVICE) {
            inventory.remove(equipmentId);
            return true;
        }
        return false;
    }

    @Override
    public List<Equipment> getAllEquipment() {
        return new ArrayList<>(inventory.values());
    }

    @Override
    public List<Equipment> getAvailableEquipment() {
        return inventory.values().stream()
                .filter(equipment -> equipment.getStatus() == Equipment.Status.AVAILABLE)
                .collect(Collectors.toList());
    }

    /**
     * Get equipment by condition
     * @param condition The condition to filter by
     * @return List of equipment with the specified condition
     */
    public List<Equipment> getEquipmentByCondition(Equipment.Condition condition) {
        return inventory.values().stream()
                .filter(equipment -> equipment.getCondition() == condition)
                .collect(Collectors.toList());
    }

    /**
     * Save inventory to a file
     * @return true if save was successful, false otherwise
     */
    public boolean saveInventoryToFile() {
        // Implementation for file I/O
        return true;
    }

    /**
     * Load inventory from a file
     * @return true if load was successful, false otherwise
     */
    public boolean loadInventoryFromFile() {
        // Implementation for file I/O
        return true;
    }
}
