package com.university.controller;

import com.university.model.*;
import com.university.service.StudentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StudentController {
    @FXML
    private TableView<LendingRecord> lendingTable;
    @FXML
    private TableColumn<LendingRecord, String> recordIdColumn;
    @FXML
    private TableColumn<LendingRecord, String> equipmentColumn;
    @FXML
    private TableColumn<LendingRecord, String> borrowDateColumn;
    @FXML
    private TableColumn<LendingRecord, String> dueDateColumn;
    @FXML
    private TableColumn<LendingRecord, String> statusColumn;
    @FXML
    private TableColumn<LendingRecord, String> purposeColumn;
    @FXML
    private TableColumn<LendingRecord, String> approvedColumn;

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Equipment> equipmentTable;
    @FXML
    private TableColumn<Equipment, String> equipmentIdColumn;
    @FXML
    private TableColumn<Equipment, String> equipmentNameColumn;
    @FXML
    private TableColumn<Equipment, String> conditionColumn;

    @FXML
    private ComboBox<Equipment> equipmentComboBox;
    @FXML
    private ComboBox<Course> courseComboBox;
    @FXML
    private TextArea purposeField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private Label messageLabel;

    private StudentService studentService;
    private Student currentStudent;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public void setStudent(Student student) {
        this.currentStudent = student;
        this.studentService = new StudentService();
        initialize();
    }

    @FXML
    private void initialize() {
        if (currentStudent == null) return;

        // Initialize lending history table
        recordIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRecordId()));
        equipmentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEquipment().stream()
                        .map(Equipment::getName)
                        .collect(Collectors.joining(", "))));
        borrowDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                dateFormat.format(cellData.getValue().getBorrowDate())));
        dueDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                dateFormat.format(cellData.getValue().getDueDate())));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus().toString()));
        purposeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPurpose()));
        approvedColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getApproved() != null ? cellData.getValue().getApproved().toString() : "Pending"));
        loadLendingHistory();

        // Initialize equipment table
        equipmentIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEquipmentId()));
        equipmentNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        conditionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCondition().toString()));

        // Initialize combo boxes
        courseComboBox.setItems(FXCollections.observableArrayList(currentStudent.getEnrolledCourses()));
        loadAvailableEquipment();

        // Initialize personal info
        Person person = currentStudent.getPersonDetails();
        if (person != null) {
            phoneField.setText(person.getPhoneNumber());
            emailField.setText(person.getEmail());
        }
    }

    private void loadLendingHistory() {
        List<LendingRecord> records = studentService.getLendingHistory(currentStudent);
        lendingTable.setItems(FXCollections.observableArrayList(records));
    }

    private void loadAvailableEquipment() {
        List<Equipment> equipment = studentService.getAvailableEquipment();
        equipmentTable.setItems(FXCollections.observableArrayList(equipment));
        equipmentComboBox.setItems(FXCollections.observableArrayList(equipment));
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        List<Equipment> equipment = studentService.searchEquipment(searchTerm);
        equipmentTable.setItems(FXCollections.observableArrayList(equipment));
        equipmentComboBox.setItems(FXCollections.observableArrayList(equipment));
    }

    @FXML
    private void handleLoanRequest() {
        Equipment selectedEquipment = equipmentComboBox.getSelectionModel().getSelectedItem();
        Course selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();
        String purpose = purposeField.getText().trim();

        if (selectedEquipment == null || selectedCourse == null || purpose.isEmpty()) {
            messageLabel.setText("Please select equipment, course, and provide a purpose");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            String recordId = UUID.randomUUID().toString();
            studentService.requestEquipmentLoan(currentStudent, selectedEquipment, selectedCourse, purpose, recordId);
            messageLabel.setText("Loan request submitted successfully");
            messageLabel.setStyle("-fx-text-fill: green;");
            loadLendingHistory();
            purposeField.clear();
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleUpdateInfo() {
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        try {
            studentService.updatePersonalInfo(currentStudent, phone, email);
            messageLabel.setText("Personal information updated successfully");
            messageLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            LoginController loginController = fxmlLoader.getController();
            loginController.resetForm();
            Stage stage = (Stage) lendingTable.getScene().getWindow();
            stage.setTitle("University Equipment Lending System");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error logging out");
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
}