package de.terranova.nations.gui;

import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.roseGUI.RoseGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RealEstateBrowserGUI  extends RoseGUI {
    public RealEstateBrowserGUI(@NotNull Player player) {
        super(player, "realestate-browser", Chat.blueFade("Real Estate Browser"), 6);
    }
}
