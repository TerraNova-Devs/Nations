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
        registerSubCommand(RegionCommands.class, "region");
        registerSubCommand(new BankCommands(), "bank");
        registerSubCommand(new SelectCommands(), "select");
        registerSubCommand(new AccessCommands(), "access");
        registerSubCommand(new NPCCommands(), "npc");
        setupHelpCommand();
        initialize();
    }




}
