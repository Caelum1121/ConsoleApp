package asm2_clone.db;

import asm2_clone.model.AcademicStaff;
import asm2_clone.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AcademicDAO {
    public AcademicStaff getAcademicByUserId(String userId) {
        String sql = "SELECT id, name, dob, email FROM staffs WHERE id = ? AND role = 'academic'";
        System.out.println(sql);
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                LocalDate dateOfBirth = rs.getDate("dob").toLocalDate();
                String email = rs.getString("email");

                AcademicStaff academic = new AcademicStaff();
                academic.setId(id);
                academic.setFullName(name);
                academic.setDateOfBirth(java.sql.Date.valueOf(dateOfBirth));
                academic.setContactInfo(email);
                academic.setCoursesTaught(getCoursesByAcademicId(id));
                return academic;
            }
        } catch (Exception e) {
            System.err.println("Error loading academic staff by user id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private List<Course> getCoursesByAcademicId(String academicId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT course_id, course_name FROM courses WHERE supervisor_id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, academicId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getString("course_id"));
                course.setCourseName(rs.getString("course_name"));
                courses.add(course);
            }
        } catch (Exception e) {
            System.err.println("Error loading courses for academic staff: " + e.getMessage());
            e.printStackTrace();
        }
        return courses;
    }


    public boolean updateAcademicInfo(AcademicStaff academic) {
        String sql = "UPDATE staffs SET name = ?, dob = ?, email = ? WHERE id = ? AND role = 'academic'";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, academic.getFullName());
            stmt.setDate(2, new java.sql.Date(academic.getDateOfBirth().getTime()));
            stmt.setString(3, academic.getContactInfo());
            stmt.setString(4, academic.getId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating academic info: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
