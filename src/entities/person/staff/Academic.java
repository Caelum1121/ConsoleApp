package entities.person.staff;

import entities.person.Student;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author Chang Fang Cih - s4073761
 */

public class Academic  extends Staff {
    private String expertise;
    private List<Student> supervisedStudents;

    /**
     * Constructor for creating academic staff objects
     * @param id Person's ID
     * @param fullName Academic's full name
     * @param dateOfBirth Academic's date of birth
     * @param phoneNumber Academic's phone number
     * @param email Academic's email
     * @param staffId Academic's staff ID
     * @param expertise Academic's area of expertise
     */
    public Academic(String id, String fullName, Date dateOfBirth, String phoneNumber, String email,
                    String staffId, String expertise) {
        super(id, fullName, dateOfBirth, phoneNumber, email, staffId);
        this.expertise = expertise;
        this.supervisedStudents = new ArrayList<>();
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public List<Student> getSupervisedStudents() {
        return supervisedStudents;
    }

    public void addSupervisedStudent(Student student) {
        if (!supervisedStudents.contains(student)) {
            supervisedStudents.add(student);
        }
    }

    @Override
    public String toString() {
        return "Academic{" +
                "expertise='" + expertise + '\'' +
                ", supervisedStudents=" + supervisedStudents.size() +
                ", " + super.toString() +
                '}';
    }
}
