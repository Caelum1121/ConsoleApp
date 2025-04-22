package com.university.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author GroupHDGs
 */
public class Academic extends Borrower {
    private String staffId;
    private String expertise;
    private List<Student> supervisedStudents;
    private List<Course> assignedCourses;

    public Academic(String id, String fullName, Date dateOfBirth, String phoneNumber, String email,
                    String staffId, String expertise, String username, String password) {
        super(username, password, new Person(id, fullName, dateOfBirth, phoneNumber, email));
        if (staffId == null || staffId.trim().isEmpty()) {
            throw new IllegalArgumentException("Staff ID cannot be null or empty");
        }
        this.staffId = staffId;
        this.expertise = expertise;
        this.supervisedStudents = new ArrayList<>();
        this.assignedCourses = new ArrayList<>();
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        if (staffId == null || staffId.trim().isEmpty()) {
            throw new IllegalArgumentException("Staff ID cannot be null or empty");
        }
        this.staffId = staffId;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public List<Student> getSupervisedStudents() {
        return new ArrayList<>(supervisedStudents);
    }

    public void addSupervisedStudent(Student student) {
        if (student != null && !supervisedStudents.contains(student)) {
            supervisedStudents.add(student);
        }
    }

    public List<Course> getAssignedCourses() {
        return new ArrayList<>(assignedCourses);
    }

    public void assignCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }
        if (!assignedCourses.contains(course)) {
            assignedCourses.add(course);
            course.setAcademic(this);
        }
    }

    @Override
    public List<LendingRecord> getLendingHistory() {
        List<LendingRecord> history = new ArrayList<>();
        for (Student student : supervisedStudents) {
            history.addAll(student.getLendingRecords());
        }
        return history;
    }

    public Map<String, Object> generateLendingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<LendingRecord> records = getLendingHistory();
        stats.put("totalBorrowed", records.size());
        stats.put("currentlyBorrowed", records.stream()
                .filter(r -> r.getStatus() == LendingRecord.Status.BORROWED)
                .count());
        stats.put("overdueItems", records.stream()
                .filter(LendingRecord::isOverdue)
                .count());
        stats.put("borrowingFrequencyPerCourse", assignedCourses.stream()
                .collect(Collectors.toMap(
                        Course::getCourseName,
                        c -> c.getEnrolledStudents().stream()
                                .flatMap(s -> s.getLendingRecords().stream())
                                .count()
                )));
        return stats;
    }

    @Override
    public void updatePersonalInfo(ContactInfo contactInfo) {
        if (contactInfo == null) {
            throw new IllegalArgumentException("Contact info cannot be null");
        }
        getPersonDetails().setContactInfo(contactInfo);
    }

    // Add getFullName to resolve the error
    public String getFullName() {
        return getPersonDetails().getFullName();
    }

    @Override
    public String toString() {
        return "Academic{staffId='" + staffId + "', expertise='" + expertise + "', " + getPersonDetails().toString() + "}";
    }
}
