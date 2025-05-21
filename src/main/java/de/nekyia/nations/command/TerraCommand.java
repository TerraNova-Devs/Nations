package de.nekyia.nations.command;

import de.nekyia.nations.regions.access.AccessCommands;
import de.nekyia.nations.regions.access.AccessControlled;
import de.nekyia.nations.regions.access.AccessLevel;
import de.nekyia.nations.regions.bank.BankCommands;
import de.nekyia.nations.regions.base.RegionCommands;
import de.nekyia.nations.regions.base.RegionType;
import de.nekyia.nations.regions.base.SelectCommands;
import de.nekyia.nations.regions.base.TerraSelectCache;
import de.nekyia.nations.regions.npc.NPCCommands;
import de.nekyia.nations.utils.commands.AbstractCommand;
import de.nekyia.nations.utils.commands.CachedSupplier;
import de.nekyia.nations.utils.commands.PlayerAwarePlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class TerraCommand extends AbstractCommand {

    public TerraCommand() {
        addPlaceholder("$ONLINEPLAYERS", new CachedSupplier<>(
                () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
                10000
        ));

        addPlaceholder("$REGION_NAMES", RegionType::getNameCache);
        addPlaceholder("$REGISTERED_REGION_TYPES", RegionType::getRegionTypes);
        addPlaceholder("$RANKS", () ->
                Arrays.stream(AccessLevel.values())
                        .filter(level -> level != AccessLevel.ADMIN)
                        .filter(level -> level != AccessLevel.MAJOR)
                        .map(Enum::name)
                        .collect(Collectors.toList()));
        addPlaceholder("$REGION_ACCESS_USERS",
                PlayerAwarePlaceholder.ofCachedPlayerFunction(
                        (UUID uuid) -> {
                            return TerraSelectCache.getSelect(uuid)
                                    .map(cache -> {
                                        if (cache.getRegion() instanceof AccessControlled access) {
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
        registerSubCommand(new BankCommands(), "bank");
        registerSubCommand(new SelectCommands(), "select");
        registerSubCommand(new AccessCommands(), "access");
        registerSubCommand(new NPCCommands(), "npc");

        setupHelpCommand();
        initialize();
    }




}
