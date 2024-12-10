package de.terranova.nations.regions.bank;

import java.sql.Timestamp;

public class Transaction {

    public String user;
    public int amount;
    public Timestamp timestamp;
    public int total;

    public Transaction(String user, int amount, Timestamp timestamp, int total) {
        this.user = user;
        this.amount = amount;
        this.timestamp = timestamp;
        this.total = total;
    }

}
