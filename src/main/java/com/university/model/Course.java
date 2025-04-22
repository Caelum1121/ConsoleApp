package com.university.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author GroupHDGs
 */
public class Course {
    private String courseId;
    private String courseName;
    private Academic academic;
    private List<Student> enrolledStudents;

    public Course(String courseId, String courseName, Academic academic) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be null or empty");
        }
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be null or empty");
        }
        if (academic == null) {
            throw new IllegalArgumentException("Academic staff cannot be null");
        }
        this.courseId = courseId;
        this.courseName = courseName;
        this.academic = academic;
        this.enrolledStudents = new ArrayList<>();
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public Academic getAcademic() {
        return academic;
    }

    public void setAcademic(Academic academic) {
        if (academic == null) {
            throw new IllegalArgumentException("Academic staff cannot be null");
        }
        this.academic = academic;
    }

    public List<Student> getEnrolledStudents() {
        return new ArrayList<>(enrolledStudents);
    }

    public void enrollStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        if (enrolledStudents.contains(student)) {
            throw new IllegalArgumentException("Student is already enrolled");
        }
        enrolledStudents.add(student);
    }

    public void removeStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        if (!enrolledStudents.contains(student)) {
            throw new IllegalArgumentException("Student is not enrolled");
        }
        enrolledStudents.remove(student);
    }

    @Override
    public String toString() {
        return "Course{courseId='" + courseId + "', courseName='" + courseName + "'}";
    }
}
