package asm2_clone.db;

import asm2_clone.model.Course;
import asm2_clone.model.AcademicStaff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;

public class CourseDAO {

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.course_id, c.course_name, s.id as academic_id, s.name as academic_name, s.email as academic_email " +
                "FROM courses c " +
                "LEFT JOIN staffs s ON c.supervisor_id = s.id " +
                "WHERE s.role = 'academic' OR s.role IS NULL " +
                "ORDER BY c.course_name";

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getString("course_id"));
                course.setCourseName(rs.getString("course_name"));
                course.setAcademicStaffId(rs.getString("academic_id"));
                course.setAcademicStaffName(rs.getString("academic_name"));
                course.setAcademicStaffEmail(rs.getString("academic_email"));
                courses.add(course);
            }
        } catch (Exception e) {
            System.err.println("Error loading courses: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }

    public Map<String, List<String>> getStudentsInCourses() {
        Map<String, List<String>> courseStudents = new HashMap<>();
        String sql = "SELECT sc.course_id, s.name as student_name " +
                "FROM student_course sc " +
                "JOIN students s ON sc.student_id = s.id " +
                "ORDER BY sc.course_id, s.name";

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String courseId = rs.getString("course_id").toUpperCase();
                String studentName = rs.getString("student_name");
                if (studentName != null) {
                    courseStudents.computeIfAbsent(courseId, k -> new ArrayList<>()).add(studentName);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading course enrollments: " + e.getMessage());
            e.printStackTrace();
        }

        if (courseStudents.isEmpty()) {
            System.out.println("ℹ️ No enrollments found. Try adding students to courses.");
        } else {
            System.out.println("✅ Enrollments loaded for " + courseStudents.size() + " course(s).");
        }

        return courseStudents;
    }

    public boolean assignAcademicToCourse(String courseId, String academicId) {
        String sql = "UPDATE courses SET supervisor_id = ? WHERE course_id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, academicId);
            stmt.setString(2, courseId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Course> getCoursesForAdminDashboard() {
        List<Course> courses = new ArrayList<>();
        String query = """
            SELECT c.course_id, c.course_name, c.supervisor_id,
                   COUNT(DISTINCT sc.student_id) as student_count,
                   COUNT(DISTINCT ce.equipment_id) as equipment_count
            FROM courses c
            LEFT JOIN student_course sc ON c.course_id = sc.course_id
            LEFT JOIN course_equipment ce ON c.course_id = ce.course_id
            GROUP BY c.course_id, c.course_name, c.supervisor_id
            ORDER BY c.course_id
        """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getString("course_id"));
                course.setCourseName(rs.getString("course_name"));
                course.setAcademicStaffId(rs.getString("supervisor_id"));
                course.setStudentCount(rs.getInt("student_count"));
                course.setEquipmentCount(rs.getInt("equipment_count"));
                courses.add(course);
            }
        } catch (Exception e) {
            System.err.println("Error loading courses for admin dashboard: " + e.getMessage());
            e.printStackTrace();
        }

        return courses;
    }

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (course_id, course_name, supervisor_id) VALUES (?, ?, ?)";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getCourseId());
            stmt.setString(2, course.getCourseName());
            stmt.setString(3, course.getAcademicStaffId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error adding course: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET course_name = ? WHERE course_id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getCourseName());
            stmt.setString(2, course.getCourseId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating course: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCourse(String courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting course: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<AcademicStaff> getAvailableAcademicStaff() {
        List<AcademicStaff> availableStaff = new ArrayList<>();
        String sql = """
            SELECT s.* FROM staffs s 
            LEFT JOIN courses c ON s.id = c.supervisor_id 
            WHERE s.role = 'academic' AND c.supervisor_id IS NULL
            ORDER BY s.id
        """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                AcademicStaff staff = new AcademicStaff();
                staff.setId(rs.getString("id"));
                staff.setFullName(rs.getString("name"));
                staff.setContactInfo(rs.getString("email"));
                availableStaff.add(staff);
            }
        } catch (Exception e) {
            System.err.println("Error loading available academic staff: " + e.getMessage());
            e.printStackTrace();
        }
        return availableStaff;
    }

    public boolean addAcademicStaff(AcademicStaff staff) {
        String sql = "INSERT INTO staffs (id, name, email, role, dob) VALUES (?, ?, ?, 'academic', ?)";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, staff.getId());
            stmt.setString(2, staff.getFullName());
            stmt.setString(3, staff.getContactInfo());
            stmt.setDate(4, (java.sql.Date) staff.getDateOfBirth());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error adding academic staff: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String generateNextCourseId() {
        String sql = "SELECT course_id FROM courses ORDER BY course_id DESC LIMIT 1";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                String lastId = rs.getString("course_id");
                // Extract the number part and increment it
                int nextNumber = Integer.parseInt(lastId.substring(1)) + 1;
                // Format with leading zeros (C001, C002, etc.)
                return String.format("C%03d", nextNumber);
            } else {
                // If no courses exist, start with C001
                return "C001";
            }
        } catch (Exception e) {
            System.err.println("Error generating next course ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateCourseStudents(String courseId, int studentCount) {
        String sql = "DELETE FROM student_course WHERE course_id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.executeUpdate();
            
            // Insert new student records
            sql = "INSERT INTO student_course (course_id, student_id) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(sql);
            for (int i = 0; i < studentCount; i++) {
                insertStmt.setString(1, courseId);
                insertStmt.setString(2, "S" + String.format("%03d", i + 1)); // Generate student IDs
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCourseEquipment(String courseId, int equipmentCount) {
        String sql = "DELETE FROM course_equipment WHERE course_id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.executeUpdate();
            
            // Insert new equipment records
            sql = "INSERT INTO course_equipment (course_id, equipment_id) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(sql);
            for (int i = 0; i < equipmentCount; i++) {
                insertStmt.setString(1, courseId);
                insertStmt.setInt(2, i + 1); // Equipment IDs start from 1
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCourseStudents(String courseId) {
        String sql = "DELETE FROM student_course WHERE course_id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCourseEquipment(String courseId) {
        String sql = "DELETE FROM course_equipment WHERE course_id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, String>> getAvailableStudents() {
        List<Map<String, String>> students = new ArrayList<>();
        String sql = "SELECT id, name, email FROM students";
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, String> student = new HashMap<>();
                student.put("id", rs.getString("id"));
                student.put("fullName", rs.getString("name"));
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("Error getting students: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }

    public List<Map<String, Object>> getAvailableEquipment() {
        List<Map<String, Object>> equipment = new ArrayList<>();
        String sql = "SELECT id, name FROM equipment";
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> eq = new HashMap<>();
                eq.put("id", rs.getInt("id"));
                eq.put("name", rs.getString("name"));
                equipment.add(eq);
            }
        } catch (SQLException e) {
            System.err.println("Error getting equipment: " + e.getMessage());
            e.printStackTrace();
        }
        return equipment;
    }

    public boolean addStudentToCourse(String studentId, String courseId) {
        String sql = "INSERT INTO student_course (student_id, course_id) VALUES (?, ?)";
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                return true;
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean addEquipmentToCourse(int equipmentId, String courseId) {
        String sql = "INSERT INTO course_equipment (course_id, equipment_id) VALUES (?, ?)";
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, courseId);
            stmt.setInt(2, equipmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                return true;
            }
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getStudentsInCourse(String courseId) {
        List<String> studentIds = new ArrayList<>();
        String sql = "SELECT student_id FROM student_course WHERE course_id = ?";
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                studentIds.add(rs.getString("student_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studentIds;
    }

    public List<String> getEquipmentInCourse(String courseId) {
        List<String> equipmentIds = new ArrayList<>();
        String sql = "SELECT equipment_id FROM course_equipment WHERE course_id = ?";
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                equipmentIds.add(String.valueOf(rs.getInt("equipment_id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipmentIds;
    }

    public void batchAddStudentsToCourse(List<String> studentIds, String courseId) {
        String sql = "INSERT INTO student_course (student_id, course_id) VALUES (?, ?)";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            for (String studentId : studentIds) {
                stmt.setString(1, studentId);
                stmt.setString(2, courseId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error in batch student insert: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void batchAddEquipmentToCourse(List<Integer> equipmentIds, String courseId) {
        String sql = "INSERT INTO course_equipment (course_id, equipment_id) VALUES (?, ?)";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            for (Integer equipmentId : equipmentIds) {
                stmt.setString(1, courseId);
                stmt.setInt(2, equipmentId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error in batch equipment insert: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get the course for a given equipment_id
    public Course getCourseByEquipmentId(int equipmentId) {
        String sql = "SELECT c.course_id, c.course_name FROM courses c JOIN course_equipment ce ON c.course_id = ce.course_id WHERE ce.equipment_id = ? LIMIT 1";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getString("course_id"));
                course.setCourseName(rs.getString("course_name"));
                return course;
            }
        } catch (Exception e) {
            System.err.println("Error getting course for equipment id: " + equipmentId);
            e.printStackTrace();
        }
        return null;
    }
}
