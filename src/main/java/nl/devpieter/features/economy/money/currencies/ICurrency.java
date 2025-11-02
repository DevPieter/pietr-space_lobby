package nl.devpieter.features.economy.money.currencies;

import net.kyori.adventure.text.Component;

public interface ICurrency {

    String getId();

    Component getName();

    Component getSymbol();

    Component format(double amount);
}
