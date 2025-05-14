package asm2_clone.controller;

import asm2_clone.db.EquipmentDAO;
import asm2_clone.db.LendingRecordDAO;
import asm2_clone.model.Equipment;
import asm2_clone.model.LendingRecord;
import asm2_clone.model.ProfessionalStaff;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

public class ProfessionalDashboardEquipmentController {
    @FXML private FlowPane equipmentGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> conditionFilter;
    @FXML private Button backButton;
    @FXML private AnchorPane loadingOverlay;

    private ProfessionalStaff professional;
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final Set<Equipment> selectedEquipments = new HashSet<>();
    private List<Equipment> allEquipments;


    @FXML
    public void initialize() {
        conditionFilter.setItems(FXCollections.observableArrayList(
                "All Conditions", "Brand New", "Good", "Needs Maintenance", "Damaged", "Out of Service"
        ));
        conditionFilter.setValue("All Conditions");

        conditionFilter.setOnAction(e -> updateEquipmentDisplay());
        searchField.textProperty().addListener((obs, oldText, newText) -> updateEquipmentDisplay());
    }

    public void setProfessional(ProfessionalStaff professional) {
        this.professional = professional;
        allEquipments = equipmentDAO.getAllAvailableEquipment();
        updateEquipmentDisplay();
    }

    private void updateEquipmentDisplay() {
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
        if (selectedEquipments.isEmpty() || professional == null) return;

        // 顯示輸入對話框要求填寫 purpose
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Borrowing Purpose");
        dialog.setHeaderText("Enter the purpose for borrowing:");
        dialog.setContentText("Purpose:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "You must enter a purpose to proceed.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String purpose = result.get().trim();

        // 加入 loading 遮罩（如果你有 UI 結構是 StackPane + loadingOverlay）
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(true);
        }

        // 🔄 新執行緒避免凍結 UI
        new Thread(() -> {
            LendingRecord record = new LendingRecord();
            record.setRecordId(LendingRecordDAO.generateNextRecordId());
            record.setBorrower(professional);
            record.setBorrowDate(new Date(System.currentTimeMillis()));
            record.setStatus(LendingRecord.Status.BORROWED);
            record.setPurpose(purpose);
            record.setEquipmentList(new ArrayList<>(selectedEquipments));

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

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "✅ Borrowing Success!", ButtonType.OK);
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "❌ Failed to borrow equipment.", ButtonType.OK);
                    alert.showAndWait();
                }
            });
        }).start();
    }

    @FXML
    private void onUsersTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/professional_dashboard.fxml"));
            Parent userRoot = loader.load();

            // 傳遞 professional 資料
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

    @FXML
    private void onHistoryTabClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/asm2_clone/professional_dashboard_borrowing.fxml"));
            Parent equipmentRoot = loader.load();
            ProfessionalDashboardBorrowingController controller = loader.getController();
            controller.setProfessional(professional);
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
