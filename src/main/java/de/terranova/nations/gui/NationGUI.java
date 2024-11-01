package de.terranova.nations.gui;

import de.mcterranova.terranovaLib.roseGUI.RoseGUI;
import de.mcterranova.terranovaLib.utils.Chat;
import org.bukkit.entity.Player;

public class NationGUI extends RoseGUI {
    public NationGUI(Player player) {
        super(player, "nation-gui", Chat.blueFade("<b>Nation Menu"), 5);
        // Initialize GUI components
    }

    // Implement onOpen and onClose methods
}
