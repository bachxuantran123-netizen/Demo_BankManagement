package src.Entities;

public abstract class BankAccount {
    protected int id;
    protected String accountCode;
    protected String accountName;
    protected String creationDate;

    public BankAccount(int id, String accountCode, String accountName, String creationDate) {
        this.id = id;
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.creationDate = creationDate;
    }

    public int getId() {
        return id;
    }
    public String getAccountCode() {
        return accountCode;
    }
    public String getAccountName() {
        return accountName;
    }
    public String getCreationDate() {
        return creationDate;
    }

    public abstract String toCSV();

    @Override
    public String toString() {
        return String.format("| %-4d | %-12s | %-20s | %-12s |", id, accountCode, accountName, creationDate);
    }
}