package de.terranova.nations.commands.terraSubCommands;

import de.mcterranova.terranovaLib.utils.BiomeUtil;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.commands.SubCommand;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.base.RegionType;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TerraRegionSubCommand extends SubCommand implements BasicCommand {

    public TerraRegionSubCommand(String permission) {
        super(permission);
    }
    Player p;
    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        p = isPlayer(commandSourceStack);

        if (args[0].equalsIgnoreCase("remove")) {
            TerraSelectCache selectedCache = TerraSelectCache.selectCache.get(p.getUniqueId());
            if (selectedCache == null) {
                p.sendMessage(Chat.errorFade("Bitte nutze für die Aktion erst ./t select <Stadtname> um die zu betreffende Stadt auszuwählen."));
                return;
            }
            handleRemove(p, selectedCache);
        }



        if (args[0].equalsIgnoreCase("create") && args.length >= 3) {

            String type = args[1].toLowerCase();
            String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 2, args.length)));

            if(!RegionType.registry.containsKey(type)){
                p.sendMessage(Chat.errorFade(String.format("Bitte benutze nur folgende Regionstypen: %s", RegionType.registry.keySet())));
                return;
            }
            handleCreate(p, type, name);
        }
    }

    private void handleCreate(Player p, String type, String name) {
        if (!hasPermission(p, permission + "." + type)) {
            p.sendMessage(Chat.errorFade("You do not have the necessary permission."));
            return;
        }

        Optional<RegionType> regionTypeOpt = RegionType.createRegionType(type, name, p);
        if (regionTypeOpt.isPresent()) {
            //RegionType regionType = regionTypeOpt.get();
            p.sendMessage(Chat.greenFade("Region " + name + " wurde erfolgreich gegründet."));
        } else {
            p.sendMessage(Chat.errorFade("Die Erstellung der Region wurde abgebrochen."));
        }
    }

    private void handleRemove(Player player, TerraSelectCache selectedCache) {
        RegionType region = selectedCache.getRegion();
        AccessLevel playerAccess = selectedCache.getAccess();

        if (region == null) {
            player.sendMessage(Chat.errorFade("Keine ausgewählte Region gefunden."));
            return;
        }

        if (playerAccess == null || !hasAccess(playerAccess, AccessLevel.MAJOR)) {
            player.sendMessage(Chat.errorFade("You do not have the required access level to remove this settlement."));
            return;
        }

        region.remove();
        player.sendMessage(Chat.greenFade("Die Stadt " + region.getName() + " wurde erfolgreich entfernt."));
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("create")) {
            // Suggest actions for the first argument
            return filterSuggestions(RegionType.registry.keySet(), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            // Suggest available region types for the "create" action
            return filterSuggestions(RegionType.registry.keySet(), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            // Suggest placeholder for the region name
            return List.of("<region_name>");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            // Suggest settlements from the player's selected cache
            Player player = isPlayer(commandSourceStack);
            if (player != null && TerraSelectCache.selectCache.containsKey(player.getUniqueId())) {
                TerraSelectCache selectedCache = TerraSelectCache.selectCache.get(player.getUniqueId());
                if (selectedCache != null) {
                    RegionType region = selectedCache.getRegion();
                    if (region != null) {
                        return filterSuggestions(List.of(region.getName()), args[1]);
                    }
                }
            }
        }

        // Fallback to an empty list if no matches
        return List.of();
    }
}
