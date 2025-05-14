package asm2_clone.controller;

import asm2_clone.db.LendingRecordDAO;
import asm2_clone.model.LendingRecord;
import asm2_clone.model.Equipment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class AdminDashboardBorrowingController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<LendingRecord> recordTable;
    @FXML private TableColumn<LendingRecord, String> userCol;
    @FXML private TableColumn<LendingRecord, String> equipmentCol;
    @FXML private TableColumn<LendingRecord, String> statusCol;
    @FXML private TableColumn<LendingRecord, String> borrowDateCol;
    @FXML private TableColumn<LendingRecord, String> dueDateCol;
    @FXML private Button backButton;

    private ObservableList<LendingRecord> allRecords;

    @FXML
    public void initialize() {
        // Set up status filter
        statusFilter.setItems(FXCollections.observableArrayList(
            "All Status", "BORROWED", "OVERDUE", "RETURNED"
        ));
        statusFilter.setValue("All Status");
        
        setupTableColumns();
        setupFilters();
        
        // Show loading indicator
        recordTable.setPlaceholder(new Label("Loading records..."));
        
        // Load records in background
        Task<ObservableList<LendingRecord>> loadTask = new Task<>() {
            @Override
            protected ObservableList<LendingRecord> call() {
                return LendingRecordDAO.getAllRecords();
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            allRecords = loadTask.getValue();
            recordTable.setItems(allRecords);
            recordTable.setPlaceholder(new Label("No records found"));
        });
        
        loadTask.setOnFailed(e -> {
            recordTable.setPlaceholder(new Label("Error loading records"));
            loadTask.getException().printStackTrace();
        });
        
        new Thread(loadTask).start();

        // Enforce transparent background and border at runtime
        recordTable.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        recordTable.setBorder(null);
        recordTable.setBackground(null);
    }

    private void setupTableColumns() {
        // User column
        userCol.setCellValueFactory(cellData -> {
            String borrowerName = cellData.getValue().getBorrowerName();
            String borrowerId = cellData.getValue().getBorrower_id();
            return new SimpleStringProperty(borrowerName + " (" + borrowerId + ")");
        });

        // Equipment column
        equipmentCol.setCellValueFactory(cellData -> {
            List<String> equipment = cellData.getValue().getEquipmentNames();
            if (equipment == null || equipment.isEmpty()) {
                return new SimpleStringProperty("No equipment");
            }
            return new SimpleStringProperty("• " + String.join("\n• ", equipment));
        });

        // Status column
        statusCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toString())
        );

        // Borrow Date column
        borrowDateCol.setCellValueFactory(cellData -> {
            Date borrowDate = cellData.getValue().getBorrow_date();
            if (borrowDate == null) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(new SimpleDateFormat("yyyy-MM-dd").format(borrowDate));
        });

        // Due Date column
        dueDateCol.setCellValueFactory(cellData -> {
            Date dueDate = cellData.getValue().getReturnDate();
            if (dueDate == null) return new SimpleStringProperty("N/A");
            return new SimpleStringProperty(new SimpleDateFormat("yyyy-MM-dd").format(dueDate));
        });

        // Add row factory for selection highlighting
        recordTable.setRowFactory(tv -> new TableRow<LendingRecord>() {
            @Override
            protected void updateItem(LendingRecord item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setStyle("");
                } else {
                    if (isSelected()) {
                        setStyle("-fx-background-color: #0078D4;");
                        for (Node node : getChildren()) {
                            if (node instanceof TableCell) {
                                ((TableCell) node).setTextFill(javafx.scene.paint.Color.WHITE);
                            }
                        }
                    } else {
                        if (getIndex() % 2 == 0) {
                            setStyle("-fx-background-color: white;");
                        } else {
                            setStyle("-fx-background-color: #f5f5f5;");
                        }
                        updateTextColors();
                    }
                }
            }

            private void updateTextColors() {
                for (Node node : getChildren()) {
                    if (node instanceof TableCell) {
                        TableCell cell = (TableCell) node;
                        if (cell.getTableColumn() == statusCol) {
                            String status = (String) cell.getItem();
                            if (status != null) {
                                switch (status) {
                                    case "BORROWED" -> cell.setTextFill(javafx.scene.paint.Color.web("#2196F3"));
                                    case "OVERDUE" -> cell.setTextFill(javafx.scene.paint.Color.web("#f44336"));
                                    case "RETURNED" -> cell.setTextFill(javafx.scene.paint.Color.web("#4CAF50"));
                                }
                            }
                        } else {
                            cell.setTextFill(javafx.scene.paint.Color.BLACK);
                        }
                    }
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if (selected) {
                    setStyle("-fx-background-color: #0078D4;");
                    for (Node node : getChildren()) {
                        if (node instanceof TableCell) {
                            ((TableCell) node).setTextFill(javafx.scene.paint.Color.WHITE);
                        }
                    }
                } else {
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: white;");
                    } else {
                        setStyle("-fx-background-color: #f5f5f5;");
                    }
                    updateTextColors();
                }
            }
        });

        // Set column widths
        userCol.setPrefWidth(250);
        equipmentCol.setPrefWidth(300);
        statusCol.setPrefWidth(150);
        borrowDateCol.setPrefWidth(150);
        dueDateCol.setPrefWidth(150);

        // Style the table
        recordTable.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;");
        recordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        for (TableColumn<?, ?> col : recordTable.getColumns()) {
            col.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");
        }
    }

    private void setupFilters() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> 
            filterRecords(newValue, statusFilter.getValue())
        );

        statusFilter.setOnAction(e -> 
            filterRecords(searchField.getText(), statusFilter.getValue())
        );
    }

    private void loadRecords() {
        allRecords = LendingRecordDAO.getAllRecords();
        recordTable.setItems(allRecords);
    }

    private void filterRecords(String searchText, String status) {
        if (allRecords == null) return;

        ObservableList<LendingRecord> filteredList = allRecords.filtered(record -> {
            boolean matchesSearch = true;
            boolean matchesStatus = true;

            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseSearch = searchText.toLowerCase();
                String borrowerName = record.getBorrowerName() != null ? record.getBorrowerName().toLowerCase() : "";
                matchesSearch = borrowerName.contains(lowerCaseSearch) ||
                              record.getBorrower_id().toLowerCase().contains(lowerCaseSearch);
            }

            if (status != null && !status.equals("All Status")) {
                matchesStatus = record.getStatus().toString().equals(status.toUpperCase());
            }

            return matchesSearch && matchesStatus;
        });

        recordTable.setItems(filteredList);
    }

    @FXML
    private void onUsersTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("UNI LEND - Admin Dashboard");
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
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("UNI LEND - Equipment Dashboard");
            stage.setScene(scene);
        } catch (Exception e) {
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

    @FXML
    private void onStatisticsTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/admin_dashboard_statistics.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("UNI LEND - Statistics");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
