package LendingManager;

import entities.equipment.Equipment;
import entities.lending.LendingRecord;
import java.util.Date;
import java.util.List;


public interface  LendingManager {
    /**
     * Create a lending record for a student
     * @param recordId Unique ID for the record
     * @param studentId ID of the student borrowing equipment
     * @param equipmentIds List of equipment IDs being borrowed
     * @param academicId ID of the responsible academic
     * @param borrowDate Date of borrowing
     * @param purpose Purpose of borrowing
     * @return Created lending record or null if creation failed
     */
    LendingRecord createStudentLendingRecord(String recordId, String studentId,
                                             List<String> equipmentIds, String academicId,
                                             Date borrowDate, String purpose);

    /**
     * Create a lending record for a staff member
     * @param recordId Unique ID for the record
     * @param staffId ID of the staff member borrowing equipment
     * @param equipmentIds List of equipment IDs being borrowed
     * @param borrowDate Date of borrowing
     * @param purpose Purpose of borrowing
     * @return Created lending record or null if creation failed
     */
    LendingRecord createStaffLendingRecord(String recordId, String staffId,
                                           List<String> equipmentIds,
                                           Date borrowDate, String purpose);

    /**
     * Process the return of borrowed equipment
     * @param recordId ID of the lending record
     * @param returnDate Date of return
     * @return Updated lending record or null if not found
     */
    LendingRecord returnEquipment(String recordId, Date returnDate);

    /**
     * Delete a lending record
     * @param recordId ID of the record to delete
     * @return True if deleted successfully, false otherwise
     */
    boolean deleteLendingRecord(String recordId);

    /**
     * Get a lending record by ID
     * @param recordId ID of the record to retrieve
     * @return The lending record or null if not found
     */
    LendingRecord getLendingRecord(String recordId);

    /**
     * Get all lending records
     * @return List of all lending records
     */
    List<LendingRecord> getAllLendingRecords();

    /**
     * Get all lending records for a specific borrower
     * @param borrowerId ID of the borrower
     * @return List of lending records for the borrower
     */
    List<LendingRecord> getLendingRecordsForBorrower(String borrowerId);

    /**
     * Get all active (borrowed) lending records
     * @return List of active lending records
     */
    List<LendingRecord> getActiveLendingRecords();

    /**
     * Get all lending records supervised by a specific academic
     * @param academicId ID of the academic
     * @return List of lending records supervised by the academic
     */
    List<LendingRecord> getLendingRecordsByAcademic(String academicId);

    /**
     * Check all currently borrowed items for overdue status
     * @param currentDate Current date to check against
     * @param daysAllowed Number of days allowed for borrowing
     * @return List of records that are now overdue
     */
    List<LendingRecord> checkForOverdueItems(Date currentDate, int daysAllowed);

    /**
     * Find all records containing a specific piece of equipment
     * @param equipmentId ID of the equipment
     * @return List of lending records containing the equipment
     */
    List<LendingRecord> findRecordsWithEquipment(String equipmentId);

    /**
     * Update the purpose of a lending record
     * @param recordId ID of the record to update
     * @param newPurpose New purpose to set
     * @return Updated lending record or null if not found
     */
    LendingRecord updateLendingPurpose(String recordId, String newPurpose);
}
