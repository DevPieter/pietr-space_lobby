package nl.devpieter.features.economy.money.models;

import java.util.UUID;

public class BalanceHolder {

    private final UUID playerId;
    public final String balanceId;

    public double amount;

    public BalanceHolder(UUID playerId, String balanceId) {
        this.playerId = playerId;
        this.balanceId = balanceId;
    }

    public UUID playerId() {
        return playerId;
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

    public void grantBalance(double amount) {
        if (amount <= 0) return;
        this.amount += amount;
    }

    public boolean subtractBalance(double amount, boolean allowNegative) {
        if (!allowNegative && this.amount - amount < 0) return false;

        this.amount -= amount;
        return true;
    }
}
