package asm2_clone.controller;

import asm2_clone.db.AcademicDAO;
import asm2_clone.db.CourseDAO;
import asm2_clone.model.AcademicStaff;
import asm2_clone.model.Course;

import asm2_clone.service.LendingRecordService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AcademicDashboardController {
    @FXML public Label equipmentTab;
    @FXML private Label fullNameLabel, emailLabel, dobLabel, staffIdLabel;
    @FXML private TextField fullNameField, emailField;
    @FXML private DatePicker dobPicker;
    @FXML private Button editProfileButton, confirmButton, backButton;
    @FXML private FlowPane courseTagPane;
    @FXML private VBox courseContainer;

    private AcademicStaff academic;
    private String academicId;

    // Called externally to set the user context
    public void setAcademicId(String academicId) {
        System.out.println("Academic ID received: " + academicId);
        this.academicId = academicId;
        loadAcademicProfile();
    }

    private void loadAcademicProfile() {
        AcademicDAO dao = new AcademicDAO();
        academic = dao.getAcademicByUserId(academicId);
        System.out.println("Fetched academic: " + academic);
        if (academic != null) {
            fullNameLabel.setText(academic.getFullName());
            emailLabel.setText(academic.getContactInfo());
            dobLabel.setText(academic.getDateOfBirth() != null ? academic.getDateOfBirth().toString() : "");
            staffIdLabel.setText(academic.getId());
            System.out.println("➡️ Courses to load: " + academic.getCoursesTaught().size());
            loadCourses(academic.getCoursesTaught());
            System.out.println("fullNameLabel null? " + (fullNameLabel == null));
            System.out.println("emailLabel null? " + (emailLabel == null));
            System.out.println("dobLabel null? " + (dobLabel == null));
        } else {
            System.err.println("Academic profile not found for ID: " + academicId);
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
                            "-fx-pref-width: 220;" +
                            "-fx-cursor: hand;" + // 鼠標手勢提示可點擊
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);"
            );

            Label courseIdLabel = new Label(course.getCourseId());
            courseIdLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

            Label courseNameLabel = new Label(course.getCourseName());
            courseNameLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666666;");
            courseNameLabel.setWrapText(true);

            courseBox.getChildren().addAll(courseIdLabel, courseNameLabel);

            courseBox.setOnMouseClicked(e -> {
                String courseId = course.getCourseId().toUpperCase();
                Map<String, List<String>> map = new CourseDAO().getStudentsInCourses();

                System.out.println("🟡 Clicked course ID: " + courseId);
                System.out.println("🟢 Available course IDs: " + map.keySet());
                System.out.println("🔵 Students found: " + map.get(courseId));

                List<String> students = map.get(courseId);
                showStudentsPopup(course.getCourseName(), students);
            });

            FlowPane.setMargin(courseBox, new Insets(0, 8, 8, 0));
            courseTagPane.getChildren().add(courseBox);
        }
    }


    private void showStudentsInCourse(String courseName, List<String> students) {
        VBox container = new VBox();
        container.setSpacing(8);

        Label header = new Label("Students in " + courseName);
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        container.getChildren().add(header);

        if (students != null && !students.isEmpty()) {
            for (String name : students) {
                Label studentLabel = new Label(name);
                studentLabel.setStyle("-fx-background-color: #f2f2f2; -fx-padding: 6 12; -fx-background-radius: 6;");
                container.getChildren().add(studentLabel);
            }
        } else {
            Label emptyLabel = new Label("No students enrolled.");
            container.getChildren().add(emptyLabel);
        }

        courseContainer.getChildren().clear();
        courseContainer.getChildren().add(container);
    }

    private void setEditMode(boolean editing) {
        fullNameLabel.setVisible(!editing);
        emailLabel.setVisible(!editing);
        dobLabel.setVisible(!editing);

        fullNameField.setVisible(editing);
        emailField.setVisible(editing);
        dobPicker.setVisible(editing);
        dobPicker.setDisable(!editing);

        confirmButton.setVisible(editing);
        editProfileButton.setVisible(!editing);

        if (editing) {
            dobPicker.toFront();
            fullNameField.setText(fullNameLabel.getText());
            emailField.setText(emailLabel.getText());
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
        setEditMode(true);
    }

    @FXML
    private void onConfirm() {
        LocalDate localDate = dobPicker.getValue();
        java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);

        AcademicDAO dao = new AcademicDAO();
        academic = dao.getAcademicByUserId(academicId);

        academic.setFullName(fullNameField.getText());
        academic.setContactInfo(emailField.getText());
        academic.setDateOfBirth(sqlDate);

        boolean success = dao.updateAcademicInfo(academic);

        if (success) {
            fullNameLabel.setText(fullNameField.getText());
            emailLabel.setText(emailField.getText());
            dobLabel.setText(localDate.toString());
            setEditMode(false);
        } else {
            System.err.println("Failed to update academic info.");
        }
    }

    @FXML
    private void onEquipmentTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/academic_equipment.fxml"));
            Parent equipmentRoot = loader.load();
            AcademicEquipmentController controller = loader.getController();
            controller.setAcademic(academic);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(equipmentRoot, 1000, 700));
        } catch (Exception e) {
            System.err.println("Error loading equipment dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void onHistoryTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/academic_borrowing.fxml"));
            Parent root = loader.load();
            AcademicBorrowingController controller = loader.getController();
            controller.setAcademic(academic);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            System.err.println("Error loading borrowing dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            System.err.println("Error returning to login: " + e.getMessage());
        }
    }

    private void showStudentsPopup(String courseName, List<String> students) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Enrolled Students - " + courseName);

        VBox container = new VBox(15);
        container.setStyle("-fx-padding: 20; -fx-background-color: #fdfdfd; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label title = new Label("Students in “" + courseName + "”");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox listBox = new VBox(8);
        listBox.setStyle("-fx-padding: 10;");

        if (students == null || students.isEmpty()) {
            Label none = new Label("No students are currently enrolled in this course.");
            none.setStyle("-fx-font-style: italic; -fx-text-fill: #777;");
            listBox.getChildren().add(none);
        } else {
            for (String student : students) {
                Label studentLabel = new Label("• " + student);
                studentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #222;");
                listBox.getChildren().add(studentLabel);
            }
        }

        scrollPane.setContent(listBox);
        container.getChildren().addAll(title, scrollPane);

        Scene scene = new Scene(container, 400, 350);
        popupStage.setScene(scene);
        popupStage.show();
    }

    @FXML
    private void onStatisticsTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/academic_statistics.fxml"));
            Parent statsRoot = loader.load();
            AcademicStatisticsController controller = loader.getController();
            controller.setAcademicId(academic.getId());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(statsRoot, 1000, 700));
            stage.setTitle("UNI LEND - Statistics");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onPendingTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/academic_pending.fxml"));
            Parent statsRoot = loader.load();
            AcademicPendingController controller = loader.getController();
            controller.setAcademic(academic);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(statsRoot, 1000, 700));
            stage.setTitle("UNI LEND - Pending Requests");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
