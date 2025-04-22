package com.university.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author GroupHDGs
 */
public class Visitor extends User {
    public Visitor() {
        super("visitor", "none");
    }

    public List<Equipment> filterEquipment(List<Equipment> equipmentList, String name, Equipment.Condition condition) {
        return equipmentList.stream()
                .filter(e -> e.getStatus() == Equipment.Status.AVAILABLE)
                .filter(e -> name == null || e.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(e -> condition == null || e.getCondition() == condition)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Visitor{}";
    }
}
