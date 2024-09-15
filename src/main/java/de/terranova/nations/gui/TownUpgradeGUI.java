package de.terranova.nations.gui;


import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.Settle;
import de.terranova.nations.settlements.level.Objective;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class TownUpgradeGUI extends RoseGUI {

    Settle settle;

    public TownUpgradeGUI(Player player, Settle settle) {
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
            new TownGUI(player).open();
        });

        RoseItem score = new RoseItem.Builder()
                .material(Material.GOLD_BLOCK)
                .displayName(Chat.yellowFade("Coming Soon..."))
                .build();

        RoseItem submit = new RoseItem.Builder()
                .material(canLevelup ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
                .displayName(canLevelup ? Chat.greenFade("Level Up!") : Chat.redFade("More Resources Needed!") )
                .build();
        if (canLevelup) {
            submit.onClick(e -> {
                settle.levelUP();
                new TownUpgradeGUI(player, settle).open();
            });
        }

        RoseItem objective_a = new RoseItem.Builder()
                .material(goalObjective.getMaterial_a())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalObjective.getMaterial_a().replaceAll("_", " ").toLowerCase())))
                .addLore(progressObjective.getObjective_a() == goalObjective.getObjective_a() ? Chat.greenFade(String.format(progressObjective.getObjective_a() + " / " + goalObjective.getObjective_a())) : Chat.redFade(String.format(progressObjective.getObjective_a() + " / " + goalObjective.getObjective_a())))
                .isEnchanted(progressObjective.getObjective_a() == goalObjective.getObjective_a())
                .build();
        if (progressObjective.getObjective_a() != goalObjective.getObjective_a()) {
            objective_a.onClick(e -> {
                settle.contributeObjective(player, "a");
                new TownUpgradeGUI(player, settle).open();
            });
        }

        RoseItem objective_b = new RoseItem.Builder()
                .material(goalObjective.getMaterial_b())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalObjective.getMaterial_b().replaceAll("_", " ").toLowerCase())))
                .addLore(progressObjective.getObjective_b() == goalObjective.getObjective_b() ? Chat.greenFade(String.format(progressObjective.getObjective_b() + " / " + goalObjective.getObjective_b())) : Chat.redFade(String.format(progressObjective.getObjective_b() + " / " + goalObjective.getObjective_b())))
                .isEnchanted(progressObjective.getObjective_b() == goalObjective.getObjective_b())
                .build();
        if (progressObjective.getObjective_b() != goalObjective.getObjective_b()) {
            objective_b.onClick(e -> {
                settle.contributeObjective(player, "b");
                new TownUpgradeGUI(player, settle).open();
            });
        }

        RoseItem objective_c = new RoseItem.Builder()
                .material(goalObjective.getMaterial_c())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalObjective.getMaterial_c().replaceAll("_", " ").toLowerCase())))
                .addLore(progressObjective.getObjective_c() == goalObjective.getObjective_c() ? Chat.greenFade(String.format(progressObjective.getObjective_c() + " / " + goalObjective.getObjective_c())) : Chat.redFade(String.format(progressObjective.getObjective_c() + " / " + goalObjective.getObjective_c())))
                .isEnchanted(progressObjective.getObjective_c() == goalObjective.getObjective_c())
                .build();
        if (progressObjective.getObjective_c() != goalObjective.getObjective_c()) {
            objective_c.onClick(e -> {
                settle.contributeObjective(player, "c");
                new TownUpgradeGUI(player, settle).open();
            });
        }

        RoseItem objective_d = new RoseItem.Builder()
                .material(goalObjective.getMaterial_d())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalObjective.getMaterial_d().replaceAll("_", " ").toLowerCase())))
                .addLore(progressObjective.getObjective_d() == goalObjective.getObjective_d() ? Chat.greenFade(String.format(progressObjective.getObjective_d() + " / " + goalObjective.getObjective_d())) : Chat.redFade(String.format(progressObjective.getObjective_d() + " / " + goalObjective.getObjective_d())))
                .isEnchanted(progressObjective.getObjective_d() == goalObjective.getObjective_d())
                .build();
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
}
