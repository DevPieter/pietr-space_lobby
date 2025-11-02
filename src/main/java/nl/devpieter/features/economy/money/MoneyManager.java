package nl.devpieter.features.economy.money;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import nl.devpieter.features.economy.money.currencies.CoinCurrency;
import nl.devpieter.features.economy.money.currencies.ICurrency;
import nl.devpieter.features.economy.money.currencies.ShardCurrency;
import nl.devpieter.features.economy.money.models.BalanceHolder;
import nl.devpieter.utils.minestom.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MoneyManager {

    private static MoneyManager INSTANCE;

    private final List<ICurrency> currencies = new ArrayList<>();

    private final HashMap<UUID, List<BalanceHolder>> balanceHolders = new HashMap<>();

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

    public @Nullable ICurrency getCurrency(String id) {
        for (ICurrency currency : currencies) {
            if (!currency.getId().equals(id)) continue;
            return currency;
        }

        return null;
    }

    public void grantBalance(Player player, String balanceId, double amount) {
        if (amount <= 0) return;

        BalanceHolder balanceHolder = getBalance(player.getUuid(), balanceId);
        balanceHolder.grantBalance(amount);

        Component message = TextUtils.combine(
                TextUtils.miniText("<#65c97c>You received"),
                Component.space(),
                MoneyManager.getInstance().getCurrency(balanceId).format(amount),
                Component.space(),
                TextUtils.miniText("<#65c97c>in your"),
                Component.space(),
                MoneyManager.getInstance().getCurrency(balanceId).getName(),
                Component.space(),
                TextUtils.miniText("<#65c97c>balance.")
        );

        player.sendMessage(message);
    }

    public boolean subtractBalance(Player player, String balanceId, double amount) {
        if (amount <= 0) return false;

        BalanceHolder balanceHolder = getBalance(player.getUuid(), balanceId);
        return balanceHolder.subtractBalance(amount, false);
    }

    private @NotNull BalanceHolder getBalance(UUID uuid, String balanceId) {
        balanceHolders.putIfAbsent(uuid, new ArrayList<>());
        List<BalanceHolder> holders = balanceHolders.get(uuid);

        for (BalanceHolder holder : holders) {
            if (!holder.balanceId().equals(balanceId)) continue;
            return holder;
        }

        BalanceHolder newHolder = new BalanceHolder(uuid, balanceId);
        holders.add(newHolder);

        return newHolder;
    }
}
