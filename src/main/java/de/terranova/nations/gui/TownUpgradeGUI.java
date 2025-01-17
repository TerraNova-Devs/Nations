package de.terranova.nations.gui;


import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.grid.SettleRegionType;
import de.terranova.nations.regions.rank.RankObjective;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class TownUpgradeGUI extends RoseGUI {

    SettleRegionType settle;

    public TownUpgradeGUI(Player player, SettleRegionType settle) {
        super(player, "town-upgrade-gui", Chat.blueFade("<b>Town Upgrades"), 6);
        this.settle = settle;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        RankObjective progressRankObjective = settle.getRank().getRankObjective();
        RankObjective goalRankObjective;

        if (!(NationsPlugin.levelObjectives.size() + 1 == settle.getRank().getLevel())) {
            goalRankObjective = NationsPlugin.levelObjectives.get(settle.getRank().getLevel());
        } else {
            goalRankObjective = new RankObjective(0, 0, 0, 0, 0, "Coming Soon...", "Coming Soon...", "Coming Soon...");
        }

        boolean canLevelup = settle.getBank().getCredit() >= goalRankObjective.getSilver()  && progressRankObjective.getObjective_a() == goalRankObjective.getObjective_a() && progressRankObjective.getObjective_b() == goalRankObjective.getObjective_b() &&
                progressRankObjective.getObjective_c() == goalRankObjective.getObjective_c();

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
                settle.getBank().cashTransfer("Region-LevelUp",-goalRankObjective.getSilver());
                settle.getRank().levelUP();

                new TownUpgradeGUI(player, settle).open();
            });
        }

        RoseItem objective_bank = new RoseItem.Builder()
                .material("terranova_silver")
                .displayName(Chat.blueFade("<b>" + "Bank"))
                .addLore(settle.getBank().getCredit() >= goalRankObjective.getSilver() ? Chat.greenFade(String.format(settle.getBank().getCredit() + " / " + goalRankObjective.getSilver())) : Chat.redFade(String.format(settle.getBank().getCredit() + " / " + goalRankObjective.getSilver())))
                .isEnchanted(settle.getBank().getCredit() >= goalRankObjective.getSilver())
                .build();

        RoseItem objective_a = new RoseItem.Builder()
                .material(goalRankObjective.getMaterial_a())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalRankObjective.getMaterial_a().replaceAll("_", " ").toLowerCase())))
                .addLore(progressRankObjective.getObjective_a() == goalRankObjective.getObjective_a() ? Chat.greenFade(String.format(progressRankObjective.getObjective_a() + " / " + goalRankObjective.getObjective_a())) : Chat.redFade(String.format(progressRankObjective.getObjective_a() + " / " + goalRankObjective.getObjective_a())))
                .isEnchanted(progressRankObjective.getObjective_a() == goalRankObjective.getObjective_a())
                .build();
        if (progressRankObjective.getObjective_a() != goalRankObjective.getObjective_a()) {
            objective_a.onClick(e -> {
                settle.getRank().contributeObjective(player, "a");
                new TownUpgradeGUI(player, settle).open();
            });
        }

        RoseItem objective_b = new RoseItem.Builder()
                .material(goalRankObjective.getMaterial_b())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalRankObjective.getMaterial_b().replaceAll("_", " ").toLowerCase())))
                .addLore(progressRankObjective.getObjective_b() == goalRankObjective.getObjective_b() ? Chat.greenFade(String.format(progressRankObjective.getObjective_b() + " / " + goalRankObjective.getObjective_b())) : Chat.redFade(String.format(progressRankObjective.getObjective_b() + " / " + goalRankObjective.getObjective_b())))
                .isEnchanted(progressRankObjective.getObjective_b() == goalRankObjective.getObjective_b())
                .build();
        if (progressRankObjective.getObjective_b() != goalRankObjective.getObjective_b()) {
            objective_b.onClick(e -> {
                settle.getRank().contributeObjective(player, "b");
                new TownUpgradeGUI(player, settle).open();
            });
        }

        RoseItem objective_c = new RoseItem.Builder()
                .material(goalRankObjective.getMaterial_c())
                .displayName(Chat.blueFade("<b>" + WordUtils.capitalize(goalRankObjective.getMaterial_c().replaceAll("_", " ").toLowerCase())))
                .addLore(progressRankObjective.getObjective_c() == goalRankObjective.getObjective_c() ? Chat.greenFade(String.format(progressRankObjective.getObjective_c() + " / " + goalRankObjective.getObjective_c())) : Chat.redFade(String.format(progressRankObjective.getObjective_c() + " / " + goalRankObjective.getObjective_c())))
                .isEnchanted(progressRankObjective.getObjective_c() == goalRankObjective.getObjective_c())
                .build();
        if (progressRankObjective.getObjective_c() != goalRankObjective.getObjective_c()) {
            objective_c.onClick(e -> {
                settle.getRank().contributeObjective(player, "c");
                new TownUpgradeGUI(player, settle).open();
            });
        }

        addItem(13, score);
        addItem(40, submit);
        addItem(19, objective_bank);
        addItem(21, objective_a);
        addItem(23, objective_b);
        addItem(25, objective_c);
        addItem(45, back);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }
}
