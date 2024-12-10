package de.terranova.nations.regions.bank;

import de.mcterranova.terranovaLib.InventoryUtil.ItemTransfer;
import de.mcterranova.terranovaLib.database.UniqueTimestampGenerator;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.base.RegionType;
import de.terranova.nations.regions.base.RegionTypeListener;
import de.terranova.nations.regions.rank.RankedRegion;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Bank implements RegionTypeListener {

    private static final UniqueTimestampGenerator tsg = new UniqueTimestampGenerator();

    private final List<Transaction> transactions;
    private int credit;
    private boolean transactionInProgress = false;
    private final BankDatabase bankDatabase;
    private final RegionType regionType;
    private final BankHolder bankHolder;

    public Bank(RegionType regionType) {
        if(!(regionType instanceof BankHolder bankHolderr)) throw new IllegalArgumentException();
        this.bankDatabase = new BankDatabase(regionType.getId());
        this.transactions = bankDatabase.getLatestTransactions();
        Optional<Transaction> latestTransaction = bankDatabase.getLatestBankStatus();
        latestTransaction.ifPresent(transaction -> this.credit = transaction.total);
        this.regionType = regionType;
        this.bankHolder = bankHolderr;
        regionType.addListener(this);
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
        Timestamp timestamp = tsg.generate(regionType.getId());
        Transaction transaction = new Transaction(record, amount, timestamp, credit += amount);

        bankDatabase.insertTransaction(transaction);
        transactions.add(transaction);
        NationsPlugin.nationsLogger.logInfo("(Transfer) Type: " + regionType.getType() + ", ID: " + regionType.getId() + ", Name: " + regionType.getName() + ", User: " + record + ", Amount: " + amount + ", BankCredit: " + credit);
        bankHolder.onTransaction(record, amount);
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

    @Override
    public void onRegionTypeRemoved(){
        bankDatabase.deleteAllEntries();
    }
}
