package entities.person.staff;

import entities.person.Person;

import java.util.Date;

/**
 * @author Chang Fang Cih - s4073761
 */

public class Staff extends Person {
    private String staffId;

    /**
     * Constructor for creating staff objects
     * @param id Person's ID
     * @param fullName Staff's full name
     * @param dateOfBirth Staff's date of birth
     * @param phoneNumber Staff's phone number
     * @param email Staff's email
     * @param staffId Staff's staff ID
     */
    public Staff(String id, String fullName, Date dateOfBirth, String phoneNumber, String email, String staffId) {
        super(id, fullName, dateOfBirth, phoneNumber, email);
        this.staffId = staffId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "staffId='" + staffId + '\'' +
                ", " + super.toString() +
                '}';
    }
}
