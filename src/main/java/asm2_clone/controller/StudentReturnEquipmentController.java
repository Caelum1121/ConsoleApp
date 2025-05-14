package asm2_clone.controller;

import asm2_clone.db.LendingRecordDAO;
import asm2_clone.db.CourseDAO;
import asm2_clone.model.Equipment;
import asm2_clone.model.LendingRecord;
import asm2_clone.model.Student;
import asm2_clone.model.Course;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import asm2_clone.controller.NotificationUtil;

import java.util.List;

public class StudentReturnEquipmentController {
    @FXML private VBox equipmentContainer;
    private Student student;

    public void setStudent(Student student) {
        this.student = student;
        loadBorrowedEquipment();
    }

    @FXML
    public void initialize() {
        // Remove backButton logic from initialize
    }

    private void loadBorrowedEquipment() {
        equipmentContainer.getChildren().clear();
        // Each LendingRecord from DAO contains only one equipment for the return page
        List<LendingRecord> borrowed = LendingRecordDAO.getBorrowedOrOverdueEquipmentByStudentId(student.getId());
        CourseDAO courseDAO = new CourseDAO();
        for (LendingRecord record : borrowed) {
            Equipment eq = record.getEquipment(); // Only one equipment per record
            Course course = courseDAO.getCourseByEquipmentId(eq.getId());
            equipmentContainer.getChildren().add(createEquipmentCard(record, eq, course));
        }
        if (borrowed.isEmpty()) {
            Label none = new Label("No equipment to return.");
            none.setStyle("-fx-text-fill: #888; -fx-font-size: 15px; -fx-padding: 20 0 0 0;");
            equipmentContainer.getChildren().add(none);
        }
    }

    private VBox createEquipmentCard(LendingRecord record, Equipment eq, Course course) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-padding: 18;");
        Label name = new Label(eq.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #222;");
        Label recordId = new Label("Record ID: " + record.getRecordId());
        Label category = new Label("Category: " + (eq.getCategory() != null ? eq.getCategory() : "-"));
        Label courseLabel = new Label("Course: " + (course != null ? course.getCourseName() : "-"));
        Label borrowDate = new Label("Borrow Date: " + (record.getBorrowDate() != null ? record.getBorrowDate().toString() : "-"));
        Label needToReturn = new Label("Need to Return By: " + (record.getNeedToReturnDate() != null ? record.getNeedToReturnDate().toString() : "-"));
        // Set 'Need to Return By' text color to red if overdue
        String eqStatus = record.getEquipmentStatuses() != null ? record.getEquipmentStatuses().get(eq.getId()) : null;
        if (eqStatus != null && eqStatus.equalsIgnoreCase("overdue")) {
            needToReturn.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
        } else {
            needToReturn.setStyle("-fx-text-fill: #222; -fx-font-size: 13px;");
        }

        VBox info = new VBox(name, recordId, category, courseLabel, borrowDate, needToReturn);
        info.setSpacing(2);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button returnBtn = new Button("Return Equipment");
        returnBtn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 20 6 20; -fx-background-radius: 8;");
        returnBtn.setOnAction(e -> {
            boolean ok = LendingRecordDAO.returnEquipment(record.getRecordId(), eq.getId());
            if (ok) {
                Stage stage = (Stage) equipmentContainer.getScene().getWindow();
                NotificationUtil.showSuccess(stage, "Equipment returned successfully!");
                loadBorrowedEquipment();
            }
        });

        HBox row = new HBox(info, spacer, returnBtn);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.getChildren().add(row);
        return card;
    }

    @FXML
    private void onProfileTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/student_dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            StudentDashboardController controller = loader.getController();
            controller.setUserId(student.getId());
            Stage stage = (Stage) equipmentContainer.getScene().getWindow();
            Scene dashboardScene = new Scene(dashboardRoot, 1000, 700);
            stage.setTitle("UNI LEND - Student Dashboard");
            stage.setScene(dashboardScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBorrowingHistoryTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/student_borrowing_history.fxml"));
            Parent historyRoot = loader.load();
            StudentBorrowingHistoryController controller = loader.getController();
            controller.setStudentId(student.getId());
            Stage stage = (Stage) equipmentContainer.getScene().getWindow();
            Scene historyScene = new Scene(historyRoot, 1000, 700);
            stage.setTitle("UNI LEND - Borrowing History");
            stage.setScene(historyScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBorrowEquipmentTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/student_borrow_equipment.fxml"));
            Parent borrowRoot = loader.load();
            StudentBorrowEquipmentController controller = loader.getController();
            controller.setStudent(student);
            Stage stage = (Stage) equipmentContainer.getScene().getWindow();
            Scene borrowScene = new Scene(borrowRoot, 1000, 700);
            stage.setTitle("UNI LEND - Borrow Equipment");
            stage.setScene(borrowScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 