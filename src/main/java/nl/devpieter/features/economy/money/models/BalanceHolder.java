package nl.devpieter.features.economy.money.models;

public class BalanceHolder {

    public final String balanceId;
    public double amount;

    public BalanceHolder(String balanceId) {
        this.balanceId = balanceId;
    }

    public String balanceId() {
        return balanceId;
    }

    public double amount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void addAmount(double amount) {
        this.amount += amount;
    }

    public boolean subtractAmount(double amount, boolean allowNegative) {
        if (!allowNegative && this.amount - amount < 0) return false;

        this.amount -= amount;
        return true;
    }
}
