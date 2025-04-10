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


        double codeSuccessRate = calculateCodeExamSuccessRate(startDate, endDate);
        double conduitSuccessRate = calculateConduitExamSuccessRate(startDate, endDate);



        Map<Integer, Long> vehicleUsageRaw = calculateVehicleUsage(startDate, endDate);

        AnalyticsStats stats = new AnalyticsStats();
        stats.setCodeExamSuccessRate(codeSuccessRate);
        stats.setConduitExamSuccessRate(conduitSuccessRate);


        Map<String, Long> usageByImmat = convertUsageToImmat(vehicleUsageRaw);
        stats.setVehicleUsageByLabel(usageByImmat);

        return stats;
    }



    
    public List<RevenueExpensePoint> getMonthlyRevenueExpenses(int monthsCount) {
        LocalDate today = LocalDate.now();
        List<RevenueExpensePoint> list = new ArrayList<>();


        for (int i = monthsCount - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today).minusMonths(i);
            LocalDate startOfMonth = ym.atDay(1);
            LocalDate endOfMonth   = ym.atEndOfMonth();

            double rev = getRevenueBetween(startOfMonth, endOfMonth);
            double exp = getExpensesBetween(startOfMonth, endOfMonth);


            String label = ym.getMonth().toString().substring(0,3) + " " + ym.getYear();

            RevenueExpensePoint point = new RevenueExpensePoint(label, rev, exp);
            list.add(point);
        }

        return list;
    }

    
    public List<RevenueExpensePoint> getYearlyRevenueExpenses(int yearsCount) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        List<RevenueExpensePoint> list = new ArrayList<>();



        for (int i = yearsCount - 1; i >= 0; i--) {
            int year = currentYear - i;

            LocalDate startOfYear = LocalDate.of(year, 1, 1);
            LocalDate endOfYear   = LocalDate.of(year, 12, 31);

            double rev = getRevenueBetween(startOfYear, endOfYear);
            double exp = getExpensesBetween(startOfYear, endOfYear);

            RevenueExpensePoint point = new RevenueExpensePoint(String.valueOf(year), rev, exp);
            list.add(point);
        }

        return list;
    }



    
    private double getRevenueBetween(LocalDate start, LocalDate end) {
        double revenue = 0.0;

        List<Payment> allPayments = paymentService.getAllPayments();
        for (Payment pay : allPayments) {
            if ("PAID".equalsIgnoreCase(pay.getStatus())) {
                LocalDate pd = pay.getPaymentDate();
                if (pd != null && !pd.isBefore(start) && !pd.isAfter(end)) {
                    revenue += pay.getTotalAmount();
                }
            }
        }

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

        List<ExamenCode> codeExams = examenCodeService.getAllExamenCodes();
        for (ExamenCode ex : codeExams) {
            if (ex.getPaiementStatus() == ExamenCode.PaymentStatus.PAID) {
                LocalDate examDate = ex.getExamDatetime().toLocalDate();
                if (!examDate.isBefore(start) && !examDate.isAfter(end)) {
                    revenue += ex.getPrice();
                }
            }
        }

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

    
    private double getExpensesBetween(LocalDate start, LocalDate end) {
        double expenses = 0.0;


        List<VehiculeMaintenance> maints = maintenanceService.getAllMaintenance();
        for (VehiculeMaintenance vm : maints) {
            LocalDate d = vm.getDateMaintenance();
            if (d != null && !d.isBefore(start) && !d.isAfter(end)) {
                expenses += vm.getCost();
            }
        }

        List<VehiculeDocument> docs = documentService.getAllDocuments();
        for (VehiculeDocument doc : docs) {
            LocalDate docDate = doc.getDateObtention();
            if (docDate != null && !docDate.isBefore(start) && !docDate.isAfter(end)) {
                expenses += doc.getCost();
            }
        }



        long daysBetween = ChronoUnit.DAYS.between(start, end);


        int payMultiplier = (daysBetween >= 360) ? 12 : (daysBetween >= 25 ? 1 : 0);

        if (payMultiplier > 0) {
            List<Moniteur> allMons = moniteurService.getAllMoniteurs();
            double monthlySum = allMons.stream().mapToDouble(Moniteur::getSalaire).sum();
            expenses += (monthlySum * payMultiplier);
        }

        return expenses;
    }



    
    private Map<Integer, Long> calculateVehicleUsage(LocalDate start, LocalDate end) {
        Map<Integer, Long> usageMap = new HashMap<>();


        List<SeanceConduit> seances = seanceConduitService.getAllSeances();
        for (SeanceConduit sc : seances) {
            LocalDate d = sc.getSessionDatetime().toLocalDate();
            if (!d.isBefore(start) && !d.isAfter(end)) {
                int vId = sc.getVehiculeId();
                usageMap.put(vId, usageMap.getOrDefault(vId, 0L) + 1);
            }
        }


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

    
    private Map<String, Long> convertUsageToImmat(Map<Integer, Long> usageMap) {
        Map<String, Long> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, Long> entry : usageMap.entrySet()) {
            Integer vehId = entry.getKey();
            Long usageCount = entry.getValue();

            String label = "Véhicule " + vehId;
            Optional<Vehicule> optVeh = vehiculeService.getVehiculeById(vehId);
            if (optVeh.isPresent()) {
                label = optVeh.get().getImmatriculation();
                label += " (x" + usageCount + ")";
            }
            result.put(label, usageCount);
        }

        return result;
    }



    
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

    
    public static class AnalyticsStats {
        private double codeExamSuccessRate;
        private double conduitExamSuccessRate;

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
