package service;

import entite.ExamenCode;
import entite.ExamenConduit;
import entite.Moniteur;
import entite.Payment;
import entite.PaymentInstallment;
import entite.VehiculeMaintenance;
import entite.VehiculeDocument;
import entite.SeanceConduit;
import entite.Vehicule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Updated AnalyticsService:
 * - Multi-point monthly/yearly line charts
 * - Vehicle usage labeled by immatriculation
 */
public class AnalyticsService {

    private final PaymentService paymentService = new PaymentService();
    private final PaymentInstallmentService installmentService = new PaymentInstallmentService();
    private final ExamenCodeService examenCodeService = new ExamenCodeService();
    private final ExamenConduitService examenConduitService = new ExamenConduitService();
    private final VehiculeMaintenanceService maintenanceService = new VehiculeMaintenanceService();
    private final VehiculeDocumentService documentService = new VehiculeDocumentService();
    private final VehiculeService vehiculeService = new VehiculeService();
    private final MoniteurService moniteurService = new MoniteurService();
    private final SeanceConduitService seanceConduitService = new SeanceConduitService();

    // ===================== MAIN ANALYTICS =====================

    /**
     * Taux de réussite, vehicle usage, etc. still computed over
     * either last month or last year as a single range (for exam success).
     * But the line chart for revenue/expenses is multi-point
     * using the new methods below.
     */
    public AnalyticsStats getAnalyticsStats(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        if ("month".equalsIgnoreCase(period)) {
            start = now.minus(1, ChronoUnit.MONTHS);
        } else {
            start = now.minus(1, ChronoUnit.YEARS);
        }

        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = now.toLocalDate();

        // For the single-range exam success rates:
        double codeSuccessRate = calculateCodeExamSuccessRate(startDate, endDate);
        double conduitSuccessRate = calculateConduitExamSuccessRate(startDate, endDate);

        // Vehicle usage map, but we will also store immatriculation labels.
        // We'll handle the labeling in a separate method below.
        Map<Integer, Long> vehicleUsageRaw = calculateVehicleUsage(startDate, endDate);

        AnalyticsStats stats = new AnalyticsStats();
        stats.setCodeExamSuccessRate(codeSuccessRate);
        stats.setConduitExamSuccessRate(conduitSuccessRate);

        // Convert usage raw data into a label -> usage map
        Map<String, Long> usageByImmat = convertUsageToImmat(vehicleUsageRaw);
        stats.setVehicleUsageByLabel(usageByImmat);

        return stats;
    }

    // ===================== LINE CHART DATA METHODS =====================

    /**
     * Returns 12 monthly data points for the last 12 months (including the current).
     * Each record has a label (e.g. "Mar 2025"), plus monthlyRevenue, monthlyExpenses.
     */
    public List<RevenueExpensePoint> getMonthlyRevenueExpenses(int monthsCount) {
        LocalDate today = LocalDate.now();
        List<RevenueExpensePoint> list = new ArrayList<>();

        // We go from oldest to newest: i = monthsCount-1 down to 0
        for (int i = monthsCount - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today).minusMonths(i);
            LocalDate startOfMonth = ym.atDay(1);
            LocalDate endOfMonth   = ym.atEndOfMonth();

            double rev = getRevenueBetween(startOfMonth, endOfMonth);
            double exp = getExpensesBetween(startOfMonth, endOfMonth);

            // e.g. label "Mar 2025"
            String label = ym.getMonth().toString().substring(0,3) + " " + ym.getYear();

            RevenueExpensePoint point = new RevenueExpensePoint(label, rev, exp);
            list.add(point);
        }

        return list;
    }

    /**
     * Returns 5 yearly data points for the last 5 years (including the current).
     * Each record has a label (e.g. "2025"), plus totalRevenue, totalExpenses for that year.
     */
    public List<RevenueExpensePoint> getYearlyRevenueExpenses(int yearsCount) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        List<RevenueExpensePoint> list = new ArrayList<>();

        // e.g. if yearsCount=5, we want [currentYear-4 ... currentYear]
        // oldest to newest
        for (int i = yearsCount - 1; i >= 0; i--) {
            int year = currentYear - i;
            // entire year from Jan 1 to Dec 31
            LocalDate startOfYear = LocalDate.of(year, 1, 1);
            LocalDate endOfYear   = LocalDate.of(year, 12, 31);

            double rev = getRevenueBetween(startOfYear, endOfYear);
            double exp = getExpensesBetween(startOfYear, endOfYear);

            RevenueExpensePoint point = new RevenueExpensePoint(String.valueOf(year), rev, exp);
            list.add(point);
        }

        return list;
    }

    // ===================== REVENUE & EXPENSE HELPERS =====================

    /**
     * Sum of all revenue (payments, installments, exam fees) between [start, end].
     */
    private double getRevenueBetween(LocalDate start, LocalDate end) {
        double revenue = 0.0;
        // 1) All main payments
        List<Payment> allPayments = paymentService.getAllPayments();
        for (Payment pay : allPayments) {
            if ("PAID".equalsIgnoreCase(pay.getStatus())) {
                LocalDate pd = pay.getPaymentDate();
                if (pd != null && !pd.isBefore(start) && !pd.isAfter(end)) {
                    revenue += pay.getTotalAmount();
                }
            }
        }
        // 2) Payment installments
        for (Payment pay : allPayments) {
            List<PaymentInstallment> installments =
                    installmentService.getInstallmentsByPaymentId(pay.getId());
            for (PaymentInstallment pi : installments) {
                if (pi.getStatus() == PaymentInstallment.Status.PAID) {
                    LocalDate dp = pi.getDatePaid();
                    if (dp != null && !dp.isBefore(start) && !dp.isAfter(end)) {
                        revenue += pi.getAmountDue();
                    }
                }
            }
        }
        // 3) ExamenCode fees
        List<ExamenCode> codeExams = examenCodeService.getAllExamenCodes();
        for (ExamenCode ex : codeExams) {
            if (ex.getPaiementStatus() == ExamenCode.PaymentStatus.PAID) {
                LocalDate examDate = ex.getExamDatetime().toLocalDate();
                if (!examDate.isBefore(start) && !examDate.isAfter(end)) {
                    revenue += ex.getPrice();
                }
            }
        }
        // 4) ExamenConduit fees
        List<ExamenConduit> conduitExams = examenConduitService.getAllExamenConduits();
        for (ExamenConduit ex : conduitExams) {
            if (ex.getPaiementStatus() == ExamenConduit.PaymentStatus.PAID) {
                LocalDate examDate = ex.getExamDatetime().toLocalDate();
                if (!examDate.isBefore(start) && !examDate.isAfter(end)) {
                    revenue += ex.getPrice();
                }
            }
        }

        return revenue;
    }

    /**
     * Sum of all expenses (maintenance, documents, moniteur salaries) between [start, end].
     * For moniteur salaries, we assume they are paid monthly
     * and we check if the pay period intersects the date range or not.
     * But in simpler terms, we might just sum them if the range is large enough.
     */
    private double getExpensesBetween(LocalDate start, LocalDate end) {
        double expenses = 0.0;

        // 1) Maintenance
        List<VehiculeMaintenance> maints = maintenanceService.getAllMaintenance();
        for (VehiculeMaintenance vm : maints) {
            LocalDate d = vm.getDateMaintenance();
            if (d != null && !d.isBefore(start) && !d.isAfter(end)) {
                expenses += vm.getCost();
            }
        }
        // 2) Vehicle docs
        List<VehiculeDocument> docs = documentService.getAllDocuments();
        for (VehiculeDocument doc : docs) {
            LocalDate docDate = doc.getDateObtention();
            if (docDate != null && !docDate.isBefore(start) && !docDate.isAfter(end)) {
                expenses += doc.getCost();
            }
        }
        // 3) Moniteurs salaries (a simple approximation—if the range is >=1 month, we add monthly salary
        // for each moniteur. If the range is >=1 year, maybe we add 12 months.
        // You can refine this logic as you wish.
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        // If we consider any range over 25 days as "1 monthly pay" (naive approach)
        // You can do something more precise if needed.
        int payMultiplier = (daysBetween >= 360) ? 12 : (daysBetween >= 25 ? 1 : 0);

        if (payMultiplier > 0) {
            List<Moniteur> allMons = moniteurService.getAllMoniteurs();
            double monthlySum = allMons.stream().mapToDouble(Moniteur::getSalaire).sum();
            expenses += (monthlySum * payMultiplier);
        }

        return expenses;
    }

    // ===================== VEHICLE USAGE =====================

    /**
     * Returns a raw usage map: vehicleId -> usageCount in [start, end].
     */
    private Map<Integer, Long> calculateVehicleUsage(LocalDate start, LocalDate end) {
        Map<Integer, Long> usageMap = new HashMap<>();

        // Seances
        List<SeanceConduit> seances = seanceConduitService.getAllSeances();
        for (SeanceConduit sc : seances) {
            LocalDate d = sc.getSessionDatetime().toLocalDate();
            if (!d.isBefore(start) && !d.isAfter(end)) {
                int vId = sc.getVehiculeId();
                usageMap.put(vId, usageMap.getOrDefault(vId, 0L) + 1);
            }
        }

        // ExamenConduit
        List<ExamenConduit> conduitExams = examenConduitService.getAllExamenConduits();
        for (ExamenConduit ex : conduitExams) {
            LocalDate d = ex.getExamDatetime().toLocalDate();
            if (!d.isBefore(start) && !d.isAfter(end)) {
                int vId = ex.getVehiculeId();
                usageMap.put(vId, usageMap.getOrDefault(vId, 0L) + 1);
            }
        }

        return usageMap;
    }

    /**
     * Convert a vehicle usage map (vehId->count) to a immatriculation->count map.
     */
    private Map<String, Long> convertUsageToImmat(Map<Integer, Long> usageMap) {
        Map<String, Long> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, Long> entry : usageMap.entrySet()) {
            Integer vehId = entry.getKey();
            Long usageCount = entry.getValue();

            String label = "Véhicule " + vehId; // fallback
            Optional<Vehicule> optVeh = vehiculeService.getVehiculeById(vehId);
            if (optVeh.isPresent()) {
                label = optVeh.get().getImmatriculation(); // use immatric as label
                label += " (x" + usageCount + ")";
            }
            result.put(label, usageCount);
        }

        return result;
    }

    // ===================== EXAM SUCCESS RATES =====================

    /**
     * Taux de réussite for code exam in [start, end]: #PASSED / (#PASSED + #FAILED + #ABSENT).
     */
    private double calculateCodeExamSuccessRate(LocalDate start, LocalDate end) {
        List<ExamenCode> codeExams = examenCodeService.getAllExamenCodes();
        int passedCount = 0;
        int totalFinished = 0;
        for (ExamenCode exam : codeExams) {
            LocalDate d = exam.getExamDatetime().toLocalDate();
            if (!d.isBefore(start) && !d.isAfter(end)) {
                if (exam.getStatus() != ExamenCode.ExamStatus.PENDING) {
                    totalFinished++;
                    if (exam.getStatus() == ExamenCode.ExamStatus.PASSED) {
                        passedCount++;
                    }
                }
            }
        }
        if (totalFinished == 0) return 0.0;
        return (double) passedCount / totalFinished;
    }

    /**
     * Taux de réussite for conduit exam in [start, end].
     */
    private double calculateConduitExamSuccessRate(LocalDate start, LocalDate end) {
        List<ExamenConduit> conduitExams = examenConduitService.getAllExamenConduits();
        int passedCount = 0;
        int totalFinished = 0;
        for (ExamenConduit exam : conduitExams) {
            LocalDate d = exam.getExamDatetime().toLocalDate();
            if (!d.isBefore(start) && !d.isAfter(end)) {
                if (exam.getStatus() != ExamenConduit.ExamStatus.PENDING) {
                    totalFinished++;
                    if (exam.getStatus() == ExamenConduit.ExamStatus.PASSED) {
                        passedCount++;
                    }
                }
            }
        }
        if (totalFinished == 0) return 0.0;
        return (double) passedCount / totalFinished;
    }

    // ===================== DATA MODELS =====================

    /**
     * Returned by getMonthlyRevenueExpenses(...) and getYearlyRevenueExpenses(...).
     * Each point has a label (like "Mar 2025" or "2023") and the
     * total revenue/expenses in that period.
     */
    public static class RevenueExpensePoint {
        private String label;
        private double revenue;
        private double expenses;

        public RevenueExpensePoint(String label, double revenue, double expenses) {
            this.label = label;
            this.revenue = revenue;
            this.expenses = expenses;
        }

        public String getLabel() {
            return label;
        }
        public double getRevenue() {
            return revenue;
        }
        public double getExpenses() {
            return expenses;
        }
    }

    /**
     * The main "Stats" object for the analytics screen,
     * excluding the multi-month/year line data (which we get from the separate methods).
     */
    public static class AnalyticsStats {
        private double codeExamSuccessRate;
        private double conduitExamSuccessRate;
        // Instead of an int->long usage map, we store immatric label->count
        private Map<String, Long> vehicleUsageByLabel;

        public double getCodeExamSuccessRate() { return codeExamSuccessRate; }
        public void setCodeExamSuccessRate(double codeExamSuccessRate) {
            this.codeExamSuccessRate = codeExamSuccessRate;
        }

        public double getConduitExamSuccessRate() { return conduitExamSuccessRate; }
        public void setConduitExamSuccessRate(double conduitExamSuccessRate) {
            this.conduitExamSuccessRate = conduitExamSuccessRate;
        }

        public Map<String, Long> getVehicleUsageByLabel() {
            return vehicleUsageByLabel;
        }
        public void setVehicleUsageByLabel(Map<String, Long> vehicleUsageByLabel) {
            this.vehicleUsageByLabel = vehicleUsageByLabel;
        }
    }
}
