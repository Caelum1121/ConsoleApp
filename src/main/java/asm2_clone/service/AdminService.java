package asm2_clone.service;

import asm2_clone.db.AdminDAO;
import asm2_clone.model.AdminPerson;
import javafx.collections.ObservableList;

public class AdminService {
    private final AdminDAO adminDAO = new AdminDAO();

    public ObservableList<AdminPerson> getAllPeople() {
        return adminDAO.getAllPeople();
    }

    public boolean addPerson(AdminPerson person) {
        try {
            return adminDAO.addPerson(person);
        } catch (Exception e) {
            return false;
        }
    }

    public void updatePerson(AdminPerson person) {
        adminDAO.updatePerson(person);
    }

    public void deletePerson(AdminPerson person) {
        adminDAO.deletePerson(person);
    }

    public boolean enrollStudentInCourse(String studentId, String courseId) {
        return adminDAO.enrollStudentInCourse(studentId, courseId);
    }

    public boolean updateProfessionalDepartment(String professionalId, String department) {
        return adminDAO.updateProfessionalDepartment(professionalId, department);
    }

    public void updateStudentCourseAndSupervisor(String studentId, String courseId, boolean clearExisting) {
        adminDAO.updateStudentCourseAndSupervisor(studentId, courseId, clearExisting);
    }
} 