package org.nations.commands;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nations.Nations;
import org.nations.settlements.settlement;
import org.nations.utils.ChatUtils;

import java.util.List;

public class settle implements CommandExecutor, TabCompleter {

    Nations plugin;

    public settle(Nations plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage("Du musst für diesen Command ein Spieler sein!");
            return false;
        }

        if (args.length == 0) {
            ChatUtils.sendMessage(p, "Nations Plugin by gerryxn. Version 1.0.0 as of 13.07.2024 | Copyright Pixel Party.");
            return false;
        }

        if (!p.hasPermission("hcs.admin")) {
            return false;
        }

        if (args[0].equalsIgnoreCase("test")) {
            if(plugin.settlementManager.canSettle(p.getUniqueId())){
                settlement newsettle = new settlement(p.getUniqueId(), p.getLocation(), "test");

                NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "hi");

                //https://mineskin.org/ für signature & texture values
                SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                skinTrait.setSkinPersistent("beggar","pIy+sAOaqt70OtSP5DgR0KjBL/PvY9f/+ofULATfjGraa6JVWj5sZ1PTU8L+0xSz07saePgfHPehmV6YnpSvKS+ot7MmZZcUFMq/1PXthwusk7VcjUjwPJCFZgMGG4Fd3v5xQAZG7+xhavWap94lZpvm6fO/IyIkrG+UYQRdDt2GvGSeIxJXCE0OYr3kptxQmKRyWGc7Kpwfx8kcwRW19TIJ94GD51xmWM5jQ8Drro1ycJROtETG9rDuuyy7axdLJFN/WZ4c+JiWDE4F2oRNNfX19tawyIDIYLTqisjWPms0tp0j/TT3DPgW2EZceMq6SVuSLvinT+qO+xkCPKqq9VTGT65QFXSESKSYPsojTKKjsGcFaFUF826VYA+cZJfr8jbGfOvyX3aobOq4vaRomqojlR+GI7/UOxkWpjTAbEjkxRXH0fhcSv5fFbPNqFEb/Sx9/YBn09dPh0d1KfzCfe01zRfrzfqrlBWJdB5uvQHXSVcJt56ArPxb1gWTQInAVeQKQeiwmyr7fTkc+2JL/86reF1OhUVLlG8q7iIi38S5R02oQaQQrqEpLgysJ32q6iwuHqqxZgwga3cBmPCn4DDG4mR1EPoeEOHv9TWlFkopT7ha187JO6YthlBZis9u/Fa/crdIqyic7ej/GwopW+YsYwbw102QpDSc+ZwT1Do=",
                        "ewogICJ0aW1lc3RhbXAiIDogMTY3MzIxODMyOTczMCwKICAicHJvZmlsZUlkIiA6ICJmMjc0YzRkNjI1MDQ0ZTQxOGVmYmYwNmM3NWIyMDIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJIeXBpZ3NlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kOTFlMmQ2ODk0NTcyZjA5ZDY0N2I5MmVhOWNmMjE2MDFjZThmNzgzNGMzYzhkZTM0NmI3NGM4ZjMxM2E2MDUwIgogICAgfQogIH0KfQ==");
                LookClose lookTrait = npc.getOrAddTrait(LookClose.class);
                lookTrait.toggle();
                npc.setAlwaysUseNameHologram(true);
                npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);
                npc.spawn(p.getLocation());

                plugin.settlementManager.addSettlement(p.getUniqueId(), newsettle);

            }
        }
        if (args[0].equalsIgnoreCase("here")) {
            return false;
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
