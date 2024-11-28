package de.terranova.nations.regions.bank;

import java.sql.Timestamp;
import java.util.List;

public interface BankHolder {
    Bank getBank();
    List<Transaction> dataBaseRetrieveBank();
    void dataBaseCallTransaction(int value, int amount, String username, Timestamp timestamp);
}