package de.nekyia.nations.regions.bank;

import de.mcterranova.terranovaLib.commands.CommandAnnotation;
import de.mcterranova.terranovaLib.utils.Chat;
import de.nekyia.nations.regions.access.Access;
import de.nekyia.nations.regions.base.TerraSelectCache;
import de.nekyia.nations.regions.access.AccessLevel;
import org.bukkit.entity.Player;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class BankCommands {

    public BankCommands(){

    }

    @CommandAnnotation(
            domain = "bank.balance",
            permission = "terra.bank.balance",
            description = "Checks the bank balance",
            usage = "/terra bank balance"
    )
    public boolean checkBalance(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        if (cache == null) return false;
        if(!(cache.getRegion() instanceof BankHolder bank)) {
            p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keine Bank"));
            return false;
        }
        p.sendMessage(Chat.greenFade(String.format("Die balance der Stadt beträgt: %s Silber",bank.getBank().getCredit())));
        return true;
    }

    @CommandAnnotation(
            domain = "bank.deposit.$0",
            permission = "terra.bank.deposit",
            description = "Deposits an amount to the bank",
            usage = "/terra bank deposit <amount>"
    )
    public boolean deposit(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        if (cache == null) return false;
        if(!(cache.getRegion() instanceof BankHolder bank)) {
            p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keine Bank"));
            return false;
        }
        if(!Access.hasAccess(cache.getAccess(), AccessLevel.CITIZEN)) {
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
        p.sendMessage(Chat.cottonCandy("Du hast erfolgreich: " + deposit + " Silber eingezahlt."));
        return true;
    }
    @CommandAnnotation(
            domain = "bank.debug",
            permission = "nations.admin",
            description = "xxxx",
            usage = "xxxx"
    )
    public boolean debug(Player p, String[] args) {

        p.sendMessage("Returned Timestamp: " + TimestampGenerator.processUUID(UUID.randomUUID()));


        return true;
    }
    @CommandAnnotation(
            domain = "bank.withdraw.$0",
            permission = "terra.bank.withdraw",
            description = "Withdraws an amount from the bank",
            usage = "/terra bank withdraw <amount>"
    )
    public boolean withdraw(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        if (cache == null) return false;
        if(!(cache.getRegion() instanceof BankHolder bank)) {
            p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keine Bank"));
            return false;
        }
        if(!Access.hasAccess(cache.getAccess(), AccessLevel.COUNCIL)) {
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
        p.sendMessage(Chat.cottonCandy("Du hast erfolgreich: " + withdraw + " Silber abgehoben."));
        return true;
    }

    @CommandAnnotation(
            domain = "bank.history",
            permission = "terra.bank.history",
            description = "Shows you the banks recent transactions",
            usage = "/terra bank history"
    )
    public boolean history(Player p, String[] args) {
        TerraSelectCache cache = TerraSelectCache.hasSelect(p);
        if (cache == null) return false;
        if(!(cache.getRegion() instanceof BankHolder bank)) {
            p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keine Bank"));
            return false;
        }
        if(!Access.hasAccess(cache.getAccess(), AccessLevel.CITIZEN)) {
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
