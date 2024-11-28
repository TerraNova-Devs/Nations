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

    public Bank(BankHolder holder) {
        this.credit = 0;
        transactions = new ArrayList<>();
    }

    public Bank(BankHolder holder, int credit) {
        this.credit = credit;
        this.holder = holder;
        this.transactions = holder.dataBaseRetrieveBank();
    }

    public void cashIn(Player p, int amount, RegionType type) {
        if (!startCashTransaction(p)) return;

        try {
            int charged = ItemTransfer.charge(p, "terranova_silver", amount, false);
            updateBankBalance(p, type, charged, "deposited");
        } finally {
            transactionInProgress = false;
        }
    }
    
    public void cashOut(Player p, int amount, RegionType type) {
        if (!startCashTransaction(p)) return;

        try {
            int credited = ItemTransfer.credit(p, "terranova_silver", Math.min(amount, credit), false);
            updateBankBalance(p, type, -credited, "withdrew");
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

    private void updateBankBalance(Player p, RegionType regionType, int amount, String action) {



        if (transactions.size() >= 50) transactions.removeFirst();
        Timestamp time = Timestamp.from(Instant.now());
        transactions.add(new Transaction(p.getName(), amount, time));

        credit += amount;
        holder.dataBaseCallTransaction(credit, amount, p.getName(), time);

        Bukkit.getLogger().info(String.format("Player %s -> Settlement %s -> %s %s, Total Amount: %s", p.getName(), regionType.getName(), action, Math.abs(amount), credit));
        p.sendMessage(Chat.greenFade(String.format("You have successfully %s %s from the settlement %s's treasury. New total: %s.", action, Math.abs(amount), regionType.getName(), credit)));

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
