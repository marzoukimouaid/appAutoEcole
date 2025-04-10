package entite;

import java.time.LocalDate;

public class PaymentInstallment {

    public enum Status {
        PENDING, PAID
    }

    private int installmentId;
    private int paymentId;
    private int installmentNumber;
    private LocalDate dueDate;
    private double amountDue;
    private Status status;
    private LocalDate datePaid;
    private boolean notified;

    public PaymentInstallment() {}


    public PaymentInstallment(int installmentId, int paymentId, int installmentNumber, LocalDate dueDate,
                              double amountDue, Status status, LocalDate datePaid, boolean notified) {
        this.installmentId = installmentId;
        this.paymentId = paymentId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.amountDue = amountDue;
        this.status = status;
        this.datePaid = datePaid;
        this.notified = notified;
    }


    public PaymentInstallment(int paymentId, int installmentNumber, LocalDate dueDate, double amountDue, Status status) {
        this.paymentId = paymentId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.amountDue = amountDue;
        this.status = status;
        this.datePaid = null;
        this.notified = false;
    }


    public int getInstallmentId() {
        return installmentId;
    }
    public void setInstallmentId(int installmentId) {
        this.installmentId = installmentId;
    }
    public int getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }
    public int getInstallmentNumber() {
        return installmentNumber;
    }
    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public double getAmountDue() {
        return amountDue;
    }
    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public LocalDate getDatePaid() {
        return datePaid;
    }
    public void setDatePaid(LocalDate datePaid) {
        this.datePaid = datePaid;
    }
    public boolean isNotified() {
        return notified;
    }
    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
