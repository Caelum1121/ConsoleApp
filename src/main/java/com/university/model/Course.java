package com.university.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an academic course in the university equipment lending system.
 * @author GroupHDGs
 */
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @Column(name = "course_id")
    private String courseId;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    @ManyToOne
    @JoinColumn(name = "academic_id")
    private Academic academic;

    @ManyToMany(mappedBy = "enrolledCourses")
    private List<Student> enrolledStudents = new ArrayList<>();

    protected Course() {}

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
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Academic getAcademic() {
        return academic;
    }

    public void setAcademic(Academic academic) {
        this.academic = academic;
    }

    public List<Student> getEnrolledStudents() {
        return new ArrayList<>(enrolledStudents);
    }

    public void enrollStudent(Student student) {
        if (student != null && !enrolledStudents.contains(student)) {
            enrolledStudents.add(student);
            student.getEnrolledCourses().add(this);
        }
    }

    public void removeStudent(Student student) {
        if (student != null && enrolledStudents.contains(student)) {
            enrolledStudents.remove(student);
            student.getEnrolledCourses().remove(this);
        }
    }

    @Override
    public String toString() {
        return "Course{courseId='" + courseId + "', courseName='" + courseName + "'}";
    }
}