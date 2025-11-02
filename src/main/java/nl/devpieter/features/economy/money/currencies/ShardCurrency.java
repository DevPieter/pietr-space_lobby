package nl.devpieter.features.economy.money.currencies;

import net.kyori.adventure.text.Component;
import nl.devpieter.utils.minestom.TextUtils;

public class ShardCurrency extends Currency {

    @Override
    public String getId() {
        return "shard";
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("<gradient:#a05fb8:#d291f0>Shard");
    }

    @Override
    public Component getSymbol() {
        return TextUtils.miniText("<#a05fb8>\uD83D\uDC8E");
    }

    @Override
    public Component format(double amount) {
        return TextUtils.miniText("<gradient:#a05fb8:#d291f0>\uD83D\uDC8E%,.2f", amount);
    }
}
