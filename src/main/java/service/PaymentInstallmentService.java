package service;

import dao.PaymentInstallmentDao;
import entite.PaymentInstallment;
import entite.PaymentInstallment.Status;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PaymentInstallmentService {

    private final PaymentInstallmentDao installmentDao = new PaymentInstallmentDao();

    public boolean createInstallment(PaymentInstallment installment) {
        return installmentDao.create(installment);
    }

    public Optional<PaymentInstallment> getInstallmentById(int installmentId) {
        return installmentDao.findById(installmentId);
    }

    public List<PaymentInstallment> getInstallmentsByPaymentId(int paymentId) {
        return installmentDao.findByPaymentId(paymentId);
    }

    public boolean updateInstallment(PaymentInstallment installment) {
        return installmentDao.update(installment);
    }

    /**
     * Marks an installment as paid.
     */
    public boolean markInstallmentAsPaid(int installmentId, LocalDate datePaid) {
        Optional<PaymentInstallment> opt = installmentDao.findById(installmentId);
        if(opt.isPresent()) {
            PaymentInstallment installment = opt.get();
            installment.setDatePaid(datePaid);
            installment.setStatus(Status.PAID);
            return installmentDao.update(installment);
        }
        return false;
    }
}
