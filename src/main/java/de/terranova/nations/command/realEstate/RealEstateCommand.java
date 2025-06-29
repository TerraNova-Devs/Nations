package de.terranova.nations.command.realEstate;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.terranova.nations.command.commands.AbstractCommand;
import de.terranova.nations.command.commands.CommandAnnotation;
import de.terranova.nations.gui.RealEstateBrowserGUI;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.realEstate.CanBeSold;
import de.terranova.nations.utils.Chat;
import org.bukkit.entity.Player;

import java.util.Optional;


public class RealEstateCommand extends AbstractCommand {
    public RealEstateCommand() {
        setupHelpCommand();
        initialize();
    }

    public static Optional<ProtectedRegion> getRegionByName(Player player, String regionName) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) {
            return Optional.empty();
        }

        ProtectedRegion region = regionManager.getRegion(regionName);
        return Optional.ofNullable(region);
    }

    @CommandAnnotation(domain = "browser", permission = "nations.realestate.browser", description = "Opens the Realestate Browser", usage = "/realestate browser")
    public boolean openBrowser(Player p, String[] args) {
        new RealEstateBrowserGUI(p);
        return true;
    }

    @CommandAnnotation(domain = "rent.$name", permission = "nations.realestate.rent", description = "Opens the Realestate Browser", usage = "/realestate browser")
    public boolean rent(Player p, String[] args) {
        Optional<ProtectedRegion> Oregion = getRegionByName(p, args[0]);
        if (!Oregion.isPresent()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[0] + " existiert nicht."));
            return false;
        }
        Optional<Region> region = de.terranova.nations.regions.RegionManager.retrieveRegion(Oregion.get());
        if (!region.isPresent()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[0] + " ist keine Nations Region."));
            return false;
        }
        if (!(region.get() instanceof CanBeSold agent)) {
            p.sendMessage(Chat.errorFade("Die Region " + args[0] + " hat kein RealEstate Modul."));
            return false;
        }
        agent.getAgent().rentEstate(p);
        return true;
    }

    @CommandAnnotation(domain = "buy.$name", permission = "nations.realestate.buy", description = "Opens the Realestate Browser", usage = "/realestate browser")
    public boolean buy(Player p, String[] args) {
        Optional<ProtectedRegion> Oregion = getRegionByName(p, args[0]);
        if (!Oregion.isPresent()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[0] + " existiert nicht."));
            return false;
        }
        Optional<Region> region = de.terranova.nations.regions.RegionManager.retrieveRegion(Oregion.get());
        if (!region.isPresent()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[0] + " ist keine Nations Region."));
            return false;
        }
        if (!(region.get() instanceof CanBeSold agent)) {
            p.sendMessage(Chat.errorFade("Die Region " + args[0] + " hat kein RealEstate Modul."));
            return false;
        }
        agent.getAgent().buyEstate(p);
        return true;
    }

    @CommandAnnotation(domain = "sell.$name", permission = "nations.realestate.sell", description = "Opens the Realestate Browser", usage = "/realestate browser")
    public boolean sell(Player p, String[] args) {
        return true;
    }

    @CommandAnnotation(domain = "nosell.$name", permission = "nations.realestate.buy", description = "Opens the Realestate Browser", usage = "/realestate browser")
    public boolean nosell(Player p, String[] args) {
        return true;
    }
}
