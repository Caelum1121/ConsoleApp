package asm2_clone.model;

import java.util.Date;
import java.util.List;

public class Student extends Person {
    private String studentId;
    private List<LendingRecord> lendingRecords;
    private List<Course> enrolledCourses;
    private String supervisor;
    
    public Student() {}

    public Student(String id, String fullName, String contactInfo, Date dateOfBirth, String studentId, List<LendingRecord> lendingRecords, List<Course> enrolledCourses) {
        super(id, fullName, contactInfo, dateOfBirth);
        this.studentId = studentId;
        this.lendingRecords = lendingRecords;
        this.enrolledCourses = enrolledCourses;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public List<LendingRecord> getLendingRecords() { return lendingRecords; }
    public void setLendingRecords(List<LendingRecord> lendingRecords) { this.lendingRecords = lendingRecords; }
    public List<Course> getEnrolledCourses() { return enrolledCourses; }
    public void setEnrolledCourses(List<Course> enrolledCourses) { this.enrolledCourses = enrolledCourses; }
    public String getSupervisor() { return supervisor; }
    public void setSupervisor(String supervisor) { this.supervisor = supervisor; }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", lendingRecords=" + lendingRecords +
                ", enrolledCourses=" + enrolledCourses +
                "} " + super.toString();
    }
} 