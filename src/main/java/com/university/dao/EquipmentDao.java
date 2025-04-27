package com.university.dao;

import com.university.config.DatabaseConnection;
import com.university.model.Equipment;
import com.university.model.LendingRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class EquipmentDao {
    public List<Equipment> findAvailableEquipment() {
        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            TypedQuery<Equipment> query = em.createQuery(
                    "SELECT DISTINCT e FROM Equipment e " +
                            "LEFT JOIN LendingRecord lr ON e MEMBER OF lr.equipment " +
                            "WHERE lr IS NULL OR lr.status != :status",
                    Equipment.class
            );
            query.setParameter("status", LendingRecord.Status.BORROWED);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Equipment> searchEquipment(String searchTerm) {
        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            TypedQuery<Equipment> query = em.createQuery(
                    "SELECT DISTINCT e FROM Equipment e " +
                            "LEFT JOIN LendingRecord lr ON e MEMBER OF lr.equipment " +
                            "WHERE (lr IS NULL OR lr.status != :status) " +
                            "AND LOWER(e.name) LIKE :searchTerm",
                    Equipment.class
            );
            query.setParameter("status", LendingRecord.Status.BORROWED);
            query.setParameter("searchTerm", "%" + searchTerm.toLowerCase() + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Equipment findById(String equipmentId) {
        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            return em.find(Equipment.class, equipmentId);
        } finally {
            em.close();
        }
    }
}