package asm2_clone.model;

import java.time.LocalDate;

public class AdminPerson {
    private String name;
    private String role;
    private String email;
    private String courseOrDept;
    private String id;
    private LocalDate dateOfBirth;
    private String supervisor;

    public AdminPerson(String name, String role, String email) {
        this.name = name;
        this.role = role;
        this.email = email;
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getCourseOrDept() { return courseOrDept; }
    public void setCourseOrDept(String courseOrDept) { this.courseOrDept = courseOrDept; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public String getSupervisor() { return supervisor; }
    public void setSupervisor(String supervisor) { this.supervisor = supervisor; }
}
