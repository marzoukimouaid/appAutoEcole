package service;

import dao.PaymentDao;
import entite.Payment;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PaymentService {

    private final PaymentDao paymentDao = new PaymentDao();


    public boolean createFullPayment(int userId, double totalAmount, LocalDate paymentDate) {
        Payment payment = new Payment(userId, totalAmount, "FULL", paymentDate, "PAID");
        return paymentDao.create(payment);
    }


    public Optional<Payment> createInstallmentPayment(int userId, double totalAmount, LocalDate paymentDate) {
        Payment payment = new Payment(userId, totalAmount, "INSTALLMENT", paymentDate, "PENDING");
        boolean created = paymentDao.create(payment);
        if (created) {
            return Optional.of(payment);
        }
        return Optional.empty();
    }

    public Optional<Payment> getPaymentById(int id) {
        return paymentDao.findById(id);
    }

    public List<Payment> getPaymentsForUser(int userId) {
        return paymentDao.findByUserId(userId);
    }

    public boolean updatePayment(Payment payment) {
        return paymentDao.update(payment);
    }

    public boolean deletePayment(int id) {
        return paymentDao.delete(id);
    }

    
    public List<Payment> getAllPayments() {
        return paymentDao.findAll();
    }
}
