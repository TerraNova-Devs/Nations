package de.terranova.nations.regions.modules.bank;

import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

public interface BankHolder {

    Bank getBank();

    @ApiStatus.OverrideOnly
    default void onTransaction(String record, int credit){}

    UUID getId();
}