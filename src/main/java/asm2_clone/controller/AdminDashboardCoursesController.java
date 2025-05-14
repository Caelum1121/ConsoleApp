package asm2_clone.controller;

import asm2_clone.model.Course;
import asm2_clone.model.AcademicStaff;
import asm2_clone.db.CourseDAO;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class AdminDashboardCoursesController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> courseNameCol;
    @FXML private TableColumn<Course, String> codeCol;
    @FXML private TableColumn<Course, Integer> studentsCol;
    @FXML private TableColumn<Course, Integer> equipmentCol;
    @FXML private TableColumn<Course, Void> actionsCol;
    @FXML private Button backButton;
    @FXML private Button addNewButton;

    private ObservableList<Course> allCourses;
    private final CourseDAO courseDAO = new CourseDAO();

    @FXML
    public void initialize() {
        // Set up table columns
        setupTableColumns();
        
        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCourses(newValue);
        });

        // Load initial data
        loadCourses();
    }

    private void setupTableColumns() {
        // Set cell value factories
        courseNameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        studentsCol.setCellValueFactory(new PropertyValueFactory<>("studentCount"));
        equipmentCol.setCellValueFactory(new PropertyValueFactory<>("equipmentCount"));

        // Actions column
        actionsCol.setCellFactory(col -> new TableCell<Course, Void>() {
            private final HBox actionBox = new HBox(20);  // Increased spacing between links
            private final Hyperlink editLink = new Hyperlink("Edit");
            private final Hyperlink deleteLink = new Hyperlink("Delete");

            {
                actionBox.setAlignment(Pos.CENTER);
                editLink.setStyle("-fx-text-fill: #0078D4; -fx-underline: false;");  // Changed to match screenshot
                deleteLink.setStyle("-fx-text-fill: #D32F2F; -fx-underline: false;"); // Changed to match screenshot
                
                editLink.setOnMouseEntered(e -> editLink.setUnderline(true));
                editLink.setOnMouseExited(e -> editLink.setUnderline(false));
                deleteLink.setOnMouseEntered(e -> deleteLink.setUnderline(true));
                deleteLink.setOnMouseExited(e -> deleteLink.setUnderline(false));

                actionBox.getChildren().addAll(editLink, deleteLink);

                editLink.setOnAction(event -> {
                    Course course = getTableRow().getItem();
                    if (course != null) handleEdit(course);
                });

                deleteLink.setOnAction(event -> {
                    Course course = getTableRow().getItem();
                    if (course != null) handleDelete(course);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    @FXML
    private void onUsersTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) courseTable.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onEquipmentTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard_equipment.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) courseTable.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setTitle("UNI LEND - Login");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCourses() {
        try {
            List<Course> courses = courseDAO.getCoursesForAdminDashboard();
            allCourses = FXCollections.observableArrayList(courses);
        courseTable.setItems(allCourses);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading courses: " + e.getMessage());
        }
    }

    private void filterCourses(String searchText) {
        if (allCourses == null) return;
        
        if (searchText == null || searchText.isEmpty()) {
            courseTable.setItems(allCourses);
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();
        ObservableList<Course> filteredList = allCourses.filtered(course -> 
            course.getCourseName().toLowerCase().contains(lowerCaseFilter) ||
            course.getCourseId().toLowerCase().contains(lowerCaseFilter)
        );
        courseTable.setItems(filteredList);
    }

    @FXML
    private void onAddNew() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Add New Course");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Main container
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setPrefWidth(600);  // Made wider

        // Fixed top section (always visible)
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(0, 0, 10, 0));
        
        // Course Code field
        Label codeLabel = new Label("Course Code:");
        TextField codeField = new TextField(courseDAO.generateNextCourseId());
        codeField.setDisable(true);
        
        // Course Name field
        Label nameLabel = new Label("Course Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter course name");

        // Supervisor selection
        Label supervisorLabel = new Label("Supervisor:");
        ComboBox<AcademicStaff> supervisorComboBox = new ComboBox<>();
        supervisorComboBox.setPromptText("Select a supervisor");
        supervisorComboBox.setPrefWidth(400);  // Made wider
        loadAvailableSupervisors(supervisorComboBox);

        Button addSupervisorBtn = new Button("Add New Supervisor");
        addSupervisorBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2196F3; -fx-underline: true;");
        addSupervisorBtn.setOnAction(e -> showAddSupervisorDialog(supervisorComboBox));

        topSection.getChildren().addAll(
            codeLabel, codeField,
            nameLabel, nameField,
            supervisorLabel, supervisorComboBox,
            addSupervisorBtn
        );

        // Scrollable section
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);  // Set preferred height
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox scrollContent = new VBox(15);
        scrollContent.setPadding(new Insets(10));

        // Students Section
        VBox studentsSection = new VBox(10);
        Label studentsLabel = new Label("Select Students:");
        TextField studentSearchField = new TextField();
        studentSearchField.setPromptText("Search students...");

        ScrollPane studentScrollPane = new ScrollPane();
        studentScrollPane.setFitToWidth(true);
        studentScrollPane.setPrefHeight(150);  // Reduced from 200 to 150
        studentScrollPane.setMaxHeight(150);   // Added max height
        studentScrollPane.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4;");

        VBox studentCheckboxes = new VBox(5);
        studentCheckboxes.setPadding(new Insets(5));

        // Load and sort students by ID number
        List<Map<String, String>> students = courseDAO.getAvailableStudents();
        students.sort((a, b) -> {
            // Extract numbers from IDs (S001 -> 1, S015 -> 15)
            int idA = Integer.parseInt(a.get("id").substring(1));
            int idB = Integer.parseInt(b.get("id").substring(1));
            return Integer.compare(idA, idB);
        });

        List<CheckBox> studentBoxes = new ArrayList<>();
        for (Map<String, String> student : students) {
            String id = student.get("id");
            String name = student.get("fullName");
            CheckBox cb = new CheckBox(String.format("%s - %s", id, name));
            cb.setUserData(id);
            studentBoxes.add(cb);
            studentCheckboxes.getChildren().add(cb);
        }

        studentScrollPane.setContent(studentCheckboxes);

        // Student search functionality
        studentSearchField.textProperty().addListener((obs, old, newValue) -> {
            String search = newValue.toLowerCase();
            studentCheckboxes.getChildren().clear();
            studentBoxes.forEach(cb -> {
                if (cb.getText().toLowerCase().contains(search)) {
                    studentCheckboxes.getChildren().add(cb);
                }
            });
        });

        studentsSection.getChildren().addAll(studentsLabel, studentSearchField, studentScrollPane);

        // Equipment Section
        VBox equipmentSection = new VBox(10);
        Label equipmentLabel = new Label("Select Equipment:");
        TextField equipmentSearchField = new TextField();
        equipmentSearchField.setPromptText("Search equipment...");

        ScrollPane equipmentScrollPane = new ScrollPane();
        equipmentScrollPane.setFitToWidth(true);
        equipmentScrollPane.setPrefHeight(150);  // Reduced from 200 to 150
        equipmentScrollPane.setMaxHeight(150);   // Added max height
        equipmentScrollPane.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4;");

        VBox equipmentCheckboxes = new VBox(5);
        equipmentCheckboxes.setPadding(new Insets(5));

        // Load and sort equipment by ID number
        List<Map<String, Object>> equipment = courseDAO.getAvailableEquipment();
        equipment.sort((a, b) -> {
            Integer idA = (Integer) a.get("id");
            Integer idB = (Integer) b.get("id");
            return idA.compareTo(idB);
        });

        List<CheckBox> equipmentBoxes = new ArrayList<>();
        for (Map<String, Object> eq : equipment) {
            Integer id = (Integer) eq.get("id");
            String name = (String) eq.get("name");
            CheckBox cb = new CheckBox(String.format("ID: %d - %s", id, name));
            cb.setUserData(id);
            equipmentBoxes.add(cb);
            equipmentCheckboxes.getChildren().add(cb);
        }

        equipmentScrollPane.setContent(equipmentCheckboxes);

        // Equipment search functionality
        equipmentSearchField.textProperty().addListener((obs, old, newValue) -> {
            String search = newValue.toLowerCase();
            equipmentCheckboxes.getChildren().clear();
            equipmentBoxes.forEach(cb -> {
                if (cb.getText().toLowerCase().contains(search)) {
                    equipmentCheckboxes.getChildren().add(cb);
                }
            });
        });

        equipmentSection.getChildren().addAll(equipmentLabel, equipmentSearchField, equipmentScrollPane);

        // Add sections to scroll content
        scrollContent.getChildren().addAll(studentsSection, equipmentSection);
        scrollPane.setContent(scrollContent);

        // Add everything to main container
        mainContainer.getChildren().addAll(topSection, scrollPane);

        // Set dialog content
        dialog.getDialogPane().setContent(mainContainer);
        dialog.getDialogPane().setPrefWidth(650);
        dialog.getDialogPane().setPrefHeight(600);  // Reduced from 700 to 600

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the OK button
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Course course = new Course();
                course.setCourseId(codeField.getText());
                course.setCourseName(nameField.getText().trim());
                course.setAcademicStaffId(supervisorComboBox.getValue().getId());

                // Get selected student IDs
                List<String> selectedStudentIds = studentBoxes.stream()
                    .filter(CheckBox::isSelected)
                    .map(cb -> (String)cb.getUserData())
                    .collect(Collectors.toList());
                course.setSelectedStudentIds(selectedStudentIds);

                // Get selected equipment IDs
                List<Integer> selectedEquipmentIds = equipmentBoxes.stream()
                    .filter(CheckBox::isSelected)
                    .map(cb -> (Integer)cb.getUserData())
                    .collect(Collectors.toList());
                course.setSelectedEquipmentIds(selectedEquipmentIds);

                return course;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(course -> {
            try {
                if (courseDAO.addCourse(course)) {
                    // Use batch insert for better performance
                    courseDAO.batchAddStudentsToCourse(course.getSelectedStudentIds(), course.getCourseId());
                    courseDAO.batchAddEquipmentToCourse(course.getSelectedEquipmentIds(), course.getCourseId());
                    loadCourses(); // Refresh the table
                    showAlert("Success", "Course added successfully!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to add course.");
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to add course: " + e.getMessage());
            }
        });
    }

    private List<AcademicStaff> loadAvailableSupervisors(ComboBox<AcademicStaff> comboBox) {
        try {
            List<AcademicStaff> availableSupervisors = courseDAO.getAvailableAcademicStaff();
            if (comboBox != null) {
                comboBox.setItems(FXCollections.observableArrayList(availableSupervisors));
                
                // Custom cell factory to display staff name
                comboBox.setCellFactory(lv -> new ListCell<AcademicStaff>() {
                    @Override
                    protected void updateItem(AcademicStaff staff, boolean empty) {
                        super.updateItem(staff, empty);
                        if (empty || staff == null) {
                            setText(null);
                        } else {
                            setText(staff.getFullName() + " (" + staff.getId() + ")");
                        }
                    }
                });

                // Match the button cell display
                comboBox.setButtonCell(new ListCell<AcademicStaff>() {
                    @Override
                    protected void updateItem(AcademicStaff staff, boolean empty) {
                        super.updateItem(staff, empty);
                        if (empty || staff == null) {
                            setText("Select a supervisor");
                        } else {
                            setText(staff.getFullName() + " (" + staff.getId() + ")");
                        }
                    }
                });
            }
            return availableSupervisors;
        } catch (Exception e) {
            showAlert("Error", "Failed to load available supervisors: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void showAddSupervisorDialog(ComboBox<AcademicStaff> supervisorComboBox) {
        Dialog<AcademicStaff> dialog = new Dialog<>();
        dialog.setTitle("Add New Supervisor");
        dialog.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField idField = new TextField();
        idField.setPromptText("Enter ID (e.g., A014)");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter full name");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("Select date of birth");

        // Add ID format validation and auto-formatting
        idField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                // Only allow A followed by numbers
                if (!newValue.matches("A\\d*")) {
                    idField.setText(oldValue);
                }
            }
        });

        grid.add(new Label("Staff ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Date of Birth:"), 0, 3);
        grid.add(dobPicker, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (idField.getText().isEmpty() || nameField.getText().isEmpty() || 
                    emailField.getText().isEmpty() || dobPicker.getValue() == null) {
                    showAlert("Missing Fields", "Please fill in all fields.");
                    return null;
                }
                
                // Format the ID to ensure it has leading zeros
                String formattedId = formatStaffId(idField.getText().toUpperCase());
                
                AcademicStaff staff = new AcademicStaff();
                staff.setId(formattedId);
                staff.setFullName(nameField.getText());
                staff.setContactInfo(emailField.getText());
                staff.setDateOfBirth(java.sql.Date.valueOf(dobPicker.getValue()));
                return staff;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(staff -> {
            try {
                if (courseDAO.addAcademicStaff(staff)) {
                    loadAvailableSupervisors(supervisorComboBox); // Refresh the combo box
                    supervisorComboBox.setValue(staff); // Select the newly added staff
                    showAlert("Success", "Supervisor added successfully!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to add supervisor.");
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to add supervisor: " + e.getMessage());
            }
        });
    }

    // Add this new helper method to format the staff ID
    private String formatStaffId(String id) {
        if (id == null || id.isEmpty() || !id.startsWith("A")) {
            return id;
        }
        
        // Extract the numeric part
        String numPart = id.substring(1);
        try {
            int num = Integer.parseInt(numPart);
            // Format to ensure 3 digits with leading zeros
            return String.format("A%03d", num);
        } catch (NumberFormatException e) {
            return id;
        }
    }

    private void handleEdit(Course course) {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Main container
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setPrefWidth(600);

        // Fixed top section
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(0, 0, 10, 0));
        
        // Course Code field (disabled)
        Label codeLabel = new Label("Course Code:");
        TextField codeField = new TextField(course.getCourseId());
        codeField.setDisable(true);
        
        // Course Name field
        Label nameLabel = new Label("Course Name:");
        TextField nameField = new TextField(course.getCourseName());
        nameField.setPromptText("Enter course name");

        // Supervisor section (disabled, showing current supervisor)
        Label supervisorLabel = new Label("Supervisor:");
        TextField supervisorField = new TextField();
        supervisorField.setDisable(true);

        // Get and display current supervisor name
        for (AcademicStaff staff : loadAvailableSupervisors(null)) {  // Pass null since we don't need to update any ComboBox
            if (staff.getId().equals(course.getAcademicStaffId())) {
                supervisorField.setText(staff.getFullName() + " (" + staff.getId() + ")");
                break;
            }
        }

        topSection.getChildren().addAll(
            codeLabel, codeField,
            nameLabel, nameField,
            supervisorLabel, supervisorField  // Use TextField instead of ComboBox
        );

        // Scrollable section
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox scrollContent = new VBox(15);
        scrollContent.setPadding(new Insets(10));

        // Load and sort students
        List<Map<String, String>> students = courseDAO.getAvailableStudents();
        students.sort((a, b) -> {
            int idA = Integer.parseInt(a.get("id").substring(1));
            int idB = Integer.parseInt(b.get("id").substring(1));
            return Integer.compare(idA, idB);
        });

        // Load and sort equipment
        List<Map<String, Object>> equipment = courseDAO.getAvailableEquipment();
        equipment.sort((a, b) -> {
            Integer idA = (Integer) a.get("id");
            Integer idB = (Integer) b.get("id");
            return idA.compareTo(idB);
        });

        // Get current students and equipment in course
        List<String> currentStudents = courseDAO.getStudentsInCourse(course.getCourseId());
        List<String> currentEquipment = courseDAO.getEquipmentInCourse(course.getCourseId());

        // Students Section
        VBox studentsSection = new VBox(10);
        Label studentsLabel = new Label("Select Students:");
        TextField studentSearchField = new TextField();
        studentSearchField.setPromptText("Search students...");

        ScrollPane studentScrollPane = new ScrollPane();
        studentScrollPane.setFitToWidth(true);
        studentScrollPane.setPrefHeight(150);
        studentScrollPane.setMaxHeight(150);
        studentScrollPane.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4;");

        VBox studentCheckboxes = new VBox(5);
        studentCheckboxes.setPadding(new Insets(5));

        List<CheckBox> studentBoxes = new ArrayList<>();
        for (Map<String, String> student : students) {
            String id = student.get("id");
            String name = student.get("fullName");
            CheckBox cb = new CheckBox(String.format("%s - %s", id, name));
            cb.setUserData(id);
            cb.setSelected(currentStudents.contains(id));
            studentBoxes.add(cb);
            studentCheckboxes.getChildren().add(cb);
        }

        studentScrollPane.setContent(studentCheckboxes);

        // Student search functionality
        studentSearchField.textProperty().addListener((obs, old, newValue) -> {
            String search = newValue.toLowerCase();
            studentCheckboxes.getChildren().clear();
            studentBoxes.forEach(cb -> {
                if (cb.getText().toLowerCase().contains(search)) {
                    studentCheckboxes.getChildren().add(cb);
                }
            });
        });

        studentsSection.getChildren().addAll(studentsLabel, studentSearchField, studentScrollPane);

        // Equipment Section
        VBox equipmentSection = new VBox(10);
        Label equipmentLabel = new Label("Select Equipment:");
        TextField equipmentSearchField = new TextField();
        equipmentSearchField.setPromptText("Search equipment...");

        ScrollPane equipmentScrollPane = new ScrollPane();
        equipmentScrollPane.setFitToWidth(true);
        equipmentScrollPane.setPrefHeight(150);
        equipmentScrollPane.setMaxHeight(150);
        equipmentScrollPane.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4;");

        VBox equipmentCheckboxes = new VBox(5);
        equipmentCheckboxes.setPadding(new Insets(5));

        List<CheckBox> equipmentBoxes = new ArrayList<>();
        for (Map<String, Object> eq : equipment) {
            Integer id = (Integer) eq.get("id");
            String name = (String) eq.get("name");
            CheckBox cb = new CheckBox(String.format("ID: %d - %s", id, name));
            cb.setUserData(id);
            cb.setSelected(currentEquipment.contains(id.toString()));
            equipmentBoxes.add(cb);
            equipmentCheckboxes.getChildren().add(cb);
        }

        equipmentScrollPane.setContent(equipmentCheckboxes);

        // Equipment search functionality
        equipmentSearchField.textProperty().addListener((obs, old, newValue) -> {
            String search = newValue.toLowerCase();
            equipmentCheckboxes.getChildren().clear();
            equipmentBoxes.forEach(cb -> {
                if (cb.getText().toLowerCase().contains(search)) {
                    equipmentCheckboxes.getChildren().add(cb);
                }
            });
        });

        equipmentSection.getChildren().addAll(equipmentLabel, equipmentSearchField, equipmentScrollPane);

        // Add sections to scroll content
        scrollContent.getChildren().addAll(studentsSection, equipmentSection);
        scrollPane.setContent(scrollContent);

        // Add everything to main container
        mainContainer.getChildren().addAll(topSection, scrollPane);

        // Set dialog content
        dialog.getDialogPane().setContent(mainContainer);
        dialog.getDialogPane().setPrefWidth(650);
        dialog.getDialogPane().setPrefHeight(600);  // Reduced from 700 to 600

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the OK button
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                // Validate required fields
                if (nameField.getText().trim().isEmpty()) {
                    showAlert("Missing Fields", "Please enter a course name.", Alert.AlertType.WARNING);
                    return null;
                }

                course.setCourseName(nameField.getText().trim());
                // Keep existing supervisor ID
                course.setAcademicStaffId(course.getAcademicStaffId());

                // Get selected student IDs
                List<String> selectedStudentIds = studentBoxes.stream()
                    .filter(CheckBox::isSelected)
                    .map(cb -> (String)cb.getUserData())
                    .collect(Collectors.toList());
                course.setSelectedStudentIds(selectedStudentIds);

                // Get selected equipment IDs
                List<Integer> selectedEquipmentIds = equipmentBoxes.stream()
                    .filter(CheckBox::isSelected)
                    .map(cb -> (Integer)cb.getUserData())
                    .collect(Collectors.toList());
                course.setSelectedEquipmentIds(selectedEquipmentIds);

                return course;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedCourse -> {
            try {
                if (courseDAO.updateCourse(updatedCourse)) {
                    // First delete existing relationships
                    courseDAO.deleteCourseStudents(updatedCourse.getCourseId());
                    courseDAO.deleteCourseEquipment(updatedCourse.getCourseId());
                    
                    // Then add new relationships using batch insert
                    courseDAO.batchAddStudentsToCourse(updatedCourse.getSelectedStudentIds(), updatedCourse.getCourseId());
                    courseDAO.batchAddEquipmentToCourse(updatedCourse.getSelectedEquipmentIds(), updatedCourse.getCourseId());
                    
                    loadCourses(); // Refresh the table
                    showAlert("Success", "Course updated successfully!", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Failed to update course.");
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to update course: " + e.getMessage());
            }
        });
    }

    private void handleDelete(Course course) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Course");
        confirmDialog.setContentText("Are you sure you want to delete course " + course.getCourseId() + 
                                   "?\nThis will also remove all associated students and equipment.");

        Button okButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete from related tables first
                    courseDAO.deleteCourseStudents(course.getCourseId());
                    courseDAO.deleteCourseEquipment(course.getCourseId());
                    
                    if (courseDAO.deleteCourse(course.getCourseId())) {
                        loadCourses(); // Refresh the table
                        showAlert("Success", "Course and related data deleted successfully!", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Error", "Failed to delete course.");
                    }
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete course: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String content) {
        showAlert(title, content, Alert.AlertType.ERROR);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onCoursesTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard_courses.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("UNI LEND - Courses Dashboard");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBorrowingRecordsTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard_borrowing.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("UNI LEND - Borrowing Records");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onStatisticsTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard_statistics.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("UNI LEND - Statistics Dashboard");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
