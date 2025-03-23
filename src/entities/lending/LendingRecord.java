package entities.lending;

import entities.person.Student;
import entities.person.Person;
import entities.person.staff.Academic;
import entities.person.staff.Staff;
import entities.equipment.Equipment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * @author Chang Fang Cih - s4073761
 */

public class LendingRecord {
    public enum Status {
        BORROWED, RETURNED, OVERDUE
    }

    private String recordId;
    private Person borrower;
    private List<Equipment> equipment;
    private Academic responsibleAcademic;  // Null if borrower is staff
    private Date borrowDate;
    private Date returnDate;  // Null if not yet returned
    private Status status;
    private String purpose;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    /**
     * Constructor for creating lending record objects for students
     * @param recordId Record's ID
     * @param borrower Student who is borrowing
     * @param equipment List of equipment being borrowed
     * @param responsibleAcademic Academic responsible for the student
     * @param borrowDate Date when equipment was borrowed
     * @param purpose Purpose of borrowing
     */
    public LendingRecord(String recordId, Student borrower, List<Equipment> equipment,
                         Academic responsibleAcademic, Date borrowDate, String purpose) {
        this.recordId = recordId;
        this.borrower = borrower;
        this.equipment = new ArrayList<>(equipment);
        this.responsibleAcademic = responsibleAcademic;
        this.borrowDate = borrowDate;
        this.returnDate = null;
        this.status = Status.BORROWED;
        this.purpose = purpose;

        // Update equipment status
        for (Equipment item : equipment) {
            item.setStatus(Equipment.Status.BORROWED);
        }

        // Add this record to student's records
        borrower.addLendingRecord(this);

        // Add student to academic's supervised list
        if (responsibleAcademic != null) {
            responsibleAcademic.addSupervisedStudent(borrower);
        }
    }

    /**
     * Constructor for creating lending record objects for staff
     * @param recordId Record's ID
     * @param borrower Staff member who is borrowing
     * @param equipment List of equipment being borrowed
     * @param borrowDate Date when equipment was borrowed
     * @param purpose Purpose of borrowing
     */
    public LendingRecord(String recordId, Staff borrower, List<Equipment> equipment,
                         Date borrowDate, String purpose) {
        this.recordId = recordId;
        this.borrower = borrower;
        this.equipment = new ArrayList<>(equipment);
        this.responsibleAcademic = null;
        this.borrowDate = borrowDate;
        this.returnDate = null;
        this.status = Status.BORROWED;
        this.purpose = purpose;

        // Update equipment status
        for (Equipment item : equipment) {
            item.setStatus(Equipment.Status.BORROWED);
        }
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public Person getBorrower() {
        return borrower;
    }

    public List<Equipment> getEquipment() {
        return equipment;
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

    // Method to get formatted borrow date
    public String getFormattedBorrowDate() {
        return dateFormat.format(borrowDate);
    }

    // Method to get formatted return date
    public String getFormattedReturnDate() {
        return returnDate != null ? dateFormat.format(returnDate) : "Not returned";
    }

    /**
     * Method to return equipment
     * @param returnDate Date when equipment is returned
     */
    public void returnEquipment(Date returnDate) {
        this.returnDate = returnDate;
        this.status = Status.RETURNED;

        // Update equipment status
        for (Equipment item : equipment) {
            item.setStatus(Equipment.Status.AVAILABLE);
        }
    }

/**
 * Method to check if lending is overdue
 * @param currentDate Current date to check against
/**
 * Method to check if lending is overdue
 * @param currentDate Current date to check against
 * @param daysAllowed Number of days allowed for borrowing
 * @return True if lending is overdue, false otherwise
 */
public boolean isOverdue(Date currentDate, int daysAllowed) {
    if (status == Status.BORROWED) {
        long diffInMillies = currentDate.getTime() - borrowDate.getTime();
        long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);

        if (diffInDays > daysAllowed) {
            setStatus(Status.OVERDUE);
            return true;
        }
    }
    return false;
}

    @Override
    public String toString() {
        return "LendingRecord{" +
                "recordId='" + recordId + '\'' +
                ", borrower=" + borrower.getFullName() +
                ", equipment=" + equipment.size() + " items" +
                ", responsibleAcademic=" + (responsibleAcademic != null ? responsibleAcademic.getFullName() : "N/A") +
                ", borrowDate=" + getFormattedBorrowDate() +
                ", returnDate=" + getFormattedReturnDate() +
                ", status=" + status +
                ", purpose='" + purpose + '\'' +
                '}';
    }
}
