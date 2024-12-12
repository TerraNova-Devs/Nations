package de.terranova.nations.command.commands;

import de.terranova.nations.command.AbstractCommand;
import de.terranova.nations.regions.access.AccessCommands;
import de.terranova.nations.regions.bank.BankCommands;
import de.terranova.nations.regions.base.RegionCommands;
import de.terranova.nations.regions.base.SelectCommands;
import de.terranova.nations.regions.npc.NPCCommands;

public class TerraCommand extends AbstractCommand {

    @Override
    protected void registerSubCommands() {
        registerSubCommand(RegionCommands.class, "region");
        registerSubCommand(BankCommands.class, "bank");
        registerSubCommand(SelectCommands.class, "select");
        registerSubCommand(AccessCommands.class, "access");
        registerSubCommand(NPCCommands.class, "npc");
    }
}
