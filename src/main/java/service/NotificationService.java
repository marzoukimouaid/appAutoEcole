package service;

import dao.NotificationDao;
import entite.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class NotificationService {

    private final NotificationDao notificationDao = new NotificationDao();


    public boolean sendNotification(int userId, String message) {

        Notification notification = new Notification(userId, message, LocalDateTime.now(), false);
        return notificationDao.create(notification);
    }


    public Optional<Notification> getNotificationById(int id) {
        return notificationDao.findById(id);
    }


    public List<Notification> getNotificationsForUser(int userId) {
        return notificationDao.findByUserId(userId);
    }


    public boolean markNotificationAsRead(int notificationId) {
        Optional<Notification> opt = notificationDao.findById(notificationId);
        if (opt.isPresent()) {
            Notification notification = opt.get();
            notification.setRead(true);
            return notificationDao.update(notification);
        }
        return false;
    }


    public boolean deleteNotification(int notificationId) {
        return notificationDao.delete(notificationId);
    }
}
