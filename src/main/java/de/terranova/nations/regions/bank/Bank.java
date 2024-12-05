package de.terranova.nations.regions.bank;

import de.mcterranova.terranovaLib.InventoryUtil.ItemTransfer;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.base.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Bank {

    private BankHolder holder;
    private final List<Transaction> transactions;
    private int credit;
    private boolean transactionInProgress = false;


    public Bank(BankHolder holder) {
        this.credit = 0;
        this.holder = holder;
        transactions = new ArrayList<>();
    }

    public Bank(BankHolder holder, int credit) {
        this.credit = credit;
        this.holder = holder;
        this.transactions = holder.dataBaseRetrieveBank();
    }

    public Integer cashInFromInv(Player p, int amount) {
        if (!startCashTransaction(p)) return null;
        int charged;

        try {
            charged = ItemTransfer.charge(p, "terranova_silver", amount, false);
            updateBankBalance(p.getName(), charged);

        } finally {
            transactionInProgress = false;

        }
        return charged;
    }
    
    public Integer cashOutFromInv(Player p, int amount) {
        if (!startCashTransaction(p)) return null;
        int credited;
        try {
            credited = ItemTransfer.credit(p, "terranova_silver", Math.min(amount, credit), false);
            updateBankBalance(p.getName(),  -credited);
        } finally {
            transactionInProgress = false;
        }
        return credited;
    }

    public void cashTransfer(String record, int amount) {
        if (!startCashTransaction()) return;

        try {
            updateBankBalance(record, amount);
        } finally {
            transactionInProgress = false;
        }
    }

    private boolean startCashTransaction(Player p) {
        if (transactionInProgress) {
            p.sendMessage(Chat.errorFade("An error occurred while using the bank. Please try again."));
            return false;
        }
        transactionInProgress = true;
        return true;
    }

    private boolean startCashTransaction() {
        if (transactionInProgress) return false;
        transactionInProgress = true;
        return true;
    }

    private void updateBankBalance(String record, int amount) {

        if (transactions.size() >= 50) transactions.removeFirst();
        Timestamp time = Timestamp.from(Instant.now());
        transactions.add(new Transaction(record, amount, time));

        credit += amount;
        holder.dataBaseCallTransaction(credit, amount, record, time);

        holder.onTransaction(record, credit);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getCredit() {
        return credit;
    }

    public Bank getBank() {
        return this;
    }

    public void setHolder(BankHolder holder) {
        this.holder = holder;
    }
}
