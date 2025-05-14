package asm2_clone.controller;

import asm2_clone.db.LendingRecordDAO;
import asm2_clone.model.LendingRecord;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.Node;

public class StudentBorrowingHistoryController {
    @FXML private VBox historyContainer;
    @FXML private Button backButton;
    @FXML private ComboBox<String> statusFilter;
    private String studentId;
    private List<LendingRecord> allRecords = null;

    public void setStudentId(String studentId) {
        System.out.println("Setting student ID: " + studentId); // Debug log
        this.studentId = studentId;
        loadBorrowingHistory();
    }

    @FXML
    private void initialize() {
        if (statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected"));
            statusFilter.setValue("All");
            statusFilter.setOnAction(e -> updateDisplay());
        }
    }

    private void loadBorrowingHistory() {
        if (studentId == null || studentId.isEmpty()) {
            System.err.println("Student ID is null or empty");
            return;
        }
        System.out.println("[DEBUG] Student ID: " + studentId);
        List<LendingRecord> records = LendingRecordDAO.getRecordsByStudentId(studentId);
        System.out.println("[DEBUG] Records found: " + records.size());
        for (LendingRecord r : records) {
            System.out.println("[DEBUG] Record approval status: " + (r.getApprovalStatus() != null ? r.getApprovalStatus().name() : "null"));
        }
        this.allRecords = records;
        updateDisplay();
    }

    private void updateDisplay() {
        if (allRecords == null) return;
        historyContainer.getChildren().clear();
        String selectedStatus = statusFilter != null ? statusFilter.getValue() : "All";
        boolean filterAll = selectedStatus.equals("All");
        // Map filter value to enum name
        String filterEnum = selectedStatus.equals("All") ? null : selectedStatus.toUpperCase();
        for (LendingRecord record : allRecords) {
            String approval = record.getApprovalStatus() != null ? record.getApprovalStatus().name() : "";
            if (!filterAll && !approval.equalsIgnoreCase(filterEnum)) continue;
            if (record.getEquipmentList() != null && !record.getEquipmentList().isEmpty()) {
                for (int i = 0; i < record.getEquipmentList().size(); i++) {
                    VBox row = createHistoryRow(record, i);
                    historyContainer.getChildren().add(row);
                }
            } else {
                VBox row = createHistoryRow(record, -1);
                historyContainer.getChildren().add(row);
            }
        }
        if (historyContainer.getChildren().isEmpty()) {
            Label noRecordsLabel = new Label("No borrowing history found");
            noRecordsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
            historyContainer.getChildren().add(noRecordsLabel);
        }
    }

    private VBox createHistoryRow(LendingRecord record, int equipmentIdx) {
        // Card container
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Equipment Name (big, bold)
        String equipmentName = "-";
        String returnDate = record.getReturnDate() != null ? record.getReturnDate().toString() : "-";
        if (equipmentIdx >= 0 && record.getEquipmentList() != null && equipmentIdx < record.getEquipmentList().size()) {
            equipmentName = record.getEquipmentList().get(equipmentIdx).getName();
            if (record.getEquipmentReturnDates() != null &&
                    record.getEquipmentReturnDates().containsKey(record.getEquipmentList().get(equipmentIdx).getId())) {
                returnDate = record.getEquipmentReturnDates().get(record.getEquipmentList().get(equipmentIdx).getId()) != null
                        ? record.getEquipmentReturnDates().get(record.getEquipmentList().get(equipmentIdx).getId()).toString()
                        : "-";
            }
        } else if (record.getEquipment() != null) {
            equipmentName = record.getEquipment().getName();
        }

        Label nameLabel = new Label(equipmentName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222;");

        // Borrow Date + Approval Status
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label borrowDateLabel = new Label("Borrow Date: " + (record.getBorrowDate() != null ? record.getBorrowDate().toString() : "-"));
        borrowDateLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        // Approval status label
        String approvalStatus = (record.getApprovalStatus() != null)
                ? record.getApprovalStatus().name()
                : "PENDING";
        Label statusLabel = new Label(approvalStatus.charAt(0) + approvalStatus.substring(1).toLowerCase());
        statusLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 0 10 0 0;");

        switch (approvalStatus.toUpperCase()) {
            case "APPROVED" -> statusLabel.setStyle(statusLabel.getStyle() + "-fx-text-fill: #2ecc71;"); // green
            case "REJECTED" -> statusLabel.setStyle(statusLabel.getStyle() + "-fx-text-fill: #e74c3c;"); // red
            case "PENDING"  -> statusLabel.setStyle(statusLabel.getStyle() + "-fx-text-fill: #f1c40f;"); // yellow
            default         -> statusLabel.setStyle(statusLabel.getStyle() + "-fx-text-fill: #888;");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox statusBox = new VBox(2);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        statusBox.getChildren().add(statusLabel);

        topRow.getChildren().addAll(borrowDateLabel, spacer, statusBox);

        // Left column info
        VBox leftCol = new VBox(2);
        leftCol.getChildren().add(new Label("Record ID: " + record.getRecordId()));
        leftCol.getChildren().add(new Label("Purpose: " + (record.getPurpose() != null ? record.getPurpose() : "-")));
        leftCol.getChildren().add(new Label("Return at: " + (returnDate != null ? returnDate : "-")));
        leftCol.getChildren().forEach(node -> ((Label)node).setStyle("-fx-text-fill: #222; -fx-font-size: 13px;"));

        // ➕ Add Equipment Status (Borrowed / Overdue / Returned)
        String equipmentStatusDisplay = "-";
        if (equipmentIdx >= 0 && record.getEquipmentList() != null && equipmentIdx < record.getEquipmentList().size()) {
            int equipmentId = record.getEquipmentList().get(equipmentIdx).getId();
            if (record.getEquipmentStatuses() != null && record.getEquipmentStatuses().containsKey(equipmentId)) {
                equipmentStatusDisplay = record.getEquipmentStatuses().get(equipmentId);
            }
        } else if (record.getStatus() != null) {
            equipmentStatusDisplay = record.getStatus().name();
        }

        Label eqStatusLabel = new Label("Status: " + equipmentStatusDisplay);
        eqStatusLabel.setStyle("-fx-text-fill: #222; -fx-font-size: 13px;");
        leftCol.getChildren().add(eqStatusLabel);

        // 通用樣式設置
        leftCol.getChildren().forEach(node -> ((Label)node).setStyle("-fx-text-fill: #222; -fx-font-size: 13px;"));

        // Final row
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(leftCol);

        card.getChildren().addAll(nameLabel, topRow, row);
        return card;
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/student_dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            StudentDashboardController controller = loader.getController();
            controller.setUserId(studentId);
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene dashboardScene = new Scene(dashboardRoot, 1000, 700);
            stage.setTitle("UNI LEND - Student Dashboard");
            stage.setScene(dashboardScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onProfileTabClicked() { onBack(); }

    @FXML
    private void onBorrowEquipmentTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/student_borrow_equipment.fxml"));
            Node borrowNode = loader.load();
            StudentBorrowEquipmentController controller = loader.getController();
            // Pass the student object if available, otherwise just studentId
            if (studentId != null && !studentId.isEmpty()) {
                // If you have a Student object, pass it; otherwise, you may need to fetch it
                // For now, just setStudentId if that's what your controller expects
                // controller.setStudentId(studentId); // Uncomment if needed
            }
            // Find the main content area (StackPane) in the dashboard
            Stage stage = (Stage) historyContainer.getScene().getWindow();
            Scene scene = stage.getScene();
            Node mainContent = scene.lookup("#mainContent");
            if (mainContent instanceof javafx.scene.layout.StackPane stackPane) {
                stackPane.getChildren().setAll(borrowNode);
            } else {
                // fallback: replace the whole scene
                stage.setScene(new Scene((Parent) borrowNode, 1000, 700));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onReturnEquipmentTabClicked() {
        // TODO: Implement navigation to return equipment page
    }
}