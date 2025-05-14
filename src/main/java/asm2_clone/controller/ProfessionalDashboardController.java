package asm2_clone.controller;

import asm2_clone.db.ProfessionalDAO;
import asm2_clone.model.ProfessionalStaff;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProfessionalDashboardController {
    @FXML private Label fullNameLabel, emailLabel, departmentLabel, dobLabel, staffIdLabel;
    @FXML private TextField fullNameField, emailField, departmentField;
    @FXML private DatePicker dobPicker;
    @FXML private Button editProfileButton, confirmButton, backButton;

    private ProfessionalStaff professional;
    private String professionalId;

    public void setProfessionalId(String professionalId) {
        this.professionalId = professionalId;
        loadProfessionalProfile();
    }

    public void loadProfessionalProfile() {
        ProfessionalDAO dao = new ProfessionalDAO();
        professional = dao.getProfessionalByUserId(professionalId);
        if (professional != null) {
            fullNameLabel.setText(professional.getFullName());
            emailLabel.setText(professional.getContactInfo());
            dobLabel.setText(professional.getDateOfBirth() != null ? professional.getDateOfBirth().toString() : "");
            staffIdLabel.setText(professional.getId());
            departmentLabel.setText(professional.getDepartment());

        }
    }

    private void setEditMode(boolean editing) {
        fullNameLabel.setVisible(!editing);
        emailLabel.setVisible(!editing);
        departmentLabel.setVisible(!editing);
        dobLabel.setVisible(!editing);

        fullNameField.setVisible(editing);
        emailField.setVisible(editing);
        departmentField.setVisible(editing);
        dobPicker.setVisible(editing);
        dobPicker.setDisable(!editing);

        confirmButton.setVisible(editing);
        editProfileButton.setVisible(!editing);

        if (editing) {
            dobPicker.toFront();
            fullNameField.setText(fullNameLabel.getText());
            emailField.setText(emailLabel.getText());
            departmentField.setText(departmentLabel.getText());
            if (!dobLabel.getText().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    dobPicker.setValue(LocalDate.parse(dobLabel.getText(), formatter));
                } catch (Exception e) {
                    System.err.println("Date parse failed: " + dobLabel.getText());
                }
            }
        }
    }

    @FXML
    private void onEdit() {
        System.out.println("Edit clicked");
        setEditMode(true);
    }

    @FXML
    private void onConfirm() {
        LocalDate localDate = dobPicker.getValue();
        System.out.println("dobPicker value = " + localDate);
        java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
        System.out.println("Date updating to DB: " + sqlDate);

        // Persist to DB
        ProfessionalDAO dao = new ProfessionalDAO();
        professional = dao.getProfessionalByUserId(professionalId);
        System.out.println("Updating ID: " + professional.getId());

        professional.setFullName(fullNameField.getText());
        professional.setContactInfo(emailField.getText());
        professional.setDepartment(departmentField.getText());
        professional.setDateOfBirth(sqlDate);

        boolean success = dao.updateProfessionalInfo(professional);

        if (success) {
            fullNameLabel.setText(fullNameField.getText());
            emailLabel.setText(emailField.getText());
            departmentLabel.setText(departmentField.getText());
            dobLabel.setText(localDate.toString());
            setEditMode(false);
        } else {
            System.err.println("Failed to update info.");
        }
    }

    @FXML
    private void onEquipmentTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/professional_dashboard_equipment.fxml"));
            Parent equipmentRoot = loader.load();
            ProfessionalDashboardEquipmentController controller = loader.getController();
            controller.setProfessional(professional);
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene equipmentScene = new Scene(equipmentRoot, 1000, 700);
            stage.setTitle("UNI LEND - Equipment Borrowing");
            stage.setScene(equipmentScene);
        } catch (Exception e) {
            System.err.println("Error loading equipment borrowing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onHistoryTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/professional_dashboard_borrowing.fxml"));
            Parent equipmentRoot = loader.load();
            ProfessionalDashboardBorrowingController controller = loader.getController();
            controller.setProfessional(professional);
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene equipmentScene = new Scene(equipmentRoot, 1000, 700);
            stage.setTitle("UNI LEND - Borrowing History");
            stage.setScene(equipmentScene);
        } catch (Exception e) {
            System.err.println("Error loading equipment borrowing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/login.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene loginScene = new Scene(loginRoot, 800, 600);
            stage.setTitle("UNI LEND - Login");
            stage.setScene(loginScene);
        } catch (Exception e) {
            System.err.println("Error returning to login view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
