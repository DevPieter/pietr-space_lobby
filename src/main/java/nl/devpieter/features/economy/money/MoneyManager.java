package nl.devpieter.features.economy.money;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import nl.devpieter.features.economy.money.currencies.CoinCurrency;
import nl.devpieter.features.economy.money.currencies.ICurrency;
import nl.devpieter.features.economy.money.currencies.ShardCurrency;
import nl.devpieter.features.economy.money.models.BalanceHolder;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MoneyManager {

    private static MoneyManager INSTANCE;

    private final List<ICurrency> currencies = new ArrayList<>();

    private final HashMap<UUID, List<BalanceHolder>> balances = new HashMap<>();

    private MoneyManager() {
        currencies.addAll(List.of(
                new CoinCurrency(),
                new ShardCurrency()
        ));
    }

    public static MoneyManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MoneyManager();
        return INSTANCE;
    }

    public void grantBalance(Player player, String balanceId, double amount) {
        if (amount <= 0) return;

        BalanceHolder balanceHolder = getBalance(player.getUuid(), balanceId);
        balanceHolder.addAmount(amount);

        Component message = TextUtils.combine(
                TextUtils.miniText("<#65c97c>You received"),
                Component.space(),
                MoneyManager.getInstance().getCurrencyById(balanceId).format(amount),
                Component.space(),
                TextUtils.miniText("<#65c97c>in your"),
                Component.space(),
                MoneyManager.getInstance().getCurrencyById(balanceId).getName(),
                Component.space(),
                TextUtils.miniText("<#65c97c>balance.")
        );

        player.sendMessage(message);
    }

    public boolean subtractBalance(Player player, String balanceId, double amount, boolean allowNegative) {
        if (amount <= 0) return false;

        BalanceHolder balanceHolder = getBalance(player.getUuid(), balanceId);
        return balanceHolder.subtractAmount(amount, allowNegative);
    }

    private @NotNull BalanceHolder getBalance(UUID uuid, String balanceId) {
        List<BalanceHolder> pBalances = balances.getOrDefault(uuid, new ArrayList<>());

        for (BalanceHolder balanceHolder : pBalances) {
            if (!balanceHolder.balanceId().equals(balanceId)) continue;
            return balanceHolder;
        }

        BalanceHolder newBalanceHolder = new BalanceHolder(balanceId);
        pBalances.add(newBalanceHolder);
        balances.put(uuid, pBalances);

        return newBalanceHolder;
    }

    public ICurrency getCurrencyById(String id) {
        for (ICurrency currency : currencies) {
            if (!currency.getId().equals(id)) continue;
            return currency;
        }

        return null;
    }
}
