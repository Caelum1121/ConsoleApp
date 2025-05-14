package asm2_clone.db;

import asm2_clone.model.AdminPerson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.sql.SQLException;

public class AdminDAO {
    public ObservableList<AdminPerson> getAllPeople() {
        ObservableList<AdminPerson> people = FXCollections.observableArrayList();

        // Get Students with their courses and supervisors
        String studentSql = "WITH StudentInfo AS (" +
                "    SELECT s.id, s.name, s.dob, s.email, " +
                "           string_agg(DISTINCT c.course_name, ', ' ORDER BY c.course_name) as courses, " +
                "           string_agg(DISTINCT st.name, ', ' ORDER BY st.name) as supervisor_names " +
                "    FROM students s " +
                "    LEFT JOIN student_course sc ON s.id = sc.student_id " +
                "    LEFT JOIN courses c ON sc.course_id = c.course_id " +
                "    LEFT JOIN staffs st ON c.supervisor_id = st.id " +
                "    GROUP BY s.id, s.name, s.dob, s.email" +
                ") SELECT * FROM StudentInfo ORDER BY name";

        // Get Staff with their courses/departments
        String staffSql = "WITH StaffInfo AS (" +
                "    SELECT st.id, st.name, st.dob, st.email, st.role, st.department, " +
                "           string_agg(DISTINCT c.course_name, ', ' ORDER BY c.course_name) as courses_taught " +
                "    FROM staffs st " +
                "    LEFT JOIN courses c ON st.id = c.supervisor_id " +
                "    GROUP BY st.id, st.name, st.dob, st.email, st.role, st.department" +
                ") SELECT * FROM StaffInfo ORDER BY name";

        try (Connection conn = DB_Connection.getConnection()) {
            // Get students
            try (PreparedStatement stmt = conn.prepareStatement(studentSql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    AdminPerson person = new AdminPerson(
                        rs.getString("name"),
                        "Student",
                        rs.getString("email")
                    );
                    person.setId(rs.getString("id"));
                    Date sqlDob = rs.getDate("dob");
                    if (sqlDob != null) {
                        person.setDateOfBirth(sqlDob.toLocalDate());
                    }
                    String courses = rs.getString("courses");
                    String supervisors = rs.getString("supervisor_names");
                    person.setCourseOrDept(courses != null ? courses : "");
                    person.setSupervisor(supervisors != null ? supervisors : "");
                    people.add(person);
                }
            }

            // Get staff
            try (PreparedStatement stmt = conn.prepareStatement(staffSql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String role = rs.getString("role").equals("academic") ? "Academic Staff" : "Professional Staff";
                    AdminPerson person = new AdminPerson(
                        rs.getString("name"),
                        role,
                        rs.getString("email")
                    );
                    person.setId(rs.getString("id"));
                    Date sqlDob = rs.getDate("dob");
                    if (sqlDob != null) {
                        person.setDateOfBirth(sqlDob.toLocalDate());
                    }
                    if ("Academic Staff".equals(role)) {
                        String coursesTaught = rs.getString("courses_taught");
                        person.setCourseOrDept(coursesTaught != null ? coursesTaught : "");
                    } else {
                        String department = rs.getString("department");
                        person.setCourseOrDept(department != null ? department : "");
                    }
                    people.add(person);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return people;
    }

    public void deletePerson(AdminPerson person) {
        String id = person.getId();
        try (Connection conn = DB_Connection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try {
                if (id.startsWith("S")) {
                    // First remove from student_course
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM student_course WHERE student_id=?")) {
                        stmt.setString(1, id);
                        stmt.executeUpdate();
                    }
                    
                    // Then delete from students
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM students WHERE id=?")) {
                        stmt.setString(1, id);
                        stmt.executeUpdate();
                    }
                } else if (id.startsWith("A") || id.startsWith("P")) {
                    // For both academic and professional staff
                    
                    // First update any courses supervised by this academic (if it's an academic)
                    if (id.startsWith("A")) {
                        try (PreparedStatement stmt = conn.prepareStatement("UPDATE courses SET supervisor_id=NULL WHERE supervisor_id=?")) {
                            stmt.setString(1, id);
                            stmt.executeUpdate();
                        }
                    }
                    
                    // Then delete from staffs
                    try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM staffs WHERE id=?")) {
                        stmt.setString(1, id);
                        stmt.executeUpdate();
                    }
                }

                // Finally, delete from user table for all types
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM \"user\" WHERE id=?")) {
                    stmt.setString(1, id);
                    stmt.executeUpdate();
                }

                conn.commit(); // Commit all changes
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                System.err.println("Error during delete transaction: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error in deletePerson: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean addPerson(AdminPerson person) {
        try (Connection conn = DB_Connection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // First insert into user table
                String userSql = "INSERT INTO \"user\" (id, username, password, role, email, fullname) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                    stmt.setString(1, person.getId());
                    stmt.setString(2, generateUsername(person.getName()));
                    stmt.setString(3, "password123");
                    stmt.setString(4, getRoleValue(person.getRole()));
                    stmt.setString(5, person.getEmail());
                    stmt.setString(6, person.getName());
                    stmt.executeUpdate();
                }

                // Then insert into role-specific table
                if (person.getId().startsWith("S")) {
                    String studentSql = "INSERT INTO students (id, name, dob, email) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(studentSql)) {
                        stmt.setString(1, person.getId());
                        stmt.setString(2, person.getName());
                        stmt.setDate(3, java.sql.Date.valueOf(person.getDateOfBirth()));
                        stmt.setString(4, person.getEmail());
                        stmt.executeUpdate();
                    }
                } else if (person.getId().startsWith("A") || person.getId().startsWith("P")) {
                    String staffSql = "INSERT INTO staffs (id, name, dob, email, role, department) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(staffSql)) {
                        stmt.setString(1, person.getId());
                        stmt.setString(2, person.getName());
                        stmt.setDate(3, java.sql.Date.valueOf(person.getDateOfBirth()));
                        stmt.setString(4, person.getEmail());
                        stmt.setString(5, person.getId().startsWith("A") ? "academic" : "professional");
                        stmt.setString(6, person.getId().startsWith("P") ? person.getCourseOrDept() : null);
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private String getRoleValue(String role) {
        switch (role.toLowerCase()) {
            case "student": return "student";
            case "academic staff": return "academic";
            case "professional staff": return "professional";
            default: return role.toLowerCase();
        }
    }

    private String generateUsername(String fullName) {
        // Convert full name to lowercase and remove spaces
        String baseName = fullName.toLowerCase().replaceAll("\\s+", ".");
        // Take first 20 characters if name is too long
        if (baseName.length() > 20) {
            baseName = baseName.substring(0, 20);
        }
        return baseName;
    }

    public boolean enrollStudentInCourse(String studentId, String courseId) {
        String checkSql = "SELECT 1 FROM student_course WHERE student_id = ? AND course_id = ?";
        String insertSql = "INSERT INTO student_course (student_id, course_id) VALUES (?, ?)";
        
        try (Connection conn = DB_Connection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, studentId);
                    checkStmt.setString(2, courseId);
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (!rs.next()) {
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, studentId);
                            insertStmt.setString(2, courseId);
                            insertStmt.executeUpdate();
                        }
                    }
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateProfessionalDepartment(String professionalId, String department) {
        String sql = "UPDATE staffs SET department = ? WHERE id = ? AND role = 'professional'";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, department);
            stmt.setString(2, professionalId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateStudentCourseAndSupervisor(String studentId, String courseId, boolean clearExisting) {
        try (Connection conn = DB_Connection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (clearExisting) {
                    String deleteSql = "DELETE FROM student_course WHERE student_id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setString(1, studentId);
                        deleteStmt.executeUpdate();
                    }
                }

                String insertSql = "INSERT INTO student_course (student_id, course_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, studentId);
                    insertStmt.setString(2, courseId);
                    insertStmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Add a staff-student supervision relationship if not already present
    public void addStaffSupervision(String staffId, String studentId) {
        String checkSql = "SELECT 1 FROM staff_supervision WHERE staff_id = ? AND student_id = ?";
        String insertSql = "INSERT INTO staff_supervision (staff_id, student_id) VALUES (?, ?)";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, staffId);
            checkStmt.setString(2, studentId);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, staffId);
                    insertStmt.setString(2, studentId);
                    insertStmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updatePerson(AdminPerson person) {
        String id = person.getId();
        try (Connection conn = DB_Connection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try {
                // Update user table first
                String userSql = "UPDATE \"user\" SET email=?, fullname=? WHERE id=?";
                try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                    stmt.setString(1, person.getEmail());
                    stmt.setString(2, person.getName());
                    stmt.setString(3, id);
                    stmt.executeUpdate();
                }

                if (id.startsWith("S")) {
                    // Update student info
                    String studentSql = "UPDATE students SET name=?, dob=?, email=? WHERE id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(studentSql)) {
                        stmt.setString(1, person.getName());
                        stmt.setDate(2, java.sql.Date.valueOf(person.getDateOfBirth()));
                        stmt.setString(3, person.getEmail());
                        stmt.setString(4, id);
                        stmt.executeUpdate();
                    }

                    // Handle student course updates in separate method
                    // ... existing student course update code ...

                } else if (id.startsWith("A") || id.startsWith("P")) {
                    // Update staff info
                    String staffSql = "UPDATE staffs SET name=?, dob=?, email=?, department=? WHERE id=?";
                    try (PreparedStatement stmt = conn.prepareStatement(staffSql)) {
                        stmt.setString(1, person.getName());
                        stmt.setDate(2, java.sql.Date.valueOf(person.getDateOfBirth()));
                        stmt.setString(3, person.getEmail());
                        stmt.setString(4, id.startsWith("P") ? person.getCourseOrDept() : null); // Set department only for professional staff
                        stmt.setString(5, id);
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error updating person: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
