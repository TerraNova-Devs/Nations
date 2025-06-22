package de.terranova.nations.gui;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.roseGUI.RoseGUI;
import de.terranova.nations.utils.roseGUI.RoseItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.*;

public class TownSettingsGUI extends RoseGUI {

    ProtectedRegion region;
    SettleRegion settle;

    public TownSettingsGUI(Player player, SettleRegion settle) {
        super(player, "town-settings-gui", Chat.blueFade("<b>Einstellungen"), 6);
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

        addStateFlag(Flags.SNOW_FALL, 10, Material.SNOW, "Soll Schnee in deiner Stadt bleiben?");
        addStateFlag(Flags.PVP, 11, Material.NETHERITE_SWORD, "Soll PvP in deiner Stadt erlaubt sein?");
        addStateFlag(Flags.LEAF_DECAY, 12, Material.OAK_LEAVES, "Sollen Blätter in deiner Stadt verschwinden?");
        addStateFlag(Flags.FROSTED_ICE_FORM, 13, Material.ICE, "Soll Frostwalker in deiner Stadt funktionieren?");
        addStateFlag(Flags.FIRE_SPREAD, 14, Material.FLINT_AND_STEEL, "Soll sich Feuer in deiner Stadt ausbreiten?");
        addStateFlag(Flags.ENDERPEARL, 15, Material.ENDER_PEARL, "Sollen Enderperlen in deiner Stadt funktionieren?");
        addStateFlag(Flags.CHORUS_TELEPORT, 16, Material.CHORUS_FRUIT, "Sollen Chorusfrüchte in deiner Stadt funktionieren?");
        addSetFlag(Flags.DENY_SPAWN, 19, Material.ZOMBIE_HEAD, "Sollen Monster in deiner Stadt spawnen?",
                EntityType.REGISTRY.get("minecraft:phantom"), // used to check if it's "enabled"
                new HashSet<>(Collections.singletonList(EntityType.REGISTRY.get("minecraft:zombie_villager"))),
                new HashSet<>(Arrays.asList(EntityType.REGISTRY.get("minecraft:zombie_villager"), EntityType.REGISTRY.get("minecraft:zombie"), EntityType.REGISTRY.get("minecraft:spider"),
                        EntityType.REGISTRY.get("minecraft:skeleton"), EntityType.REGISTRY.get("minecraft:enderman"), EntityType.REGISTRY.get("minecraft:phantom"), EntityType.REGISTRY.get("minecraft:drowned"),
                        EntityType.REGISTRY.get("minecraft:witch"), EntityType.REGISTRY.get("minecraft:pillager"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:husk"),
                        EntityType.REGISTRY.get("minecraft:creeper"))));

        RoseItem back = new RoseItem.Builder()
                .material(Material.SPECTRAL_ARROW)
                .displayName(Chat.yellowFade("<b>Zurück</b>"))
                .build();
        back.onClick(e -> {
            new TownGUI(player, settle).open();
        });
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
                .addLore(flagValue ? Chat.greenFade(String.format("<i>Derzeit: %s", "enabled")) : Chat.redFade(String.format("<i>Derzeit: %s", "disabled")))
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

    public <T> void addSetFlag(SetFlag<T> flag, int slot, Material displayMaterial, String description,
                               T testValue, Set<T> enabledSet, Set<T> disabledSet) {

        Set<T> currentSet = region.getFlag(flag);
        boolean isEnabled = currentSet == null || !currentSet.contains(testValue);

        RoseItem item = new RoseItem.Builder()
                .material(displayMaterial)
                .displayName(Chat.blueFade("Flag: " + flag.getName()))
                .addLore(Chat.cottonCandy("<i>" + description))
                .addLore(isEnabled
                        ? Chat.greenFade(String.format("<i>Derzeit: %s", "enabled"))
                        : Chat.redFade(String.format("<i>Derzeit: %s", "disabled")))
                .build();

        item.onClick(e -> {
            region.setFlag(flag, isEnabled ? new HashSet<>(disabledSet) : new HashSet<>(enabledSet));
            open(); // GUI refresh
        });

        addItem(slot, item);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

}
