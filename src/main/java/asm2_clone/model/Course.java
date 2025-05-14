package asm2_clone.model;

import java.util.List;

public class Course {
    private String courseId;
    private String courseName;
    private AcademicStaff academicStaff;
    private List<Student> enrolledStudents;
    private String academicStaffName;    // For simple display without full object
    private String academicStaffEmail;   // For simple display without full object
    private String academicStaffId;
    private int studentCount;
    private int equipmentCount;
    private List<Student> selectedStudents;
    private List<Equipment> selectedEquipment;
    private List<String> selectedStudentIds;
    private List<Integer> selectedEquipmentIds;
    private List<Equipment> equipmentRelated;

    public Course() {}

    public Course(String courseId, String courseName, AcademicStaff academicStaff, List<Student> enrolledStudents) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.academicStaff = academicStaff;
        this.enrolledStudents = enrolledStudents;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public AcademicStaff getAcademicStaff() { return academicStaff; }
    public void setAcademicStaff(AcademicStaff academicStaff) { this.academicStaff = academicStaff; }
    public List<Student> getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(List<Student> enrolledStudents) { this.enrolledStudents = enrolledStudents; }

    public String getAcademicStaffName() { return academicStaffName; }
    public void setAcademicStaffName(String academicStaffName) { this.academicStaffName = academicStaffName; }

    public String getAcademicStaffEmail() { return academicStaffEmail; }
    public void setAcademicStaffEmail(String academicStaffEmail) { this.academicStaffEmail = academicStaffEmail; }

    public String getAcademicStaffId() { return academicStaffId; }
    public void setAcademicStaffId(String academicStaffId) { this.academicStaffId = academicStaffId; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public int getEquipmentCount() { return equipmentCount; }
    public void setEquipmentCount(int equipmentCount) { this.equipmentCount = equipmentCount; }

    public List<Student> getSelectedStudents() {
        return selectedStudents;
    }

    public void setSelectedStudents(List<Student> selectedStudents) {
        this.selectedStudents = selectedStudents;
    }

    public List<Equipment> getSelectedEquipment() {
        return selectedEquipment;
    }

    public void setSelectedEquipment(List<Equipment> selectedEquipment) {
        this.selectedEquipment = selectedEquipment;
    }

    public List<String> getSelectedStudentIds() {
        return selectedStudentIds;
    }

    public void setSelectedStudentIds(List<String> selectedStudentIds) {
        this.selectedStudentIds = selectedStudentIds;
    }

    public List<Integer> getSelectedEquipmentIds() {
        return selectedEquipmentIds;
    }

    public void setSelectedEquipmentIds(List<Integer> selectedEquipmentIds) {
        this.selectedEquipmentIds = selectedEquipmentIds;
    }

    public List<Equipment> getEquipmentRelated() {
        return equipmentRelated;
    }

    public void setEquipmentRelated(List<Equipment> equipmentRelated) {
        this.equipmentRelated = equipmentRelated;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", academicStaff=" + (academicStaff != null ? academicStaff.getFullName() : academicStaffName) +
                ", enrolledStudents=" + (enrolledStudents != null ? enrolledStudents.size() : 0) + " students" +
                '}';
    }
} 