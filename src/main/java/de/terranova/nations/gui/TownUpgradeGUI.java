package de.terranova.nations.gui;


import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.roseGUI.RoseItem;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.professions.ProfessionManager;
import de.terranova.nations.professions.ProfessionProgressManager;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.rank.RankObjective;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class TownUpgradeGUI extends RoseGUI {

    SettleRegion settle;

    public TownUpgradeGUI(Player player, SettleRegion settle) {
        super(player, "town-upgrade-gui", Chat.blueFade("<b>Verbesserungen"), 6);
        this.settle = settle;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

        RankObjective progressRankObjective = settle.getRank().getRankObjective();
        RankObjective goalRankObjective;

        if (!(NationsPlugin.levelObjectives.size() + 1 == settle.getRank().getLevel())) {
            goalRankObjective = NationsPlugin.levelObjectives.get(settle.getRank().getLevel());
        } else {
            goalRankObjective = new RankObjective(0, 0);
        }

        boolean canLevelup = settle.getBank().getCredit() >= goalRankObjective.getSilver() && ProfessionProgressManager.loadForSettlement(settle.getId()).getScore() >= goalRankObjective.getScore();

        RoseItem filler = new RoseItem.Builder()
                .material(Material.BLACK_STAINED_GLASS_PANE)
                .displayName("")
                .build();
        fillGui(filler);

        RoseItem back = new RoseItem.Builder()
                .material(Material.SPECTRAL_ARROW)
                .displayName(Chat.yellowFade("<b>Zurück</b>"))
                .build();
        back.onClick(e -> {
            new TownGUI(player, settle).open();
        });

        RoseItem submit = new RoseItem.Builder()
                .material(canLevelup ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK)
                .displayName(canLevelup ? Chat.greenFade("Level Up!") : Chat.redFade("Nicht genügend Resourcen!") )
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
                .addLore(settle.getBank().getCredit() >= goalRankObjective.getSilver() ? Chat.greenFade(String.format(settle.getBank().getCredit() + " / " + goalRankObjective.getSilver())) : Chat.yellowFade(String.format(settle.getBank().getCredit() + " / " + goalRankObjective.getSilver())))
                .isEnchanted(settle.getBank().getCredit() >= goalRankObjective.getSilver())
                .build();

        addItem(40, submit);
        addItem(19, objective_bank);
        addItem(45, back);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }
}
