package com.university.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a lending record for equipment borrowing in the university equipment lending system.
 * @author GroupHDGs
 */
@Entity
@Table(name = "lending_records")
public class LendingRecord {
    public enum Status {
        BORROWED, RETURNED, OVERDUE
    }

    @Id
    @Column(name = "record_id")
    private String recordId;

    @ManyToOne
    @JoinColumn(name = "borrower_id")
    private Borrower borrower;

    @ManyToMany
    @JoinTable(
            name = "lending_record_equipment",
            joinColumns = @JoinColumn(name = "record_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private List<Equipment> equipment = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "responsible_academic_id")
    private Academic responsibleAcademic;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "borrow_date")
    @Temporal(TemporalType.DATE)
    private Date borrowDate;

    @Column(name = "due_date")
    @Temporal(TemporalType.DATE)
    private Date dueDate;

    @Column(name = "return_date")
    @Temporal(TemporalType.DATE)
    private Date returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "approved")
    private Boolean approved;

    protected LendingRecord() {}

    public LendingRecord(String recordId, Borrower borrower, List<Equipment> equipment,
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
        if (course == null || (borrower instanceof Student && !course.getEnrolledStudents().contains(borrower))) {
            throw new IllegalArgumentException("Student must be enrolled in the course");
        }
        if (borrowDate == null || dueDate == null || !dueDate.after(borrowDate)) {
            throw new IllegalArgumentException("Invalid borrow or due date");
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new IllegalArgumentException("Purpose cannot be null or empty");
        }
        if (borrower instanceof Student) {
            long diffInDays = (dueDate.getTime() - borrowDate.getTime()) / (1000 * 60 * 60 * 24);
            if (diffInDays > 14) {
                throw new IllegalArgumentException("Student borrowing period cannot exceed 2 weeks");
            }
        }

        this.recordId = recordId;
        this.borrower = borrower;
        this.equipment = new ArrayList<>(equipment);
        this.responsibleAcademic = responsibleAcademic;
        this.course = course;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.status = Status.BORROWED;
        this.purpose = purpose;
        this.approved = (borrower instanceof Student) ? false : true;
        borrower.addLendingRecord(this);
    }

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
        this.returnDate = null;
        this.status = Status.BORROWED;
        this.purpose = purpose;
        this.approved = true;
        borrower.addLendingRecord(this);
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

    public void setBorrower(Borrower borrower) {
        this.borrower = borrower;
    }

    public List<Equipment> getEquipment() {
        return new ArrayList<>(equipment);
    }

    public void setEquipment(List<Equipment> equipment) {
        this.equipment = new ArrayList<>(equipment);
    }

    public Academic getResponsibleAcademic() {
        return responsibleAcademic;
    }

    public void setResponsibleAcademic(Academic responsibleAcademic) {
        this.responsibleAcademic = responsibleAcademic;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setActualReturnDate(Date returnDate) {
        this.returnDate = returnDate;
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

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public boolean isOverdue() {
        if (returnDate != null || status == Status.RETURNED) {
            return false;
        }
        return new Date().after(dueDate);
    }

    @Override
    public String toString() {
        return "LendingRecord{recordId='" + recordId + "', borrower=" + borrower + ", status=" + status + "}";
    }
}