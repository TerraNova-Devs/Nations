package de.terranova.nations.regions.bank;

import java.sql.Timestamp;
import java.time.Instant;

import static de.mcterranova.terranovaLib.violetData.violetSerialization.databaseTimestampRE;

public class Transaction {

    public String user;
    public int amount;
    public Instant date;

    public Transaction(String user, int amount, Instant date) {
        this.user = user;
        this.amount = amount;
        this.date = date;
    }

    public Transaction(String user, int amount, Timestamp date) {
        this.user = user;
        this.amount = amount;
        this.date = databaseTimestampRE(date);
    }

}
