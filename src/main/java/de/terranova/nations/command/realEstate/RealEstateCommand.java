package de.terranova.nations.command.realEstate;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.terranova.nations.command.commands.AbstractCommand;
import de.terranova.nations.command.commands.CachedSupplier;
import de.terranova.nations.command.commands.CommandAnnotation;
import de.terranova.nations.database.dao.RealEstateDAO;
import de.terranova.nations.gui.RealEstateBrowserGUI;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.realEstate.HasRealEstateAgent;
import de.terranova.nations.regions.modules.realEstate.RealEstateListing;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.InventoryUtil.ItemTransfer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class RealEstateCommand extends AbstractCommand {
    public RealEstateCommand() {
        addPlaceholder("$SETTLES", new CachedSupplier<>(() -> de.terranova.nations.regions.RegionManager.retrieveAllCachedRegions("settle").values().stream().map(Region::getName).toList(),100000) );
        addPlaceholder("$type", () -> List.of("rent","buy"));
        registerSubCommand(this, "browser");
        registerSubCommand(this, "sell");
        registerSubCommand(this, "buy");
        registerSubCommand(this, "rent");
        registerSubCommand(this, "info");
        registerSubCommand(this, "add");
        registerSubCommand(this, "remove");
        registerSubCommand(this, "resign");
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

    @CommandAnnotation(
            domain = "browser.$SETTLES",
            permission = "nations.realestate.browser",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser <Stadt>"
    )
    public boolean openBrowser(Player p, String[] args) {
        Optional<Region> osettle = de.terranova.nations.regions.RegionManager.retrieveRegion("settle",args[1]);
        if(osettle.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die von dir genannte Stadt konnte nicht gefunden werden."));
            return false;
        }
        new RealEstateBrowserGUI(p,osettle.get()).open();
        return true;
    }

    @CommandAnnotation(
            domain = "browser",
            permission = "nations.realestate.browser",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser <Stadt>"
    )
    public boolean browser(Player p, String[] args) {
        new RealEstateBrowserGUI(p,null).open();
        return true;
    }

    @CommandAnnotation(
            domain = "info.$name",
            permission = "nations.realestate.rent",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser")
    public boolean info(Player p, String[] args) {
        Optional<ProtectedRegion> Oregion = getRegionByName(p, args[1]);
        if (Oregion.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " existiert nicht."));
            return false;
        }
        Optional<Region> region = de.terranova.nations.regions.RegionManager.retrieveRegion(Oregion.get());
        if (region.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " ist keine Nations Region."));
            return false;
        }
        if (!(region.get() instanceof HasRealEstateAgent agent)) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " hat kein RealEstate Modul."));
            return false;
        }
        Instant time = agent.getAgent().getRentEndingTime();
        p.sendMessage(Chat.cottonCandy("Infos: " + agent.getAgent().getRegion().getName()));
        p.sendMessage(Chat.cottonCandy("Besitzer: " + Bukkit.getOfflinePlayer(agent.getAgent().getRegionLandlord()).getName()));
        if(!agent.getAgent().getRegionLandlord().equals(agent.getAgent().getRegionUser())){
            p.sendMessage(Chat.cottonCandy("Mieter: " + Bukkit.getOfflinePlayer(agent.getAgent().getRegionLandlord()).getName()));
        }

        if(time != null){
            p.sendMessage(Chat.cottonCandy("Mietzeit:" + Chat.prettyInstant(time)));
        }
        return true;
    }

    @CommandAnnotation(domain = "rent.$name",
            permission = "nations.realestate.rent",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean rent(Player p, String[] args) {
        Optional<ProtectedRegion> Oregion = getRegionByName(p, args[1]);
        if (Oregion.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " existiert nicht."));
            return false;
        }
        Optional<Region> region = de.terranova.nations.regions.RegionManager.retrieveRegion(Oregion.get());
        if (region.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " ist keine Nations Region."));
            return false;
        }
        if (!(region.get() instanceof HasRealEstateAgent agent)) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " hat kein RealEstate Modul."));
            return false;
        }
        agent.getAgent().rentEstate(p);
        return true;
    }

    @CommandAnnotation(
            domain = "buy.$name",
            permission = "nations.realestate.buy",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean buy(Player p, String[] args) {
        Optional<ProtectedRegion> Oregion = getRegionByName(p, args[1]);
        if (Oregion.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " existiert nicht."));
            return false;
        }
        Optional<Region> region = de.terranova.nations.regions.RegionManager.retrieveRegion(Oregion.get());
        if (region.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " ist keine Nations Region."));
            return false;
        }
        if (!(region.get() instanceof HasRealEstateAgent agent)) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " hat kein RealEstate Modul."));
            return false;
        }
        agent.getAgent().buyEstate(p);
        return true;
    }

    @CommandAnnotation(
            domain = "resign.$name",
            permission = "nations.realestate.buy",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean resign(Player p, String[] args) {
        Optional<ProtectedRegion> Oregion = getRegionByName(p, args[1]);
        if (Oregion.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " existiert nicht."));
            return false;
        }
        Optional<Region> region = de.terranova.nations.regions.RegionManager.retrieveRegion(Oregion.get());
        if (region.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " ist keine Nations Region."));
            return false;
        }
        if (!(region.get() instanceof HasRealEstateAgent agent)) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " hat kein RealEstate Modul."));
            return false;
        }
        agent.getAgent().endRentByPlayer(p);
        return true;
    }

    @CommandAnnotation(
            domain = "sell.$name.$buyamount.$rentamount",
            permission = "nations.realestate.nosell",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean sell(Player p, String[] args) {
        Optional<ProtectedRegion> Oregion = getRegionByName(p, args[1]);
        if (Oregion.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " existiert nicht."));
            return false;
        }
        Optional<Region> region = de.terranova.nations.regions.RegionManager.retrieveRegion(Oregion.get());
        if (region.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " ist keine Nations Region."));
            return false;
        }
        if(region.get() instanceof HasRealEstateAgent agent) {
            try {
                int buy = Integer.parseInt(args[2]);
                int rent = Integer.parseInt(args[3]);

                if(agent.getAgent().sellEstate(p,buy ,rent)) p.sendMessage(Chat.greenFade("Region erfolgreich auf den Markt gebracht."));
            } catch (NumberFormatException e) {
                p.sendMessage(Chat.errorFade("Bitte gib Zahlen ein!"));
            }

        } else {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " kann nicht verkauft werden."));
        }
        return true;
    }

    @CommandAnnotation(
            domain = "offer.$name.$type.$amount.$user",
            permission = "nations.realestate.nosell",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean offer(Player p, String[] args) {
        Optional<ProtectedRegion> Oregion = getRegionByName(p, args[1]);
        if (Oregion.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " existiert nicht."));
            return false;
        }
        Optional<Region> region = de.terranova.nations.regions.RegionManager.retrieveRegion(Oregion.get());
        if (region.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " ist keine Nations Region."));
            return false;
        }
        if (!Set.of("rent", "buy").contains(args[2])) {
            p.sendMessage(Chat.errorFade("Du musst richtig spezifizieren ob die region zum kaufen oder mieten ist."));
            return false;
        }
        Player player = Bukkit.getPlayer(args[4]);
        if(player == null) {
            p.sendMessage(Chat.errorFade("Der von dir angewählte Spieler ist nicht online."));
            return false;
        }
        if(region.get() instanceof HasRealEstateAgent agent) {
            try {
                int amount = Integer.parseInt(args[3]);

                if(agent.getAgent().offerEstate(p,args[2] ,amount,player.getUniqueId())) p.sendMessage(Chat.greenFade("Region erfolgreich angeboten."));
            } catch (NumberFormatException e) {
                p.sendMessage(Chat.errorFade("Bitte gib Zahlen ein!"));
            }

        } else {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " kann nicht verkauft werden."));
        }
        return true;
    }

    @CommandAnnotation(
            domain = "nosell.$name",
            permission = "nations.realestate.nobuy",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean nosell(Player p, String[] args) {
        return true;
    }

    @CommandAnnotation(
            domain = "add.$name.$name",
            permission = "nations.realestate.buy",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean add(Player p, String[] args) {
        Optional<ProtectedRegion> Oregion = getRegionByName(p, args[1]);
        if (Oregion.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " existiert nicht."));
            return false;
        }
        Optional<Region> region = de.terranova.nations.regions.RegionManager.retrieveRegion(Oregion.get());
        if (region.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " ist keine Nations Region."));
            return false;
        }
        if (!(region.get() instanceof HasRealEstateAgent agent)) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " hat kein RealEstate Modul."));
            return false;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            p.sendMessage(Chat.errorFade("Der Spieler " + args[2] + " ist nicht online."));
            return false;
        }
        if(agent.getAgent().hasmember(target.getUniqueId())){
            p.sendMessage(Chat.errorFade("Der von dir banannte Spieler " + target.getName() + " ist bereits hinzugefügt."));
            return false;
        }
        agent.getAgent().addmember(target.getUniqueId());
        Chat.greenFade("Du hast Spieler " + target.getName() + " erfolgreich zu " + agent.getAgent().getRegion().getName() + " hinzugefügt.");
        return true;
    }

    @CommandAnnotation(
            domain = "remove.$name.$name",
            permission = "nations.realestate.buy",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean remove(Player p, String[] args) {
        Optional<ProtectedRegion> Oregion = getRegionByName(p, args[1]);
        if (Oregion.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " existiert nicht."));
            return false;
        }
        Optional<Region> region = de.terranova.nations.regions.RegionManager.retrieveRegion(Oregion.get());
        if (region.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " ist keine Nations Region."));
            return false;
        }
        if (!(region.get() instanceof HasRealEstateAgent agent)) {
            p.sendMessage(Chat.errorFade("Die Region " + args[1] + " hat kein RealEstate Modul."));
            return false;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            p.sendMessage(Chat.errorFade("Der Spieler " + args[2] + " ist nicht online."));
            return false;
        }
        if(!agent.getAgent().hasmember(target.getUniqueId())){
            p.sendMessage(Chat.errorFade("Der von dir banannte Spieler " + target.getName() + " ist kein Mitglied der Region."));
            return false;
        }
        agent.getAgent().removemember(target.getUniqueId());
        Chat.greenFade("Du hast Spieler " + target.getName() + " erfolgreich von " + agent.getAgent().getRegion().getName() + " entfernt.");
        return true;
    }

    @CommandAnnotation(
            domain = "holdings",
            permission = "nations.realestate.buy",
            description = "Opens the Realestate Browser",
            usage = "/realestate browser"
    )
    public boolean holdings(Player p, String[] args) {
        if(!RealEstateListing.holdings.containsKey(p.getUniqueId())){
            p.sendMessage(Chat.cottonCandy("Es gibt nichts für dich abzuholen."));
            return true;
        }
        int credited = ItemTransfer.credit(p,"terranova_silver", RealEstateListing.holdings.get(p.getUniqueId()),false);

        if(credited == 0){
            p.sendMessage(Chat.errorFade("Du benötigst mehr freien Platz im Inventar."));
            return true;
        }

        RealEstateListing.holdings.compute(p.getUniqueId(), (key, value) -> value == null || value - credited <= 0 ? null : value - credited);
        RealEstateDAO.upsertHolding(p.getUniqueId(), RealEstateListing.holdings.getOrDefault(p.getUniqueId(), 0));
        p.sendMessage(Chat.greenFade("Dir wurde erfolgreich " + credited + " gutgeschrieben."));
        if(RealEstateListing.holdings.containsKey(p.getUniqueId())){
            p.sendMessage(Chat.blueFade("Verbleibend: " + RealEstateListing.holdings.get(p.getUniqueId())));
        }
        return true;
    }

}
