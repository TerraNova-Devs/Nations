package de.terranova.nations.regions.modules.realEstate;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class RealEstateAgent {

    BankHolder parentBank;
    AccessControlled parentTown;
    Region region;
    Region parentRegion;
    RealEstateData data;

    public RealEstateAgent(Region region, RealEstateData data) {
        this.data = data;
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

        DefaultDomain owners = region.getWorldguardRegion().getOwners();
        if (!owners.getUniqueIds().isEmpty()) {
            data.ownerId = owners.getUniqueIds().stream().findFirst().get();
            data.isForRent = false;
            data.isForBuy = false;
        }

        if (data.isForBuy || data.isForRent) {
            RealEstateManager.addRealestate(this.parentRegion.getId(),(CanBeSold) region);
        }
    }



    public void buyEstate(Player buyer) {
        if (data.ownerId != null) {
            buyer.sendMessage(Chat.errorFade("Dieses Grundstück ist von " + Bukkit.getOfflinePlayer(data.ownerId).getName() + " belegt."));
        }
        if (!data.isForBuy) {
            buyer.sendMessage(Chat.errorFade("Dieses Grundstück steht nicht zum verkauf erhältlich."));
        }

        int transfer = ItemTransfer.charge(buyer, "terranova_silver", data.buyPrice, true);
        if (transfer == -1) {
            buyer.sendMessage(Chat.errorFade("Du hast leider nicht genug Silver in deinem Inventory"));
        } else {
            parentBank.getBank().cashTransfer(String.format("Property bought by %s(%s) for %s", buyer.getName(), buyer.getUniqueId(), transfer), transfer);
        }

        addOwner(region.getWorldguardRegion(), buyer.getUniqueId());
        data.isForBuy = false;
        data.isForRent = false;
        data.ownerId = buyer.getUniqueId();
        data.timestamp = Instant.now();
        buyer.sendMessage(Chat.greenFade(String.format("Du hast soeben erfolgreich %s für %s Silber gekauft.", region.getName(), transfer)));
        parentTown.getAccess().broadcast(String.format("%s hat soeben erfolgreich für %s Silber %s gekauft.", buyer.getName(), transfer, region.getName()), AccessLevel.CITIZEN);

    }

    public void rentEstate(Player buyer) {
        if (buyer.getUniqueId() != data.ownerId) {
            if (data.ownerId != null) {
                buyer.sendMessage(Chat.errorFade("Dieses Grundstück ist von " + Bukkit.getOfflinePlayer(data.ownerId).getName() + " belegt."));
            }
            if (!data.isForRent) {
                buyer.sendMessage(Chat.errorFade("Dieses Grundstück ist nicht zur miete erhältlich."));
            }
        }

        int transfer = ItemTransfer.charge(buyer, "terranova_silver", data.rentPrice, true);
        if (transfer == -1) {
            buyer.sendMessage(Chat.errorFade("Du hast leider nicht genug Silver in deinem Inventory"));
        } else {
            parentBank.getBank().cashTransfer(String.format("Property rented by %s(%s) for %s", buyer.getName(), buyer.getUniqueId(), transfer), transfer);
        }

        addOwner(region.getWorldguardRegion(), buyer.getUniqueId());
        data.isForBuy = false;
        data.isForRent = false;
        if (buyer.getUniqueId() != data.ownerId) {
            data.timestamp = Instant.now();
        }
        data.timestamp = data.timestamp.plus(7, ChronoUnit.DAYS);
        data.ownerId = buyer.getUniqueId();
        buyer.sendMessage(Chat.greenFade(String.format("Du hast soeben erfolgreich %s für %s Silber 7 Tage gemietet.", region.getName(), transfer)));
        parentTown.getAccess().broadcast(String.format("%s hat soeben erfolgreich für %s Silber %s 7 Tage gemietet.", buyer.getName(), transfer, region.getName()), AccessLevel.CITIZEN);
    }

    public void sellEstate(Player seller, boolean isForBuy,int buyAmount,boolean isForRent, int rentAmount) {

        if(region.getWorldguardRegion().getOwners().size() != 0){
            if(!region.getWorldguardRegion().getOwners().contains(seller.getUniqueId())){
                seller.sendMessage(Chat.errorFade("Du kannst keine Region anbieten die nicht deine ist."));
                return;

            }
        } else {
            if(!parentTown.getAccess().getAccessLevel(seller.getUniqueId()).equals(AccessLevel.VICE)){
                seller.sendMessage(Chat.errorFade("Du besitzt keine Rechte dieses Grundstück zu verkaufen."));
                return;
            }
        }

        data.isForBuy = isForBuy;
        data.isForRent = isForRent;
        data.buyPrice = buyAmount;
        data.rentPrice = rentAmount;
        data.ownerId = seller.getUniqueId();

        RealEstateManager.addRealestate(this.parentRegion.getId(),(CanBeSold) region);
    }

    public void addOwner(ProtectedRegion region, UUID ownerUuid) {
        DefaultDomain owners = region.getOwners();
        owners.addPlayer(ownerUuid);
        region.setOwners(owners); // optional, da `getOwners()` nicht kopiert
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
    public int getRentPrice() {return data.buyPrice; }



}
