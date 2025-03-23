package LendingManager;

import entities.person.Person;
import entities.equipment.Equipment;
import entities.lending.LendingRecord;
import entities.person.Student;
import entities.person.staff.Academic;
import entities.person.staff.Staff;
import java.util.*;

/**
 * Service class for managing lending records
 * @author Chang Fang Cih - s4073761
 */

public class LendingImplement {
    private Map<String, LendingRecord> lendingRecords;
    private Map<String, Equipment> equipmentInventory;
    private Map<String, Person> people;

    /**
     * Constructor initializing the service
     * @param equipmentInventory Map of all available equipment (by ID)
     * @param people Map of all people (by ID)
     */
    public LendingImplement(Map<String, Equipment> equipmentInventory, Map<String, Person> people) {
        this.lendingRecords = new HashMap<>();
        this.equipmentInventory = equipmentInventory;
        this.people = people;
    }

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
    public LendingRecord createStudentLendingRecord(String recordId, String studentId,
                                                    List<String> equipmentIds, String academicId,
                                                    Date borrowDate, String purpose) {
        // Validate inputs
        if (lendingRecords.containsKey(recordId)) {
            System.out.println("Lending record ID already exists: " + recordId);
            return null;
        }

        // Find student
        Person person = people.get(studentId);
        if (person == null || !(person instanceof Student)) {
            System.out.println("Student not found: " + studentId);
            return null;
        }
        Student student = (Student) person;

        // Find responsible academic
        Person academicPerson = people.get(academicId);
        if (academicPerson == null || !(academicPerson instanceof Academic)) {
            System.out.println("Academic not found: " + academicId);
            return null;
        }
        Academic academic = (Academic) academicPerson;

        // Get equipment and check availability
        List<Equipment> equipment = new ArrayList<>();
        for (String equipmentId : equipmentIds) {
            Equipment item = equipmentInventory.get(equipmentId);
            if (item == null) {
                System.out.println("Equipment not found: " + equipmentId);
                return null;
            }
            if (item.getStatus() != Equipment.Status.AVAILABLE) {
                System.out.println("Equipment not available: " + equipmentId);
                return null;
            }
            equipment.add(item);
        }

        // Create and store lending record
        LendingRecord record = new LendingRecord(recordId, student, equipment, academic, borrowDate, purpose);
        lendingRecords.put(recordId, record);
        return record;
    }

    /**
     * Create a lending record for a staff member
     * @param recordId Unique ID for the record
     * @param staffId ID of the staff member borrowing equipment
     * @param equipmentIds List of equipment IDs being borrowed
     * @param borrowDate Date of borrowing
     * @param purpose Purpose of borrowing
     * @return Created lending record or null if creation failed
     */
    public LendingRecord createStaffLendingRecord(String recordId, String staffId,
                                                  List<String> equipmentIds,
                                                  Date borrowDate, String purpose) {
        // Validate inputs
        if (lendingRecords.containsKey(recordId)) {
            System.out.println("Lending record ID already exists: " + recordId);
            return null;
        }

        // Find staff
        Person person = people.get(staffId);
        if (person == null || !(person instanceof Staff)) {
            System.out.println("Staff not found: " + staffId);
            return null;
        }
        Staff staff = (Staff) person;

        // Get equipment and check availability
        List<Equipment> equipment = new ArrayList<>();
        for (String equipmentId : equipmentIds) {
            Equipment item = equipmentInventory.get(equipmentId);
            if (item == null) {
                System.out.println("Equipment not found: " + equipmentId);
                return null;
            }
            if (item.getStatus() != Equipment.Status.AVAILABLE) {
                System.out.println("Equipment not available: " + equipmentId);
                return null;
            }
            equipment.add(item);
        }

        // Create and store lending record
        LendingRecord record = new LendingRecord(recordId, staff, equipment, borrowDate, purpose);
        lendingRecords.put(recordId, record);
        return record;
    }

    /**
     * Process the return of borrowed equipment
     * @param recordId ID of the lending record
     * @param returnDate Date of return
     * @return Updated lending record or null if not found
     */
    public LendingRecord returnEquipment(String recordId, Date returnDate) {
        LendingRecord record = lendingRecords.get(recordId);
        if (record == null) {
            System.out.println("Lending record not found: " + recordId);
            return null;
        }

        if (record.getStatus() == LendingRecord.Status.RETURNED) {
            System.out.println("Equipment already returned for record: " + recordId);
            return record;
        }

        record.returnEquipment(returnDate);
        return record;
    }

    /**
     * Delete a lending record
     * @param recordId ID of the record to delete
     * @return True if deleted successfully, false otherwise
     */
    public boolean deleteLendingRecord(String recordId) {
        LendingRecord record = lendingRecords.get(recordId);
        if (record == null) {
            System.out.println("Lending record not found: " + recordId);
            return false;
        }

        // If equipment is still borrowed, make it available again
        if (record.getStatus() == LendingRecord.Status.BORROWED ||
                record.getStatus() == LendingRecord.Status.OVERDUE) {
            for (Equipment item : record.getEquipment()) {
                item.setStatus(Equipment.Status.AVAILABLE);
            }
        }

        lendingRecords.remove(recordId);
        return true;
    }

    /**
     * Get a lending record by ID
     * @param recordId ID of the record to retrieve
     * @return The lending record or null if not found
     */
    public LendingRecord getLendingRecord(String recordId) {
        return lendingRecords.get(recordId);
    }

    /**
     * Get all lending records
     * @return List of all lending records
     */
    public List<LendingRecord> getAllLendingRecords() {
        return new ArrayList<>(lendingRecords.values());
    }

    /**
     * Get all lending records for a specific borrower
     * @param borrowerId ID of the borrower
     * @return List of lending records for the borrower
     */
    public List<LendingRecord> getLendingRecordsForBorrower(String borrowerId) {
        List<LendingRecord> results = new ArrayList<>();

        for (LendingRecord record : lendingRecords.values()) {
            if (record.getBorrower().getId().equals(borrowerId)) {
                results.add(record);
            }
        }

        return results;
    }

    /**
     * Get all active (borrowed) lending records
     * @return List of active lending records
     */
    public List<LendingRecord> getActiveLendingRecords() {
        List<LendingRecord> results = new ArrayList<>();

        for (LendingRecord record : lendingRecords.values()) {
            if (record.getStatus() == LendingRecord.Status.BORROWED ||
                    record.getStatus() == LendingRecord.Status.OVERDUE) {
                results.add(record);
            }
        }

        return results;
    }

    /**
     * Get all lending records supervised by a specific academic
     * @param academicId ID of the academic
     * @return List of lending records supervised by the academic
     */
    public List<LendingRecord> getLendingRecordsByAcademic(String academicId) {
        List<LendingRecord> results = new ArrayList<>();

        for (LendingRecord record : lendingRecords.values()) {
            Academic academic = record.getResponsibleAcademic();
            if (academic != null && academic.getId().equals(academicId)) {
                results.add(record);
            }
        }

        return results;
    }

    /**
     * Check all currently borrowed items for overdue status
     * @param currentDate Current date to check against
     * @param daysAllowed Number of days allowed for borrowing
     * @return List of records that are now overdue
     */
    public List<LendingRecord> checkForOverdueItems(Date currentDate, int daysAllowed) {
        List<LendingRecord> overdueRecords = new ArrayList<>();

        for (LendingRecord record : lendingRecords.values()) {
            if (record.getStatus() == LendingRecord.Status.BORROWED) {
                if (record.isOverdue(currentDate, daysAllowed)) {
                    overdueRecords.add(record);
                }
            }
        }

        return overdueRecords;
    }

    /**
     * Find all records containing a specific piece of equipment
     * @param equipmentId ID of the equipment
     * @return List of lending records containing the equipment
     */
    public List<LendingRecord> findRecordsWithEquipment(String equipmentId) {
        List<LendingRecord> results = new ArrayList<>();

        for (LendingRecord record : lendingRecords.values()) {
            for (Equipment item : record.getEquipment()) {
                if (item.getEquipmentId().equals(equipmentId)) {
                    results.add(record);
                    break;
                }
            }
        }

        return results;
    }

    /**
     * Update the purpose of a lending record
     * @param recordId ID of the record to update
     * @param newPurpose New purpose to set
     * @return Updated lending record or null if not found
     */
    public LendingRecord updateLendingPurpose(String recordId, String newPurpose) {
        LendingRecord record = lendingRecords.get(recordId);
        if (record == null) {
            System.out.println("Lending record not found: " + recordId);
            return null;
        }

        record.setPurpose(newPurpose);
        return record;
    }

    public boolean saveLendingRecordsToFile() {
        // Implementation for file I/O
        return true;
    }

    /**
     * Load lending records from a file
     * @return true if load was successful, false otherwise
     */
    public boolean loadLendingRecordsFromFile() {
        // Implementation for file I/O
        return true;
    }
}
