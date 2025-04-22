package com.university.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author GroupHDGs
 */
public class Student extends Borrower {
    private String studentId;
    private List<LendingRecord> lendingRecords;
    private List<Course> enrolledCourses;

    public Student(String id, String fullName, Date dateOfBirth, String phoneNumber, String email,
                   String studentId, String username, String password) {
        super(username, password, new Person(id, fullName, dateOfBirth, phoneNumber, email));
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }
        this.studentId = studentId;
        this.lendingRecords = new ArrayList<>();
        this.enrolledCourses = new ArrayList<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }
        this.studentId = studentId;
    }

    public List<LendingRecord> getLendingRecords() {
        return new ArrayList<>(lendingRecords);
    }

    public void addLendingRecord(LendingRecord record) {
        if (record != null) {
            lendingRecords.add(record);
        }
    }

    public List<Course> getEnrolledCourses() {
        return new ArrayList<>(enrolledCourses);
    }

    public void enrollInCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }
        if (!enrolledCourses.contains(course)) {
            enrolledCourses.add(course);
            course.enrollStudent(this);
        }
    }

    @Override
    public List<LendingRecord> getLendingHistory() {
        return new ArrayList<>(lendingRecords);
    }

    public List<LendingRecord> filterLendingHistory(Date startDate, Date endDate, LendingRecord.Status status) {
        return lendingRecords.stream()
                .filter(r -> startDate == null || !r.getBorrowDate().before(startDate))
                .filter(r -> endDate == null || !r.getBorrowDate().after(endDate))
                .filter(r -> status == null || r.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public void updatePersonalInfo(ContactInfo contactInfo) {
        if (contactInfo == null) {
            throw new IllegalArgumentException("Contact info cannot be null");
        }
        getPersonDetails().setContactInfo(contactInfo);
    }

    @Override
    public String toString() {
        return "Student{studentId='" + studentId + "', " + getPersonDetails().toString() + "}";
    }
}