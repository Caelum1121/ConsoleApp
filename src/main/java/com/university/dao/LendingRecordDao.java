package com.university.dao;

import com.university.config.DatabaseConnection;
import com.university.model.LendingRecord;
import com.university.model.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class LendingRecordDao {
    public List<LendingRecord> findByStudent(Student student) {
        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            TypedQuery<LendingRecord> query = em.createQuery(
                    "SELECT lr FROM LendingRecord lr WHERE lr.borrower = :student",
                    LendingRecord.class
            );
            query.setParameter("student", student);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void save(LendingRecord lendingRecord) {
        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(lendingRecord);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to save lending record", e);
        } finally {
            em.close();
        }
    }
}
