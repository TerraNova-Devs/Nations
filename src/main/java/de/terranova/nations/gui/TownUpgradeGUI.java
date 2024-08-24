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
        super(player, "town-upgrade-gui", Chat.blueFade("Town Upgrade"), 6);
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

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta mfiller = filler.getItemMeta();
        mfiller.displayName(Chat.stringToComponent(""));
        filler.setItemMeta(mfiller);
        fillGui(filler);

        Icon back = new Icon(new roseItem.Builder()
                .material(Material.SPECTRAL_ARROW)
                .displayName(Chat.redFade("<b>Go Back</b>"))
                .build().stack);
        back.onClick(e -> {
            new TownGUI(player).open();
        });

        ItemStack score = new ItemStack(Material.GOLD_BLOCK);

        ItemStack submit;
        if (canLevelup) {
            submit = new ItemStack(Material.EMERALD_BLOCK);
        } else {
            submit = new ItemStack(Material.REDSTONE_BLOCK);
        }
        ItemMeta msubmit = submit.getItemMeta();
        if (canLevelup) {
            msubmit.displayName(Chat.greenFade("Level Up!"));
        } else {
            msubmit.displayName(Chat.redFade("More Resources Needed!"));
        }
        submit.setItemMeta(msubmit);
        Icon isubmit = new Icon(submit);
        if (canLevelup) {
            isubmit.onClick(e -> {
                settle.levelUP();
                new TownUpgradeGUI(player, settle).open();
            });
        }

        Icon objective_a = new Icon(new roseItem.Builder()
                .material(goalObjective.getMaterial_a())
                .displayName(Chat.blueFade(goalObjective.getMaterial_a()))
                .addLore(Chat.redFade(String.format(progressObjective.getObjective_a() + " / " + goalObjective.getObjective_a())))
                .isEnchanted(progressObjective.getObjective_a() == goalObjective.getObjective_a())
                .build().stack);
        if (progressObjective.getObjective_a() != goalObjective.getObjective_a()) {
            objective_a.onClick(e -> {
                int charged = chargeStrict(player, goalObjective.getMaterial_a(), goalObjective.getObjective_a() - progressObjective.getObjective_a(), false);
                if (charged <= 0) return;
                progressObjective.setObjective_a(progressObjective.getObjective_a() + charged);
                settle.setObjectives(progressObjective);
                new TownUpgradeGUI(player, settle).open();
            });
        }


        Icon objective_b = new Icon(new roseItem.Builder()
                .material(goalObjective.getMaterial_b())
                .displayName(Chat.blueFade(goalObjective.getMaterial_b()))
                .addLore(Chat.redFade(String.format(progressObjective.getObjective_b() + " / " + goalObjective.getObjective_b())))
                .isEnchanted(progressObjective.getObjective_b() == goalObjective.getObjective_b())
                .build().stack);
        if (progressObjective.getObjective_b() != goalObjective.getObjective_b()) {
            objective_b.onClick(e -> {
                int charged = chargeStrict(player, goalObjective.getMaterial_b(), goalObjective.getObjective_b() - progressObjective.getObjective_b(), false);
                if (charged <= 0) return;
                progressObjective.setObjective_b(progressObjective.getObjective_b() + charged);
                settle.setObjectives(progressObjective);
                new TownUpgradeGUI(player, settle).open();
            });
        }

        Icon objective_c = new Icon(new roseItem.Builder()
                .material(goalObjective.getMaterial_c())
                .displayName(Chat.blueFade(goalObjective.getMaterial_c()))
                .addLore(Chat.redFade(String.format(progressObjective.getObjective_c() + " / " + goalObjective.getObjective_c())))
                .isEnchanted(progressObjective.getObjective_c() == goalObjective.getObjective_c())
                .build().stack);
        if (progressObjective.getObjective_c() != goalObjective.getObjective_c()) {
            objective_c.onClick(e -> {
                int charged = chargeStrict(player, goalObjective.getMaterial_c(), goalObjective.getObjective_c() - progressObjective.getObjective_c(), false);
                if (charged <= 0) return;
                progressObjective.setObjective_c(progressObjective.getObjective_c() + charged);
                settle.setObjectives(progressObjective);
                new TownUpgradeGUI(player, settle).open();
            });
        }

        Icon objective_d = new Icon(new roseItem.Builder()
                .material(goalObjective.getMaterial_d())
                .displayName(Chat.blueFade(goalObjective.getMaterial_d()))
                .addLore(Chat.redFade(String.format(progressObjective.getObjective_d() + " / " + goalObjective.getObjective_d())))
                .isEnchanted(progressObjective.getObjective_d() == goalObjective.getObjective_d())
                .build().stack);
        if (progressObjective.getObjective_d() != goalObjective.getObjective_d()) {
            objective_d.onClick(e -> {
                int charged = chargeStrict(player, goalObjective.getMaterial_d(), goalObjective.getObjective_d() - progressObjective.getObjective_d(), false);
                if (charged <= 0) return;
                progressObjective.setObjective_d(progressObjective.getObjective_d() + charged);
                settle.setObjectives(progressObjective);
                new TownUpgradeGUI(player, settle).open();
            });
        }


        addItem(13, score);
        addItem(40, isubmit);
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
