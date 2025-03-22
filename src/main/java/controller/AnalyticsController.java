package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import service.AnalyticsService;
import service.AnalyticsService.AnalyticsStats;
import service.AnalyticsService.RevenueExpensePoint;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AnalyticsController implements Initializable {

    @FXML
    private Label labelRevenue;
    @FXML
    private Label labelExpenses;
    @FXML
    private Label labelCodeSuccess;
    @FXML
    private Label labelConduitSuccess;

    @FXML
    private LineChart<String, Number> lineChartRevenue;   // NEW: separate chart
    @FXML
    private LineChart<String, Number> lineChartExpenses;  // NEW: separate chart

    @FXML
    private PieChart pieChartVehicleUsage;
    @FXML
    private PieChart pieChartExamSuccess;

    @FXML
    private ToggleGroup periodToggleGroup;

    private final AnalyticsService analyticsService = new AnalyticsService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Default: "month"
        loadStats("month");

        // Listen for toggle changes
        periodToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String period = newVal.getUserData().toString();  // "month" or "year"
                loadStats(period);
            }
        });
    }

    private void loadStats(String period) {
        // 1) Basic stats: exam success, usage
        AnalyticsStats stats = analyticsService.getAnalyticsStats(period);

        double codeSuccess = stats.getCodeExamSuccessRate() * 100.0;
        double conduitSuccess = stats.getConduitExamSuccessRate() * 100.0;
        labelCodeSuccess.setText(String.format("%.1f %%", codeSuccess));
        labelConduitSuccess.setText(String.format("%.1f %%", conduitSuccess));

        // 2) Clear the line charts
        lineChartRevenue.getData().clear();
        lineChartExpenses.getData().clear();
        lineChartRevenue.setCreateSymbols(true);
        lineChartRevenue.setAnimated(false);
        lineChartExpenses.setCreateSymbols(true);
        lineChartExpenses.setAnimated(false);

        // We'll build the data from multi-point logic
        List<RevenueExpensePoint> points;
        if ("month".equalsIgnoreCase(period)) {
            points = analyticsService.getMonthlyRevenueExpenses(12);
        } else {
            // year
            points = analyticsService.getYearlyRevenueExpenses(5);
        }

        // Build a single Series for Revenue, a single Series for Expenses
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenus (TND)");

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Dépenses (TND)");

        // Fill them with data
        for (RevenueExpensePoint p : points) {
            XYChart.Data<String, Number> revData = new XYChart.Data<>(p.getLabel(), p.getRevenue());
            attachTooltip(revData, p.getLabel(), "Revenus", p.getRevenue());
            revenueSeries.getData().add(revData);

            XYChart.Data<String, Number> expData = new XYChart.Data<>(p.getLabel(), p.getExpenses());
            attachTooltip(expData, p.getLabel(), "Dépenses", p.getExpenses());
            expenseSeries.getData().add(expData);
        }

        // Add each Series to the separate line charts
        lineChartRevenue.getData().add(revenueSeries);
        lineChartExpenses.getData().add(expenseSeries);

        // Show final data point's revenue/expense in top labels
        RevenueExpensePoint lastPoint = points.get(points.size() - 1);
        labelRevenue.setText(String.format("%.2f TND", lastPoint.getRevenue()));
        labelExpenses.setText(String.format("%.2f TND", lastPoint.getExpenses()));

        // 3) Vehicle usage
        pieChartVehicleUsage.getData().clear();
        Map<String, Long> usageMap = stats.getVehicleUsageByLabel();
        for (Map.Entry<String, Long> e : usageMap.entrySet()) {
            String label = e.getKey();
            long count = e.getValue();
            PieChart.Data slice = new PieChart.Data(label, count);
            pieChartVehicleUsage.getData().add(slice);
        }

        // 4) Exam success pie
        pieChartExamSuccess.getData().clear();
        pieChartExamSuccess.getData().add(new PieChart.Data("Code", codeSuccess));
        pieChartExamSuccess.getData().add(new PieChart.Data("Conduit", conduitSuccess));
    }

    /**
     * Helper to attach a tooltip to each data point.
     */
    private void attachTooltip(XYChart.Data<String, Number> data, String label, String type, double value) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                String msg = String.format("%s: %s\n%s: %.2f TND", type.equals("Revenus") ? "Période" : "Période",
                        label, type, value);
                Tooltip t = new Tooltip(msg);
                Tooltip.install(newNode, t);
            }
        });
    }
}
