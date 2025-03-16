package Utils;

import entite.VehiculeDocument;
import entite.User;
import service.NotificationService;
import service.UserService;
import service.VehiculeDocumentService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DocumentExpiryNotificationScheduler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final VehiculeDocumentService documentService = new VehiculeDocumentService();
    private final UserService userService = new UserService();
    private final NotificationService notificationService = new NotificationService();

    /**
     * Starts the scheduler to check for documents expiring within 24 hours every 5 seconds (for testing).
     */
    public void start() {
        Runnable task = () -> {
            try {
                System.out.println("[Scheduler] Checking for expiring documents...");
                // Find documents expiring within the next 1 day that haven't been notified.
                List<VehiculeDocument> expiringDocs = documentService.findDocumentsExpiringSoon(1);
                System.out.println("[Scheduler] Found " + expiringDocs.size() + " expiring document(s) that haven't been notified.");
                if (!expiringDocs.isEmpty()) {
                    // Retrieve all secretaires
                    List<User> secretaires = userService.getSecretaires();
                    System.out.println("[Scheduler] Found " + secretaires.size() + " secretaire(s).");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    for (VehiculeDocument doc : expiringDocs) {
                        String message = "Attention: Le document " + doc.getDocType().name() +
                                " pour le véhicule ID " + doc.getVehiculeId() +
                                " expire le " + doc.getDateExpiration().format(formatter) + ".";
                        // Send notification to each secretaire
                        secretaires.forEach(sec -> {
                            boolean sent = notificationService.sendNotification(sec.getId(), message);
                            if (sent) {
                                System.out.println("[Scheduler] Sent notification to secretaire id: " + sec.getId() +
                                        " with message: " + message);
                            } else {
                                System.err.println("[Scheduler] FAILED to send notification to secretaire id: " + sec.getId());
                            }
                        });
                        // Mark the document as notified to prevent redundant notifications.
                        doc.setNotified(true);
                        boolean updated = documentService.updateDocument(doc);
                        if (updated) {
                            System.out.println("[Scheduler] Marked document id " + doc.getDocId() + " as notified.");
                        } else {
                            System.err.println("[Scheduler] FAILED to mark document id " + doc.getDocId() + " as notified.");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[Scheduler] Exception in scheduled task:");
                e.printStackTrace();
            }
        };

        // For testing, schedule every 5 seconds. (Change to 1 hour for production.)
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);
    }

    /**
     * Stops the scheduler.
     */
    public void stop() {
        scheduler.shutdown();
        System.out.println("[Scheduler] Scheduler stopped.");
    }
}
