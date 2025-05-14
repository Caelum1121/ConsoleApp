package asm2_clone.model;

import java.util.List;
import java.util.Date;

public class AcademicStaff extends Staff {
    private String expertise;
    private List<Course> coursesTaught;

    public AcademicStaff() {
        super();
    }

    public AcademicStaff(String id, String fullName, String contactInfo, Date dateOfBirth, String staffId, String expertise) {
        super(id, fullName, contactInfo, dateOfBirth, staffId);
        this.expertise = expertise;
    }

    // Getters and Setters
    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public List<Course> getCoursesTaught() {
        return coursesTaught;
    }

    public void setCoursesTaught(List<Course> coursesTaught) {
        this.coursesTaught = coursesTaught;
    }

    @Override
    public String toString() {
        return "AcademicStaff{" +
                "id='" + getId() + '\'' +
                ", name='" + getFullName() + '\'' +
                ", email='" + getContactInfo() + '\'' +
                ", expertise='" + expertise + '\'' +
                '}';
    }
} 