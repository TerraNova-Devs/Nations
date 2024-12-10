package de.terranova.nations.regions.bank;

import org.jetbrains.annotations.ApiStatus;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface BankHolder {

    Bank getBank();

    @ApiStatus.OverrideOnly
    default void onTransaction(String record, int credit){}

    UUID getId();
}