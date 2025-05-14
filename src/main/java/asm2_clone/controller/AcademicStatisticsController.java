package asm2_clone.controller;

import asm2_clone.db.AcademicDAO;
import asm2_clone.service.LendingRecordService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Map;

public class AcademicStatisticsController {
    @FXML public Label studentTotalLabel;
    @FXML public Label selfTotalLabel;
    @FXML public Label equipmentTab;
    @FXML public Button backButton;

    @FXML private BarChart<String, Number> courseChart;
    @FXML private NumberAxis yAxis;

    @FXML private Label studentBorrowedLabel, studentOverdueLabel, studentReturnedLabel;
    @FXML private Label selfBorrowedLabel, selfOverdueLabel, selfReturnedLabel;
    @FXML private Label studentPendingLabel, selfPendingLabel;

    private String academicId;

    public void setAcademicId(String id) {
        this.academicId = id;
        loadSupervisedStudentChartAndStats();
        loadSelfStatistics(); // Separate for academic's own records
    }

    // Combines chart + stats loading for supervised students
    private void loadSupervisedStudentChartAndStats() {
        System.out.println("Loading supervised student stats + chart for academic ID: " + academicId);
        if (academicId == null) return;

        LendingRecordService service = new LendingRecordService();
        Map<String, Map<String, Integer>> courseData = service.getBorrowingStatusByCourse(academicId);
        Map<String, Map<String, Integer>> allStats = service.getSeparatedStatsByAcademicId(academicId);
        Map<String, Integer> studentStats = allStats.getOrDefault("student", Map.of());

        courseChart.getData().clear();
        XYChart.Series<String, Number> borrowedSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> returnedSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> overdueSeries = new XYChart.Series<>();

        borrowedSeries.setName("Borrowed");
        returnedSeries.setName("Returned");
        overdueSeries.setName("Overdue");

        int maxY = 0;

        for (String course : courseData.keySet()) {
            Map<String, Integer> stat = courseData.get(course);
            int b = stat.getOrDefault("borrowed", stat.getOrDefault("approved", 0));
            int r = stat.getOrDefault("returned", 0);
            int o = stat.getOrDefault("overdue", 0);
            int p = stat.getOrDefault("pending", 0); // just in case

            System.out.printf("[DEBUG] Course: %s → borrowed=%d, returned=%d, overdue=%d, pending=%d%n", course, b, r, o, p);

            maxY = Math.max(maxY, b + r + o); // exclude pending
            borrowedSeries.getData().add(new XYChart.Data<>(course, b));
            returnedSeries.getData().add(new XYChart.Data<>(course, r));
            overdueSeries.getData().add(new XYChart.Data<>(course, o));
        }

        courseChart.getData().addAll(borrowedSeries, returnedSeries, overdueSeries);
        courseChart.setLegendVisible(true);
        courseChart.setTitle("Borrowed Equipment per Course (by Status)");
        courseChart.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(Math.max(10, maxY));
        yAxis.setTickUnit(1);

        Platform.runLater(() -> {
            styleSeries(borrowedSeries, "#0000ff"); // blue
            styleSeries(returnedSeries, "#008000"); // green
            styleSeries(overdueSeries, "#ff0000");  // red

            // Delay legend styling to ensure it's rendered
            Platform.runLater(() -> {
                courseChart.lookupAll(".chart-legend-item").forEach(node -> {
                    Label label = (Label) node.lookup(".chart-legend-item-label");
                    Node symbol = node.lookup(".chart-legend-item-symbol");
                    if (label != null && symbol != null) {
                        switch (label.getText()) {
                            case "Borrowed" -> symbol.setStyle("-fx-background-color: #0000ff;");
                            case "Returned" -> symbol.setStyle("-fx-background-color: #008000;");
                            case "Overdue"  -> symbol.setStyle("-fx-background-color: #ff0000;");
                        }
                    }
                });
            });
        });

        int borrowed = studentStats.getOrDefault("borrowed", studentStats.getOrDefault("approved", 0));
        int returned = studentStats.getOrDefault("returned", 0);
        int overdue  = studentStats.getOrDefault("overdue", 0);
        int pending  = studentStats.getOrDefault("pending", 0);
        int total = borrowed + returned + overdue;
        System.out.println("[DEBUG] Student Stats Summary: " + studentStats);

        studentTotalLabel.setText("Total: " + total + " items");
        studentBorrowedLabel.setText(String.valueOf(borrowed));
        studentReturnedLabel.setText(String.valueOf(returned));
        studentOverdueLabel.setText(String.valueOf(overdue));
        studentPendingLabel.setText(String.valueOf(pending));
    }

    // Self (academic's own borrowing)
    private void loadSelfStatistics() {
        LendingRecordService service = new LendingRecordService();
        Map<String, Map<String, Integer>> stats = service.getSeparatedStatsByAcademicId(academicId);
        Map<String, Integer> selfStats = stats.getOrDefault("self", Map.of());

        int borrowed = selfStats.getOrDefault("borrowed", selfStats.getOrDefault("approved", 0));
        int returned = selfStats.getOrDefault("returned", 0);
        int overdue  = selfStats.getOrDefault("overdue", 0);
        int pending  = selfStats.getOrDefault("pending", 0);
        int total = borrowed + returned + overdue;

        selfTotalLabel.setText("Total: " + total + " items");
        selfBorrowedLabel.setText(String.valueOf(borrowed));
        selfReturnedLabel.setText(String.valueOf(returned));
        selfOverdueLabel.setText(String.valueOf(overdue));
        selfPendingLabel.setText(String.valueOf(pending));
    }

    private void styleSeries(XYChart.Series<String, Number> series, String hexColor) {
        for (XYChart.Data<String, Number> dataPoint : series.getData()) {
            Node node = dataPoint.getNode();
            if (node != null) {
                node.setStyle("-fx-bar-fill: " + hexColor + ";");

                Label label = new Label(String.valueOf(dataPoint.getYValue()));
                label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");
                if (node instanceof StackPane pane) {
                    pane.getChildren().add(label);
                    StackPane.setAlignment(label, javafx.geometry.Pos.TOP_CENTER);
                }
                System.out.println("Applying style to " + series.getName() + " bar: " + dataPoint.getXValue() + " = " + dataPoint.getYValue());
                Tooltip.install(node, new Tooltip(series.getName() + ": " + dataPoint.getYValue()));
            }
        }
    }

    @FXML
    private void onDashboardClicked() {
        switchScene("/asm2_clone/academic_dashboard.fxml", controller -> {
            ((AcademicDashboardController) controller).setAcademicId(academicId);
        });
    }

    @FXML
    private void onHistoryClicked() {
        switchScene("/asm2_clone/academic_borrowing.fxml", controller -> {
            ((AcademicBorrowingController) controller).setAcademic(new AcademicDAO().getAcademicByUserId(academicId));
        });
    }

    @FXML
    private void onEquipmentClicked() {
        switchScene("/asm2_clone/academic_equipment.fxml", controller -> {
            ((AcademicEquipmentController) controller).setAcademic(new AcademicDAO().getAcademicByUserId(academicId));
        });
    }

    @FXML
    private void onPendingTabClicked() {
        switchScene("/asm2_clone/academic_pending.fxml", controller -> {
            ((AcademicPendingController) controller).setAcademic(new AcademicDAO().getAcademicByUserId(academicId));
        });
    }

    private void switchScene(String fxmlPath, SceneControllerConfigurator configurator) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            configurator.configure(controller);
            Stage stage = (Stage) courseChart.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private interface SceneControllerConfigurator {
        void configure(Object controller);
    }
}