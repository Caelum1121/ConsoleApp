package asm2_clone.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class LendingRecord {
    public enum Status {
        BORROWED,
        OVERDUE,
        RETURNED,
        PENDING
    }

    public enum ApprovalStatus {
        APPROVED,
        PENDING,
        REJECTED
    }

    private String recordId;
    private String borrowerId;
    private String borrowerRole;
    private Date borrowDate;
    private Date needToReturnDate;  // New field: +14 days from borrow date
    private Date returnDate;        // New field: actual return date
    private Status status;
    private String purpose;
    private String supervisorId;
    private ApprovalStatus approvalStatus;  // Updated to enum
    private Equipment equipment;
    private List<Equipment> equipmentList;
    private Person borrower;
    private Course course;
    private String borrowerName;
    private List<String> equipmentNames;
    private Map<Integer, String> equipmentStatuses; // New field to track individual equipment statuses
    private Map<Integer, Date> equipmentReturnDates; // New field to track individual equipment return dates
    
    // Constructors
    public LendingRecord() {}
    
    // New style getters and setters
    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }
    public String getBorrowerRole() { return borrowerRole; }
    public void setBorrowerRole(String borrowerRole) { this.borrowerRole = borrowerRole; }
    public Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Date borrowDate) { this.borrowDate = borrowDate; }
    public Date getNeedToReturnDate() { return needToReturnDate; }
    public void setNeedToReturnDate(Date needToReturnDate) { this.needToReturnDate = needToReturnDate; }
    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
    public List<Equipment> getEquipmentList() { return equipmentList; }
    public void setEquipmentList(List<Equipment> equipmentList) { this.equipmentList = equipmentList; }
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    public List<String> getEquipmentNames() { return equipmentNames; }
    public void setEquipmentNames(List<String> equipmentNames) { this.equipmentNames = equipmentNames; }
    public Map<Integer, String> getEquipmentStatuses() { return equipmentStatuses; }
    public void setEquipmentStatuses(Map<Integer, String> equipmentStatuses) { this.equipmentStatuses = equipmentStatuses; }
    public Map<Integer, Date> getEquipmentReturnDates() { return equipmentReturnDates; }
    public void setEquipmentReturnDates(Map<Integer, Date> equipmentReturnDates) { this.equipmentReturnDates = equipmentReturnDates; }

    // Old style getters and setters for backward compatibility
    public String getRecord_id() { return recordId; }
    public void setRecord_id(String record_id) { this.recordId = record_id; }
    public String getBorrower_id() { return borrowerId; }
    public void setBorrower_id(String borrower_id) { this.borrowerId = borrower_id; }
    public String getBorrower_role() { return borrowerRole; }
    public void setBorrower_role(String borrower_role) { this.borrowerRole = borrower_role; }
    public Date getBorrow_date() { return borrowDate; }
    public void setBorrow_date(Date borrow_date) { this.borrowDate = borrow_date; }
    public String getSupervisor_id() { return supervisorId; }
    public void setSupervisor_id(String supervisor_id) { this.supervisorId = supervisor_id; }
    public String getApproval_status() { return approvalStatus.toString(); }
    public void setApproval_status(String approval_status) { this.approvalStatus = ApprovalStatus.valueOf(approval_status.toUpperCase()); }

    // Add new methods for Person and Course
    public Person getBorrower() { return borrower; }
    public void setBorrower(Person borrower) { 
        this.borrower = borrower;
        if (borrower != null) {
            this.borrowerId = borrower.getId();
            // Set borrower role based on type
            if (borrower instanceof Student) {
                this.borrowerRole = "student";
            } else if (borrower instanceof AcademicStaff) {
                this.borrowerRole = "academic";
            } else if (borrower instanceof ProfessionalStaff) {
                this.borrowerRole = "professional";
            }
        }
    }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
} 