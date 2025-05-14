package asm2_clone.controller;

import asm2_clone.db.StudentDAO;
import asm2_clone.model.Course;
import asm2_clone.model.Student;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class StudentDashboardController {
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label dobLabel;
    @FXML private Label studentIdLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private DatePicker dobPicker;
    @FXML private Button editProfileButton;
    @FXML private Button confirmButton;
    @FXML private Button backButton;
    @FXML private FlowPane courseTagPane;
    @FXML private StackPane mainContent;
    @FXML private VBox profileContent;
    @FXML private Label profileTab;
    @FXML private Label historyTab;
    @FXML private Label borrowTab;
    @FXML private Label returnTab;

    private String userId;
    private Student student;

    public void setUserId(String userId) {
        this.userId = userId;
        loadStudentProfile();
    }

    private void loadStudentProfile() {
        StudentDAO dao = new StudentDAO();
        student = dao.getStudentByUserId(userId);
        if (student != null) {
            fullNameLabel.setText(student.getFullName());
            emailLabel.setText(student.getContactInfo());
            dobLabel.setText(student.getDateOfBirth() != null ? student.getDateOfBirth().toString() : "");
            studentIdLabel.setText(student.getId());
            loadCourses(student.getEnrolledCourses());
        }
    }

    private void loadCourses(List<Course> courses) {
        courseTagPane.getChildren().clear();
        for (Course course : courses) {
            VBox courseBox = new VBox(3);
            courseBox.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #EEEEEE;" +
                "-fx-border-radius: 4;" +
                "-fx-padding: 12;" +
                "-fx-pref-width: 220;"
            );

            // Course ID
            Label courseIdLabel = new Label(course.getCourseId());
            courseIdLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

            // Course Name
            Label courseNameLabel = new Label(course.getCourseName());
            courseNameLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666666;");
            courseNameLabel.setWrapText(true);

            courseBox.getChildren().addAll(courseIdLabel, courseNameLabel);
            FlowPane.setMargin(courseBox, new Insets(0, 8, 8, 0));
            courseTagPane.getChildren().add(courseBox);
        }
    }

    private void setEditMode(boolean editing) {
        fullNameLabel.setVisible(!editing);
        emailLabel.setVisible(!editing);
        dobLabel.setVisible(!editing);

        fullNameField.setVisible(editing);
        emailField.setVisible(editing);
        dobPicker.setVisible(editing);

        confirmButton.setVisible(editing);
        editProfileButton.setVisible(!editing);

        if (editing) {
            fullNameField.setText(fullNameLabel.getText());
            emailField.setText(emailLabel.getText());
            if (!dobLabel.getText().isEmpty()) {
                dobPicker.setValue(LocalDate.parse(dobLabel.getText()));
            }
        }
    }

    @FXML
    private void onEdit() {
        setEditMode(true);
    }

    @FXML
    private void onConfirm() {
        student.setFullName(fullNameField.getText());
        student.setContactInfo(emailField.getText());
        student.setDateOfBirth(java.sql.Date.valueOf(dobPicker.getValue()));

        StudentDAO dao = new StudentDAO();
        if (dao.updateStudentInfo(student)) {
            fullNameLabel.setText(fullNameField.getText());
            emailLabel.setText(emailField.getText());
            dobLabel.setText(dobPicker.getValue().toString());
            setEditMode(false);
            
            // Show success notification
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            NotificationUtil.showSuccess(stage, "Profile updated successfully!");
        } else {
            // Show error notification
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            NotificationUtil.showError(stage, "Failed to update profile. Please try again.");
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
            e.printStackTrace();
        }
    }

    private void setActiveTab(Label active) {
        Label[] tabs = {profileTab, historyTab, borrowTab, returnTab};
        for (Label tab : tabs) {
            if (tab == active) {
                tab.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-underline: true; -fx-text-fill: #222; -fx-padding: 15 0; -fx-cursor: hand;");
            } else {
                tab.setStyle("-fx-font-size: 16px; -fx-font-weight: normal; -fx-underline: false; -fx-text-fill: #666; -fx-padding: 15 0; -fx-cursor: hand;");
            }
        }
    }

    @FXML
    public void onProfileTabClicked() {
        setActiveTab(profileTab);
        mainContent.getChildren().setAll(profileContent);
    }

    @FXML
    public void onBorrowingHistoryTabClicked() {
        setActiveTab(historyTab);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/student_borrowing_history.fxml"));
            Node historyNode = loader.load();
            asm2_clone.controller.StudentBorrowingHistoryController controller = loader.getController();
            controller.setStudentId(student.getId());
            mainContent.getChildren().setAll(historyNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onBorrowEquipmentTabClicked() {
        setActiveTab(borrowTab);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/student_borrow_equipment.fxml"));
            Node borrowNode = loader.load();
            StudentBorrowEquipmentController controller = loader.getController();
            controller.setStudent(student);
            mainContent.getChildren().setAll(borrowNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onReturnEquipmentTabClicked() {
        setActiveTab(returnTab);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/student_return_equipment.fxml"));
            Node returnNode = loader.load();
            asm2_clone.controller.StudentReturnEquipmentController controller = loader.getController();
            controller.setStudent(student);
            mainContent.getChildren().setAll(returnNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 