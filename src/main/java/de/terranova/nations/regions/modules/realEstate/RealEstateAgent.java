package de.terranova.nations.regions.modules.realEstate;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.database.dao.RealEstateDAO;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.HasParent;
import de.terranova.nations.regions.modules.access.AccessControlled;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.regions.modules.bank.BankHolder;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.utils.InventoryUtil.ItemTransfer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class RealEstateAgent {

    BankHolder parentBank;
    AccessControlled parentTown;
    Region region;
    Region parentRegion;

    RealEstateData data;
    UUID propertyOwner;
    boolean isRented;

    public RealEstateAgent(Region region, RealEstateData data) {
        //Wenn es einen Eintrag(data) gibt ist die Region auf dem Markt oder Vermietet sonst Verkauft und in Besitz
        if(data != null) {
            this.data = data;
            this.propertyOwner = data.ownerId;
        } else {
            this.propertyOwner = Bukkit.getOfflinePlayer(region.getWorldguardRegion().getOwners().getPlayers().stream().findFirst().get()).getUniqueId();
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
        if(data != null) {
            if(data.rentPrice != 0 && !isForRent()) {
                isRented = true;
                if(Instant.now().isAfter(data.timestamp)){
                    rentEnded();
                }
            }
        }

    }

    public void rentEnded() {
        String oldOwner = overwriteOwner(propertyOwner);
        RealEstateDAO.removeRealEstate(this);
        isRented = false;
        parentTown.getAccess().broadcast(String.format("%s seine Miete für %s | %s ist ausgelaufen.", oldOwner, region.getName(),Chat.prettyLocation(region.getRegionCenter())), AccessLevel.COUNCIL);
    }

    public void addToOfferCacheMarket(){
        if (data.isForBuy || data.isForRent) {
            RealEstateOfferCache.addRealestate(this.parentRegion.getId(),(CanBeSold) region);
        }
    }


    public void buyEstate(Player buyer) {

        if (!data.isForBuy) {
            buyer.sendMessage(Chat.errorFade("Dieses Grundstück ist von " + Bukkit.getOfflinePlayer(data.ownerId).getName() + " belegt."));
            return;
        }else if(buyer.getUniqueId() == data.ownerId){
            buyer.sendMessage(Chat.errorFade("Du kannst dein eigen angebotenes Grundtstück nicht erwerben. "));
            return;
        }

        int transfer = ItemTransfer.charge(buyer, "terranova_silver", data.buyPrice, true);
        if (transfer == -1) {
            buyer.sendMessage(Chat.errorFade("Du hast leider nicht genug Silver in deinem Inventory"));
            return;
        } else {
            parentBank.getBank().cashTransfer(String.format("Property bought by %s(%s) for %s", buyer.getName(), buyer.getUniqueId(), transfer), transfer);
        }

        overwriteOwner( buyer.getUniqueId());
        data.isForBuy = false;
        data.isForRent = false;
        data.ownerId = buyer.getUniqueId();
        data.timestamp = Instant.now();
        RealEstateDAO.removeRealEstate(this);
        RealEstateOfferCache.removeRealestate(parentRegion.getId(),region.getId());
        buyer.sendMessage(Chat.greenFade(String.format("Du hast soeben erfolgreich %s für %s Silber gekauft.", region.getName(), transfer)));
        parentTown.getAccess().broadcast(String.format("%s hat soeben erfolgreich für %s Silber %s gekauft.", buyer.getName(), transfer, region.getName()), AccessLevel.CITIZEN);

    }

    public void rentEstate(Player buyer) {

        Bukkit.broadcast(Chat.cottonCandy(isRented + ""));
        Bukkit.broadcast(Chat.cottonCandy(buyer.getUniqueId() + ""));
        Bukkit.broadcast(Chat.cottonCandy(region.getWorldguardRegion().getOwners().getUniqueIds().stream().findFirst().get() + ""));
        Bukkit.broadcast(Chat.cottonCandy(data.ownerId + ""));
        if(buyer.getUniqueId().equals(data.ownerId)){
            buyer.sendMessage(Chat.errorFade("Du kannst dein eigen angebotenes Grundtstück nicht erwerben. "));
            return;
        } else
        if (isRented && !region.getWorldguardRegion().getOwners().getUniqueIds().stream().findFirst().get().equals(buyer.getUniqueId())) {
            buyer.sendMessage(Chat.errorFade("Dieses Grundstück ist von " + Bukkit.getOfflinePlayer(data.ownerId).getName() + " belegt."));
            return;
        }


        int transfer = ItemTransfer.charge(buyer, "terranova_silver", data.rentPrice, true);
        if (transfer == -1) {
            buyer.sendMessage(Chat.errorFade("Du hast leider nicht genug Silver in deinem Inventory"));
            return;
        } else {
            parentBank.getBank().cashTransfer(String.format("Property rented by %s(%s) for %s", buyer.getName(), buyer.getUniqueId(), transfer), transfer);
        }

        if(isRented) {
            ZonedDateTime nowZdt = Instant.now().atZone(ZoneId.systemDefault());
            ZonedDateTime otherZdt = data.timestamp.atZone(ZoneId.systemDefault());
            long monthsBetween = ChronoUnit.MONTHS.between(nowZdt, otherZdt);

            if(monthsBetween >= 2){
                buyer.sendMessage(Chat.errorFade("Du kannst nicht mehr als 2 Monate im voraus mieten."));
                return;
            }
            data.timestamp = data.timestamp.plus(14, ChronoUnit.DAYS);

        } else {
            data.timestamp = Instant.now().plus(14, ChronoUnit.DAYS);
        }

        overwriteOwner(buyer.getUniqueId());
        data.isForBuy = false;
        data.isForRent = false;
        isRented = true;


        RealEstateDAO.upsertRealEstate(this);
        RealEstateOfferCache.removeRealestate(parentRegion.getId(),region.getId());
        buyer.sendMessage(Chat.greenFade(String.format("Du hast soeben erfolgreich %s für %s Silber 14 Tage gemietet.", region.getName(), transfer)));
        parentTown.getAccess().broadcast(String.format("%s hat soeben erfolgreich für %s Silber %s 14 Tage gemietet.", buyer.getName(), transfer, region.getName()), AccessLevel.CITIZEN);
    }

    public boolean sellEstate(Player seller, boolean isForBuy,int buyAmount,boolean isForRent, int rentAmount) {

        if(seller.getUniqueId() != data.ownerId){
            if(!region.getWorldguardRegion().getOwners().contains(seller.getUniqueId())){
                seller.sendMessage(Chat.errorFade("Du kannst keine Region anbieten die nicht deine ist."));
                return false;
            }
        }

        if(isRented){
            seller.sendMessage(Chat.errorFade("Du kannst keine Region anbieten die gerade vermietet ist."));
            return false;
        }

        data.isForBuy = isForBuy;
        data.isForRent = isForRent;
        data.buyPrice = buyAmount;
        data.rentPrice = rentAmount;
        data.ownerId = seller.getUniqueId();
        data.timestamp = Instant.now();

        RealEstateDAO.upsertRealEstate(this);
        RealEstateOfferCache.addRealestate(this.parentRegion.getId(),(CanBeSold) region);
        return true;
    }

    public String overwriteOwner(UUID ownerUuid) {
        DefaultDomain owners = region.getWorldguardRegion().getOwners();
        String name = owners.getPlayers().stream().findFirst().orElse(null);
        owners.clear();
        owners.addPlayer(ownerUuid);
        region.getWorldguardRegion().setOwners(owners);
        return name;
    }

    public Region getRegion() {
        return region;
    }

    public UUID getRegionOwner() {
        return data.ownerId;
    }

    public Instant getTimestamp() {
        return data.timestamp;
    }


    public boolean isForBuy() {return data.isForBuy; }
    public boolean isForRent() {return data.isForRent; }
    public int getBuyPrice() {return data.buyPrice; }
    public int getRentPrice() {return data.rentPrice; }

    public Instant getRentEndingTime(){
        if(!isRented) return null;
        return data.timestamp;
    }

}
