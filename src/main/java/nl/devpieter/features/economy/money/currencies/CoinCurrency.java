package nl.devpieter.features.economy.money.currencies;

import net.kyori.adventure.text.Component;
import nl.devpieter.utils.minestom.TextUtils;

public class CoinCurrency extends Currency {

    @Override
    public String getId() {
        return "coin";
    }

    @Override
    public Component getName() {
        return TextUtils.miniText("<gradient:#ffd700:#ffa500>Coin");
    }

    @Override
    public Component getSymbol() {
        return TextUtils.miniText("<#ffd700>€");
    }

    @Override
    public Component format(double amount) {
        return TextUtils.miniText("<gradient:#ffd700:#ffa500>€%,.2f", amount);
    }
}
