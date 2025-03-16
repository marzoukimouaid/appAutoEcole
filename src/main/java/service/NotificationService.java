package service;

import dao.NotificationDao;
import entite.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class NotificationService {

    private final NotificationDao notificationDao = new NotificationDao();

    // Sends a notification to a user with the given message.
    public boolean sendNotification(int userId, String message) {
        // Create notification with current timestamp and unread status
        Notification notification = new Notification(userId, message, LocalDateTime.now(), false);
        return notificationDao.create(notification);
    }

    // Retrieves a notification by its id
    public Optional<Notification> getNotificationById(int id) {
        return notificationDao.findById(id);
    }

    // Retrieves all notifications for a specific user
    public List<Notification> getNotificationsForUser(int userId) {
        return notificationDao.findByUserId(userId);
    }

    // Marks a notification as read
    public boolean markNotificationAsRead(int notificationId) {
        Optional<Notification> opt = notificationDao.findById(notificationId);
        if (opt.isPresent()) {
            Notification notification = opt.get();
            notification.setRead(true);
            return notificationDao.update(notification);
        }
        return false;
    }

    // Deletes a notification
    public boolean deleteNotification(int notificationId) {
        return notificationDao.delete(notificationId);
    }
}
