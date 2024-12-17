package de.terranova.nations.command;

import de.mcterranova.terranovaLib.commands.AbstractCommand;
import de.mcterranova.terranovaLib.optimization.CachedSupplier;
import de.terranova.nations.regions.access.AccessCommands;
import de.terranova.nations.regions.bank.BankCommands;
import de.terranova.nations.regions.base.RegionCommands;
import de.terranova.nations.regions.base.RegionType;
import de.terranova.nations.regions.base.SelectCommands;
import de.terranova.nations.regions.npc.NPCCommands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class TerraCommand extends AbstractCommand {

    public TerraCommand() {
        addPlaceholder("$ONLINEPLAYERS", new CachedSupplier<>(() -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), 10000));
        addPlaceholder("$REGION_NAMES", RegionType::getNameCache);
        addPlaceholder("$REGISTERED_REGION_TYPES", RegionType::getRegionTypes);
    }

    @Override
    protected void registerSubCommands() {
        registerSubCommand(RegionCommands.class, "region");
        registerSubCommand(BankCommands.class, "bank");
        registerSubCommand(SelectCommands.class, "select");
        registerSubCommand(AccessCommands.class, "access");
        registerSubCommand(NPCCommands.class, "npc");
    }

}
