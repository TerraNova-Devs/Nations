package de.terranova.nations.regions.modules.bank;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.dao.BankDAO;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionListener;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.InventoryUtil.ItemTransfer;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Bank implements RegionListener {

    private final List<Transaction> transactions;
    private int credit;
    private boolean transactionInProgress = false;
    private final BankDAO bankDAO;
    private final Region region;
    private final BankHolder bankHolder;

    public Bank(Region region) {
        if (!(region instanceof BankHolder bankHolder)) throw new IllegalArgumentException();
        this.bankDAO = new BankDAO();
        this.transactions = new LinkedList<>(bankDAO.getLatestTransactions(region.getId()));
        Optional<Transaction> latestTransaction = bankDAO.getBankCredit(region.getId());
        latestTransaction.ifPresent(transaction -> this.credit = transaction.total);
        this.region = region;
        this.bankHolder = bankHolder;
        region.addListener(this);
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
            updateBankBalance(p.getName(), -credited);
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
        if (transactions.size() >= 50) transactions.remove(0);
        Transaction transaction = new Transaction(record, amount, InstantGenerator.generateInstant(region.getId()), credit += amount);
        bankDAO.insertTransaction(region.getId(), transaction);
        transactions.add(transaction);
        NationsPlugin.nationsLogger.logInfo("(Transfer) Type: " + region.getType() + ", ID: " + region.getId() + ", Name: " + region.getName() + ", User: " + record + ", Amount: " + amount + ", BankCredit: " + credit);
        bankHolder.onTransaction(record, amount);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getCredit() {
        return credit;
    }

    @Override
    public void onRegionRenamed(String newRegionName) {
    }

    @Override
    public void onRegionRemoved() {
        bankDAO.deleteAllEntries(region.getId());
    }
}