package src.Entities;

public class PaymentAccount extends BankAccount {
    private String cardNumber;
    private double balance;

    public PaymentAccount(int id, String code, String name, String date, String cardNum, double balance) {
        super(id, code, name, date);
        this.cardNumber = cardNum;
        this.balance = balance;
    }

    public String getCardNumber() {
        return cardNumber;
    }
    public double getBalance() {
        return balance;
    }

    @Override
    public String toCSV() {
        return id + "," + accountCode + "," + accountName + "," + creationDate + "," +
                cardNumber + "," + balance;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" PAYMENT | Card: %-15s | Bal: $%,.0f", cardNumber, balance);
    }
}