package com.university.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * @author GroupHDGs
 */
public class LendingRecord {

    public enum Status {
        BORROWED, RETURNED, OVERDUE
    }

    private String recordId;
    private Borrower borrower; // Changed from Person to Borrower
    private List<Equipment> equipment;
    private Academic responsibleAcademic;
    private Course course; // For student borrowings
    private Date borrowDate;
    private Date returnDate; // Null if not yet returned
    private Status status;
    private String purpose;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private Date dueDate;
    private List<String> notes = new ArrayList<>();

    /**
     * Constructor for student borrowers.
     *
     * @param recordId            Unique record identifier
     * @param borrower            The borrowing student
     * @param equipment           List of borrowed equipment
     * @param responsibleAcademic Supervising academic
     * @param course              Course associated with the borrowing
     * @param borrowDate          Date of borrowing
     * @param dueDate             Expected return date
     * @param purpose             Purpose of borrowing
     */
    public LendingRecord(String recordId, Student borrower, List<Equipment> equipment,
                         Academic responsibleAcademic, Course course, Date borrowDate, Date dueDate, String purpose) {
        if (recordId == null || recordId.trim().isEmpty()) {
            throw new IllegalArgumentException("Record ID cannot be null or empty");
        }
        if (borrower == null) {
            throw new IllegalArgumentException("Borrower cannot be null");
        }
        if (equipment == null || equipment.isEmpty()) {
            throw new IllegalArgumentException("Equipment list cannot be null or empty");
        }
        if (responsibleAcademic == null) {
            throw new IllegalArgumentException("Responsible academic cannot be null");
        }
        if (course == null || !course.getEnrolledStudents().contains(borrower)) {
            throw new IllegalArgumentException("Student must be enrolled in the course");
        }
        if (borrowDate == null || dueDate == null || !dueDate.after(borrowDate)) {
            throw new IllegalArgumentException("Invalid borrow or due date");
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new IllegalArgumentException("Purpose cannot be null or empty");
        }
        long diffInMillies = dueDate.getTime() - borrowDate.getTime();
        long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);
        if (diffInDays > 14) {
            throw new IllegalArgumentException("Student borrowing period cannot exceed 2 weeks");
        }

        this.recordId = recordId;
        this.borrower = borrower; // Student extends Borrower, so this is safe
        this.equipment = new ArrayList<>(equipment);
        this.responsibleAcademic = responsibleAcademic;
        this.course = course;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.status = Status.BORROWED;
        this.purpose = purpose;

        for (Equipment item : equipment) {
            item.setStatus(Equipment.Status.BORROWED);
        }
        borrower.addLendingRecord(this);
        responsibleAcademic.addSupervisedStudent(borrower);
    }

    /**
     * Constructor for non-student borrowers (e.g., Academic, Professional).
     *
     * @param recordId   Unique record identifier
     * @param borrower   The borrowing person
     * @param equipment  List of borrowed equipment
     * @param borrowDate Date of borrowing
     * @param dueDate    Expected return date
     * @param purpose    Purpose of borrowing
     */
    public LendingRecord(String recordId, Borrower borrower, List<Equipment> equipment,
                         Date borrowDate, Date dueDate, String purpose) {
        if (recordId == null || recordId.trim().isEmpty()) {
            throw new IllegalArgumentException("Record ID cannot be null or empty");
        }
        if (borrower == null) {
            throw new IllegalArgumentException("Borrower cannot be null");
        }
        if (equipment == null || equipment.isEmpty()) {
            throw new IllegalArgumentException("Equipment list cannot be null or empty");
        }
        if (borrowDate == null || dueDate == null || !dueDate.after(borrowDate)) {
            throw new IllegalArgumentException("Invalid borrow or due date");
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new IllegalArgumentException("Purpose cannot be null or empty");
        }

        this.recordId = recordId;
        this.borrower = borrower;
        this.equipment = new ArrayList<>(equipment);
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.purpose = purpose;
        this.status = Status.BORROWED;
        this.responsibleAcademic = null; // No academic for non-student borrowers
        this.course = null; // No course for non-student borrowers

        for (Equipment item : equipment) {
            item.setStatus(Equipment.Status.BORROWED);
        }
        if (borrower instanceof Professional) {
            ((Professional) borrower).addLendingRecord(this);
        }
    }

    // Getters and setters
    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        if (course != null && borrower instanceof Student && !course.getEnrolledStudents().contains((Student) borrower)) {
            throw new IllegalArgumentException("Student must be enrolled in the course");
        }
        this.course = course;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public List<Equipment> getEquipment() {
        return new ArrayList<>(equipment);
    }

    public Academic getResponsibleAcademic() {
        return responsibleAcademic;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    // Formatted date accessors
    public String getFormattedBorrowDate() {
        return dateFormat.format(borrowDate);
    }

    public String getFormattedDueDate() {
        return dueDate != null ? dateFormat.format(dueDate) : "N/A";
    }

    public String getFormattedReturnDate() {
        return returnDate != null ? dateFormat.format(returnDate) : "Not returned";
    }

    /**
     * Returns the equipment and updates the status.
     *
     * @param returnDate Date when equipment is returned
     */
    public void returnEquipment(Date returnDate) {
        this.returnDate = returnDate;
        this.status = Status.RETURNED;

        for (Equipment item : equipment) {
            item.setStatus(Equipment.Status.AVAILABLE);
        }
    }

    public int getDaysOverdue() {
        return (int) ((new Date().getTime() - dueDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    public List<String> getNotes() {
        return new ArrayList<>(notes);
    }

    public void setActualReturnDate(Date actualReturnDate) {
        this.returnDate = actualReturnDate;
    }

    public Date getActualReturnDate() {
        return returnDate;
    }

    public boolean isOverdue() {
        return status == Status.OVERDUE;
    }

    /**
     * Checks if the record is overdue relative to a current date.
     *
     * @param currentDate Date to check against
     * @param daysAllowed Maximum allowed borrowing days
     * @return true if overdue, false otherwise
     */
    public boolean isOverdue(Date currentDate, int daysAllowed) {
        if (status == Status.RETURNED) return false;
        long diffInMillies = currentDate.getTime() - getBorrowDate().getTime();
        long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);
        return diffInDays > daysAllowed;
    }

    @Override
    public String toString() {
        return "LendingRecord{" +
                "recordId='" + recordId + '\'' +
                ", borrower=" + borrower.getPersonDetails().getFullName() +
                ", equipment=" + equipment.size() + " items" +
                ", responsibleAcademic=" + (responsibleAcademic != null ? responsibleAcademic.getFullName() : "N/A") +
                ", course=" + (course != null ? course.getCourseName() : "N/A") +
                ", borrowDate=" + getFormattedBorrowDate() +
                ", returnDate=" + getFormattedReturnDate() +
                ", status=" + status +
                ", purpose='" + purpose + '\'' +
                '}';
    }
}