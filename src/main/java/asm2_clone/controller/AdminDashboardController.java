package asm2_clone.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.control.cell.PropertyValueFactory;
import asm2_clone.model.AdminPerson;
import asm2_clone.service.AdminService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import asm2_clone.controller.NotificationUtil;
import asm2_clone.service.CourseService;
import asm2_clone.model.Course;
import java.util.List;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class AdminDashboardController {
    @FXML private TextField searchField;
    @FXML private Button backButton;
    @FXML private Button addNewButton;
    @FXML private TableView<AdminPerson> dataTable;
    @FXML private TableColumn<AdminPerson, String> nameCol;
    @FXML private TableColumn<AdminPerson, String> roleCol;
    @FXML private TableColumn<AdminPerson, String> emailCol;
    @FXML private TableColumn<AdminPerson, String> courseCol;
    @FXML private TableColumn<AdminPerson, String> supervisorCol;
    @FXML private TableColumn<AdminPerson, Void> actionsCol;

    private final AdminService adminService = new AdminService();
    private ObservableList<AdminPerson> allPeople;
    private final CourseService courseService = new CourseService();
    private List<Course> allCourses;

    @FXML
    public void initialize() {
        allCourses = courseService.getAllCourses(); // Initialize courses first
        
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseOrDept"));
        supervisorCol.setCellValueFactory(new PropertyValueFactory<>("supervisor"));

        // Set minimum widths for columns
        nameCol.setMinWidth(120);
        roleCol.setMinWidth(100);
        emailCol.setMinWidth(150);
        courseCol.setMinWidth(200);
        supervisorCol.setMinWidth(150);
        actionsCol.setMinWidth(100);

        // Custom cell factory for courses with better wrapping
        courseCol.setCellFactory(col -> new TableCell<AdminPerson, String>() {
            private final Text text = new Text();
            private final VBox box = new VBox(text);
            {
                text.setWrappingWidth(195); // Slightly less than column min width
                box.setAlignment(Pos.CENTER_LEFT);
                box.setPadding(new Insets(5));
                setGraphic(box);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    text.setText(null);
                    setGraphic(null);
                } else {
                    text.setText(item.replaceAll(", ", "\n• ").replaceFirst("^", "• "));
                    setGraphic(box);
                }
            }
        });

        // Custom cell factory for supervisors with better wrapping
        supervisorCol.setCellFactory(col -> new TableCell<AdminPerson, String>() {
            private final Text text = new Text();
            private final VBox box = new VBox(text);
            {
                text.setWrappingWidth(145); // Slightly less than column min width
                box.setAlignment(Pos.CENTER_LEFT);
                box.setPadding(new Insets(5));
                setGraphic(box);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    text.setText(null);
                    setGraphic(null);
                } else {
                    text.setText(item.replaceAll(", ", "\n• ").replaceFirst("^", "• "));
                    setGraphic(box);
                }
            }
        });

        // Set the resize policy to fill the window and avoid horizontal scroll
        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Show UI immediately, then load users in background
        dataTable.setPlaceholder(new Label("Loading users..."));
        new Thread(() -> {
            ObservableList<AdminPerson> people = adminService.getAllPeople();
            javafx.application.Platform.runLater(() -> {
                allPeople = people;
                dataTable.setItems(allPeople);
                dataTable.setFixedCellSize(-1); // Let row height auto-fit content
                dataTable.refresh();
                dataTable.setPlaceholder(new Label("No content in table"));
            });
        }).start();

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterPeople(newValue);
        });

        actionsCol.setCellFactory(param -> new TableCell<AdminPerson, Void>() {
            private final Hyperlink editLink = new Hyperlink("Edit");
            private final Hyperlink deleteLink = new Hyperlink("Delete");
            private final HBox actionBox = new HBox(8, editLink, deleteLink);

            {
                actionBox.setAlignment(Pos.CENTER);
                actionBox.setSpacing(8);
                actionBox.setPadding(javafx.geometry.Insets.EMPTY);
                actionBox.setStyle("-fx-padding: 0; -fx-background-color: transparent;");

                editLink.setStyle("-fx-text-fill: #1976D2; -fx-underline: false; -fx-font-size: 12px; -fx-padding: 0;");
                editLink.setOnMouseEntered(e -> editLink.setUnderline(true));
                editLink.setOnMouseExited(e -> editLink.setUnderline(false));

                deleteLink.setStyle("-fx-text-fill: #E53935; -fx-underline: false; -fx-font-size: 12px; -fx-padding: 0;");
                deleteLink.setOnMouseEntered(e -> deleteLink.setUnderline(true));
                deleteLink.setOnMouseExited(e -> deleteLink.setUnderline(false));

                editLink.setFocusTraversable(false);
                deleteLink.setFocusTraversable(false);

                editLink.setOnAction(event -> {
                    AdminPerson person = getTableView().getItems().get(getIndex());
                    showEditDialog(person, false);
                });
                deleteLink.setOnAction(event -> {
                    AdminPerson person = getTableView().getItems().get(getIndex());
                    handleDelete(person);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        addNewButton.setOnAction(event -> showEditDialog(null, true));
    }

    private void filterPeople(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            dataTable.setItems(allPeople);
            return;
        }
        String lower = searchText.toLowerCase();
        ObservableList<AdminPerson> filtered = allPeople.filtered(person ->
            (person.getName() != null && person.getName().toLowerCase().contains(lower)) ||
            (person.getRole() != null && person.getRole().toLowerCase().contains(lower)) ||
            (person.getEmail() != null && person.getEmail().toLowerCase().contains(lower))
        );
        dataTable.setItems(filtered);
    }

    private void refreshTable() {
        try {
            // Get fresh data
            ObservableList<AdminPerson> newData = adminService.getAllPeople();
            
            Platform.runLater(() -> {
                try {
                    // Create a new scene with fresh data
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard.fxml"));
                    Parent root = loader.load();
                    AdminDashboardController controller = loader.getController();
                    
                    // Get the current stage
                    Stage stage = (Stage) dataTable.getScene().getWindow();
                    
                    // Set the new scene
                    Scene scene = new Scene(root, 1000, 700);
                    stage.setScene(scene);
                    stage.show();
                    
                } catch (Exception e) {
                    System.err.println("Error reloading view: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Error getting fresh data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add the formatId method here at class level
    private String formatId(String id) {
        if (id == null || id.length() < 2) return id;
        
        String prefix = id.substring(0, 1);
        String numPart = id.substring(1);
        
        try {
            int num = Integer.parseInt(numPart);
            return prefix + String.format("%03d", num); // Format to 3 digits with leading zeros
        } catch (NumberFormatException e) {
            return id;
        }
    }

    private void showEditDialog(AdminPerson person, boolean isNew) {
        Dialog<AdminPerson> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Add New User" : "Edit User");

        // Create a ScrollPane to handle overflow content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        // Main content container
        VBox content = new VBox(8);
        content.setStyle("-fx-padding: 20 30 20 30; -fx-background-color: white;");
        content.setPrefWidth(500); // Set preferred width
        content.setMaxHeight(600); // Set maximum height

        // Title label
        Label titleLabel = new Label(isNew ? "Add New User" : "Edit User");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 18 0;");

        // Form fields
        VBox formFields = new VBox(15); // Increased spacing between fields
        
        // ID Field group
        VBox idGroup = new VBox(5);
        Label idLabel = new Label("ID Number");
        idLabel.setStyle("-fx-font-weight: bold;");
        TextField idField = new TextField();
        idField.setPromptText("Enter ID (e.g. S001, A001, P001)");
        Label idHint = new Label("S = Student, A = Academic, P = Professional");
        idHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        Label idHint2 = new Label("Enter ID (S/A/P) to select course or department.");
        idHint2.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        idGroup.getChildren().addAll(idLabel, idField, idHint, idHint2);

        // Name Field group
        VBox nameGroup = new VBox(5);
        Label nameLabel = new Label("Full Name");
        nameLabel.setStyle("-fx-font-weight: bold;");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter full name");
        nameGroup.getChildren().addAll(nameLabel, nameField);

        // Email Field group
        VBox emailGroup = new VBox(5);
        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-weight: bold;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email address");
        emailGroup.getChildren().addAll(emailLabel, emailField);

        // Date of Birth Field group
        VBox dobGroup = new VBox(5);
        Label dobLabel = new Label("Date of Birth");
        dobLabel.setStyle("-fx-font-weight: bold;");
        DatePicker dobPicker = new DatePicker();
        dobPicker.setPromptText("mm/dd/yyyy");
        dobGroup.getChildren().addAll(dobLabel, dobPicker);

        // Course selection UI
        VBox courseSelectionUI = createCourseSelectionUI();
        courseSelectionUI.setVisible(false);
        courseSelectionUI.setManaged(false);

        // Department UI
        VBox departmentUI = createDepartmentUI();
        departmentUI.setVisible(false);
        departmentUI.setManaged(false);

        // Add all form groups
        formFields.getChildren().addAll(idGroup, nameGroup, emailGroup, dobGroup, courseSelectionUI, departmentUI);

        // Modify the ID field listener
        idField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                courseSelectionUI.setVisible(false);
                courseSelectionUI.setManaged(false);
                departmentUI.setVisible(false);
                departmentUI.setManaged(false);

                if (newVal.startsWith("S")) {
                    courseSelectionUI.setVisible(true);
                    courseSelectionUI.setManaged(true);
                    Object[] uiElements = (Object[]) courseSelectionUI.getUserData();
                    Button addButton = (Button) uiElements[0];
                    Label helpText = (Label) uiElements[1];
                    addButton.setVisible(true);
                    addButton.setManaged(true);
                    helpText.setText("Students can enroll in multiple courses");
                } else if (newVal.startsWith("P")) {
                    departmentUI.setVisible(true);
                    departmentUI.setManaged(true);
                }
            }
        });

        // Pre-fill fields if editing
        if (!isNew && person != null) {
            idField.setText(person.getId());
            idField.setDisable(true);
            nameField.setText(person.getName());
            emailField.setText(person.getEmail());
            if (person.getDateOfBirth() != null) {
                dobPicker.setValue(person.getDateOfBirth());
            }
            // Pre-fill course or department if available
            if (person.getId().startsWith("S")) {
                courseSelectionUI.setVisible(true);
                courseSelectionUI.setManaged(true);
                if (person.getCourseOrDept() != null) {
                    for (Course c : allCourses) {
                        if (c.getCourseId().equals(person.getCourseOrDept())) {
                            for (Node node : courseSelectionUI.getChildren()) {
                                if (node instanceof ComboBox) {
                                    ComboBox<Course> comboBox = (ComboBox<Course>) node;
                                    comboBox.setValue(c);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (person.getId().startsWith("P")) {
                departmentUI.setVisible(true);
                departmentUI.setManaged(true);
                TextField departmentField = (TextField) departmentUI.lookup("TextField");
                if (departmentField != null && person.getCourseOrDept() != null) {
                    departmentField.setText(person.getCourseOrDept());
                }
            }
        }

        content.getChildren().addAll(titleLabel, formFields);
        scrollPane.setContent(content);
        dialog.getDialogPane().setContent(scrollPane);

        // Set minimum dimensions for the dialog
        dialog.getDialogPane().setMinHeight(500);
        dialog.getDialogPane().setMinWidth(550);
        
        // Set maximum dimensions
        dialog.getDialogPane().setMaxHeight(700);
        dialog.getDialogPane().setMaxWidth(600);

        // Style the dialog buttons
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        
        saveButton.setText("Save");
        saveButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 24;");
        cancelButton.setText("Cancel");
        cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #333; -fx-padding: 8 24;");

        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                LocalDate dob = dobPicker.getValue();

                // Basic validation
                if (id.isEmpty() || name.isEmpty() || email.isEmpty() || dob == null) {
                    showAlert("Missing Fields", "Please fill in all fields.", "All fields are required.");
                    return null;
                }

                // Validate and format ID
                if (!id.matches("^[SAP]\\d{1,3}$")) {
                    showAlert("Invalid ID Format", "ID must be S/A/P followed by 1-3 digits.", "Examples: S1, S01, S001");
                    return null;
                }
                
                // Format ID with leading zeros
                id = formatId(id);

                // Create new person object
                AdminPerson newPerson = new AdminPerson(name, getRoleFromId(id), email);
                newPerson.setId(id);
                newPerson.setDateOfBirth(dob);

                // Handle specific fields based on role
                if (id.startsWith("S")) {
                    List<String> selectedCourses = validateCourseSelection(courseSelectionUI);
                    if (selectedCourses == null || selectedCourses.isEmpty()) {
                        showAlert("Missing Course", "Please select at least one course.", null);
                        return null;
                    }
                    newPerson.setCourseOrDept(String.join(",", selectedCourses));
                } else if (id.startsWith("P")) {
                    String department = validateDepartmentField(departmentUI);
                    if (department == null || department.isEmpty()) {
                        showAlert("Missing Department", "Please enter a department.", null);
                        return null;
                    }
                    newPerson.setCourseOrDept(department);
                } else if (id.startsWith("A")) {
                    newPerson.setCourseOrDept(null);
                }

                return newPerson;
            }
            return null;
        });

        // Handle dialog result
        dialog.showAndWait().ifPresent(result -> {
            Stage mainStage = (Stage) dataTable.getScene().getWindow();
            if (isNew) {
                try {
                    boolean success = adminService.addPerson(result);
                    if (success) {
                        // Handle role-specific operations
                        if (result.getId().startsWith("S")) {
                            String[] courses = result.getCourseOrDept().split(",");
                            for (String courseId : courses) {
                                adminService.enrollStudentInCourse(result.getId(), courseId);
                            }
                        } else if (result.getId().startsWith("P")) {
                            adminService.updateProfessionalDepartment(result.getId(), result.getCourseOrDept());
                        }
                        
                        // Show notification first
                        NotificationUtil.showSuccess(mainStage, getUserTypeString(result.getId()) + " successfully added!");
                        
                        // Then refresh the view
                        refreshTable();
                        
                    } else {
                        NotificationUtil.showError(mainStage, "Failed to add new user. Please try again.");
                    }
                } catch (Exception e) {
                    System.err.println("Error adding new user: " + e.getMessage());
                    e.printStackTrace();
                    NotificationUtil.showError(mainStage, "Error adding user: " + e.getMessage());
                }
            } else {
                try {
                    adminService.updatePerson(result);
                    
                    // Handle role-specific updates
                    if (result.getId().startsWith("S")) {
                        String[] courses = result.getCourseOrDept().split(",");
                        boolean isFirst = true;
                        for (String courseId : courses) {
                            adminService.updateStudentCourseAndSupervisor(result.getId(), courseId, isFirst);
                            isFirst = false;
                        }
                    } else if (result.getId().startsWith("P")) {
                        adminService.updateProfessionalDepartment(result.getId(), result.getCourseOrDept());
                    }
                    // No course updates for academic staff
                    
                    // Show notification first
                    NotificationUtil.showSuccess(mainStage, getUserTypeString(result.getId()) + " successfully updated!");
                    
                    // Then refresh the view
                    refreshTable();
                    
                } catch (Exception e) {
                    System.err.println("Error updating user: " + e.getMessage());
                    e.printStackTrace();
                    NotificationUtil.showError(mainStage, "Error updating user: " + e.getMessage());
                }
            }
        });
    }

    private String getRoleFromId(String id) {
        if (id == null || id.isEmpty()) return "Unknown";
        switch (id.charAt(0)) {
            case 'S': return "Student";
            case 'A': return "Academic Staff";
            case 'P': return "Professional Staff";
            default: return "Unknown";
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
            System.err.println("Error returning to login view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onEquipmentTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard_equipment.fxml"));
            Parent equipmentRoot = loader.load();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard.fxml"));
            Parent userRoot = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene userScene = new Scene(userRoot, 1000, 700);
            stage.setTitle("UNI LEND - Admin Dashboard");
            stage.setScene(userScene);
        } catch (Exception e) {
            System.err.println("Error loading user dashboard: " + e.getMessage());
            e.printStackTrace();
        }
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

    private VBox createCourseSelectionUI() {
        VBox courseContainer = new VBox(10);
        courseContainer.setStyle("-fx-padding: 10 0;");

        // Label for courses section
        Label coursesLabel = new Label("Courses");
        coursesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Help text
        Label helpText = new Label("Students can enroll in multiple courses");
        helpText.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px; -fx-padding: 0 0 10 0;");

        // Container for course selections
        VBox selectionsContainer = new VBox(8);
        selectionsContainer.setStyle("-fx-padding: 0 0 10 0;");
        selectionsContainer.getStyleClass().add("selections-container");

        // Create initial course row
        HBox initialRow = createCourseRow(selectionsContainer);
        selectionsContainer.getChildren().add(initialRow);

        // Add Course button
        Button addCourseBtn = new Button("Add Another Course");
        addCourseBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #2196F3;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 8 16;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: #2196F3;" +
            "-fx-border-radius: 4;"
        );

        // Add hover effects
        addCourseBtn.setOnMouseEntered(e -> 
            addCourseBtn.setStyle(
                "-fx-background-color: #E3F2FD;" +
                "-fx-text-fill: #2196F3;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 16;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #2196F3;" +
                "-fx-border-radius: 4;"
            )
        );
        
        addCourseBtn.setOnMouseExited(e -> 
            addCourseBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #2196F3;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 16;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #2196F3;" +
                "-fx-border-radius: 4;"
            )
        );

        // Add button click handler
        addCourseBtn.setOnAction(e -> {
            HBox newRow = createCourseRow(selectionsContainer);
            selectionsContainer.getChildren().add(newRow);
        });

        // Store references to UI elements that need to be updated
        courseContainer.setUserData(new Object[]{addCourseBtn, helpText});

        courseContainer.getChildren().addAll(coursesLabel, helpText, selectionsContainer, addCourseBtn);
        return courseContainer;
    }

    private HBox createCourseRow(VBox parent) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        ComboBox<Course> courseBox = createCourseComboBox();
        courseBox.setPrefWidth(300);

        // Only show remove button if not the first row
        if (!parent.getChildren().isEmpty()) {
            Button removeBtn = new Button("Remove");
            removeBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #F44336;" +
                "-fx-font-size: 12px;" +
                "-fx-cursor: hand;"
            );

            // Hover effect
            removeBtn.setOnMouseEntered(e -> {
                removeBtn.setStyle(
                    "-fx-background-color: #FFEBEE;" +
                    "-fx-text-fill: #F44336;" +
                    "-fx-font-size: 12px;" +
                    "-fx-cursor: hand;"
                );
            });
            
            removeBtn.setOnMouseExited(e -> {
                removeBtn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #F44336;" +
                    "-fx-font-size: 12px;" +
                    "-fx-cursor: hand;"
                );
            });

            removeBtn.setOnAction(e -> parent.getChildren().remove(row));
            row.getChildren().addAll(courseBox, removeBtn);
        } else {
            row.getChildren().add(courseBox);
        }

        return row;
    }

    private ComboBox<Course> createCourseComboBox() {
        ComboBox<Course> courseBox = new ComboBox<>();
        courseBox.setPromptText("Select a course");
        courseBox.setItems(FXCollections.observableArrayList(allCourses));
        
        // Custom cell factory for better display
        courseBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCourseName());
                    setStyle("-fx-padding: 5 10;");
                }
            }
        });
        
        // Match the button cell display with cell factory
        courseBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select a course");
                } else {
                    setText(item.getCourseName());
                }
            }
        });

        courseBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #CCCCCC;" +
            "-fx-border-radius: 4;" +
            "-fx-padding: 5;"
        );

        return courseBox;
    }

    // Add this new method to create department UI
    private VBox createDepartmentUI() {
        VBox departmentContainer = new VBox(10);
        departmentContainer.setStyle("-fx-padding: 10 0;");

        // Label for department section
        Label departmentLabel = new Label("Department");
        departmentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Help text
        Label helpText = new Label("Enter the department for professional staff");
        helpText.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px; -fx-padding: 0 0 10 0;");

        // Department text field
        TextField departmentField = new TextField();
        departmentField.setPromptText("Enter department name");
        departmentField.setPrefWidth(300);
        departmentField.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #CCCCCC;" +
            "-fx-border-radius: 4;" +
            "-fx-padding: 8;"
        );

        departmentContainer.getChildren().addAll(departmentLabel, helpText, departmentField);
        return departmentContainer;
    }

    // Add helper method for showing alerts
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (content != null) {
            alert.setContentText(content);
        }
        alert.showAndWait();
    }

    // Add this helper method to handle table refresh and notifications
    private void refreshTableAndNotify(Stage stage, String userId, boolean isNew) {
        try {
            // Get fresh data from service
            ObservableList<AdminPerson> newPeople = adminService.getAllPeople();
            
            Platform.runLater(() -> {
                try {
                    // Create a temporary list to avoid direct modification
                    ObservableList<AdminPerson> tempList = FXCollections.observableArrayList();
                    tempList.addAll(newPeople);
                    
                    // Set the new items to the table
                    dataTable.setItems(null); // Clear current items
                    dataTable.setItems(tempList); // Set new items
                    allPeople = tempList; // Update reference
                    
                    // Determine message based on action and user type
                    String action = isNew ? "added!" : userId == null ? "deleted!" : "updated!";
                    String userType = getUserTypeString(userId);
                    String message = userType + " successfully " + action;
                    
                    // Show success notification
                    NotificationUtil.showSuccess(stage, message);
                    
                    // Update placeholder if needed
                    if (tempList.isEmpty()) {
                        dataTable.setPlaceholder(new Label("No content in table"));
                    }
                } catch (Exception e) {
                    System.err.println("Error updating table UI: " + e.getMessage());
                    e.printStackTrace();
                    NotificationUtil.showError(stage, "Error refreshing display. Please try again.");
                }
            });
        } catch (Exception e) {
            System.err.println("Error refreshing data: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> 
                NotificationUtil.showError(stage, "Error refreshing data. Please try again.")
            );
        }
    }

    // Helper method to get user type string
    private String getUserTypeString(String id) {
        if (id == null) return "User";
        switch (id.charAt(0)) {
            case 'S': return "Student";
            case 'A': return "Academic Staff";
            case 'P': return "Professional Staff";
            default: return "User";
        }
    }

    // Add this method to validate department field
    private String validateDepartmentField(VBox departmentUI) {
        TextField departmentField = (TextField) departmentUI.lookup("TextField");
        if (departmentField == null) {
            System.err.println("Department field not found"); // Debug log
            return null;
        }
        String department = departmentField.getText().trim();
        System.out.println("Validating department: " + department); // Debug log
        return department.isEmpty() ? null : department;
    }

    // Add this method to validate course selection
    private List<String> validateCourseSelection(VBox courseSelectionUI) {
        try {
            VBox selectionsContainer = (VBox) courseSelectionUI.lookup(".selections-container");
            if (selectionsContainer == null) {
                System.err.println("Course selection container not found"); // Debug log
                return null;
            }

            List<String> selectedCourses = new ArrayList<>();
            for (Node node : selectionsContainer.getChildren()) {
                if (node instanceof HBox) {
                    HBox row = (HBox) node;
                    for (Node rowNode : row.getChildren()) {
                        if (rowNode instanceof ComboBox) {
                            @SuppressWarnings("unchecked")
                            ComboBox<Course> courseBox = (ComboBox<Course>) rowNode;
                            Course selectedCourse = courseBox.getValue();
                            if (selectedCourse != null) {
                                selectedCourses.add(selectedCourse.getCourseId());
                                System.out.println("Selected course: " + selectedCourse.getCourseId()); // Debug log
                            }
                        }
                    }
                }
            }
            return selectedCourses;
        } catch (Exception e) {
            System.err.println("Error validating course selection: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Update the delete functionality
    private void handleDelete(AdminPerson person) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Delete");
        
        ButtonType deleteType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, deleteType);
        
        VBox content = createDeleteConfirmationContent(person);
        dialog.getDialogPane().setContent(content);
        
        styleDeleteDialog(dialog, deleteType);
        
        dialog.showAndWait().ifPresent(result -> {
            if (result == deleteType) {
                try {
                    adminService.deletePerson(person);
                    
                    // Show notification first
                    Stage stage = (Stage) dataTable.getScene().getWindow();
                    NotificationUtil.showSuccess(stage, getUserTypeString(person.getId()) + " successfully deleted!");
                    
                    // Then refresh the view
                    refreshTable();
                    
                } catch (Exception e) {
                    System.err.println("Error deleting user: " + e.getMessage());
                    e.printStackTrace();
                    Stage stage = (Stage) dataTable.getScene().getWindow();
                    NotificationUtil.showError(stage, "Error deleting user. Please try again.");
                }
            }
        });
    }

    // Add these helper methods for delete functionality

    private VBox createDeleteConfirmationContent(AdminPerson person) {
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 18 28 18 28; -fx-background-color: white;");

        // Title
        Label title = new Label("Confirm Delete");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 12 0;");

        // Message
        Label message = new Label("Are you sure you want to delete ");
        Label name = new Label(person.getName());
        name.setStyle("-fx-font-weight: bold;");
        Label message2 = new Label("? This action cannot be undone.");
        
        HBox msgBox = new HBox(message, name, message2);
        msgBox.setSpacing(2);

        content.getChildren().addAll(title, msgBox);
        return content;
    }

    private void styleDeleteDialog(Dialog<ButtonType> dialog, ButtonType deleteType) {
        dialog.setOnShown(e -> {
            Button deleteBtn = (Button) dialog.getDialogPane().lookupButton(deleteType);
            if (deleteBtn != null) {
                deleteBtn.setStyle(
                    "-fx-background-color: #E53935; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 6 24;"
                );
            }
            
            Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            if (cancelBtn != null) {
                cancelBtn.setStyle(
                    "-fx-background-color: #f5f5f5; " +
                    "-fx-text-fill: #333; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 6 24;"
                );
            }
        });

        // Set dialog style
        dialog.getDialogPane().setStyle(
            "-fx-background-color: white; " +
            "-fx-padding: 0;"
        );
    }
} 