package de.terranova.nations.regions.bank;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.command.CommandAnnotation;
import de.terranova.nations.regions.base.TerraSelectCache;
import de.terranova.nations.regions.access.AccessLevel;
import org.bukkit.entity.Player;

import static de.terranova.nations.regions.base.NationCommandUtil.hasAccess;
import static de.terranova.nations.regions.base.NationCommandUtil.hasSelect;

public class BankCommands {

    @CommandAnnotation(
            domain = "bank.balance",
            permission = "terra.bank.balance",
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
            domain = "bank.deposit.$<amount>",
            permission = "terra.bank.deposit",
            description = "Deposits an amount to the bank",
            usage = "/terra bank deposit <amount>"
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
            amount = Integer.parseInt(args[2]);
            if(amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            p.sendMessage(Chat.errorFade("Bitte nutze /t bank (<withdraw|deposit>) (<value>)"));
            p.sendMessage(Chat.errorFade("Bitte gib als value eine Zahl zwischen 1 und 2304!"));
            return false;
        }
        Integer deposit = bank.getBank().cashInFromInv(p, amount);
        if(deposit == null) {
            p.sendMessage("Error während Zahlung");
            return false;
        }
        p.sendMessage("" + deposit);
        return true;
    }

    @CommandAnnotation(
            domain = "bank.withdraw.$<amount>",
            permission = "terra.bank.withdraw",
            description = "Withdraws an amount from the bank",
            usage = "/terra bank withdraw <amount>"
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
            amount = Integer.parseInt(args[2]);
            if(amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            p.sendMessage(Chat.errorFade("Bitte nutze /t bank (<withdraw|deposit>) (<value>)"));
            p.sendMessage(Chat.errorFade("Bitte gib als value eine Zahl zwischen 1 und 2304!"));
            return false;
        }
        Integer withdraw = bank.getBank().cashOutFromInv(p, amount);
        if(withdraw == null) {
            p.sendMessage("Error während Zahlung");
            return false;
        }
        p.sendMessage("" + withdraw);
        return true;
    }

    @CommandAnnotation(
            domain = "bank.history",
            permission = "terra.bank.history",
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
                p.sendMessage(Chat.cottonCandy(String.format("Transaktion: %s -> %s am %s (%s)",t.user,t.amount,Chat.prettyInstant(t.timestamp.toInstant()),t.total)));
            }
        }
        return true;
    }
}
