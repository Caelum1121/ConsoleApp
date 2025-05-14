package asm2_clone.service;

import asm2_clone.db.DB_Connection;
import asm2_clone.db.EquipmentDAO;
import asm2_clone.model.Equipment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;

public class EquipmentService {
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();

    public ObservableList<Equipment> getAllEquipment() {
        return FXCollections.observableArrayList(equipmentDAO.getAllEquipment());
    }

    public ObservableList<Equipment> filterEquipment(List<Equipment> equipmentList, String search, String condition, String status) {
        String searchLower = search == null ? "" : search.toLowerCase();
        return FXCollections.observableArrayList(
            equipmentList.stream()
                .filter(eq -> eq.getName().toLowerCase().contains(searchLower) || eq.getCategory().toLowerCase().contains(searchLower))
                .filter(eq -> "All Conditions".equals(condition) || eq.getCondition().equalsIgnoreCase(condition))
                .filter(eq -> "All Status".equals(status) || eq.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList())
        );
    }

    public void updateAllEquipmentImages() {
        equipmentDAO.updateEquipmentImagesFromLocal();
    }

    public boolean addNewEquipment(Equipment equipment, File imageFile) {
        try (Connection conn = DB_Connection.getConnection()) {
            // Get next ID
            int nextId;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT MAX(id) FROM equipment")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    nextId = rs.getInt(1) + 1;
                } else {
                    nextId = 1;
                }
            }

            // Read image file directly from the uploaded file
            byte[] imageData = null;
            if (imageFile != null && imageFile.exists()) {
                try {
                    imageData = Files.readAllBytes(imageFile.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            String sql = "INSERT INTO equipment (id, name, purchase_date, condition, status, category, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, nextId);
                stmt.setString(2, equipment.getName());
                stmt.setDate(3, java.sql.Date.valueOf(equipment.getPurchaseDate()));
                stmt.setString(4, equipment.getCondition());
                stmt.setString(5, "Available");
                stmt.setString(6, equipment.getCategory());
                
                if (imageData != null) {
                    stmt.setBytes(7, imageData);
                } else {
                    stmt.setNull(7, java.sql.Types.BINARY);
                }
                
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEquipment(Equipment equipment) {
        return equipmentDAO.updateEquipment(equipment);
    }

    public boolean deleteEquipment(int id) {
        return equipmentDAO.deleteEquipment(id);
    }

    public byte[] getEquipmentImage(int equipmentId) {
        return equipmentDAO.getEquipmentImage(equipmentId);
    }

    public void fixExistingEquipmentImages() {
        try (Connection conn = DB_Connection.getConnection()) {
            String sql = "SELECT id, name FROM equipment";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    
                    // Try to load image for this equipment
                    String cleanName = name.toLowerCase()
                        .replaceAll("\\s+", "-")
                        .replaceAll("[^a-z0-9-]", "")
                        + ".png";
                    
                    byte[] imageData = null;
                    try (InputStream imgStream = getClass().getResourceAsStream("/images_png/" + cleanName)) {
                        if (imgStream != null) {
                            imageData = imgStream.readAllBytes();
                            System.out.println("Found image for: " + name);
                        }
                    } catch (IOException e) {
                        System.err.println("Error loading image for " + name + ": " + e.getMessage());
                    }
                    
                    // Update the image in database
                    if (imageData != null) {
                        try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE equipment SET image = ? WHERE id = ?")) {
                            updateStmt.setBytes(1, imageData);
                            updateStmt.setInt(2, id);
                            updateStmt.executeUpdate();
                            System.out.println("Updated image for: " + name);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadImagesIntoDatabase() {
        try (Connection conn = DB_Connection.getConnection()) {
            String sql = "SELECT id, name FROM equipment";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    
                    // Try different name formats
                    String[] possibleNames = {
                        name + ".png",                          // Original name
                        name.replace(" ", "-") + ".png",        // Replace spaces with hyphens
                        name.replace("-", " ") + ".png",        // Replace hyphens with spaces
                        name.replace(" - ", "-") + ".png",      // Replace " - " with "-"
                        name.replace(" ", "") + ".png"          // Remove spaces completely
                    };
                    
                    InputStream imgStream = null;
                    
                    // Try each possible name silently
                    for (String fileName : possibleNames) {
                        imgStream = getClass().getResourceAsStream("/images_png/" + fileName);
                        if (imgStream != null) {
                            break;
                        }
                    }
                    
                    if (imgStream != null) {
                        try {
                            byte[] imageData = imgStream.readAllBytes();
                            try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE equipment SET image = ? WHERE id = ?")) {
                                updateStmt.setBytes(1, imageData);
                                updateStmt.setInt(2, id);
                                updateStmt.executeUpdate();
                            }
                        } finally {
                            imgStream.close();
                        }
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<Equipment> getAvailableEquipmentForStudent(String studentId) {
        return EquipmentDAO.getAvailableEquipmentForStudent(studentId);
    }
} 