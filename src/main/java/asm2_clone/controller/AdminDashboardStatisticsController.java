package asm2_clone.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class AdminDashboardStatisticsController {
    @FXML private Label totalUsersLabel;
    @FXML private Label totalEquipmentLabel;
    @FXML private Label activeBorrowsLabel;
    @FXML private Label overdueItemsLabel;
    @FXML private Label totalBorrowsLabel;
    @FXML private Label pendingBorrowsLabel;
    @FXML private Button backButton;
    @FXML private Label loadingLabel;
    @FXML private VBox statsCard;

    @FXML
    public void initialize() {
        // Show loading, hide stats
        loadingLabel.setVisible(true);
        loadingLabel.setManaged(true);
        statsCard.setVisible(false);
        statsCard.setManaged(false);

        new Thread(() -> {
            int totalUsers = asm2_clone.db.StatisticsDAO.getTotalUsers();
            int totalEquipment = asm2_clone.db.StatisticsDAO.getTotalEquipment();
            int pendingBorrows = asm2_clone.db.StatisticsDAO.getPendingBorrows();
            int activeBorrows = asm2_clone.db.StatisticsDAO.getActiveBorrows();
            int overdueItems = asm2_clone.db.StatisticsDAO.getOverdueItems();
            int totalBorrows = asm2_clone.db.StatisticsDAO.getTotalBorrows();

            javafx.application.Platform.runLater(() -> {
                totalUsersLabel.setText(String.valueOf(totalUsers));
                totalEquipmentLabel.setText(String.valueOf(totalEquipment));
                pendingBorrowsLabel.setText(String.valueOf(pendingBorrows));
                activeBorrowsLabel.setText(String.valueOf(activeBorrows));
                overdueItemsLabel.setText(String.valueOf(overdueItems));
                totalBorrowsLabel.setText(String.valueOf(totalBorrows));

                // Hide loading, show stats
                loadingLabel.setVisible(false);
                loadingLabel.setManaged(false);
                statsCard.setVisible(true);
                statsCard.setManaged(true);
            });
        }).start();
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
    private void onUsersTabClicked() { onBack(); }

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
}
