package asm2_clone.db;

import asm2_clone.model.Equipment;
import jakarta.persistence.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class EquipmentDAO {
    private static final EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("asm2_persistence_unit");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize EntityManagerFactory", e);
        }
    }

    public List<Equipment> getAllAvailableEquipment() {
        // Use JDBC instead of JPA for consistency
        String sql = "SELECT id, name, category, condition, purchase_date, status FROM equipment WHERE status = 'Available'";
        List<Equipment> equipmentList = new ArrayList<>();
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Equipment equipment = new Equipment();
                equipment.setId(rs.getInt("id"));
                equipment.setName(rs.getString("name"));
                equipment.setCategory(rs.getString("category"));
                equipment.setCondition(rs.getString("condition"));
                equipment.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
                equipment.setStatus(rs.getString("status"));
                equipmentList.add(equipment);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading available equipment: " + e.getMessage());
        }
        
        return equipmentList;
    }

    public List<Equipment> getAllEquipment() {
        List<Equipment> equipment = new ArrayList<>();
        String sql = "SELECT id, name, purchase_date, condition, status, category FROM equipment";  // Remove image from initial query
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Equipment eq = new Equipment();
                eq.setId(rs.getInt("id"));
                eq.setName(rs.getString("name"));
                eq.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
                eq.setCondition(rs.getString("condition"));
                eq.setStatus(rs.getString("status"));
                eq.setCategory(rs.getString("category"));
                // Don't load image data here
                equipment.add(eq);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipment;
    }

    public static boolean updateStatus(int id, String status) {
        String sql = "UPDATE equipment SET status = ? WHERE id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Equipment getEquipmentById(int id) {
        String sql = "SELECT id, name, category, status, condition FROM equipment WHERE id = ?"; // ✅ 不 select image

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Equipment eq = new Equipment();
                eq.setId(rs.getInt("id"));
                eq.setName(rs.getString("name"));
                eq.setCategory(rs.getString("category"));
                eq.setStatus(rs.getString("status"));
                eq.setCondition(rs.getString("condition"));
                return eq;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<Equipment> getEquipmentsByRecordId(String recordId) {
        List<Equipment> equipments = new ArrayList<>();
        String sql = """
        SELECT e.id, e.name, e.category, e.condition, lre.status, lre.return_date
        FROM lending_record_equipment lre
        JOIN equipment e ON lre.equipment_id = e.id
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
                eq.setStatus(rs.getString("status")); // ✅ 這裡是最關鍵的補上
                // 你也可以另外 setReturnDate() if needed

                equipments.add(eq);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return equipments;
    }

    public void updateEquipmentImagesFromLocal() {
        try (Connection conn = DB_Connection.getConnection()) {
            // First get all equipment
            String selectSql = "SELECT id, name FROM equipment WHERE image IS NULL";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            ResultSet rs = selectStmt.executeQuery();

            // Prepare update statement
            String updateSql = "UPDATE equipment SET image = ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");

                // Try different possible filenames
                byte[] imageBytes = null;
                String[] possibleNames = {
                    name + ".png",
                    name.replace(" ", "-") + ".png",
                    name.replace(" - ", "-") + ".png",
                    name.replace(":", "") + ".png"
                };

                // Try to load the image
                for (String fileName : possibleNames) {
                    try {
                        URL imageUrl = getClass().getResource("/images_png/" + fileName);
                        if (imageUrl != null) {
                            try (InputStream is = imageUrl.openStream()) {
                                imageBytes = is.readAllBytes();
                                break; // Exit loop if image is found and loaded
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to load image: " + fileName);
                        continue;
                    }
                }

                // Update the database if we found an image
                if (imageBytes != null) {
                    updateStmt.setBytes(1, imageBytes);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                    System.out.println("Updated image for equipment: " + name);
                } else {
                    System.out.println("No image found for equipment: " + name);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating equipment images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    public boolean addEquipment(Equipment equipment) {
        String sql = "INSERT INTO equipment (name, category, condition, purchase_date, status, image) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, equipment.getName());
            stmt.setString(2, equipment.getCategory());
            stmt.setString(3, equipment.getCondition());
            stmt.setDate(4, java.sql.Date.valueOf(equipment.getPurchaseDate()));
            stmt.setString(5, equipment.getStatus());
            
            // Handle image data
            if (equipment.getImage() != null) {
                stmt.setBytes(6, equipment.getImage());
            } else {
                stmt.setNull(6, java.sql.Types.BINARY);
            }
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEquipment(Equipment equipment) {
        String sql = "UPDATE equipment SET name = ?, category = ?, condition = ?, purchase_date = ?, status = ?, image = ? WHERE id = ?";
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, equipment.getName());
            stmt.setString(2, equipment.getCategory());
            stmt.setString(3, equipment.getCondition());
            stmt.setDate(4, java.sql.Date.valueOf(equipment.getPurchaseDate()));
            stmt.setString(5, equipment.getStatus());
            
            // Handle image data
            if (equipment.getImage() != null) {
                stmt.setBytes(6, equipment.getImage());
            } else {
                stmt.setNull(6, java.sql.Types.BINARY);
            }
            
            stmt.setInt(7, equipment.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEquipment(int id) {
        String sql = "DELETE FROM equipment WHERE id = ?";
        
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Add a separate method for loading image data
    public byte[] getEquipmentImage(int equipmentId) {
        String sql = "SELECT image FROM equipment WHERE id = ?";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("image");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Equipment> getAvailableEquipmentForStudent(String studentId) {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT e.id, e.name, e.category, e.condition, e.purchase_date, e.status " +
                "FROM equipment e " +
                "JOIN course_equipment ce ON e.id = ce.equipment_id " +
                "JOIN student_course sc ON ce.course_id = sc.course_id " +
                "WHERE sc.student_id = ? AND e.status = 'Available'";
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Equipment equipment = new Equipment();
                equipment.setId(rs.getInt("id"));
                equipment.setName(rs.getString("name"));
                equipment.setCategory(rs.getString("category"));
                equipment.setCondition(rs.getString("condition"));
                equipment.setPurchaseDate(rs.getDate("purchase_date").toLocalDate());
                equipment.setStatus(rs.getString("status"));
                equipmentList.add(equipment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading available equipment for student: " + e.getMessage());
        }
        return equipmentList;
    }
}