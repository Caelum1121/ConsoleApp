package asm2_clone.controller;

import asm2_clone.db.EquipmentDAO;
import asm2_clone.db.LendingRecordDAO;
import asm2_clone.db.StudentDAO;
import asm2_clone.model.Equipment;
import asm2_clone.model.LendingRecord;
import asm2_clone.model.Student;
import asm2_clone.model.Course;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.Node;
import java.util.*;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

public class StudentBorrowEquipmentController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> conditionFilter;
    @FXML private VBox equipmentList;
    @FXML private Button borrowSelectedButton;

    private Student student;
    private List<Equipment> allAvailableEquipment = new ArrayList<>();
    private Set<Equipment> selectedEquipment = new HashSet<>();
    private Map<Integer, Course> equipmentCourseMap = new HashMap<>();

    public void setStudent(Student student) {
        this.student = student;
        loadAvailableEquipment();
    }

    @FXML
    public void initialize() {
        if (conditionFilter != null) {
            conditionFilter.setItems(FXCollections.observableArrayList(
                "All", "Good", "Needs Maintenance", "Damaged", "Brand New"
            ));
            conditionFilter.setValue("All");
            conditionFilter.setOnAction(e -> filterAndDisplayEquipment());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayEquipment());
        }
        if (borrowSelectedButton != null) {
            borrowSelectedButton.setOnAction(e -> onBorrowSelected());
        }
    }

    private void loadAvailableEquipment() {
        if (student == null) return;

        allAvailableEquipment.clear();
        equipmentCourseMap.clear();
        Set<String> conditions = new HashSet<>();

        List<Equipment> available = EquipmentDAO.getAvailableEquipmentForStudent(student.getId());
        Map<Integer, Equipment> uniqueEquipment = new LinkedHashMap<>();

        for (Equipment eq : available) {
            uniqueEquipment.put(eq.getId(), eq);

            // 建立 equipment → course 映射
            Course matchedCourse = findCourseContainingEquipment(student, eq.getId());
            if (matchedCourse != null) {
                equipmentCourseMap.put(eq.getId(), matchedCourse);
            }

            if (eq.getCondition() != null) {
                conditions.add(eq.getCondition());
            }
        }

        allAvailableEquipment.addAll(uniqueEquipment.values());
        filterAndDisplayEquipment();
    }


    private Course findCourseContainingEquipment(Student student, int equipmentId) {
        for (Course course : student.getEnrolledCourses()) {
            if (course.getEquipmentRelated() != null) {
                for (Equipment e : course.getEquipmentRelated()) {
                    if (e.getId() == equipmentId) {
                        return course;
                    }
                }
            }
        }
        return null;
    }


    private void filterAndDisplayEquipment() {
        String search = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String condition = conditionFilter.getValue();
        equipmentList.getChildren().clear();
        List<Equipment> filtered = allAvailableEquipment.stream()
            .filter(eq -> (search.isEmpty() || eq.getName().toLowerCase().contains(search)))
            .filter(eq -> (condition == null || condition.equals("All") || condition.equals(eq.getCondition())))
            .collect(Collectors.toList());
        if (filtered.isEmpty()) {
            Label none = new Label("No available equipment found.");
            none.setStyle("-fx-text-fill: #888; -fx-font-size: 15px; -fx-padding: 20 0 0 0;");
            equipmentList.getChildren().add(none);
        } else {
            for (Equipment eq : filtered) {
                equipmentList.getChildren().add(createEquipmentRow(eq));
            }
        }
        updateBorrowButton();
    }

    private Node createEquipmentRow(Equipment eq) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: #fafafa; -fx-background-radius: 8;");
        VBox info = new VBox(2);
        Label name = new Label(eq.getName());
        name.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #222;");
        Label meta = new Label("ID: " + eq.getId() + "    Condition: " + eq.getCondition());
        meta.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");
        info.getChildren().addAll(name, meta);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button selectBtn = new Button(selectedEquipment.contains(eq) ? "Selected" : "Select");
        selectBtn.setStyle(selectedEquipment.contains(eq)
            ? "-fx-background-color: #222; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4;"
            : "-fx-background-color: white; -fx-border-color: #222; -fx-text-fill: #222; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4;");
        selectBtn.setOnAction(e -> {
            if (selectedEquipment.contains(eq)) {
                selectedEquipment.remove(eq);
            } else {
                selectedEquipment.add(eq);
            }
            filterAndDisplayEquipment();
        });
        row.getChildren().addAll(info, spacer, selectBtn);
        return row;
    }

    private void updateBorrowButton() {
        borrowSelectedButton.setText("Borrow Selected (" + selectedEquipment.size() + ")");
        borrowSelectedButton.setDisable(selectedEquipment.isEmpty());
    }

    @FXML
    private void onBorrowSelected() {
        if (selectedEquipment.isEmpty() || student == null) return;

        // Show dialog to enter purpose
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Borrowing Purpose");
        dialog.setHeaderText("Enter the purpose for borrowing the selected equipment:");
        dialog.setContentText("Purpose:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "You must enter a purpose to proceed.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        String purpose = result.get().trim();

        // Create new lending record
        LendingRecord record = new LendingRecord();
        record.setRecordId(LendingRecordDAO.generateNextRecordId());
        record.setBorrower(student);
        record.setBorrowerId(student.getId());
        record.setBorrowerRole("student");
        record.setBorrowDate(new java.util.Date());
        record.setPurpose(purpose);
        // Supervisor: pick the first course's supervisor
        Course course = equipmentCourseMap.get(selectedEquipment.iterator().next().getId());
        record.setSupervisorId(course != null ? course.getAcademicStaffId() : null);
        record.setCourse(course);
        record.setApprovalStatus(LendingRecord.ApprovalStatus.PENDING);
        record.setEquipmentList(new ArrayList<>(selectedEquipment));

        boolean success = LendingRecordDAO.insertLendingRecord(record);
        if (success) {
            selectedEquipment.clear();
            filterAndDisplayEquipment();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Borrow request sent for approval!", ButtonType.OK);
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to send borrow request.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    private void onBack() {
        // TODO: Implement navigation back to dashboard
    }
} 