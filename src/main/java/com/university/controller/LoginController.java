package com.university.controller;

import com.university.model.Student;
import com.university.service.LoginService;
import com.university.model.Visitor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.logging.Logger;

public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;

    private final LoginService loginService = new LoginService();

    @FXML
    private void initialize() {
        LOGGER.info("Initializing LoginController");
        if (visiblePasswordField != null) {
            visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
            visiblePasswordField.setVisible(false);
        }
        usernameField.setOnAction(event -> handleLogin());
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        LOGGER.info("Login attempt initiated");
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        errorLabel.setText("");
        progressIndicator.setVisible(true);

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty");
            progressIndicator.setVisible(false);
            LOGGER.warning("Empty username or password");
            return;
        }
        if (username.length() < 3 || username.length() > 20) {
            errorLabel.setText("Username must be between 3 and 20 characters");
            progressIndicator.setVisible(false);
            LOGGER.warning("Invalid username length: " + username);
            return;
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            errorLabel.setText("Username can only contain letters, numbers, and underscores");
            progressIndicator.setVisible(false);
            LOGGER.warning("Invalid username format: " + username);
            return;
        }
        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters");
            progressIndicator.setVisible(false);
            LOGGER.warning("Password too short");
            return;
        }

        try {
            String role = loginService.authenticate(username, password);
            if (role != null) {
                errorLabel.setText("");
                loadDashboard(role);
                LOGGER.info("Login successful, loading dashboard for role: " + role);
            } else {
                errorLabel.setText("Invalid username or password");
                LOGGER.warning("Authentication failed for username: " + username);
            }
        } catch (IllegalArgumentException e) {
            errorLabel.setText("Invalid input: " + e.getMessage());
            LOGGER.warning("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Unexpected error: Please contact support");
            LOGGER.severe("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
        } finally {
            progressIndicator.setVisible(false);
        }
    }

    @FXML
    private void handleVisitorLogin() {
        LOGGER.info("Visitor login initiated");
        errorLabel.setText("");
        progressIndicator.setVisible(true);
        try {
            Visitor visitor = loginService.createVisitor();
            loadDashboard("VISITOR");
            LOGGER.info("Visitor login successful, ID: " + visitor.getId());
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            LOGGER.severe("Visitor login failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            progressIndicator.setVisible(false);
        }
    }

    @FXML
    private void handleForgotPassword() {
        LOGGER.info("Forgot password initiated");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/reset_password.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = new Stage();
            stage.setTitle("Reset Password");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error loading reset password screen");
            LOGGER.severe("Failed to load reset password screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        LOGGER.info("Toggling password visibility");
        if (showPasswordCheckBox.isSelected()) {
            visiblePasswordField.setVisible(true);
            passwordField.setVisible(false);
        } else {
            visiblePasswordField.setVisible(false);
            passwordField.setVisible(true);
        }
    }

    public void resetForm() {
        LOGGER.info("Resetting login form");
        usernameField.setText("");
        passwordField.setText("");
        if (visiblePasswordField != null) {
            visiblePasswordField.setText("");
        }
        errorLabel.setText("");
        if (showPasswordCheckBox != null) {
            showPasswordCheckBox.setSelected(false);
            passwordField.setVisible(true);
            if (visiblePasswordField != null) {
                visiblePasswordField.setVisible(false);
            }
        }
    }

    private void loadDashboard(String role) throws IOException {
        String fxmlPath;
        String title;
        switch (role) {
            case "STUDENT":
                fxmlPath = "/fxml/StudentDashboard.fxml";
                title = "Student Dashboard";
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
                Scene scene = new Scene(fxmlLoader.load(), 800, 600);
                Student student = (Student) loginService.findByUsername(usernameField.getText());
                if (student == null) {
                    throw new IllegalStateException("Student not found after authentication");
                }
                StudentController controller = fxmlLoader.getController();
                controller.setStudent(student);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setTitle(title);
                stage.setScene(scene);
                stage.show();
                break;
            case "ACADEMIC":
                fxmlPath = "/fxml/AcademicDashboard.fxml";
                title = "Academic Dashboard";
                break;
            case "PROFESSIONAL":
                fxmlPath = "/fxml/professional_dashboard.fxml";
                title = "Professional Dashboard";
                break;
            case "ADMINISTRATOR":
                fxmlPath = "/fxml/admin_dashboard.fxml";
                title = "Admin Dashboard";
                break;
            case "VISITOR":
                fxmlPath = "/fxml/visitor_dashboard.fxml";
                title = "Visitor Dashboard";
                break;
            default:
                errorLabel.setText("Unknown role: " + role);
                LOGGER.warning("Unknown role: " + role);
                return;
        }
        LOGGER.info("Loading dashboard: " + fxmlPath);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}