package com.university.service;

import com.university.dao.EquipmentDao;
import com.university.dao.LendingRecordDao;
import com.university.model.*;
import jakarta.persistence.EntityManager;
import com.university.config.DatabaseConnection;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StudentService {
    private final EquipmentDao equipmentDao = new EquipmentDao();
    private final LendingRecordDao lendingRecordDao = new LendingRecordDao();

    public List<LendingRecord> getLendingHistory(Student student) {
        return lendingRecordDao.findByStudent(student);
    }

    public List<Equipment> getAvailableEquipment() {
        return equipmentDao.findAvailableEquipment();
    }

    public List<Equipment> searchEquipment(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAvailableEquipment();
        }
        return equipmentDao.searchEquipment(searchTerm);
    }

    public void requestEquipmentLoan(Student student, Equipment equipment, Course course, String purpose, String recordId) {
        if (!student.getEnrolledCourses().contains(course)) {
            throw new IllegalArgumentException("Student is not enrolled in the selected course");
        }

        Calendar calendar = Calendar.getInstance();
        Date borrowDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 14); // Max 2 weeks for students
        Date dueDate = calendar.getTime();

        LendingRecord lendingRecord = new LendingRecord(
                recordId,
                student,
                Arrays.asList(equipment),
                course.getAcademic(),
                course,
                borrowDate,
                dueDate,
                purpose
        );

        lendingRecordDao.save(lendingRecord);
    }

    public void updatePersonalInfo(Student student, String phoneNumber, String email) {
        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            em.getTransaction().begin();
            Person person = student.getPersonDetails();
            if (person != null) {
                person.setPhoneNumber(phoneNumber);
                person.setEmail(email);
                em.merge(person);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to update personal information", e);
        } finally {
            em.close();
        }
    }
}