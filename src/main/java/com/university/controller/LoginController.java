package com.university.controller;

import com.university.service.LoginService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the login screen of the university equipment lending system.
 * @author GroupHDGs
 */
public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final LoginService loginService = new LoginService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            String role = loginService.authenticate(username, password);
            if (role != null) {
                errorLabel.setText("Login successful: " + role);
                // TODO: Load role-specific dashboard FXML
            } else {
                errorLabel.setText("Invalid username or password");
            }
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleVisitorLogin() {
        try {
            loginService.createVisitor();
            errorLabel.setText("Visitor login successful");
            // TODO: Load visitor dashboard FXML
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }
}