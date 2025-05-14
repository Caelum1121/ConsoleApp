package asm2_clone.db;

import asm2_clone.model.ProfessionalStaff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ProfessionalDAO {
    // Find Professional by user id (from user table)
    public ProfessionalStaff getProfessionalByUserId(String userId) {
        String sql = "SELECT p.id, p.name, p.dob, p.email, p.department " +
                "FROM staffs p WHERE p.id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                LocalDate dateOfBirth = rs.getDate("dob").toLocalDate();
                String email = rs.getString("email");
                String department = rs.getString("department");

                ProfessionalStaff professional = new ProfessionalStaff();
                professional.setId(id);
                professional.setFullName(name);
                professional.setDateOfBirth(java.sql.Date.valueOf(dateOfBirth));
                professional.setContactInfo(email);
                professional.setDepartment(department);
                return professional;
            }
        } catch (Exception e) {
            System.err.println("Error loading professional staff by user id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateProfessionalInfo(ProfessionalStaff professional) {
        String sql = "UPDATE staffs SET name = ?, dob = ?, email = ?, department = ? WHERE id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, professional.getFullName());
            stmt.setDate(2, new java.sql.Date(professional.getDateOfBirth().getTime()));
            stmt.setString(3, professional.getContactInfo());
            stmt.setString(4, professional.getDepartment());
            stmt.setString(5, professional.getId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating professional info: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}