package de.terranova.nations.settlements.RegionTypes;

import java.time.Instant;
import java.util.UUID;

public class Transaction {

    public String user;
    public int amount;
    public Instant date;

    Transaction(String user, int amount, Instant date) {
        this.user = user;
        this.amount = amount;
        this.date = date;
    }

}
