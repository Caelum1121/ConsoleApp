package com.university.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a student user who can borrow equipment in the university equipment lending system.
 * @author GroupHDGs
 */
@Entity
@Table(name = "users")
@DiscriminatorValue("STUDENT")
public class Student extends Borrower {
    @ManyToMany
    @JoinTable(
            name = "course_students",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> enrolledCourses = new ArrayList<>();

    protected Student() {}

    public Student(String username, String password, String entityId, Person personDetails) {
        super(username, password, entityId, personDetails);
    }

    public List<Course> getEnrolledCourses() {
        return new ArrayList<>(enrolledCourses);
    }

    public void enrollInCourse(Course course) {
        if (course != null && !enrolledCourses.contains(course)) {
            enrolledCourses.add(course);
            course.getEnrolledStudents().add(this);
        }
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
        return "Student{entityId='" + getEntityId() + "', username='" + getUsername() + "'}";
    }
}