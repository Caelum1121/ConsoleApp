package asm2_clone.controller;

import asm2_clone.db.EquipmentDAO;
import asm2_clone.db.LendingRecordDAO;
import asm2_clone.model.AcademicStaff;
import asm2_clone.model.Equipment;
import asm2_clone.model.LendingRecord;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

public class AcademicEquipmentController {
    @FXML private FlowPane equipmentGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> conditionFilter;
    @FXML private AnchorPane loadingOverlay;
    @FXML private Button backButton;

    private AcademicStaff academic;  // ✅ AcademicStaff
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private List<Equipment> allEquipments;
    private final Set<Equipment> selectedEquipments = new HashSet<>();


    @FXML
    public void initialize() {
        conditionFilter.setItems(FXCollections.observableArrayList(
                "All Conditions", "Brand New", "Good", "Needs Maintenance", "Damaged", "Out of Service"
        ));
        conditionFilter.setValue("All Conditions");

        conditionFilter.setOnAction(e -> updateEquipmentDisplay());
        searchField.textProperty().addListener((obs, oldText, newText) -> updateEquipmentDisplay());
    }

    // ✅ 用於 Academic Staff
    public void setAcademic(AcademicStaff academic) {
        this.academic = academic;
        allEquipments = equipmentDAO.getAllAvailableEquipment();
        updateEquipmentDisplay();
    }

    private void updateEquipmentDisplay() {
        allEquipments = equipmentDAO.getAllAvailableEquipment();
        equipmentGrid.getChildren().clear();

        String searchText = searchField.getText().toLowerCase();
        String selectedCondition = conditionFilter.getValue();

        List<Equipment> filtered = allEquipments.stream()
                .filter(e -> e.getName().toLowerCase().contains(searchText))
                .filter(e -> selectedCondition.equals("All Conditions") || e.getCondition().equals(selectedCondition))
                .collect(Collectors.toList());

        for (Equipment eq : filtered) {
            equipmentGrid.getChildren().add(createEquipmentCard(eq));
        }
    }

    private VBox createEquipmentCard(Equipment equipment) {
        VBox card = new VBox(10);
        card.setPrefWidth(200);
        card.setStyle(baseCardStyle());
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.TOP_CENTER);

        // ✅ 勾選圖示（隱藏）
        Label checkMark = new Label("✓");
        checkMark.setStyle("-fx-text-fill: green; -fx-font-size: 24px;");
        checkMark.setVisible(false);

        // ✅ 放右上角
        HBox checkContainer = new HBox(checkMark);
        checkContainer.setAlignment(Pos.TOP_RIGHT);

        // 圖片
        ImageView imageView = new ImageView();
        InputStream imgStream = getClass().getResourceAsStream("/images_png/" + equipment.getName() + ".png");
        if (imgStream != null) {
            imageView.setImage(new Image(imgStream));
        } else {
            InputStream fallback = getClass().getResourceAsStream("/images_png/fallback.png");
            if (fallback != null) {
                imageView.setImage(new Image(fallback));
            }
        }
        imageView.setFitWidth(160);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(equipment.getName());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label categoryLabel = new Label(equipment.getCategory());
        categoryLabel.setStyle("-fx-text-fill: #666;");
        Label conditionLabel = new Label(equipment.getCondition());
        conditionLabel.setStyle(getConditionStyle(equipment.getCondition()));

        card.getChildren().addAll(checkContainer, imageView, nameLabel, categoryLabel, conditionLabel);

        // ✅ 點擊選取／取消選取
        card.setOnMouseClicked(event -> {
            if (selectedEquipments.contains(equipment)) {
                selectedEquipments.remove(equipment);
                checkMark.setVisible(false);
                card.setStyle(baseCardStyle());
            } else {
                selectedEquipments.add(equipment);
                checkMark.setVisible(true);
                card.setStyle(hoverCardStyle());
            }
        });

        return card;
    }

    private String getConditionStyle(String condition) {
        return switch (condition) {
            case "Brand New" -> "-fx-text-fill: green;";
            case "Good" -> "-fx-text-fill: #009688;";
            case "Needs Maintenance" -> "-fx-text-fill: orange;";
            case "Damaged" -> "-fx-text-fill: red;";
            case "Out of Service" -> "-fx-text-fill: gray;";
            default -> "-fx-text-fill: black;";
        };
    }

    // Style methods
    private String baseCardStyle() {
        return "-fx-background-color: white; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #ddd; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);";
    }

    private String hoverCardStyle() {
        return "-fx-background-color: #f0f0f0; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #bbb; " +
                "-fx-cursor: hand;";
    }

    @FXML
    private void onSubmitSelected() {
        if (selectedEquipments.isEmpty() || academic == null) return;

        // 顯示輸入借用目的的對話框
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

        if (loadingOverlay != null) {
            loadingOverlay.setVisible(true);
        }

        new Thread(() -> {
            // 建立 LendingRecord 物件
            LendingRecord record = new LendingRecord();
            record.setRecordId(LendingRecordDAO.generateNextRecordId());
            record.setBorrower(academic);
            record.setBorrowDate(new Date(System.currentTimeMillis()));
            record.setStatus(LendingRecord.Status.BORROWED);
            record.setPurpose(purpose);
            record.setCourse(null);  // academic 借用不需 course
            record.setEquipmentList(new ArrayList<>(selectedEquipments));

            // 執行資料庫操作並提示結果
            boolean success = LendingRecordDAO.insertLendingRecord(record);

            javafx.application.Platform.runLater(() -> {
                if (loadingOverlay != null) loadingOverlay.setVisible(false);

                if (success) {
                    for (Equipment eq : selectedEquipments) {
                        EquipmentDAO.updateStatus(eq.getId(), "Borrowed");
                    }
                    selectedEquipments.clear();
                    allEquipments = equipmentDAO.getAllAvailableEquipment();
                    updateEquipmentDisplay();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Borrowing successful!", ButtonType.OK);
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create borrowing record.", ButtonType.OK);
                    alert.showAndWait();
                }
            });
        }).start();
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
    private void onHistoryTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/academic_borrowing.fxml"));
            Parent equipmentRoot = loader.load();
            AcademicBorrowingController controller = loader.getController();
            controller.setAcademic(academic);
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene equipmentScene = new Scene(equipmentRoot, 1000, 700);
            stage.setTitle("UNI LEND - Borrowing History");
            stage.setScene(equipmentScene);
        } catch (Exception e) {
            System.err.println("Error loading equipment borrowing dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onPendingTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/academic_pending.fxml"));
            Parent statsRoot = loader.load();
            AcademicPendingController controller = loader.getController();
            controller.setAcademic(academic);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(statsRoot, 1000, 700));
            stage.setTitle("UNI LEND - Pending Requests");
        } catch (Exception e) {
            e.printStackTrace();
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
