package de.terranova.nations.regions.modules.realEstate;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.database.dao.RealEstateDAO;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.HasParent;
import de.terranova.nations.regions.modules.access.AccessControlled;
import de.terranova.nations.regions.modules.bank.BankHolder;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.InventoryUtil.ItemTransfer;
import de.terranova.nations.utils.TaskTrigger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RealEstateAgent {

    public static Map<UUID, List<RealEstateAgent>> offerCache = new HashMap<>();
    BankHolder parentBank;
    AccessControlled parentTown;
    Region region;
    Region parentRegion;
    TaskTrigger rentListener;
    RealEstateListing data;
    boolean isRented;
    private UUID offeredPlayer;
    private int offeredAmount;
    private String offeredType;


    public RealEstateAgent(Region region, RealEstateListing data) {
        //Wenn es einen Eintrag(data) gibt ist die Region auf dem Markt oder Vermietet sonst Verkauft und in Besitz
        if (data.landlord != null) {
            this.data = data;
        } else {

            this.data = data;
            if (region.getWorldguardRegion().getOwners().getPlayers().stream().findFirst().isPresent()) {
                data.landlord = Bukkit.getOfflinePlayer(region.getWorldguardRegion().getOwners().getPlayers().stream().findFirst().get()).getUniqueId();
            }
            if (region.getWorldguardRegion().getOwners().getUniqueIds().stream().findFirst().isPresent()) {
                data.landlord = Bukkit.getOfflinePlayer(region.getWorldguardRegion().getOwners().getUniqueIds().stream().findFirst().get()).getUniqueId();
            }

        }
        this.region = region;
        if (region instanceof HasParent<?> parent) {
            this.parentRegion = parent.getParent();
            if (parent.getParent() instanceof BankHolder holder) {
                this.parentBank = holder;
            }
            if (parent.getParent() instanceof AccessControlled town) {
                this.parentTown = town;
            }
        }

        //Wenn die Region nicht zur Miete ist aber ein Mietpreis vorliegt ist Sie vermietet
        if (data != null) {
            if (data.rentPrice != 0 && !isForRent()) {
                isRented = true;
                if (Instant.now().isAfter(data.timestamp)) {
                    rentEnded();
                } else {
                    rentListener = new TaskTrigger(() -> {
                        rentEnded();
                        return null;
                    });
                    rentListener.scheduleAt(data.timestamp);
                }
            }
        }

    }

    public void rentEnded() {
        overwriteOwner(data.landlord);
        RealEstateDAO.removeRealEstate(this);
        isRented = false;
    }

    public boolean endRentByPlayer(Player player) {
        if (!isRented) {
            player.sendMessage(Chat.errorFade("Du kannst kein Grundstück kündigen dass auch nicht gemietet ist."));
            return false;
        }
        if (getRegionUser() != player.getUniqueId()) {
            player.sendMessage(Chat.errorFade("Du kannst nur eigen angemietete Verträge kündigen."));
            return false;
        }
        rentEnded();
        return true;
    }

    public void addToOfferCacheMarket() {
        if (data.isForBuy || data.isForRent) {
            RealEstateMarketCache.upsertListing(this.parentRegion.getId(), (HasRealEstateAgent) region);
        }
    }

    public void buyEstate(Player buyer) {

        if (!data.isForBuy) {
            buyer.sendMessage(Chat.errorFade("Dieses Grundstück ist von " + Bukkit.getOfflinePlayer(data.landlord).getName() + " belegt."));
            return;
        } else if (buyer.getUniqueId().equals(data.landlord)) {
            buyer.sendMessage(Chat.errorFade("Du kannst dein eigen angebotenes Grundtstück nicht erwerben. "));
            return;
        }

        int transfer = ItemTransfer.charge(buyer, "terranova_silver", data.buyPrice, true);
        if (transfer == -1) {
            buyer.sendMessage(Chat.errorFade("Du hast leider nicht genug Silver in deinem Inventory"));
            return;
        } else {
            RealEstateListing.holdings.merge(data.landlord, transfer, Integer::sum);
            RealEstateDAO.upsertHolding(data.landlord, RealEstateListing.holdings.get(data.landlord));
        }
        clearOffer();
        overwriteOwner(buyer.getUniqueId());
        data.isForBuy = false;
        data.isForRent = false;
        data.landlord = buyer.getUniqueId();
        data.timestamp = Instant.now();

        stripmember();
        RealEstateDAO.removeRealEstate(this);
        RealEstateMarketCache.removeListing(parentRegion.getId(), region.getId());

        buyer.sendMessage(Chat.greenFade(String.format("Du hast soeben erfolgreich %s für %s Silber gekauft.", region.getName(), transfer)));
    }

    public void rentEstate(Player buyer) {

        if (buyer.getUniqueId().equals(data.landlord)) {
            buyer.sendMessage(Chat.errorFade("Du kannst dein eigen angebotenes Grundtstück nicht erwerben. "));
            return;
        } else if (isRented && !region.getWorldguardRegion().getOwners().getUniqueIds().stream().findFirst().get().equals(buyer.getUniqueId())) {
            buyer.sendMessage(Chat.errorFade("Dieses Grundstück ist von " + Bukkit.getOfflinePlayer(data.landlord).getName() + " belegt."));
            return;
        }

        int transfer = ItemTransfer.charge(buyer, "terranova_silver", data.rentPrice, true);
        if (transfer == -1) {
            buyer.sendMessage(Chat.errorFade("Du hast leider nicht genug Silver in deinem Inventory"));
            return;
        } else {

            RealEstateListing.holdings.merge(data.landlord, transfer, Integer::sum);
            RealEstateDAO.upsertHolding(data.landlord, RealEstateListing.holdings.get(data.landlord));


        }

        if (isRented) {
            ZonedDateTime nowZdt = Instant.now().atZone(ZoneId.systemDefault());
            ZonedDateTime otherZdt = data.timestamp.atZone(ZoneId.systemDefault());
            long monthsBetween = ChronoUnit.MONTHS.between(nowZdt, otherZdt);

            if (monthsBetween >= 2) {
                buyer.sendMessage(Chat.errorFade("Du kannst nicht mehr als 2 Monate im voraus mieten."));
                return;
            }
            data.timestamp = data.timestamp.plus(14, ChronoUnit.DAYS);
            RealEstateDAO.upsertRealEstate(this.getRegion().getId().toString(), data);
            buyer.sendMessage(Chat.greenFade(String.format("Du hast soeben erfolgreich %s für %s Silber 14 Tage verlängert, dein Mietvertrag läuft bis %s.", region.getName(), transfer, Chat.prettyInstant(data.timestamp))));
            return;

        } else {
            data.timestamp = Instant.now().plus(14, ChronoUnit.DAYS);
        }
        clearOffer();
        overwriteOwner(buyer.getUniqueId());
        data.isForBuy = false;
        data.isForRent = false;
        isRented = true;

        stripmember();
        RealEstateDAO.upsertRealEstate(this.getRegion().getId().toString(), data);
        RealEstateMarketCache.removeListing(parentRegion.getId(), region.getId());
        buyer.sendMessage(Chat.greenFade(String.format("Du hast soeben erfolgreich %s für %s Silber 14 Tage gemietet.", region.getName(), transfer)));
    }

    public boolean withdrawEstate() {
        if (!RealEstateMarketCache.hasListing(parentRegion.getId(), region.getId())) {
            return false;
        }
        RealEstateMarketCache.removeListing(parentRegion.getId(), region.getId());
        this.data.rentPrice = 0;
        this.data.buyPrice = 0;
        this.data.isForRent = false;
        this.data.isForBuy = false;
        RealEstateDAO.upsertRealEstate(this.getRegion().getId().toString(), data);
        return true;

    }

    public boolean sellEstate(Player seller, int buyAmount, int rentAmount) {

        if (seller.getUniqueId() != data.landlord) {
            if (!region.getWorldguardRegion().getOwners().contains(seller.getUniqueId())) {
                seller.sendMessage(Chat.errorFade("Du kannst keine Region anbieten die nicht deine ist."));
                return false;
            }
        }

        if (isRented) {
            seller.sendMessage(Chat.errorFade("Du kannst keine Region anbieten die gerade vermietet ist."));
            return false;
        }

        if (buyAmount == 0 && rentAmount == 0) {
            if (withdrawEstate()) {
                seller.sendMessage(Chat.greenFade("Region " + region.getName() + " wurde vom Markt erfolgreich entfernt."));
            } else {
                seller.sendMessage(Chat.errorFade("Du kannst keine Region zurückziehen die nicht auf dem Markt ist."));
            }

            return false;
        }

        data.isForBuy = buyAmount > 0;
        data.isForRent = rentAmount > 0;
        data.buyPrice = buyAmount;
        data.rentPrice = rentAmount;
        data.landlord = seller.getUniqueId();
        data.timestamp = Instant.now();

        RealEstateDAO.upsertRealEstate(this.getRegion().getId().toString(), data);
        RealEstateMarketCache.upsertListing(this.parentRegion.getId(), (HasRealEstateAgent) region);
        return true;
    }

    public boolean hasOffer(Player p) {
        return offeredPlayer.equals(p.getUniqueId());
    }

    public String getOfferType() {
        return offeredType;
    }

    public Integer getOfferAmount() {
        return offeredAmount;
    }

    public boolean offerEstate(Player offerer, String type, int amount, Player user) {

        if (offerer.getUniqueId() != data.landlord) {
            if (!region.getWorldguardRegion().getOwners().contains(offerer.getUniqueId())) {
                offerer.sendMessage(Chat.errorFade("Du kannst keine Region anbieten die nicht deine ist."));
                return false;
            }
        }

        if (isRented) {
            offerer.sendMessage(Chat.errorFade("Du kannst keine Region anbieten die gerade vermietet ist."));
            return false;
        }

        if (offerer.getUniqueId().equals(user.getUniqueId())) {
            offerer.sendMessage(Chat.errorFade("Du kannst dir selber keine region anbieten."));
            return false;
        }

        if (amount <= 0) {
            clearOffer();
        }

        offeredPlayer = user.getUniqueId();
        offeredAmount = amount;
        offeredType = type;
        offerCache.computeIfAbsent(offeredPlayer, k -> new ArrayList<>()).add(this);
        user.sendMessage(Chat.greenFade(String.format("Dir wurde die Region %s von %s zum %s angeboten für %s Coins.", region.getName(), offerer.getName(), (Objects.equals(type, "buy")) ? "kaufen" : "miete / 14 Tage", amount)));
        Component message = Component.text("Zum Annehmen einfach ", NamedTextColor.GREEN)
                .append(
                        Component.text("[hier]", NamedTextColor.AQUA)
                                .decorate(TextDecoration.BOLD)
                                .clickEvent(ClickEvent.runCommand("/re accept " + region.getName()))
                );

        user.sendMessage(message);

        return true;
    }


    public void clearOffer() {
        if (offeredPlayer != null) {
            List<RealEstateAgent> offers = offerCache.get(offeredPlayer);
            if (offers != null) {
                offers.remove(this);
                if (offers.isEmpty()) {
                    offerCache.remove(offeredPlayer);
                }
            }
        }
        offeredPlayer = null;
        offeredAmount = 0;
        offeredType = null;
    }

    public void acceptOffer(Player acquirer) {

        if (!acquirer.getUniqueId().equals(offeredPlayer)) {
            acquirer.sendMessage(Chat.errorFade("Du hast kein angebot von dieser Stadt vorliegen."));
            return;
        }

        if (offeredType.equals("rent")) {

            int transfer = ItemTransfer.charge(acquirer, "terranova_silver", offeredAmount, true);
            if (transfer == -1) {
                acquirer.sendMessage(Chat.errorFade("Du hast leider nicht genug Silver in deinem Inventory"));
                return;
            } else {
                RealEstateDAO.upsertHolding(data.landlord, transfer);
            }

            clearOffer();
            withdrawEstate();
            overwriteOwner(acquirer.getUniqueId());

            data.rentPrice = offeredAmount;
            data.buyPrice = 0;
            isRented = true;

            stripmember();
            RealEstateDAO.upsertRealEstate(this.getRegion().getId().toString(), data);
            RealEstateMarketCache.removeListing(parentRegion.getId(), region.getId());
            acquirer.sendMessage(Chat.greenFade(String.format("Du hast soeben erfolgreich %s für %s Silber 14 Tage gemietet.", region.getName(), transfer)));
        } else if (offeredType.equals("buy")) {

            int transfer = ItemTransfer.charge(acquirer, "terranova_silver", data.buyPrice, true);
            if (transfer == -1) {
                acquirer.sendMessage(Chat.errorFade("Du hast leider nicht genug Silver in deinem Inventory"));
                return;
            } else {
                RealEstateDAO.upsertHolding(data.landlord, transfer);
            }
            clearOffer();
            withdrawEstate();
            overwriteOwner(acquirer.getUniqueId());
            data.isForBuy = false;
            data.isForRent = false;
            data.landlord = acquirer.getUniqueId();
            data.timestamp = Instant.now();

            stripmember();
            RealEstateMarketCache.removeListing(parentRegion.getId(), region.getId());

            acquirer.sendMessage(Chat.greenFade(String.format("Du hast soeben erfolgreich %s für %s Silber gekauft.", region.getName(), transfer)));
        }

    }

    public String overwriteOwner(UUID ownerUuid) {
        DefaultDomain owners = region.getWorldguardRegion().getOwners();
        String name = owners.getPlayers().stream().findFirst().orElse(null);
        owners.clear();
        owners.addPlayer(ownerUuid);
        region.getWorldguardRegion().setOwners(owners);
        return name;
    }

    public boolean hasmember(UUID user) {
        DefaultDomain members = region.getWorldguardRegion().getMembers();
        return members.contains(user);
    }

    public void addmember(Player p, UUID user) {
        if (!getRegionUser().equals(p.getUniqueId())) {
            p.sendMessage(Chat.errorFade("Du kannst nicht auf Grundstücke zugreifen die dir nicht gehören!."));
            return;
        }
        DefaultDomain members = region.getWorldguardRegion().getMembers();
        members.addPlayer(user);
        region.getWorldguardRegion().setMembers(members);
        p.sendMessage(Chat.greenFade(String.format("Du hast erfolgreich %s zu %s hinzugefügt.", Bukkit.getOfflinePlayer(user).getName(), region.getName())));
    }

    public void removemember(Player p, UUID user) {
        if (!getRegionUser().equals(p.getUniqueId())) {
            p.sendMessage(Chat.errorFade("Du kannst nicht auf Grundstücke zugreifen die dir nicht gehören!."));
            return;
        }
        DefaultDomain members = region.getWorldguardRegion().getMembers();
        members.removePlayer(user);
        region.getWorldguardRegion().setMembers(members);
        p.sendMessage(Chat.greenFade(String.format("Du hast erfolgreich %s zu %s entfernt.", Bukkit.getOfflinePlayer(user).getName(), region.getName())));
    }

    public void kick(Player p) {
        RegionManager rm = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(p.getWorld()));
        if (rm == null) return;

        ProtectedRegion region = rm.getRegion(getRegion().getName());
        if (region == null) return;

        BlockVector3 pos = BukkitAdapter.asBlockVector(p.getLocation());
        if (!region.contains(pos)) return;

        // Get region bounds
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        int x = pos.x();
        int z = pos.z();

        // Push player outwards in X direction
        if (Math.abs(x - min.x()) < Math.abs(x - max.x())) {
            x = min.x() - 2;
        } else {
            x = max.x() + 2;
        }

        // Push player outwards in Z direction
        if (Math.abs(z - min.z()) < Math.abs(z - max.z())) {
            z = min.z() - 2;
        } else {
            z = max.z() + 2;
        }

        // Compute safe Y height
        Location outside = new Location(p.getWorld(), x + 0.5, 0, z + 0.5);
        outside.setY(p.getWorld().getHighestBlockYAt(outside) + 1);

        p.teleport(outside);
        p.sendMessage(ChatColor.RED + "You have been kicked from region \"" +
                region.getId() + "\"!");
    }


    public void stripmember() {
        DefaultDomain members = region.getWorldguardRegion().getMembers();
        members.clear();
        region.getWorldguardRegion().setMembers(members);
    }

    public Region getRegion() {
        return region;
    }

    public boolean isOwner(UUID user) {
        return user.equals(data.landlord);
    }

    public boolean isRented(){
        return isRented;
    }

    public UUID getLandlord() {
        return data.landlord;
    }

    public UUID getRegionUser() {
        return region.getWorldguardRegion().getOwners().getUniqueIds().stream().findFirst().orElseGet(this::getLandlord);
    }

    public Instant getTimestamp() {
        return data.timestamp;
    }


    public boolean isForBuy() {
        return data.isForBuy;
    }

    public boolean isForRent() {
        return data.isForRent;
    }

    public int getBuyPrice() {
        return data.buyPrice;
    }

    public int getRentPrice() {
        return data.rentPrice;
    }

    public Instant getRentEndingTime() {
        if (!isRented) return null;
        return data.timestamp;
    }

}
