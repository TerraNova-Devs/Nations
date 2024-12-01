package de.terranova.nations.commands.TerraCommands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.commands.CommandAnnotation;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.bank.BankHolder;
import de.terranova.nations.regions.bank.Transaction;
import org.bukkit.entity.Player;

import static de.terranova.nations.commands.NationCommandUtil.hasAccess;
import static de.terranova.nations.commands.NationCommandUtil.hasSelect;

public class BankCommands {

    @CommandAnnotation(
            domain = "terra.bank.balance",
            permission = "nations.bank.balance",
            description = "Checks the bank balance",
            usage = "/terra bank balance"
    )
    public static boolean checkBalance(Player p, String[] args) {
        TerraSelectCache cache = hasSelect(p);
        if (cache == null) return false;
        if(!(cache.getRegion() instanceof BankHolder bank)) {
            p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keine Bank"));
            return false;
        }
        p.sendMessage(Chat.greenFade(String.format("Die balance der Stadt beträgt %s",bank.getBank().getCredit())));
        return true;
    }

    @CommandAnnotation(
            domain = "terra.bank.deposit",
            permission = "nations.bank.deposit",
            description = "Deposits an amount to the bank",
            usage = "/terra bank deposit <amount>",
            tabCompletion = {"<amount>"}
    )
    public static boolean deposit(Player p, String[] args) {
        TerraSelectCache cache = hasSelect(p);
        if (cache == null) return false;
        if(!(cache.getRegion() instanceof BankHolder bank)) {
            p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keine Bank"));
            return false;
        }
        if(!hasAccess(cache.getAccess(), AccessLevel.CITIZEN)) {
            p.sendMessage(Chat.errorFade("Du musst mindestens Member sein um in die Stadtkasse einzahlen zu können"));
            return false;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
            if(amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            p.sendMessage(Chat.errorFade("Bitte nutze /t bank (<withdraw|deposit>) (<value>)"));
            p.sendMessage(Chat.errorFade("Bitte gib als value eine Zahl zwischen 1 und 2304!"));
            return false;
        }
        bank.getBank().cashInFromInv(p, amount);
        return true;
    }

    @CommandAnnotation(
            domain = "terra.bank.withdraw",
            permission = "nations.bank.withdraw",
            description = "Withdraws an amount from the bank",
            usage = "/terra bank withdraw <amount>",
            tabCompletion = {"<amount>"}
    )
    public static boolean withdraw(Player p, String[] args) {
        TerraSelectCache cache = hasSelect(p);
        if (cache == null) return false;
        if(!(cache.getRegion() instanceof BankHolder bank)) {
            p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keine Bank"));
            return false;
        }
        if(!hasAccess(cache.getAccess(), AccessLevel.COUNCIL)) {
            p.sendMessage(Chat.errorFade("Du musst mindestens Council sein um von der Stadtkasse abheben zu können"));
            return false;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
            if(amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            p.sendMessage(Chat.errorFade("Bitte nutze /t bank (<withdraw|deposit>) (<value>)"));
            p.sendMessage(Chat.errorFade("Bitte gib als value eine Zahl zwischen 1 und 2304!"));
            return false;
        }
        bank.getBank().cashOutFromInv(p, amount);
        return true;
    }

    @CommandAnnotation(
            domain = "terra.bank.history",
            permission = "nations.bank.history",
            description = "Shows you the banks recent transactions",
            usage = "/terra bank history"
    )
    public static boolean history(Player p, String[] args) {
        TerraSelectCache cache = hasSelect(p);
        if (cache == null) return false;
        if(!(cache.getRegion() instanceof BankHolder bank)) {
            p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keine Bank"));
            return false;
        }
        if(!hasAccess(cache.getAccess(), AccessLevel.CITIZEN)) {
            p.sendMessage(Chat.errorFade("Du musst mindestens Member sein um die Historie sehen zu können"));
            return false;
        }
        if(bank.getBank().getTransactions().isEmpty()) {
            p.sendMessage("No Transactions found");
        } else {
            for (Transaction t : bank.getBank().getTransactions()) {
                p.sendMessage(Chat.cottonCandy(String.format("Transaktion: %s -> %s am %s",t.user,t.amount,Chat.prettyInstant(t.date))));
            }
        }
        return true;
    }
}
