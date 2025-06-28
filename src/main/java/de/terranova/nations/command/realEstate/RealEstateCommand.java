package de.terranova.nations.command.realEstate;

import de.terranova.nations.command.commands.AbstractCommand;
import de.terranova.nations.command.commands.CommandAnnotation;
import de.terranova.nations.gui.RealEstateBrowserGUI;
import org.bukkit.entity.Player;


public class RealEstateCommand extends AbstractCommand {
    public RealEstateCommand() {
        setupHelpCommand();
        initialize();
    }

    @CommandAnnotation(
            domain = "browser",
            permission = "nations.realestate.browser",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean openBrowser(Player p, String[] args){
        new RealEstateBrowserGUI(p);
        return true;
    }

    @CommandAnnotation(
            domain = "rent",
            permission = "nations.realestate.rent",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean rent(Player p, String[] args){
        return true;
    }

    @CommandAnnotation(
            domain = "buy",
            permission = "nations.realestate.buy",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean buy(Player p, String[] args){
        return true;
    }

    @CommandAnnotation(
            domain = "sell",
            permission = "nations.realestate.buy",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean sell(Player p, String[] args){
        return true;
    }
    @CommandAnnotation(
            domain = "nosell",
            permission = "nations.realestate.buy",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean nosell(Player p, String[] args){
        return true;
    }
}
