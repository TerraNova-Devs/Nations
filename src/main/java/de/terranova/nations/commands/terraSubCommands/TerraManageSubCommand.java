package de.terranova.nations.commands.terraSubCommands;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.commands.SubCommand;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.bank.BankHolder;
import de.terranova.nations.regions.npc.NPCHolder;
import de.terranova.nations.regions.bank.Transaction;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TerraManageSubCommand extends SubCommand implements BasicCommand {
    public TerraManageSubCommand(String permission) {
        super(permission);
    }

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        Player p = isPlayer(commandSourceStack);
        if (p == null) return;
        TerraSelectCache cache = hasSelect(p);
        if (cache == null) return;
        if(cache.getAccess() == null) return;

        if(args[1].equals("bank")){

            if(!(cache.getRegion() instanceof BankHolder bank)) {
                p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keine Bank"));
                return;
            }

            if(args.length == 2){

                p.sendMessage(Chat.greenFade(String.format("Die balance der Stadt beträgt %s",bank.getBank())));
                return;
            } else if(args[2].equals("history")&& args.length == 3) {
                if(!hasAccess(cache.getAccess(), AccessLevel.CITIZEN)) {
                    p.sendMessage(Chat.errorFade("Du musst mindestens Member sein um die Historie sehen zu können"));
                    return;
                }
                if(bank.getBank().getTransactions().isEmpty()) {
                    p.sendMessage("No Transactions found");
                } else {
                    for (Transaction t : bank.getBank().getTransactions()) {
                        p.sendMessage(Chat.cottonCandy(String.format("Transaktion: %s -> %s am %s",t.user,t.amount,Chat.prettyInstant(t.date))));
                    }
                }
                return;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                p.sendMessage(Chat.errorFade("Bitte nutze /t bank (<withdraw|deposit>) (<value>)"));
                p.sendMessage(Chat.errorFade("Bitte gib als value eine Zahl zwischen 1 und 2304!"));
                return;
            }

            if(args[2].equals("withdraw")){
                if(!hasAccess(cache.getAccess(), AccessLevel.COUNCIL)) {
                    p.sendMessage(Chat.errorFade("Du musst mindestens Council sein um von der Stadtkasse abheben zu können"));
                    return;
                }
                bank.getBank().cashOut(p, amount, cache.getRegion());

            }
            if(args[2].equals("deposit")){
                if(!hasAccess(cache.getAccess(), AccessLevel.CITIZEN)) {
                    p.sendMessage(Chat.errorFade("Du musst mindestens Member sein um in die Stadtkasse einzahlen zu können"));
                    return;
                }
                bank.getBank().cashIn(p, amount,cache.getRegion());
            }

        }

        if(args[1].equals("npc")){
            if(!(cache.getRegion() instanceof NPCHolder npc)) {
                p.sendMessage(Chat.errorFade("Die von dir ausgewählte Region besitzt keinen NPC"));
            }
        }

    }

}
