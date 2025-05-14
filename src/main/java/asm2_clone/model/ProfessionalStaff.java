package asm2_clone.model;

public class ProfessionalStaff extends Staff {
    private String department;

    public ProfessionalStaff() {}

    public ProfessionalStaff(String id, String fullName, String contactInfo, java.util.Date dateOfBirth, String staffId, String department) {
        super(id, fullName, contactInfo, dateOfBirth, staffId);
        this.department = department;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() {
        return "ProfessionalStaff{" +
                "department='" + department + '\'' +
                "} " + super.toString();
    }
} 