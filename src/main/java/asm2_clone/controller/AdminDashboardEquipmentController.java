package asm2_clone.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.cell.PropertyValueFactory;
import asm2_clone.model.Equipment;
import asm2_clone.service.EquipmentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import javafx.geometry.Insets;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import javafx.scene.Node;
import javafx.concurrent.Task;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import java.io.ByteArrayInputStream;

public class AdminDashboardEquipmentController {
    @FXML private Button backButton;
    @FXML private Button addNewButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> conditionFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private FlowPane equipmentGrid;

    private final EquipmentService equipmentService = new EquipmentService();
    private ObservableList<Equipment> allEquipment;

    @FXML
    public void initialize() {
        // Initialize filters
        conditionFilter.setItems(FXCollections.observableArrayList(
            "All Conditions", "Brand New", "Good", "Needs Maintenance", "Damaged", "Out of Service"
        ));
        conditionFilter.setValue("All Conditions");
        statusFilter.setItems(FXCollections.observableArrayList(
            "All Status", "Available", "Borrowed"
        ));
        statusFilter.setValue("All Status");

        // Add listeners for search and filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateEquipmentDisplay());
        conditionFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateEquipmentDisplay());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateEquipmentDisplay());

        // Back button
        backButton.setOnAction(e -> onBack());

        // Add New button handler
        addNewButton.setOnAction(e -> onAddNew());

        // Load equipment without loading all images
        loadEquipmentAsync();
    }

    private void loadEquipmentAsync() {
        Task<ObservableList<Equipment>> loadTask = new Task<>() {
            @Override
            protected ObservableList<Equipment> call() {
                return FXCollections.observableArrayList(equipmentService.getAllEquipment());
            }
        };

        loadTask.setOnSucceeded(e -> {
            allEquipment = loadTask.getValue();
            updateEquipmentDisplay();
        });

        loadTask.setOnFailed(e -> {
            loadTask.getException().printStackTrace();
            showAlert("Error", "Failed to load equipment", "Database error occurred");
        });

        // Show loading indicator
        equipmentGrid.getChildren().clear();
        Label loadingLabel = new Label("Loading equipment...");
        loadingLabel.setStyle("-fx-font-size: 14px;");
        equipmentGrid.getChildren().add(loadingLabel);

        // Start loading in background
        new Thread(loadTask).start();
    }

    private void refreshEquipmentDisplay() {
        // Reload equipment and update display
        loadEquipmentAsync();
    }

    private void updateEquipmentDisplay() {
        equipmentGrid.getChildren().clear();
        
        if (allEquipment == null) return;
        
        String search = searchField.getText().toLowerCase();
        String condition = conditionFilter.getValue();
        String status = statusFilter.getValue();
        
        // Filter in memory instead of fetching again
        List<Equipment> filtered = allEquipment.stream()
            .filter(eq -> {
                boolean matchesSearch = search.isEmpty() || 
                                      eq.getName().toLowerCase().contains(search) ||
                                      eq.getCategory().toLowerCase().contains(search);
                boolean matchesCondition = condition.equals("All Conditions") || 
                                         eq.getCondition().equals(condition);
                boolean matchesStatus = status.equals("All Status") || 
                                      eq.getStatus().equals(status);
                return matchesSearch && matchesCondition && matchesStatus;
            })
            .collect(Collectors.toList());
        
        for (Equipment eq : filtered) {
            VBox card = createEquipmentCard(eq);
            equipmentGrid.getChildren().add(card);
        }
    }

    private VBox createEquipmentCard(Equipment equipment) {
        VBox card = new VBox(10);
        card.setPrefWidth(200);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #ddd;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);" +
                "-fx-padding: 10;"
        );

        // Create image container
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-radius: 8; -fx-padding: 0;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        Rectangle clip = new Rectangle(180, 120);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imageView.setClip(clip);

        // Load image asynchronously
        Task<byte[]> imageLoadTask = new Task<>() {
            @Override
            protected byte[] call() {
                return equipmentService.getEquipmentImage(equipment.getId());
            }
        };

        imageLoadTask.setOnSucceeded(e -> {
            byte[] imageData = imageLoadTask.getValue();
            if (imageData != null && imageData.length > 0) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
                    Image image = new Image(bis);
                    if (!image.isError()) {
                        imageView.setImage(image);
                    }
                } catch (Exception ex) {
                    // Silently handle any image loading errors
                }
            }
        });

        // Start loading the image
        new Thread(imageLoadTask).start();

        imageContainer.getChildren().add(imageView);
        
        Label nameLabel = new Label(equipment.getName());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        
        Label categoryLabel = new Label(equipment.getCategory());
        categoryLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
        
        Label conditionLabel = new Label(equipment.getCondition());
        String conditionStyle = getConditionStyle(equipment.getCondition());
        conditionLabel.setStyle(conditionStyle + "; -fx-padding: 3 12 3 12; -fx-alignment: center-left;");
        conditionLabel.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label(equipment.getStatus());
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1976D2; -fx-padding: 2 0 0 0;");

        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        
        Hyperlink editLink = new Hyperlink("Edit");
        editLink.setStyle("-fx-text-fill: #1976D2; -fx-underline: false; -fx-font-size: 12px;");
        editLink.setOnMouseEntered(e -> editLink.setUnderline(true));
        editLink.setOnMouseExited(e -> editLink.setUnderline(false));
        editLink.setOnAction(e -> handleEdit(equipment));
        
        Hyperlink deleteLink = new Hyperlink("Delete");
        deleteLink.setStyle("-fx-text-fill: #E53935; -fx-underline: false; -fx-font-size: 12px;");
        deleteLink.setOnMouseEntered(e -> deleteLink.setUnderline(true));
        deleteLink.setOnMouseExited(e -> deleteLink.setUnderline(false));
        deleteLink.setOnAction(e -> handleDelete(equipment));
        
        actions.getChildren().addAll(editLink, deleteLink);

        VBox content = new VBox(4);
        content.setPadding(new Insets(10, 10, 0, 10));
        content.getChildren().addAll(nameLabel, categoryLabel, conditionLabel, statusLabel, actions);
        
        card.getChildren().addAll(imageContainer, content);
        return card;
    }

    private String getConditionStyle(String condition) {
        String baseStyle = "-fx-background-radius: 3; -fx-font-size: 11px;";
        if (condition == null) return baseStyle;
        switch (condition) {
            case "Brand New": return baseStyle + "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;";
            case "Good": return baseStyle + "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;";
            case "Needs Maintenance": return baseStyle + "-fx-background-color: #fff8e1; -fx-text-fill: #fbc02d;";
            case "Damaged": return baseStyle + "-fx-background-color: #eeeeee; -fx-text-fill: #757575;";
            case "Out of Service": return baseStyle + "-fx-background-color: #ffebee; -fx-text-fill: #c62828;";
            default: return baseStyle + "-fx-background-color: #e0e0e0; -fx-text-fill: #757575;";
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
    private void onUsersTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard.fxml"));
            Parent userRoot = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene userScene = new Scene(userRoot, 1000, 700);
            stage.setTitle("UNI LEND - Users Dashboard");
            stage.setScene(userScene);
        } catch (Exception e) {
            System.err.println("Error loading user dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onEquipmentTabClicked() {
        // No action needed; already on Equipment page
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
            System.err.println("Error loading courses dashboard: " + e.getMessage());
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

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleEdit(Equipment equipment) {
        Dialog<Equipment> dialog = new Dialog<>();
        dialog.setTitle("Edit Equipment");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20 30 20 30; -fx-background-color: white;");
        content.setPrefWidth(500);

        Label titleLabel = new Label("Edit Equipment");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");

        // Name field
        VBox nameGroup = new VBox(5);
        Label nameLabel = new Label("Equipment Name");
        nameLabel.setStyle("-fx-font-weight: bold;");
        TextField nameField = new TextField(equipment.getName());
        nameField.setPromptText("Enter equipment name");
        nameGroup.getChildren().addAll(nameLabel, nameField);

        // Category field
        VBox categoryGroup = new VBox(5);
        Label categoryLabel = new Label("Category");
        categoryLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> categoryBox = new ComboBox<>(FXCollections.observableArrayList(
            "Electronics", "Computing", "AV Equipment", "Networking", 
            "Fabrication", "Design", "Measurement", "Imaging", "Prototyping", "Simulation"
        ));
        categoryBox.setValue(equipment.getCategory());
        categoryBox.setPromptText("Select category");
        categoryGroup.getChildren().addAll(categoryLabel, categoryBox);

        // Condition field
        VBox conditionGroup = new VBox(5);
        Label conditionLabel = new Label("Condition");
        conditionLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> conditionBox = new ComboBox<>(FXCollections.observableArrayList(
            "Brand New", "Good", "Needs Maintenance", "Damaged", "Out of Service"
        ));
        conditionBox.setValue(equipment.getCondition());
        conditionGroup.getChildren().addAll(conditionLabel, conditionBox);

        // Purchase Date field
        VBox dateGroup = new VBox(5);
        Label dateLabel = new Label("Purchase Date");
        dateLabel.setStyle("-fx-font-weight: bold;");
        DatePicker datePicker = new DatePicker(equipment.getPurchaseDate());
        dateGroup.getChildren().addAll(dateLabel, datePicker);

        // Image Upload
        VBox imageGroup = new VBox(5);
        Label imageLabel = new Label("Equipment Image");
        imageLabel.setStyle("-fx-font-weight: bold;");
        Button uploadButton = new Button("Choose Image");
        ImageView previewImage = new ImageView();
        previewImage.setFitHeight(100);
        previewImage.setFitWidth(160);
        Label imageNameLabel = new Label("Current image");
        imageNameLabel.setStyle("-fx-text-fill: #666;");
        
        // Store image data
        final byte[][] imageData = new byte[1][];
        imageData[0] = equipment.getImage(); // Store current image

        // Set initial image if exists
        if (equipment.getImage() != null) {
            try {
                Image image = new Image(new java.io.ByteArrayInputStream(equipment.getImage()));
                previewImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading current image: " + e.getMessage());
            }
        }

        // Add image chooser functionality
        uploadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                try {
                    imageData[0] = Files.readAllBytes(file.toPath());
                    Image image = new Image(file.toURI().toString());
                    previewImage.setImage(image);
                    imageNameLabel.setText(file.getName());
                } catch (Exception ex) {
                    showAlert("Error", "Failed to load image", ex.getMessage());
                }
            }
        });

        imageGroup.getChildren().addAll(imageLabel, uploadButton, previewImage, imageNameLabel);

        // Add all groups to content
        content.getChildren().addAll(titleLabel, nameGroup, categoryGroup, conditionGroup, 
                                    dateGroup, imageGroup);

        scrollPane.setContent(content);
        dialog.getDialogPane().setContent(scrollPane);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, saveButtonType);

        // Style the dialog
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        // Enable/Disable save button based on input validation
        saveButton.setDisable(true);
        nameField.textProperty().addListener((obs, old, newValue) -> 
            saveButton.setDisable(newValue.trim().isEmpty() || categoryBox.getValue() == null || 
                                conditionBox.getValue() == null || datePicker.getValue() == null));
        categoryBox.valueProperty().addListener((obs, old, newValue) -> 
            saveButton.setDisable(nameField.getText().trim().isEmpty() || newValue == null || 
                                conditionBox.getValue() == null || datePicker.getValue() == null));
        conditionBox.valueProperty().addListener((obs, old, newValue) -> 
            saveButton.setDisable(nameField.getText().trim().isEmpty() || categoryBox.getValue() == null || 
                                newValue == null || datePicker.getValue() == null));
        datePicker.valueProperty().addListener((obs, old, newValue) -> 
            saveButton.setDisable(nameField.getText().trim().isEmpty() || categoryBox.getValue() == null || 
                                conditionBox.getValue() == null || newValue == null));

        // Convert dialog result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Equipment updatedEquipment = new Equipment();
                updatedEquipment.setId(equipment.getId());
                updatedEquipment.setName(nameField.getText().trim());
                updatedEquipment.setCategory(categoryBox.getValue());
                updatedEquipment.setCondition(conditionBox.getValue());
                updatedEquipment.setPurchaseDate(datePicker.getValue());
                updatedEquipment.setStatus(equipment.getStatus()); // Preserve current status
                updatedEquipment.setImage(imageData[0]); // Use updated image data
                return updatedEquipment;
            }
            return null;
        });

        // Handle the result
        dialog.showAndWait().ifPresent(updatedEquipment -> {
            try {
                boolean success = equipmentService.updateEquipment(updatedEquipment);
                if (success) {
                    showNotification("Success", "Equipment updated successfully");
                    refreshEquipmentDisplay(); // Refresh after update
                } else {
                    showAlert("Error", "Failed to update equipment", "Database error occurred");
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to update equipment", e.getMessage());
            }
        });
    }

    private void handleDelete(Equipment equipment) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Equipment");
        confirmDialog.setContentText("Are you sure you want to delete " + equipment.getName() + "?");

        // Style the dialog buttons
        Button okButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        okButton.setText("Delete");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = equipmentService.deleteEquipment(equipment.getId());
                    if (success) {
                        showNotification("Success", "Equipment deleted successfully");
                        refreshEquipmentDisplay(); // Refresh after delete
                    } else {
                        showAlert("Error", "Failed to delete equipment", "Database error occurred");
                    }
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete equipment", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void onAddNew() {
        Dialog<Equipment> dialog = new Dialog<>();
        dialog.setTitle("Add New Equipment");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20 30 20 30; -fx-background-color: white;");
        content.setPrefWidth(500);

        Label titleLabel = new Label("Add New Equipment");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");

        // Name field
        VBox nameGroup = new VBox(5);
        Label nameLabel = new Label("Equipment Name");
        nameLabel.setStyle("-fx-font-weight: bold;");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter equipment name");
        nameGroup.getChildren().addAll(nameLabel, nameField);

        // Category field
        VBox categoryGroup = new VBox(5);
        Label categoryLabel = new Label("Category");
        categoryLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> categoryBox = new ComboBox<>(FXCollections.observableArrayList(
            "Electronics", "Computing", "AV Equipment", "Networking", 
            "Fabrication", "Design", "Measurement", "Imaging", "Prototyping", "Simulation"
        ));
        categoryBox.setPromptText("Select category");
        categoryGroup.getChildren().addAll(categoryLabel, categoryBox);

        // Condition field
        VBox conditionGroup = new VBox(5);
        Label conditionLabel = new Label("Condition");
        conditionLabel.setStyle("-fx-font-weight: bold;");
        ComboBox<String> conditionBox = new ComboBox<>(FXCollections.observableArrayList(
            "Brand New", "Good", "Needs Maintenance", "Damaged", "Out of Service"
        ));
        conditionBox.setPromptText("Select condition");
        conditionGroup.getChildren().addAll(conditionLabel, conditionBox);

        // Purchase Date field
        VBox dateGroup = new VBox(5);
        Label dateLabel = new Label("Purchase Date");
        dateLabel.setStyle("-fx-font-weight: bold;");
        DatePicker datePicker = new DatePicker();
        dateGroup.getChildren().addAll(dateLabel, datePicker);

        // Image Upload
        VBox imageGroup = new VBox(5);
        Label imageLabel = new Label("Equipment Image");
        imageLabel.setStyle("-fx-font-weight: bold;");
        
        Button uploadButton = new Button("Choose Image");
        uploadButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333;");
        
        // Create a smaller preview container with fixed size
        StackPane previewContainer = new StackPane();
        previewContainer.setPrefSize(120, 80);  // Even smaller fixed size
        previewContainer.setMaxSize(120, 80);   // Enforce maximum size
        previewContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 4;");
        
        ImageView previewImage = new ImageView();
        previewImage.setFitWidth(120);  // Match container size
        previewImage.setFitHeight(80);
        previewImage.setPreserveRatio(true);
        
        // Clip for rounded corners
        Rectangle clip = new Rectangle(120, 80);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        previewImage.setClip(clip);
        
        Label imageNameLabel = new Label("No image selected");
        imageNameLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;"); // Smaller text
        
        final File[] selectedFile = new File[1];
        
        uploadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                try {
                    selectedFile[0] = file;
                    Image image = new Image(file.toURI().toString());
                    previewImage.setImage(image);
                    imageNameLabel.setText(file.getName());
                } catch (Exception ex) {
                    showAlert("Error", "Failed to load image", ex.getMessage());
                }
            }
        });

        previewContainer.getChildren().add(previewImage);
        imageGroup.getChildren().addAll(imageLabel, uploadButton, previewContainer, imageNameLabel);

        // Add all groups to content
        content.getChildren().addAll(titleLabel, nameGroup, categoryGroup, conditionGroup, 
                                    dateGroup, imageGroup);

        scrollPane.setContent(content);
        dialog.getDialogPane().setContent(scrollPane);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, saveButtonType);

        // Enable/Disable save button based on validation
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        saveButton.setDisable(true);

        // Validation
        nameField.textProperty().addListener((obs, old, newValue) -> 
            saveButton.setDisable(newValue.trim().isEmpty() || categoryBox.getValue() == null || 
                                conditionBox.getValue() == null || datePicker.getValue() == null));
        categoryBox.valueProperty().addListener((obs, old, newValue) -> 
            saveButton.setDisable(nameField.getText().trim().isEmpty() || newValue == null || 
                                conditionBox.getValue() == null || datePicker.getValue() == null));
        conditionBox.valueProperty().addListener((obs, old, newValue) -> 
            saveButton.setDisable(nameField.getText().trim().isEmpty() || categoryBox.getValue() == null || 
                                newValue == null || datePicker.getValue() == null));
        datePicker.valueProperty().addListener((obs, old, newValue) -> 
            saveButton.setDisable(nameField.getText().trim().isEmpty() || categoryBox.getValue() == null || 
                                conditionBox.getValue() == null || newValue == null));

        // Convert dialog result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Equipment newEquipment = new Equipment();
                newEquipment.setName(nameField.getText().trim());
                newEquipment.setCategory(categoryBox.getValue());
                newEquipment.setCondition(conditionBox.getValue());
                newEquipment.setPurchaseDate(datePicker.getValue());
                newEquipment.setStatus("Available");
                
                // Add the equipment with the selected file
                boolean success = equipmentService.addNewEquipment(newEquipment, selectedFile[0]);
                if (success) {
                    showNotification("Success", "Equipment added successfully");
                    refreshEquipmentDisplay();
                } else {
                    showAlert("Error", "Failed to add equipment", "Database error occurred");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
} 