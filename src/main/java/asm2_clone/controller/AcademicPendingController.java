package asm2_clone.controller;

import asm2_clone.model.*;
import asm2_clone.db.LendingRecordDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Calendar;
import java.util.List;

public class AcademicPendingController {
    private AcademicStaff academic;
    private List<LendingRecord> records;

    @FXML private VBox cardContainer;
    @FXML private Button backButton;
    @FXML private AnchorPane loadingOverlay;

    public void setAcademic(AcademicStaff academic) {
        this.academic = academic;
        this.records = LendingRecordDAO.getPendingRecordsByAcademicId(academic.getId());
        updateDisplay();
    }

    private void showLoading() {
        Platform.runLater(() -> loadingOverlay.setVisible(true));
    }

    private void hideLoading() {
        Platform.runLater(() -> loadingOverlay.setVisible(false));
    }

    public void initialize() {}

    private void updateDisplay() {
        cardContainer.getChildren().clear();

        if (records == null || records.isEmpty()) {
            Label empty = new Label("No pending requests.");
            empty.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");
            cardContainer.getChildren().add(empty);
            return;
        }

        // ➤ 表頭
        HBox header = new HBox(20);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #f8f8f8;");
        header.getChildren().addAll(
                createBoldLabel("Student",150),
                createBoldLabel("Course", 200),
                createBoldLabel("Item", 120),
                createBoldLabel("Date", 120),
                createBoldLabel("Status", 130),
                createBoldLabel("Actions", 190)
        );
        cardContainer.getChildren().add(header);

        for (LendingRecord record : records) {
            HBox row = new HBox(20);
            row.setPadding(new Insets(14));
            row.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-border-color: #ddd;
            -fx-border-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0.1, 0, 1);
        """);

            Label student = createStyledCell(record.getBorrowerName());
            Label course = createStyledCell(
                    record.getCourse() != null ? record.getCourse().getCourseName() : "N/A");
            Label items = createStyledCell(String.join(", ", record.getEquipmentNames()));
            Label date = createStyledCell(record.getBorrowDate().toString());
            Label status = createStyledStatusLabel(record.getApprovalStatus());

            // 讓每個欄位自動撐開
            HBox.setHgrow(student, Priority.ALWAYS);
            HBox.setHgrow(course, Priority.ALWAYS);
            HBox.setHgrow(items, Priority.ALWAYS);
            HBox.setHgrow(date, Priority.ALWAYS);
            HBox.setHgrow(status, Priority.ALWAYS);

            Button approve = new Button("Approve");
            approve.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 14;");
            approve.setOnAction(e -> {
                showLoading();
                new Thread(() -> {
                    reviewLendingRequest(record, true);
                    records.remove(record);
                    Platform.runLater(() -> {
                        updateDisplay();
                        hideLoading();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText(null);
                        alert.setContentText("Approved successfully!");
                        alert.showAndWait();
                    });
                }).start();
            });

            Button decline = new Button("Decline");
            decline.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 14;");
            decline.setOnAction(e -> {
                showLoading();
                new Thread(() -> {
                    reviewLendingRequest(record, false);
                    records.remove(record);
                    Platform.runLater(() -> {
                        updateDisplay();
                        hideLoading();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText(null);
                        alert.setContentText("Declined successfully.");
                        alert.showAndWait();
                    });
                }).start();
            });

            HBox actionBox = new HBox(10, approve, decline);
            actionBox.setMinWidth(180);
            actionBox.setStyle("-fx-alignment: CENTER_RIGHT;");

            row.getChildren().addAll(student, course, items, date, status, actionBox);
            cardContainer.getChildren().add(row);
        }
    }

    private Label createBoldLabel(String text, double width) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #333; -fx-font-size: 14px;");
        label.setPrefWidth(width);
        return label;
    }

    private Label createStyledCell(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(false);
        label.setStyle("-fx-text-fill: #333; -fx-font-size: 13px;");
        return label;
    }

    private Label createStyledStatusLabel(LendingRecord.ApprovalStatus status) {
        Label label = new Label(status.name());
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        switch (status) {
            case APPROVED -> label.setStyle(label.getStyle() + "-fx-text-fill: green;");
            case REJECTED -> label.setStyle(label.getStyle() + "-fx-text-fill: red;");
            default -> label.setStyle(label.getStyle() + "-fx-text-fill: #999;");
        }
        return label;
    }



    public boolean canAcademicApprove(LendingRecord record, AcademicStaff academic) {
        if (record == null || academic == null) return false;
        Course course = record.getCourse();
        if (course == null) return false;

        boolean isSupervising = academic.getId().equals(course.getAcademicStaff().getId());
        boolean isStudentEnrolled = course.getEnrolledStudents().stream()
                .anyMatch(s -> s.getId().equals(record.getBorrowerId()));

        return isSupervising && isStudentEnrolled;
    }

    public void reviewLendingRequest(LendingRecord record, boolean approve) {
        if (approve) {
            record.setApprovalStatus(LendingRecord.ApprovalStatus.APPROVED);
            record.setStatus(LendingRecord.Status.BORROWED);

            if (record.getEquipmentList() != null) {
                for (Equipment eq : record.getEquipmentList()) {
                    eq.setStatus("Borrowed");
                }
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(record.getBorrowDate());
            cal.add(Calendar.DAY_OF_YEAR, 14);
            record.setNeedToReturnDate(cal.getTime());
        } else {
            record.setApprovalStatus(LendingRecord.ApprovalStatus.REJECTED);
            record.setStatus(null);
        }

        LendingRecordDAO.updateRecord(record);
    }

    @FXML
    private void onEquipmentTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/academic_equipment.fxml"));
            Parent equipmentRoot = loader.load();
            AcademicEquipmentController controller = loader.getController();
            controller.setAcademic(academic);
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene equipmentScene = new Scene(equipmentRoot, 1000, 700);
            stage.setTitle("UNI LEND - Equipment Dashboard");
            stage.setScene(equipmentScene);
        } catch (Exception e) {
            System.err.println("Error loading equipment dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onUsersTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/academic_dashboard.fxml"));
            Parent userRoot = loader.load();
            AcademicDashboardController controller = loader.getController();
            controller.setAcademicId(academic.getId());

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene userScene = new Scene(userRoot, 1000, 700);
            stage.setTitle("UNI LEND - Professional Dashboard");
            stage.setScene(userScene);
        } catch (Exception e) {
            System.err.println("Error loading user dashboard: " + e.getMessage());
            e.printStackTrace();
        }
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
            Parent loginRoot = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(loginRoot, 800, 600));
            stage.setTitle("UNI LEND - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
