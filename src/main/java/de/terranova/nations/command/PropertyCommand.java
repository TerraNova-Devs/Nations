package de.terranova.nations.command;

import de.mcterranova.terranovaLib.commands.AbstractCommand;
import de.mcterranova.terranovaLib.commands.CachedSupplier;
import de.mcterranova.terranovaLib.commands.PlayerAwarePlaceholder;
import de.terranova.nations.regions.access.PropertyAccessControlled;
import de.terranova.nations.regions.access.PropertyAccessLevel;
import de.terranova.nations.regions.access.TownAccessControlled;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.TerraSelectCache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PropertyCommand extends AbstractCommand {

    public PropertyCommand() {
        addPlaceholder("$ONLINEPLAYERS", new CachedSupplier<>(
                () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
                10000
        ));

        addPlaceholder("$REGION_NAMES", Region::getNameCache);
        //addPlaceholder("$REGISTERED_REGION_TYPES", () ->

        addPlaceholder("$RANKS", () ->
                Arrays.stream(PropertyAccessLevel.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()));
        addPlaceholder("$REGION_ACCESS_USERS",
                PlayerAwarePlaceholder.ofCachedPlayerFunction(
                        (UUID uuid) -> {
                            return TerraSelectCache.getSelect(uuid)
                                    .map(cache -> {
                                        if (cache.getRegion() instanceof PropertyAccessControlled access) {
                                            return access.getAccess().getAccessLevels()
                                                    .keySet()
                                                    .stream()
                                                    .map(Bukkit::getOfflinePlayer)
                                                    .map(OfflinePlayer::getName)
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toList());
                                        }
                                        return Collections.<String>emptyList();
                                    })
                                    .orElseGet(Collections::emptyList);
                        },
                        3000
                )
        );

        registerSubCommand(RegionCommands.class, "region");
        registerSubCommand(new SelectCommands(), "select");
        registerSubCommand(new AccessCommands(), "access");

        setupHelpCommand();
        initialize();
    }




}
