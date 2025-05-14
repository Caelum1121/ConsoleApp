package asm2_clone.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import asm2_clone.model.Equipment;
import asm2_clone.model.Student;
import java.util.Date;
import asm2_clone.model.Course;
import java.util.ArrayList;
import asm2_clone.db.CourseDAO;
import java.sql.SQLException;
import java.util.List;

public class StudentDAO {
    public Student getStudentByUsername(String username) {
        String sql = """
            SELECT s.*, u.username, u.password, u.email
            FROM students s
            JOIN "user" u ON s.id = u.id
            WHERE u.username = ? AND u.role = 'student'
        """;
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Student student = new Student();
                student.setId(rs.getString("id"));
                student.setFullName(rs.getString("name"));
                student.setContactInfo(rs.getString("email"));
                student.setDateOfBirth(rs.getDate("dob"));
                student.setPassword(rs.getString("password"));
                
                // Load enrolled courses
                student.setEnrolledCourses(getEnrolledCoursesByStudentId(student.getId()));
                
                return student;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Student getStudentByUserId(String userId) {
        String sql = """
            SELECT s.*, u.username, u.password, u.email
            FROM students s
            JOIN "user" u ON s.id = u.id
            WHERE s.id = ? AND u.role = 'student'
        """;
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Student student = new Student();
                student.setId(rs.getString("id"));
                student.setFullName(rs.getString("name"));
                student.setContactInfo(rs.getString("email"));
                student.setDateOfBirth(rs.getDate("dob"));
                student.setPassword(rs.getString("password"));
                
                // Load enrolled courses
                student.setEnrolledCourses(getEnrolledCoursesByStudentId(student.getId()));
                
                return student;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Course> getEnrolledCoursesByStudentId(String studentId) {
        List<Course> courses = new ArrayList<>();
        String sql = """
        SELECT c.*, s.name AS supervisor_name, s.email AS supervisor_email
        FROM courses c
        JOIN student_course sc ON c.course_id = sc.course_id
        JOIN staffs s ON c.supervisor_id = s.id
        WHERE sc.student_id = ?
    """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getString("course_id"));
                course.setCourseName(rs.getString("course_name"));
                course.setAcademicStaffId(rs.getString("supervisor_id"));
                course.setAcademicStaffName(rs.getString("supervisor_name"));
                course.setAcademicStaffEmail(rs.getString("supervisor_email"));

                // 補：為此課程查找相關設備
                course.setEquipmentRelated(getEquipmentForCourse(course.getCourseId()));

                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    private List<Equipment> getEquipmentForCourse(String courseId) {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = """
        SELECT e.*
        FROM equipment e
        JOIN course_equipment ce ON e.id = ce.equipment_id
        WHERE ce.course_id = ?
    """;

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Equipment eq = new Equipment();
                eq.setId(rs.getInt("id"));
                eq.setName(rs.getString("name"));
                eq.setCondition(rs.getString("condition"));
                eq.setStatus(rs.getString("status"));
                eq.setCategory(rs.getString("category"));
                equipmentList.add(eq);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return equipmentList;
    }



    public boolean updateStudentInfo(Student student) {
        String sqlStudent = "UPDATE students SET name = ?, dob = ?, email = ? WHERE id = ?";
        String sqlUser = "UPDATE \"user\" SET fullname = ?, email = ? WHERE id = ?";
        
        try (Connection conn = DB_Connection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmtStudent = conn.prepareStatement(sqlStudent);
                 PreparedStatement pstmtUser = conn.prepareStatement(sqlUser)) {
                
                // Update students table
                pstmtStudent.setString(1, student.getFullName());
                pstmtStudent.setDate(2, new java.sql.Date(student.getDateOfBirth().getTime()));
                pstmtStudent.setString(3, student.getContactInfo());
                pstmtStudent.setString(4, student.getId());
                pstmtStudent.executeUpdate();
                
                // Update user table
                pstmtUser.setString(1, student.getFullName());
                pstmtUser.setString(2, student.getContactInfo());
                pstmtUser.setString(3, student.getId());
                pstmtUser.executeUpdate();
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 