package de.terranova.nations.regions.bank;

import java.time.Instant;

public class Transaction {

    public String user;
    public int amount;
    public Instant instant;
    public int total;

    public Transaction(String user, int amount, Instant instant, int total) {
        this.user = user;
        this.amount = amount;
        this.instant = instant;
        this.total = total;
    }

}
