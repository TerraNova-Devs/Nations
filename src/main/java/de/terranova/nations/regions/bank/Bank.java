package de.terranova.nations.regions.bank;

import de.mcterranova.terranovaLib.InventoryUtil.ItemTransfer;
import de.mcterranova.terranovaLib.utils.Chat;
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
    private String regionName;

    public Bank(BankHolder holder, String regionName) {
        this.credit = 0;
        this.holder = holder;
        this.regionName = regionName;
        transactions = new ArrayList<>();
    }

    public Bank(BankHolder holder, String regionName, int credit) {
        this.credit = credit;
        this.holder = holder;
        this.regionName = regionName;
        this.transactions = holder.dataBaseRetrieveBank();
    }

    public void cashInFromInv(Player p, int amount) {
        if (!startCashTransaction(p)) return;

        try {
            int charged = ItemTransfer.charge(p, "terranova_silver", amount, false);
            updateBankBalance(p.getName(), charged, "deposited");
        } finally {
            transactionInProgress = false;
        }
    }
    
    public void cashOutFromInv(Player p, int amount) {
        if (!startCashTransaction(p)) return;

        try {
            int credited = ItemTransfer.credit(p, "terranova_silver", Math.min(amount, credit), false);
            updateBankBalance(p.getName(),  -credited, "withdrew");
        } finally {
            transactionInProgress = false;
        }
    }

    public void cashTransfer(String record,String action, int amount) {
        if (!startCashTransaction()) return;

        try {
            updateBankBalance(record, amount, action);
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

    private void updateBankBalance(String record, int amount, String action) {

        if (transactions.size() >= 50) transactions.removeFirst();
        Timestamp time = Timestamp.from(Instant.now());
        transactions.add(new Transaction(record, amount, time));

        credit += amount;
        holder.dataBaseCallTransaction(credit, amount, record, time);

        Bukkit.getLogger().info(String.format("Player %s -> Settlement %s -> %s %s, Total Amount: %s", record, this.regionName, action, Math.abs(amount), credit));
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
