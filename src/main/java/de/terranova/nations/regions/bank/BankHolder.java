package de.terranova.nations.regions.bank;

import org.jetbrains.annotations.ApiStatus;

import java.sql.Timestamp;
import java.util.List;

public interface BankHolder {
    Bank getBank();
    @ApiStatus.OverrideOnly
    default void onTransaction(String record, int credit){}
    List<Transaction> dataBaseRetrieveBank();
    void dataBaseCallTransaction(int value, int amount, String username, Timestamp timestamp);
}