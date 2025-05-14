package asm2_clone.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import asm2_clone.db.EquipmentDAO;
import asm2_clone.model.Equipment;
import java.io.IOException;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import java.util.stream.Collectors;

public class VisitorController {
    @FXML private FlowPane equipmentGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> conditionFilter;
    
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private List<Equipment> allEquipment;

    @FXML
    public void initialize() {
        // Initialize filters
        conditionFilter.setItems(FXCollections.observableArrayList(
            "All Conditions", "Brand New", "Good", "Needs Maintenance", "Damaged", "Out of Service"
        ));
        conditionFilter.setValue("All Conditions");

        // Add listeners for search and filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateEquipmentDisplay());
        conditionFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateEquipmentDisplay());

        // Load equipment in background
        loadEquipmentAsync();
    }

    @FXML
    private void onBack() {
        try {
            // Load the login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/login.fxml"));
            Parent loginRoot = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) searchField.getScene().getWindow();
            
            // Create and set new scene
            Scene loginScene = new Scene(loginRoot, 800, 600);
            stage.setTitle("UNI LEND - Login");
            stage.setScene(loginScene);
            
        } catch (IOException e) {
            System.err.println("Error returning to login view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEquipmentAsync() {
        Task<List<Equipment>> loadTask = new Task<>() {
            @Override
            protected List<Equipment> call() throws Exception {
                try {
                    List<Equipment> equipment = equipmentDAO.getAllAvailableEquipment();
                    if (equipment == null || equipment.isEmpty()) {
                        throw new Exception("No equipment data available");
                    }
                    return equipment;
                } catch (Exception e) {
                    System.err.println("Error in loadEquipmentAsync: " + e.getMessage());
                    throw e;
                }
            }
        };

        loadTask.setOnSucceeded(e -> {
            try {
                allEquipment = loadTask.getValue();
                updateEquipmentDisplay();
            } catch (Exception ex) {
                System.err.println("Error processing equipment data: " + ex.getMessage());
                showError("Error displaying equipment");
            }
        });

        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            System.err.println("Failed to load equipment: " + exception.getMessage());
            exception.printStackTrace();
            showError("Failed to load equipment: " + exception.getMessage());
        });

        // Show loading indicator
        equipmentGrid.getChildren().clear();
        Label loadingLabel = new Label("Loading equipment...");
        loadingLabel.setStyle("-fx-font-size: 14px;");
        equipmentGrid.getChildren().add(loadingLabel);

        // Start loading in background
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true); // Make sure thread doesn't prevent app shutdown
        loadThread.start();
    }

    private void updateEquipmentDisplay() {
        equipmentGrid.getChildren().clear();
        
        if (allEquipment == null) return;
        
        String search = searchField.getText().toLowerCase();
        String condition = conditionFilter.getValue();
        
        // Filter in memory
        List<Equipment> filtered = allEquipment.stream()
            .filter(eq -> {
                boolean matchesSearch = search.isEmpty() || 
                                      eq.getName().toLowerCase().contains(search) ||
                                      eq.getCategory().toLowerCase().contains(search);
                boolean matchesCondition = condition.equals("All Conditions") || 
                                         eq.getCondition().equals(condition);
                return matchesSearch && matchesCondition;
            })
            .collect(Collectors.toList());
        
        for (Equipment eq : filtered) {
            VBox card = createEquipmentCard(eq);
            equipmentGrid.getChildren().add(card);
        }
    }

    private VBox createEquipmentCard(Equipment equipment) {
        VBox card = new VBox();
        card.setPrefWidth(200);
        card.setMinHeight(250); // Set minimum height to keep cards uniform
        card.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; " +
                      "-fx-border-color: #ddd; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2); " +
                      "-fx-padding: 10;");

        // Image container with fixed height
        VBox imageContainer = new VBox();
        imageContainer.setMinHeight(120);
        imageContainer.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        imageContainer.getChildren().add(imageView);

        // Load image asynchronously
        Task<Image> imageLoadTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                // Try file system first (faster)
                String[] possibleNames = {
                    equipment.getName() + ".png",
                    equipment.getName().toLowerCase().replace(" ", "-") + ".png",
                    equipment.getName().toLowerCase().replace(" ", "_") + ".png"
                };
                
                for (String name : possibleNames) {
                    URL imageUrl = getClass().getResource("/images_png/" + name);
                    if (imageUrl != null) {
                        return new Image(imageUrl.toExternalForm());
                    }
                }
                
                // If file not found, try database
                byte[] imageData = equipmentDAO.getEquipmentImage(equipment.getId());
                if (imageData != null && imageData.length > 0) {
                    return new Image(new java.io.ByteArrayInputStream(imageData));
                }
                
                // Fallback to default image
                URL fallbackUrl = getClass().getResource("/images_png/fallback.png");
                return fallbackUrl != null ? new Image(fallbackUrl.toExternalForm()) : null;
            }
        };
        
        imageLoadTask.setOnSucceeded(e -> {
            Image image = imageLoadTask.getValue();
            if (image != null && !image.isError()) {
                imageView.setImage(image);
            }
        });
        
        // Start loading image in background
        new Thread(imageLoadTask).start();
        
        // Set loading placeholder
        URL loadingUrl = getClass().getResource("/images_png/loading.png");
        if (loadingUrl != null) {
            imageView.setImage(new Image(loadingUrl.toExternalForm()));
        }

        // Info container for consistent spacing
        VBox infoContainer = new VBox(5); // 5px spacing between elements
        infoContainer.setPadding(new Insets(10, 0, 0, 0)); // Top padding after image

        // Name label
        Label nameLabel = new Label(equipment.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);

        // Category label
        Label categoryLabel = new Label(equipment.getCategory());
        categoryLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        
        // Condition label
        Label conditionLabel = new Label(equipment.getCondition());
        conditionLabel.setStyle(getConditionStyle(equipment.getCondition()));
        conditionLabel.setMaxWidth(Double.MAX_VALUE);
        conditionLabel.setPadding(new Insets(4, 8, 4, 8));

        // Add all info elements with consistent spacing
        infoContainer.getChildren().addAll(nameLabel, categoryLabel, conditionLabel);
        
        // Add all components to card
        card.getChildren().addAll(imageContainer, infoContainer);

        // Add hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #f0f0f0; -fx-border-radius: 10; -fx-background-radius: 10; " +
            "-fx-border-color: #bbb; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 2); " +
            "-fx-padding: 10;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; " +
            "-fx-border-color: #ddd; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2); " +
            "-fx-padding: 10;"
        ));

        return card;
    }

    private String getConditionStyle(String condition) {
        String baseStyle = "-fx-background-radius: 4; -fx-font-size: 11px; -fx-padding: 4 8; " +
                          "-fx-alignment: CENTER-LEFT; -fx-max-width: infinity; ";
        
        if (condition == null) return baseStyle;
        switch (condition) {
            case "Brand New":
                return baseStyle + "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;";
            case "Good":
                return baseStyle + "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;";
            case "Needs Maintenance":
                return baseStyle + "-fx-background-color: #fff8e1; -fx-text-fill: #fbc02d;";
            case "Damaged":
                return baseStyle + "-fx-background-color: #eeeeee; -fx-text-fill: #757575;";
            case "Out of Service":
                return baseStyle + "-fx-background-color: #ffebee; -fx-text-fill: #c62828;";
            default:
                return baseStyle + "-fx-background-color: #e0e0e0; -fx-text-fill: #757575;";
        }
    }

    private void showError(String message) {
        // Make sure we're on the JavaFX Application Thread
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(() -> showError(message));
            return;
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
