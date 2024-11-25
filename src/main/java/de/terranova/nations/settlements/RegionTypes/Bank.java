package de.terranova.nations.settlements.RegionTypes;

import de.mcterranova.terranovaLib.InventoryUtil.ItemTransfer;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.settlements.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static de.mcterranova.terranovaLib.violetData.violetSerialization.databaseTimestampSE;

public interface Bank {

    default void cashIn(Player p, int amount, RegionType type) {
        if (!startCashTransaction(p)) return;

        try {
            int charged = ItemTransfer.charge(p, "terranova_silver", amount, false);
            updateBankBalance(p, type, charged, "deposited");
        } finally {
            setCashInProgress(false);
        }
    }

    default void cashOut(Player p, int amount, RegionType type) {
        if (!startCashTransaction(p)) return;

        try {
            int bank = getBank();
            int credited = ItemTransfer.credit(p, "terranova_silver", Math.min(amount, bank), false);
            updateBankBalance(p, type, -credited, "withdrew");
        } finally {
            setCashInProgress(false);
        }
    }

    private boolean startCashTransaction(Player p) {
        if (getCashInProgress()) {
            p.sendMessage(Chat.errorFade("An error occurred while using the bank. Please try again."));
            return false;
        }
        setCashInProgress(true);
        return true;
    }

    private void updateBankBalance(Player p, RegionType type, int amount, String action) {
        List<Transaction> transactionHistory = getTransactionHistory();
        int bank = getBank();

        if (transactionHistory.size() >= 50) transactionHistory.remove(0);
        Timestamp time = databaseTimestampSE(Instant.now());
        transactionHistory.add(new Transaction(p.getName(), amount, time));

        bank += amount;
        dataBaseCallBank(bank, amount, p.getName(), time);

        Bukkit.getLogger().info(String.format("Player %s -> Settlement %s -> %s %s, Total Amount: %s", p.getName(), type.name, action, Math.abs(amount), bank));
        p.sendMessage(Chat.greenFade(String.format("You have successfully %s %s from the settlement %s's treasury. New total: %s.", action, Math.abs(amount), type.name, bank)));

        setBank(bank);
        setTransactionHistory(transactionHistory);
    }

    void dataBaseCallBank(int value, int amount, String username, Timestamp timestamp);

    List<Transaction> dataBaseRetrieveBank();

    List<Transaction> getTransactionHistory();

    void setTransactionHistory(List<Transaction> transactions);

    int getBank();

    void setBank(int i);

    boolean getCashInProgress();

    void setCashInProgress(boolean state);

}
