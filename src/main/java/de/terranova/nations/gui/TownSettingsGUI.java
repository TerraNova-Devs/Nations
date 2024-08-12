package de.terranova.nations.gui;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.Entities;
import de.mcterranova.bona.lib.chat.Chat;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import de.terranova.nations.settlements.settlement;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.AccessFlag;
import java.util.*;

public class TownSettingsGUI extends Gui {

    ProtectedRegion region;

    public TownSettingsGUI(Player player, settlement settle) {
        super(player, "town-settings-gui", Chat.blueFade("Settings Menu"), 6);
        this.region = settle.getWorldguardRegion();
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta mfiller = filler.getItemMeta();
        mfiller.displayName(Chat.stringToComponent(""));
        filler.setItemMeta(mfiller);
        fillGui(filler);

        ItemStack back = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta mback = back.getItemMeta();
        mback.displayName(Chat.redFade("<b>Go Back</b>"));
        back.setItemMeta(mback);
        Icon iback = new Icon(back);
        addItem(45, iback);
        iback.onClick(e -> {
            new TownGUI(player).open();
        });

        addStateFlag(Flags.SNOW_FALL, 10, Material.SNOW, "Soll sich Schnee in deinem Gebiet bilden?");
        addStateFlag(Flags.PVP, 11, Material.NETHERITE_SWORD, "Aktiviert PvP in deinem Gebiet!");
        addStateFlag(Flags.LEAF_DECAY, 12, Material.OAK_LEAVES, "Whether leaves will decay!");
        addStateFlag(Flags.FROSTED_ICE_FORM, 13, Material.ICE, "Aktiviert Frostwalker!");
        addStateFlag(Flags.FIRE_SPREAD, 14, Material.FLINT_AND_STEEL, "allow fire spread");
        addStateFlag(Flags.ENDERPEARL, 15, Material.ENDER_PEARL, "Toggelt Enderperlen.");
        addStateFlag(Flags.CHORUS_TELEPORT, 16, Material.CHORUS_FRUIT, "Verhindert das TPen von Chorusfr\u00FCchten.");

        Set<EntityType> mobs =  region.getFlag(Flags.DENY_SPAWN);
        boolean isenbaled;
        if (mobs == null || mobs.contains(EntityType.REGISTRY.get("minecraft:phantom"))) isenbaled = false;
        else {
            isenbaled = true;
        }
        ItemStack item = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta mitem = item.getItemMeta();
        List<Component> litem = new ArrayList<>();
        litem.add(Chat.cottonCandy("<i>" + "Sollen Monster spawnen?"));

        if(isenbaled){
            litem.add(Chat.greenFade(String.format("<i>Currently: %s", "enabled")));
        } else {
            litem.add(Chat.redFade(String.format("<i>Currently: %s", "disabled")));
        }
        mitem.lore(litem);
        mitem.displayName(Chat.blueFade("Flag: " + Flags.DENY_SPAWN.getName()));
        item.setItemMeta(mitem);
        Icon icon = new Icon(item);
        addItem(19, icon);
        icon.onClick(e -> {
            if(isenbaled){
                Set<EntityType> set = new HashSet<>(Arrays.asList(EntityType.REGISTRY.get("minecraft:zombie_villager"), EntityType.REGISTRY.get("minecraft:zombie"),EntityType.REGISTRY.get("minecraft:spider"),
                        EntityType.REGISTRY.get("minecraft:skeleton"),EntityType.REGISTRY.get("minecraft:enderman"),EntityType.REGISTRY.get("minecraft:phantom"),EntityType.REGISTRY.get("minecraft:drowned"),
                        EntityType.REGISTRY.get("minecraft:witch"),EntityType.REGISTRY.get("minecraft:pillager")
                ));
                region.setFlag(Flags.DENY_SPAWN,set);
                open();
            } else {
                Set<EntityType> set = new HashSet<>(Collections.singletonList(EntityType.REGISTRY.get("minecraft:zombie_villager")));
                region.setFlag(Flags.DENY_SPAWN,set);
                open();
            }
        });

    }

    public void addStateFlag(StateFlag flag, int slot, Material material, String description) {
        boolean flagValue;
        StateFlag.State stateFlag = region.getFlag(flag);

        if(stateFlag == null || stateFlag.equals(StateFlag.State.DENY)) { flagValue = false; } else flagValue = true;
        ItemStack item = new ItemStack(material);
        ItemMeta mitem = item.getItemMeta();
        List<Component> litem = new ArrayList<>();
        litem.add(Chat.cottonCandy("<i>" + description));
        if(flagValue){
            litem.add(Chat.greenFade(String.format("<i>Currently: %s", "enabled")));
        } else {
            litem.add(Chat.redFade(String.format("<i>Currently: %s", "disabled")));
        }
        mitem.lore(litem);
        mitem.displayName(Chat.blueFade("Flag: " + flag.getName()));
        item.setItemMeta(mitem);
        Icon icon = new Icon(item);
        addItem(slot, icon);
        icon.onClick(e -> {
            if(flagValue){
                region.setFlag(flag, StateFlag.State.DENY);
                addStateFlag(flag, slot,material ,description);
            } else {
                region.setFlag(flag, StateFlag.State.ALLOW);
                addStateFlag(flag, slot, material,description);
            }
        });
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

}
