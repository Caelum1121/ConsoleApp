package asm2_clone.controller;

import asm2_clone.db.LendingRecordDAO;
import asm2_clone.model.Equipment;
import asm2_clone.model.LendingRecord;
import asm2_clone.model.ProfessionalStaff;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class ProfessionalDashboardBorrowingController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private VBox historyContainer;
    @FXML private AnchorPane loadingOverlay;
    @FXML private Button backButton;

    private ProfessionalStaff professional;
    private List<LendingRecord> records;
    private final Map<String, Set<Equipment>> selectedEquipmentsMap = new HashMap<>();


    @FXML
    public void initialize() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "Borrowed", "Returned", "Overdue"));
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> updateDisplay());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateDisplay());
    }

    public void setProfessional(ProfessionalStaff professional) {
        this.professional = professional;
        this.records = LendingRecordDAO.getRecordsByProfessionalId(professional.getId());
        updateDisplay();
    }

    public void updateDisplay() {
        historyContainer.getChildren().clear();
        selectedEquipmentsMap.clear();

        String keyword = searchField.getText().toLowerCase();
        String selectedStatus = statusFilter.getValue();

        for (LendingRecord record : records) {
            List<Equipment> filteredEquipment = new ArrayList<>();
            for (Equipment eq : record.getEquipmentList()) {
                String eqName = eq.getName().toLowerCase();
                String eqStatus = eq.getStatus().toLowerCase();
                String purpose = record.getPurpose().toLowerCase();
                if ((keyword.isEmpty() || eqName.contains(keyword) || purpose.contains(keyword)) &&
                        (selectedStatus.equals("All") || eqStatus.equalsIgnoreCase(selectedStatus.toLowerCase()))) {
                    filteredEquipment.add(eq);
                }
            }

            if (filteredEquipment.isEmpty()) continue;

            VBox card = new VBox(10);
            card.setPadding(new Insets(16));
            card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
            """);

            Label nameLabel = new Label(filteredEquipment.stream().map(Equipment::getName).reduce((a,b)->a+", "+b).orElse("(No equipment)"));
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label borrowLabel = new Label("Borrowed: " + record.getBorrowDate());
            Label dueLabel = new Label("Due: " + (record.getReturnDate() != null ? record.getReturnDate() : "-"));
            borrowLabel.setStyle("-fx-text-fill: #666;");
            dueLabel.setStyle("-fx-text-fill: #666;");

            VBox dateBox = new VBox(borrowLabel, dueLabel);
            dateBox.setSpacing(2);

            Label rightStatus = new Label();
            rightStatus.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #8e44ad;");
            if (filteredEquipment.size() == 1 && filteredEquipment.get(0).getStatus().equalsIgnoreCase("borrowed")) {
                rightStatus.setText("Borrowed");
            }

            HBox topRow = new HBox(nameLabel, rightStatus);
            topRow.setSpacing(10);
            topRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            HBox.setHgrow(rightStatus, Priority.NEVER);
            topRow.setAlignment(Pos.CENTER);
            topRow.setSpacing(20);
            topRow.setStyle("-fx-alignment: center-left;");

            card.getChildren().addAll(topRow, dateBox);

            VBox equipmentListBox = new VBox(6);
            Set<Equipment> selected = new HashSet<>();
            selectedEquipmentsMap.put(record.getRecordId(), selected);

            boolean showCheckbox = filteredEquipment.size() > 1;
            for (Equipment eq : filteredEquipment) {
                String eqStatus = eq.getStatus().toLowerCase();

                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);

                Label label = new Label(eq.getName());
                Label tag = new Label(eqStatus.substring(0,1).toUpperCase() + eqStatus.substring(1));
                tag.setStyle("-fx-font-weight: bold;");

                switch (eqStatus) {
                    case "returned" -> tag.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    case "borrowed" -> tag.setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold;");
                    case "overdue" -> tag.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }

                CheckBox cb = new CheckBox();
                if (eqStatus.equals("borrowed") && showCheckbox) {
                    cb.setOnAction(e -> {
                        if (cb.isSelected()) selected.add(eq);
                        else selected.remove(eq);
                    });
                } else {
                    cb.setVisible(false);
                    cb.setManaged(false);
                }

                row.getChildren().addAll(cb, label, tag);
                equipmentListBox.getChildren().add(row);
            }

            card.getChildren().add(equipmentListBox);

            if (filteredEquipment.size() == 1 && filteredEquipment.get(0).getStatus().equalsIgnoreCase("borrowed")) {
                Button returnBtn = new Button("Return");
                styleReturnButton(returnBtn);
                Equipment eq = filteredEquipment.get(0);
                returnBtn.setOnAction(e -> handleReturn(record.getRecordId(), List.of(eq)));
                card.getChildren().add(returnBtn);
            } else if (filteredEquipment.stream().anyMatch(eq -> eq.getStatus().equalsIgnoreCase("borrowed"))) {
                Button returnBtn = new Button("Return Selected");
                styleReturnButton(returnBtn);
                returnBtn.setOnAction(e -> handleReturn(record.getRecordId(), new ArrayList<>(selected)));
                card.getChildren().add(returnBtn);
            }

            historyContainer.getChildren().add(card);
        }
    }

    private void handleReturn(String recordId, List<Equipment> equipmentsToReturn) {
        if (equipmentsToReturn.isEmpty()) return;

        loadingOverlay.setVisible(true);

        new Thread(() -> {
            boolean allSuccess = true;
            for (Equipment eq : equipmentsToReturn) {
                if (!LendingRecordDAO.returnEquipment(recordId, eq.getId())) {
                    allSuccess = false;
                }
            }

            boolean finalResult = allSuccess;
            Platform.runLater(() -> {
                loadingOverlay.setVisible(false);
                Alert alert = new Alert(finalResult ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        finalResult ? "✅ Return successful!" : "❌ Some returns failed. Please try again.",
                        ButtonType.OK);
                alert.showAndWait();
                reload();
            });
        }).start();
    }

    private void styleReturnButton(Button btn) {
        btn.setStyle("""
        -fx-background-color: black;
        -fx-text-fill: white;
        -fx-padding: 6 20 6 20;
        -fx-background-radius: 8;
        -fx-cursor: hand;
    """);
    }

    @FXML
    private void onEquipmentTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/professional_dashboard_equipment.fxml"));
            Parent equipmentRoot = loader.load();
            ProfessionalDashboardEquipmentController controller = loader.getController();
            controller.setProfessional(professional);
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene equipmentScene = new Scene(equipmentRoot, 1000, 700);
            stage.setTitle("UNI LEND - Equipment Borrowing");
            stage.setScene(equipmentScene);
        } catch (Exception e) {
            System.err.println("Error loading equipment borrowing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onUsersTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/professional_dashboard.fxml"));
            Parent userRoot = loader.load();

            ProfessionalDashboardController controller = loader.getController();
            controller.setProfessionalId(professional.getId());
            controller.loadProfessionalProfile();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene userScene = new Scene(userRoot, 1000, 700);
            stage.setTitle("UNI LEND - Professional Dashboard");
            stage.setScene(userScene);
        } catch (Exception e) {
            System.err.println("Error loading user dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reload() {
        this.records = LendingRecordDAO.getRecordsByProfessionalId(professional.getId());
        updateDisplay();
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
}