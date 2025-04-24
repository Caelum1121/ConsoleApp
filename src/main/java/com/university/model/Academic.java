package com.university.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an academic staff user who can borrow equipment and manage courses.
 * @author GroupHDGs
 */
@Entity
@Table(name = "users")
@DiscriminatorValue("ACADEMIC")
public class Academic extends Borrower {
    @OneToMany(mappedBy = "academic", cascade = CascadeType.ALL)
    private List<Course> assignedCourses = new ArrayList<>();

    protected Academic() {}

    public Academic(String username, String password, String entityId, Person personDetails) {
        super(username, password, entityId, personDetails);
    }

    public List<Course> getAssignedCourses() {
        return new ArrayList<>(assignedCourses);
    }

    public void assignCourse(Course course) {
        if (course != null && !assignedCourses.contains(course)) {
            assignedCourses.add(course);
            course.setAcademic(this);
        }
    }

    public List<LendingRecord> getStudentLendingRecords() {
        List<LendingRecord> studentRecords = new ArrayList<>();
        for (Course course : assignedCourses) {
            for (Student student : course.getEnrolledStudents()) {
                studentRecords.addAll(student.getLendingRecords());
            }
        }
        return studentRecords;
    }

    public Map<String, Object> generateLendingStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        List<LendingRecord> records = getStudentLendingRecords();
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

    public void updatePersonalInfo(String phoneNumber, String email) {
        if (getPersonDetails() != null) {
            getPersonDetails().setPhoneNumber(phoneNumber);
            getPersonDetails().setEmail(email);
        }
    }

    @Override
    public List<LendingRecord> getLendingHistory() {
        return getLendingRecords();
    }

    @Override
    public String toString() {
        return "Academic{entityId='" + getEntityId() + "', username='" + getUsername() + "'}";
    }
}