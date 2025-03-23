package entities.person;

import entities.lending.LendingRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Chang Fang Cih - s4073761
 */
public class Student extends Person {
    private String studentId;
    private final List<LendingRecord> lendingRecords;

    /**
     * Constructor for creating student objects
     * @param id Person's ID
     * @param fullName Student's full name
     * @param dateOfBirth Student's date of birth
     * @param phoneNumber Student's phone number
     * @param email Student's email
     * @param studentId Student's student ID
     */
    public Student(String id, String fullName, Date dateOfBirth, String phoneNumber, String email, String studentId) {
        super(id, fullName, dateOfBirth, phoneNumber, email);
        this.studentId = studentId;
        this.lendingRecords = new ArrayList<>();
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public List<LendingRecord> getLendingRecords() {
        return lendingRecords;
    }

    public void addLendingRecord(LendingRecord record) {
        lendingRecords.add(record);
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", " + super.toString() +
                '}';
    }
}