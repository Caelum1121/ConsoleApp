package asm2_clone.db;

import asm2_clone.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LendingRecordDAO {
    public static List<LendingRecord> getRecordsByStudentId(String studentId) {
        List<LendingRecord> records = new ArrayList<>();
        String sql = """
            SELECT lr.*, e.id as equipment_id, e.name as equipment_name, e.category, e.condition, lre.status, lre.return_date, lr.approval_status
            FROM lending_record lr
            JOIN lending_record_equipment lre ON lr.record_id = lre.record_id
            JOIN equipment e ON lre.equipment_id = e.id
            WHERE lr.borrower_id = ? AND lr.borrower_role = 'student'
            ORDER BY lr.borrow_date DESC
        """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            System.out.println("Executing query for student ID: " + studentId); // Debug log
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LendingRecord record = new LendingRecord();
                record.setRecordId(rs.getString("record_id"));

                // Create Equipment object
                Equipment equipment = new Equipment();
                equipment.setId(rs.getInt("equipment_id"));
                equipment.setName(rs.getString("equipment_name"));
                equipment.setCategory(rs.getString("category"));
                equipment.setCondition(rs.getString("condition"));

                record.setEquipment(equipment);
                record.setBorrowDate(rs.getDate("borrow_date"));
                record.setReturnDate(rs.getDate("return_date"));
                record.setStatus(LendingRecord.Status.valueOf(rs.getString("status").toUpperCase()));
                record.setPurpose(rs.getString("purpose"));
                // Set the approval status
                String approvalStatus = rs.getString("approval_status");
                if (approvalStatus != null) {
                    record.setApprovalStatus(LendingRecord.ApprovalStatus.valueOf(approvalStatus.toUpperCase()));
                }

                records.add(record);
                System.out.println("Found record: " + record.getRecordId() + " with approval status: " + approvalStatus); // Debug log
            }
        } catch (SQLException e) {
            System.err.println("Error fetching records for student " + studentId);
            e.printStackTrace();
        }
        return records;
    }

//    public static boolean insertLendingRecord(LendingRecord record) {
//        String sql = """
//            INSERT INTO lending_record
//            (record_id, borrower_id, borrower_role, supervisor_id, borrow_date, return_date, status, purpose, approval_status)
//            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
//        """;
//
//        try (Connection conn = DB_Connection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            String recordId = generateNextRecordId();
//            stmt.setString(1, recordId);
//            stmt.setString(2, record.getBorrower().getId());
//            stmt.setString(3, getBorrowerRole(record.getBorrower()));
//            stmt.setString(4, null); // supervisor_id will be set separately for students
//            stmt.setDate(5, new java.sql.Date(record.getBorrowDate().getTime()));
//            stmt.setDate(6, null); // return_date is null on borrow
//            stmt.setString(7, record.getStatus().name().toLowerCase());
//            stmt.setString(8, record.getPurpose());
//            stmt.setString(9, "pending"); // Initial status for new records
//
//            boolean success = stmt.executeUpdate() > 0;
//
//            // If successful, also insert into lending_record_equipment
//            if (success) {
//                String equipSql = "INSERT INTO lending_record_equipment (record_id, equipment_id) VALUES (?, ?)";
//                try (PreparedStatement equipStmt = conn.prepareStatement(equipSql)) {
//                    equipStmt.setString(1, recordId);
//                    equipStmt.setInt(2, record.getEquipment().getId());
//                    equipStmt.executeUpdate();
//                }
//            }
//
//            return success;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    } conflict

    public static boolean insertLendingRecord(LendingRecord record) {
        String insertRecordSql = "INSERT INTO lending_record (record_id, borrower_id, borrower_role, supervisor_id, borrow_date, need_to_return, purpose, approval_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertLinkSql = "INSERT INTO lending_record_equipment (record_id, equipment_id, status) VALUES (?, ?, ?)";

        String recordId = generateNextRecordId();

        try (Connection conn = DB_Connection.getConnection()) {
            conn.setAutoCommit(false);

            // due date caculate
            java.util.Date borrowDate = record.getBorrowDate();
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(borrowDate);
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 14);
            java.util.Date returnDeadline = calendar.getTime();

            // Insert into lending_record
            try (PreparedStatement stmt = conn.prepareStatement(insertRecordSql)) {
                stmt.setString(1, recordId);
                stmt.setString(2, record.getBorrower().getId());
                stmt.setString(3, getBorrowerRole(record.getBorrower()));

                if (record.getBorrower() instanceof Student student) {
                    if (record.getCourse() != null) {
                        stmt.setString(4, record.getCourse().getAcademicStaffId());
                    } else {
                        stmt.setString(4, null);
                    }
                } else {
                    stmt.setString(4, null); // staff 不需填 supervisor
                }

                stmt.setDate(5, new java.sql.Date(record.getBorrowDate().getTime()));
                stmt.setDate(6, new java.sql.Date(returnDeadline.getTime()));
                stmt.setString(7, record.getPurpose());
                stmt.setString(8, record.getApprovalStatus() != null ? record.getApprovalStatus().name().toLowerCase() : null);
                stmt.executeUpdate();
            }

            for (Equipment eq : record.getEquipmentList()) {
                try (PreparedStatement linkStmt = conn.prepareStatement(insertLinkSql)) {
                    linkStmt.setString(1, recordId);
                    linkStmt.setInt(2, eq.getId());
                    linkStmt.setString(3, "borrowed");
                    linkStmt.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getBorrowerRole(Person borrower) {
        if (borrower instanceof Student) return "student";
        if (borrower instanceof ProfessionalStaff) return "professional";
        if (borrower instanceof AcademicStaff) return "academic";
        return "unknown";
    }

    public static String generateNextRecordId() {
        String prefix = "R";
        int maxNumber = 0;
        String query = "SELECT record_id FROM lending_record WHERE record_id ~ '^R\\d+$' ORDER BY record_id DESC LIMIT 1";
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                String id = rs.getString("record_id");
                maxNumber = Integer.parseInt(id.substring(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return prefix + String.format("%03d", maxNumber + 1);
    }

    public static List<LendingRecord> getRecordsByProfessionalId(String staffId) {
        List<LendingRecord> result = new ArrayList<>();
        String sql = "SELECT * FROM lending_record WHERE borrower_id = ? AND borrower_role = 'professional'";

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staffId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String recordId = rs.getString("record_id");
                List<Equipment> equipments = EquipmentDAO.getEquipmentsByRecordId(recordId);

                LendingRecord r = new LendingRecord();
                r.setRecordId(recordId);
                r.setBorrower(new ProfessionalStaff());
                r.setEquipmentList(equipments);
                r.setBorrowDate(rs.getDate("borrow_date"));
                r.setReturnDate(rs.getDate("need_to_return"));
                LendingRecord.Status aggregatedStatus = calculateOverallStatus(equipments);
                r.setStatus(aggregatedStatus);
                r.setPurpose(rs.getString("purpose"));
                result.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static List<LendingRecord> getRecordsByAcademicId(String academicId) {
        List<LendingRecord> result = new ArrayList<>();
        String sql = "SELECT * FROM lending_record WHERE borrower_id = ? AND borrower_role = 'academic'";

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, academicId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String recordId = rs.getString("record_id");
                List<Equipment> equipments = EquipmentDAO.getEquipmentsByRecordId(recordId);

                LendingRecord r = new LendingRecord();
                r.setRecordId(recordId);
                r.setBorrower(new ProfessionalStaff());
                r.setEquipmentList(equipments);
                r.setBorrowDate(rs.getDate("borrow_date"));
                r.setReturnDate(rs.getDate("need_to_return"));
                LendingRecord.Status aggregatedStatus = calculateOverallStatus(equipments);
                r.setStatus(aggregatedStatus);
                r.setPurpose(rs.getString("purpose"));
                result.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static LendingRecord.Status calculateOverallStatus(List<Equipment> equipments) {
        boolean hasOverdue = false;
        boolean allReturned = true;

        for (Equipment eq : equipments) {
            String status = eq.getStatus();
            if ("Overdue".equalsIgnoreCase(status)) {
                hasOverdue = true;
            }
            if (!"Returned".equalsIgnoreCase(status)) {
                allReturned = false;
            }
        }

        if (hasOverdue) return LendingRecord.Status.OVERDUE;
        if (allReturned) return LendingRecord.Status.RETURNED;
        return LendingRecord.Status.BORROWED;
    }

    public static Map<String, Map<String, Integer>> getSeparatedStatsByAcademicId(String academicId) {
        Map<String, Integer> studentStats = new HashMap<>();
        Map<String, Integer> selfStats = new HashMap<>();
        Map<String, Map<String, Integer>> combined = new HashMap<>();

        String sql = """
        SELECT lr.borrower_role, lr.approval_status, lre.status
        FROM lending_record lr
        LEFT JOIN lending_record_equipment lre ON lr.record_id = lre.record_id
        WHERE lr.supervisor_id = ? OR (lr.borrower_id = ? AND lr.borrower_role = 'academic')
    """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, academicId);
            stmt.setString(2, academicId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String role = rs.getString("borrower_role");
                String approval = rs.getString("approval_status");
                String equipmentStatus = rs.getString("status");

                Map<String, Integer> target = role.equals("academic") ? selfStats : studentStats;

                // Count pending by approval_status
                if ("pending".equalsIgnoreCase(approval)) {
                    target.put("pending", target.getOrDefault("pending", 0) + 1);
                }

                // Count equipment statuses (borrowed, returned, overdue)
                if (equipmentStatus != null) {
                    target.put(equipmentStatus.toLowerCase(), target.getOrDefault(equipmentStatus.toLowerCase(), 0) + 1);
                }
            }

            combined.put("student", studentStats);
            combined.put("self", selfStats);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return combined;
    }

    public static Map<String, Integer> getBorrowingFrequencyByCourse(String academicId) {
        Map<String, Integer> frequency = new HashMap<>();

        String sql = """
        SELECT c.course_name, COUNT(*) as borrow_count
        FROM lending_record lr
        JOIN students s ON lr.borrower_id = s.id AND lr.borrower_role = 'student'
        JOIN student_course sc ON s.id = sc.student_id
        JOIN courses c ON sc.course_id = c.course_id
        WHERE c.supervisor_id = ?
        GROUP BY c.course_name
        ORDER BY borrow_count DESC
    """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, academicId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                frequency.put(rs.getString("course_name"), rs.getInt("borrow_count"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return frequency;
    }
    public static ObservableList<LendingRecord> getAllRecords() {
        ObservableList<LendingRecord> records = FXCollections.observableArrayList();
        String sql = """
            SELECT 
                lr.borrower_id,
                lr.borrower_role,
                CASE 
                    WHEN lr.borrower_role = 'student' THEN s.name
                    ELSE st.name
                END as borrower_name,
                lre.status as equipment_status,
                STRING_AGG(e.name, ',') as equipment_names,
                MIN(lr.borrow_date) as min_borrow_date,
                MAX(lr.borrow_date) as max_borrow_date,
                MIN(lr.borrow_date + INTERVAL '14 days') as min_due_date,
                MAX(lr.borrow_date + INTERVAL '14 days') as max_due_date
            FROM lending_record lr
            INNER JOIN lending_record_equipment lre ON lr.record_id = lre.record_id
            INNER JOIN equipment e ON lre.equipment_id = e.id
            LEFT JOIN students s ON lr.borrower_id = s.id AND lr.borrower_role = 'student'
            LEFT JOIN staffs st ON lr.borrower_id = st.id AND lr.borrower_role = st.role
            GROUP BY lr.borrower_id, lr.borrower_role, borrower_name, lre.status
            ORDER BY borrower_name, lre.status
        """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String statusStr = rs.getString("equipment_status");
                LendingRecord.Status statusEnum;
                switch (statusStr.toLowerCase()) {
                    case "borrowed":
                        statusEnum = LendingRecord.Status.BORROWED;
                        break;
                    case "overdue":
                        statusEnum = LendingRecord.Status.OVERDUE;
                        break;
                    case "returned":
                        statusEnum = LendingRecord.Status.RETURNED;
                        break;
                    default:
                        // Skip unknown statuses (like "pending", "approved")
                        continue;
                }

                LendingRecord record = new LendingRecord();
                record.setBorrower_id(rs.getString("borrower_id"));
                record.setBorrower_role(rs.getString("borrower_role"));
                record.setBorrowerName(rs.getString("borrower_name"));
                record.setStatus(statusEnum);

                String equipmentNamesStr = rs.getString("equipment_names");
                if (equipmentNamesStr != null && !equipmentNamesStr.isEmpty()) {
                    record.setEquipmentNames(Arrays.asList(equipmentNamesStr.split(",")));
                }

                record.setBorrow_date(rs.getDate("min_borrow_date"));
                record.setReturnDate(rs.getDate("max_due_date"));

                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    public static String getBorrowerName(String borrowerId, String borrowerRole) {
        String sql = "";
        if (borrowerRole.equals("student")) {
            sql = "SELECT name FROM students WHERE id = ?";
        } else if (borrowerRole.equals("academic") || borrowerRole.equals("professional")) {
            sql = "SELECT name FROM staffs WHERE id = ? AND role = ?";
        }

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, borrowerId);
            if (borrowerRole.equals("academic") || borrowerRole.equals("professional")) {
                stmt.setString(2, borrowerRole);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error getting borrower name for ID: " + borrowerId + ", Role: " + borrowerRole);
        }
        return borrowerId;
    }

    public static List<Equipment> getEquipmentsByRecordId(String recordId) {
        List<Equipment> equipment = new ArrayList<>();
        String sql = """
            SELECT e.* 
            FROM equipment e 
            JOIN lending_record_equipment lre ON e.id = lre.equipment_id 
            WHERE lre.record_id = ?
        """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, recordId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Equipment eq = new Equipment();
                eq.setId(rs.getInt("id"));
                eq.setName(rs.getString("name"));
                eq.setCategory(rs.getString("category"));
                eq.setCondition(rs.getString("condition"));
                equipment.add(eq);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipment;
    }

    public static boolean returnEquipment(String recordId, int equipmentId) {
        String updateStatusSql = "UPDATE lending_record_equipment SET status = 'returned', return_date = CURRENT_DATE WHERE record_id = ? AND equipment_id = ?";
        String updateEquipmentSql = "UPDATE equipment SET status = 'Available' WHERE id = ?";

        try (Connection conn = DB_Connection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(updateStatusSql);
                 PreparedStatement stmt2 = conn.prepareStatement(updateEquipmentSql)) {

                stmt1.setString(1, recordId);
                stmt1.setInt(2, equipmentId);
                stmt1.executeUpdate();

                stmt2.setInt(1, equipmentId);
                stmt2.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateRecord(LendingRecord record) {
        String updateMainSql = """
        UPDATE lending_record 
        SET approval_status = ?, need_to_return = ? 
        WHERE record_id = ?
    """;

        String updateEquipLinkSql = """
        UPDATE lending_record_equipment 
        SET status = ? 
        WHERE record_id = ? AND equipment_id = ?
    """;

        String updateEquipmentSql = """
        UPDATE equipment 
        SET status = ? 
        WHERE id = ?
    """;

        try (Connection conn = DB_Connection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. 更新 lending_record 本體
            try (PreparedStatement stmt = conn.prepareStatement(updateMainSql)) {
                stmt.setString(1, record.getApprovalStatus().name().toLowerCase());
                if (record.getNeedToReturnDate() != null) {
                    stmt.setDate(2, new java.sql.Date(record.getNeedToReturnDate().getTime()));
                } else {
                    stmt.setNull(2, java.sql.Types.DATE);
                }
                stmt.setString(3, record.getRecordId());
                stmt.executeUpdate();
            }

            // 2. 遍歷每個設備並更新 equipment + link 表（只在 approve 時執行）
            if (record.getApprovalStatus() != LendingRecord.ApprovalStatus.REJECTED) {
                for (Equipment eq : record.getEquipmentList()) {
                    String status = "borrowed";  // 審核通過後預設為 borrowed

                    try (PreparedStatement stmt1 = conn.prepareStatement(updateEquipLinkSql);
                         PreparedStatement stmt2 = conn.prepareStatement(updateEquipmentSql)) {

                        stmt1.setString(1, status);
                        stmt1.setString(2, record.getRecordId());
                        stmt1.setInt(3, eq.getId());
                        stmt1.executeUpdate();

                        stmt2.setString(1, "Borrowed");
                        stmt2.setInt(2, eq.getId());
                        stmt2.executeUpdate();
                    }
                }
            }

            if (record.getApprovalStatus() == LendingRecord.ApprovalStatus.REJECTED) {
                for (Equipment eq : record.getEquipmentList()) {
                    try (PreparedStatement stmt = conn.prepareStatement(updateEquipmentSql)) {
                        stmt.setString(1, "Available");
                        stmt.setInt(2, eq.getId());
                        stmt.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<LendingRecord> getPendingRecordsByAcademicId(String academicId) {
        List<LendingRecord> result = new ArrayList<>();

        String sql = """
            SELECT lr.*, e.id as equipment_id, e.name as equipment_name
            FROM lending_record lr
            JOIN lending_record_equipment lre ON lr.record_id = lre.record_id
            JOIN equipment e ON lre.equipment_id = e.id
            WHERE lr.supervisor_id = ? AND lr.approval_status = 'pending'
            ORDER BY lr.borrow_date DESC
        """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, academicId);
            ResultSet rs = stmt.executeQuery();

            Map<String, LendingRecord> recordMap = new HashMap<>();

            while (rs.next()) {
                String recordId = rs.getString("record_id");
                LendingRecord record = recordMap.getOrDefault(recordId, new LendingRecord());
                if (!recordMap.containsKey(recordId)) {
                    record.setRecordId(recordId);
                    // 间接找出 Course 对象（通过 supervisor_id + borrower_id 反查）
                    String studentId = rs.getString("borrower_id");
                    String supervisorId = rs.getString("supervisor_id");
                    record.setBorrowerId(studentId);  // ✅ 補這行（若尚未設）
                    record.setBorrower_role("student");
                    record.setBorrowerName(getBorrowerName(studentId, "student")); // ✅ 關鍵！

                    Course course = null;
                    String courseQuery = """
                        SELECT c.course_id, c.course_name
                        FROM courses c
                        JOIN student_course sc ON c.course_id = sc.course_id
                        WHERE sc.student_id = ? AND c.supervisor_id = ?
                        LIMIT 1
                    """;

                    try (PreparedStatement courseStmt = conn.prepareStatement(courseQuery)) {
                        courseStmt.setString(1, studentId);
                        courseStmt.setString(2, supervisorId);
                        try (ResultSet crs = courseStmt.executeQuery()) {
                            if (crs.next()) {
                                course = new Course();
                                course.setCourseId(crs.getString("course_id"));
                                course.setCourseName(crs.getString("course_name"));
                            }
                        }
                    }

                    record.setCourse(course);

                    record.setPurpose(rs.getString("purpose"));
                    record.setBorrowDate(rs.getDate("borrow_date"));
                    record.setApprovalStatus(LendingRecord.ApprovalStatus.PENDING);

                    record.setEquipmentList(new ArrayList<>());
                    record.setEquipmentNames(new ArrayList<>());
                    recordMap.put(recordId, record);
                }

                Equipment eq = new Equipment();
                eq.setId(rs.getInt("equipment_id"));
                eq.setName(rs.getString("equipment_name"));
                recordMap.get(recordId).getEquipmentList().add(eq);
                recordMap.get(recordId).getEquipmentNames().add(eq.getName());
            }

            result.addAll(recordMap.values());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }



    public static void autoMarkOverdue() {
        String sql = """
            UPDATE lending_record_equipment le
            SET status = 'overdue'
            FROM lending_record lr
            WHERE le.record_id = lr.record_id   
          AND le.status = 'borrowed'
          AND lr.need_to_return < CURRENT_DATE
        """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void autoFixMisclassifiedOverdue() {
        String sql = """
        UPDATE lending_record_equipment le
        SET status = 'borrowed'
        FROM lending_record lr
        WHERE le.record_id = lr.record_id
        AND le.status = 'overdue'
        AND lr.need_to_return >= CURRENT_DATE
    """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int updated = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateReturnDates() {
        String sql = "UPDATE lending_record SET need_to_return = borrow_date + INTERVAL '14 days' ";

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating return dates: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static Map<String, Map<String, Integer>> getBorrowingStatusByCourse(String academicId) {
        Map<String, Map<String, Integer>> result = new HashMap<>();

        String sql = """
        SELECT c.course_name, lr.approval_status, lre.status
        FROM lending_record lr
        JOIN lending_record_equipment lre ON lr.record_id = lre.record_id
        JOIN students s ON lr.borrower_id = s.id AND lr.borrower_role = 'student'
        JOIN student_course sc ON s.id = sc.student_id
        JOIN courses c ON sc.course_id = c.course_id
        WHERE c.supervisor_id = ?
        """;


        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, academicId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String course = rs.getString("course_name");
                String approvalStatus = rs.getString("approval_status");
                String equipmentStatus = rs.getString("status");

                result.putIfAbsent(course, new HashMap<>());
                Map<String, Integer> statMap = result.get(course);

                // Count pending as separate
                if ("pending".equalsIgnoreCase(approvalStatus)) {
                    statMap.put("pending", statMap.getOrDefault("pending", 0) + 1);
                } else if (equipmentStatus != null) {
                    statMap.put(equipmentStatus.toLowerCase(), statMap.getOrDefault(equipmentStatus.toLowerCase(), 0) + 1);
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static List<LendingRecord> getBorrowedOrOverdueEquipmentByStudentId(String studentId) {
        List<LendingRecord> result = new ArrayList<>();
        String sql = """
            SELECT lr.record_id, lr.borrow_date, lr.need_to_return, lr.purpose, lre.equipment_id, lre.status as equipment_status, e.name as equipment_name, e.category, e.condition
            FROM lending_record lr
            JOIN lending_record_equipment lre ON lr.record_id = lre.record_id
            JOIN equipment e ON lre.equipment_id = e.id
            WHERE lr.borrower_id = ? AND lr.borrower_role = 'student' AND (lre.status = 'borrowed' OR lre.status = 'overdue')
            ORDER BY lr.borrow_date DESC
        """;
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LendingRecord record = new LendingRecord();
                record.setRecordId(rs.getString("record_id"));
                record.setBorrowDate(rs.getDate("borrow_date"));
                record.setNeedToReturnDate(rs.getDate("need_to_return"));
                record.setPurpose(rs.getString("purpose"));
                Equipment eq = new Equipment();
                eq.setId(rs.getInt("equipment_id"));
                eq.setName(rs.getString("equipment_name"));
                eq.setCategory(rs.getString("category"));
                eq.setCondition(rs.getString("condition"));
                record.setEquipment(eq);
                // Set status for this equipment
                Map<Integer, String> eqStatus = new HashMap<>();
                eqStatus.put(eq.getId(), rs.getString("equipment_status"));
                record.setEquipmentStatuses(eqStatus);
                result.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static boolean deleteLendingRecord(String recordId) {
        String deleteEquipmentLinkSql = "DELETE FROM lending_record_equipment WHERE record_id = ?";
        String deleteRecordSql = "DELETE FROM lending_record WHERE record_id = ?";

        try (Connection conn = DB_Connection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(deleteEquipmentLinkSql);
                 PreparedStatement stmt2 = conn.prepareStatement(deleteRecordSql)) {

                stmt1.setString(1, recordId);
                stmt1.executeUpdate();

                stmt2.setString(1, recordId);
                int affected = stmt2.executeUpdate();

                conn.commit();
                return affected > 0;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}


