package de.terranova.nations.commands;


import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.bank.BankHolder;
import org.bukkit.entity.Player;

import static de.terranova.nations.commands.SubCommand.hasSelect;

class BankCommands {

    @CommandAnnotation(
            name = "terra.bank.balance",
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
            name = "terra.bank.deposit",
            permission = "nations.bank.deposit",
            description = "Deposits an amount to the bank",
            usage = "/terra bank deposit <amount>",
            tabCompletion = {"<amount>"}
    )
    public static boolean deposit(Player p, String[] args) {

        return true;
    }

    @CommandAnnotation(
            name = "terra.bank.withdraw",
            permission = "nations.bank.withdraw",
            description = "Withdraws an amount from the bank",
            usage = "/terra bank withdraw <amount>",
            tabCompletion = {"<amount>"}
    )
    public static boolean withdraw(Player p, String[] args) {

        return true;
    }

    @CommandAnnotation(
            name = "terra.bank.history",
            permission = "nations.bank.history",
            description = "Shows you the banks recent transactions",
            usage = "/terra bank history"
    )
    public static boolean history(Player p, String[] args) {

        return true;
    }
}
