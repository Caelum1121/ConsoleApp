package entities.person.staff;

import java.util.Date;

/**
 * @author Chang Fang Cih - s4073761
 */

public class Professional extends Staff {
    private String department;

    /**
     * Constructor for creating professional staff objects
     * @param id Person's ID
     * @param fullName Professional's full name
     * @param dateOfBirth Professional's date of birth
     * @param phoneNumber Professional's phone number
     * @param email Professional's email
     * @param staffId Professional's staff ID
     * @param department Professional's department
     */
    public Professional(String id, String fullName, Date dateOfBirth, String phoneNumber, String email,
                        String staffId, String department) {
        super(id, fullName, dateOfBirth, phoneNumber, email, staffId);
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Professional{" +
                "department='" + department + '\'' +
                ", " + super.toString() +
                '}';
    }
}
