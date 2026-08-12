package nl.devpieter.features.balance;

public class BalanceManager {

    private static BalanceManager INSTANCE;

    private BalanceManager() {
    }

    public static BalanceManager getInstance() {
        if (INSTANCE == null) INSTANCE = new BalanceManager();
        return INSTANCE;
    }

    // Models:
    // - Wallet
    // - Ledger
}
