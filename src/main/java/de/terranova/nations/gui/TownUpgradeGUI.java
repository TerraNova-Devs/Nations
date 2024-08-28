package de.terranova.nations.gui;

import de.mcterranova.bona.lib.chat.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.gui.guiutil.roseItem;
import de.terranova.nations.settlements.Settlement;
import de.terranova.nations.settlements.level.Objective;
import io.th0rgal.oraxen.api.OraxenItems;
import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TownUpgradeGUI extends Gui {

    Settlement settle;

    public TownUpgradeGUI(Player player, Settlement settle) {
        super(player, "town-upgrade-gui", Chat.blueFade("<b>Town Upgrades"), 6);
        this.settle = settle;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        Objective progressObjective = settle.objective;
        Objective goalObjective;

        if (!(NationsPlugin.levelObjectives.size() + 1 == settle.level)) {
            goalObjective = NationsPlugin.levelObjectives.get(settle.level);
        } else {
            goalObjective = new Objective(0, 0, 0, 0, 0, "Coming Soon...", "Coming Soon...", "Coming Soon...", "Coming Soon...");
        }

        boolean canLevelup = progressObjective.getObjective_a() == goalObjective.getObjective_a() && progressObjective.getObjective_b() == goalObjective.getObjective_b() &&
                progressObjective.getObjective_c() == goalObjective.getObjective_c() && progressObjective.getObjective_d() == goalObjective.getObjective_d();

        ItemStack filler = new roseItem.Builder()
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .displayName("")
                .build().stack;
        fillGui(filler);

        Icon back = new Icon(new roseItem.Builder()
                .material(Material.SPECTRAL_ARROW)
                .displayName(Chat.redFade("<b>Go Back</b>"))
                .build().stack);
        back.onClick(e -> {
            new TownGUI(player).open();
        });

        Icon score = new Icon(new roseItem.Builder()
                .material(Material.GOLD_BLOCK)
                .displayName(Chat.yellowFade("Coming Soon..."))
                .build().stack);

        Icon submit = new Icon(new roseItem.Builder()
                .material(canLevelup ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
                .displayName(canLevelup ? Chat.greenFade("Level Up!") : Chat.redFade("More Resources Needed!") )
                .build().stack);
        if (canLevelup) {
            submit.onClick(e -> {
                settle.levelUP();
                new TownUpgradeGUI(player, settle).open();
            });
        }

        Icon objective_a = new Icon(new roseItem.Builder()
                .material(goalObjective.getMaterial_a())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalObjective.getMaterial_a().replaceAll("_", " ").toLowerCase())))
                .addLore(Chat.redFade(String.format(progressObjective.getObjective_a() + " / " + goalObjective.getObjective_a())))
                .isEnchanted(progressObjective.getObjective_a() == goalObjective.getObjective_a())
                .build().stack);
        if (progressObjective.getObjective_a() != goalObjective.getObjective_a()) {
            objective_a.onClick(e -> {
                settle.contributeObjective(player, "a");
                new TownUpgradeGUI(player, settle).open();
            });
        }

        Icon objective_b = new Icon(new roseItem.Builder()
                .material(goalObjective.getMaterial_b())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalObjective.getMaterial_b().replaceAll("_", " ").toLowerCase())))
                .addLore(Chat.redFade(String.format(progressObjective.getObjective_b() + " / " + goalObjective.getObjective_b())))
                .isEnchanted(progressObjective.getObjective_b() == goalObjective.getObjective_b())
                .build().stack);
        if (progressObjective.getObjective_b() != goalObjective.getObjective_b()) {
            objective_b.onClick(e -> {
                settle.contributeObjective(player, "b");
                new TownUpgradeGUI(player, settle).open();
            });
        }

        Icon objective_c = new Icon(new roseItem.Builder()
                .material(goalObjective.getMaterial_c())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalObjective.getMaterial_c().replaceAll("_", " ").toLowerCase())))
                .addLore(Chat.redFade(String.format(progressObjective.getObjective_c() + " / " + goalObjective.getObjective_c())))
                .isEnchanted(progressObjective.getObjective_c() == goalObjective.getObjective_c())
                .build().stack);
        if (progressObjective.getObjective_c() != goalObjective.getObjective_c()) {
            objective_c.onClick(e -> {
                settle.contributeObjective(player, "c");
                new TownUpgradeGUI(player, settle).open();
            });
        }

        Icon objective_d = new Icon(new roseItem.Builder()
                .material(goalObjective.getMaterial_d())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalObjective.getMaterial_d().replaceAll("_", " ").toLowerCase())))
                .addLore(Chat.redFade(String.format(progressObjective.getObjective_d() + " / " + goalObjective.getObjective_d())))
                .isEnchanted(progressObjective.getObjective_d() == goalObjective.getObjective_d())
                .build().stack);
        if (progressObjective.getObjective_d() != goalObjective.getObjective_d()) {
            objective_d.onClick(e -> {
                settle.contributeObjective(player, "d");
                new TownUpgradeGUI(player, settle).open();
            });
        }


        addItem(13, score);
        addItem(40, submit);
        addItem(19, objective_a);
        addItem(21, objective_b);
        addItem(23, objective_c);
        addItem(25, objective_d);
        addItem(45, back);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }


    private Integer chargeStrict(Player p, String itemString, int amount, boolean onlyFullCharge) {

        ItemStack item;

        if (OraxenItems.exists(itemString)) {
            item = OraxenItems.getItemById(itemString).build();
        } else {
            item = new ItemStack(Material.valueOf(itemString));
        }

        ItemStack[] stacks = p.getInventory().getContents();
        int total = 0;

        if (onlyFullCharge) {
            for (ItemStack stack : stacks) {
                if (stack == null || !stack.isSimilar(item)) continue;
                total += stack.getAmount();
            }
            if (total < amount) return -1;
        }

        total = amount;


        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] == null || !stacks[i].isSimilar(item)) continue;


            int stackAmount = stacks[i].getAmount();
            int n = total;
            if (stackAmount < total) {
                stacks[i] = null;
                total -= stackAmount;
            } else {
                stacks[i].setAmount(stackAmount - total);
                total -= total;
                break;
            }
        }
        p.getInventory().setContents(stacks);
        p.updateInventory();
        return amount - total;

    }
}
