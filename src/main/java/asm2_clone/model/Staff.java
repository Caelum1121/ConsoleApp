package asm2_clone.model;

import java.util.Date;

public abstract class Staff extends Person {
    protected String staffId;

    public Staff() {}

    public Staff(String id, String fullName, String contactInfo, Date dateOfBirth, String staffId) {
        super(id, fullName, contactInfo, dateOfBirth);
        this.staffId = staffId;
    }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    @Override
    public String toString() {
        return "Staff{" +
                "staffId='" + staffId + '\'' +
                "} " + super.toString();
    }
} 