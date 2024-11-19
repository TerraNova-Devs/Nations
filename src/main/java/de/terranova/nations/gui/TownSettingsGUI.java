package de.terranova.nations.gui;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.settlements.RegionTypes.SettleRegionType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.*;

public class TownSettingsGUI extends RoseGUI {

    ProtectedRegion region;
    SettleRegionType settle;

    public TownSettingsGUI(Player player, SettleRegionType settle) {
        super(player, "town-settings-gui", Chat.blueFade("<b>Town Settings"), 6);
        this.settle = settle;
        this.region = settle.getWorldguardRegion();
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        RoseItem filler = new RoseItem.Builder()
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .displayName("")
                .build();
        fillGui(filler);

        RoseItem back = new RoseItem.Builder()
                .material(Material.SPECTRAL_ARROW)
                .displayName(Chat.redFade("<b>Go Back</b>"))
                .build();
        back.onClick(e -> {
            new TownGUI(player, settle).open();
        });

        addStateFlag(Flags.SNOW_FALL, 10, Material.SNOW, "Soll sich Schnee in deinem Gebiet bilden?");
        addStateFlag(Flags.PVP, 11, Material.NETHERITE_SWORD, "Aktiviert PvP in deinem Gebiet!");
        addStateFlag(Flags.LEAF_DECAY, 12, Material.OAK_LEAVES, "Whether leaves will decay!");
        addStateFlag(Flags.FROSTED_ICE_FORM, 13, Material.ICE, "Aktiviert Frostwalker!");
        addStateFlag(Flags.FIRE_SPREAD, 14, Material.FLINT_AND_STEEL, "allow fire spread");
        addStateFlag(Flags.ENDERPEARL, 15, Material.ENDER_PEARL, "Toggelt Enderperlen.");
        addStateFlag(Flags.CHORUS_TELEPORT, 16, Material.CHORUS_FRUIT, "Verhindert das TPen von Chorusfr\u00FCchten.");

        Set<EntityType> mobs = region.getFlag(Flags.DENY_SPAWN);
        boolean isenbaled;
        isenbaled = mobs != null && !mobs.contains(EntityType.REGISTRY.get("minecraft:phantom"));
        RoseItem flag = new RoseItem.Builder()
                .material(Material.ZOMBIE_HEAD)
                .displayName(Chat.blueFade("Flag: " + Flags.DENY_SPAWN.getName()))
                .addLore(Chat.cottonCandy("<i>Sollen Monster spawnen?"))
                .addLore(isenbaled ? Chat.greenFade(String.format("<i>Currently: %s", "enabled")) : Chat.redFade(String.format("<i>Currently: %s", "disabled")))
                .build();
        flag.onClick(e -> {
            if (isenbaled) {
                Set<EntityType> set = new HashSet<>(Arrays.asList(EntityType.REGISTRY.get("minecraft:zombie_villager"), EntityType.REGISTRY.get("minecraft:zombie"), EntityType.REGISTRY.get("minecraft:spider"),
                        EntityType.REGISTRY.get("minecraft:skeleton"), EntityType.REGISTRY.get("minecraft:enderman"), EntityType.REGISTRY.get("minecraft:phantom"), EntityType.REGISTRY.get("minecraft:drowned"),
                        EntityType.REGISTRY.get("minecraft:witch"), EntityType.REGISTRY.get("minecraft:pillager"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:husk"),
                        EntityType.REGISTRY.get("minecraft:creeper")
                ));
                region.setFlag(Flags.DENY_SPAWN, set);
                open();
            } else {
                Set<EntityType> set = new HashSet<>(Collections.singletonList(EntityType.REGISTRY.get("minecraft:zombie_villager")));
                region.setFlag(Flags.DENY_SPAWN, set);
                open();
            }
        });
        addItem(19, flag);
        addItem(45, back);

    }

    public void addStateFlag(StateFlag flag, int slot, Material material, String description) {
        boolean flagValue;
        StateFlag.State stateFlag = region.getFlag(flag);

        flagValue = stateFlag != null && !stateFlag.equals(StateFlag.State.DENY);
        RoseItem item = new RoseItem.Builder()
                .material(material)
                .displayName(Chat.blueFade("Flag: " + flag.getName()))
                .addLore(Chat.cottonCandy("<i>" + description))
                .addLore(flagValue ? Chat.greenFade(String.format("<i>Currently: %s", "enabled")) : Chat.redFade(String.format("<i>Currently: %s", "disabled")))
                .build();
        addItem(slot, item);
        item.onClick(e -> {
            if (flagValue) {
                region.setFlag(flag, StateFlag.State.DENY);
                addStateFlag(flag, slot, material, description);
            } else {
                region.setFlag(flag, StateFlag.State.ALLOW);
                addStateFlag(flag, slot, material, description);
            }
        });
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

}
