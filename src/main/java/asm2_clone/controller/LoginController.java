package asm2_clone.controller;

import asm2_clone.db.DB_Connection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Button loginButton;
    @FXML private Button visitorButton;
    @FXML private Label errorLabel;

    @FXML
    private void onLogin() {
        System.out.println("onLogin() called"); // Debug print
        String username = usernameField.getText().trim();
        String password = showPasswordCheckBox.isSelected()
            ? passwordVisibleField.getText().trim()
            : passwordField.getText().trim();
        // Simple admin check (replace with DB check in production)
        if ("admin".equals(username) && "admin".equals(password)) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard.fxml"));
                Parent adminRoot = loader.load();
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene adminScene = new Scene(adminRoot, 1000, 700);
                stage.setTitle("UNI LEND - Admin Dashboard");
                stage.setScene(adminScene);
                stage.show();
                System.out.println("Logged in as admin, switching to admin dashboard...");
                return;
            } catch (Exception e) {
                System.err.println("Error loading admin dashboard: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        // Student check (from user table, role = 'student')
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM \"user\" WHERE username = ? AND password = ? AND role = 'student'");) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String userId = rs.getString("id");
                errorLabel.setVisible(false);
                errorLabel.setText("");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/student_dashboard.fxml"));
                Parent studentRoot = loader.load();
                // Pass userId to the controller
                StudentDashboardController controller = loader.getController();
                controller.setUserId(userId);
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene studentScene = new Scene(studentRoot, 1000, 700);
                stage.setTitle("UNI LEND - Student Dashboard");
                stage.setScene(studentScene);
                stage.show();
                System.out.println("Logged in as student, switching to student dashboard...");
                return;
            }
        } catch (Exception e) {
            System.err.println("Error checking student login: " + e.getMessage());
            e.printStackTrace();
        }
        // 3. Professional staff check
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id FROM \"user\" WHERE username = ? AND password = ? AND role = 'professional'")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String professionalId = rs.getString("id");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/professional_dashboard.fxml"));
                Parent proRoot = loader.load();

                ProfessionalDashboardController controller = loader.getController();
                controller.setProfessionalId(professionalId);

                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene proScene = new Scene(proRoot, 1000, 700);
                stage.setTitle("UNI LEND - Professional Dashboard");
                stage.setScene(proScene);
                stage.show();

                System.out.println("Logged in as professional staff, switching to professional dashboard...");
                return;
            }
        } catch (Exception e) {
            System.err.println("Error checking professional login: " + e.getMessage());
            e.printStackTrace();
        }
        // 4. Academic staff check
        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id FROM \"user\" WHERE username = ? AND password = ? AND role = 'academic'")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String academicId = rs.getString("id");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/academic_dashboard.fxml"));
                Parent acaRoot = loader.load();

                AcademicDashboardController controller = loader.getController();
                controller.setAcademicId(academicId);

                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene acaScene = new Scene(acaRoot, 1000, 700);
                stage.setTitle("UNI LEND - Academic Dashboard");
                stage.setScene(acaScene);
                stage.show();

                System.out.println("Logged in as academic staff, switching to academic dashboard...");
                return;
            }
        } catch (Exception e) {
            System.err.println("Error checking professional login: " + e.getMessage());
            e.printStackTrace();
        }
        // Show error if login fails
        errorLabel.setText("Invalid username or password. Please try again.");
        errorLabel.setVisible(true);
        System.out.println("Login clicked: " + username);
    }

    @FXML
    private void onVisitor() {
        try {
            // Load the visitor FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/visitor.fxml"));
            Parent visitorRoot = loader.load();

            // Get the current stage from any of the current scene's components
            Stage stage = (Stage) visitorButton.getScene().getWindow();
            
            // Create and set new scene with the same size as admin dashboard
            Scene visitorScene = new Scene(visitorRoot, 1000, 700);
            stage.setTitle("Equipment List - Visitor View");
            stage.setScene(visitorScene);
            stage.show();
            
            System.out.println("Switching to visitor view...");
        } catch (IOException e) {
            System.err.println("Error loading visitor view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Sync password fields
        passwordVisibleField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordVisibleField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());
        passwordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());

        // Keep values in sync
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }
}

