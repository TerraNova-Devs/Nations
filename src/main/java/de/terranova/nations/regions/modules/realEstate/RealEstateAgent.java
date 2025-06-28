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

import java.util.UUID;

public class RealEstateAgent {

     BankHolder parentBank;
     AccessControlled parentTown;
     Region region;

    RealEstateAgent(Region region){
        this.region = region;
        if(region instanceof HasParent<?> parent){
            if(parent.getParent() instanceof BankHolder holder){
                this.parentBank = holder;
            }
            if(parent.getParent() instanceof AccessControlled town){
                this.parentTown = town;
            }
        }
        DefaultDomain owners = region.getWorldguardRegion().getOwners();
        if(!owners.getUniqueIds().isEmpty()){
            ownerId = owners.getUniqueIds().stream().findFirst().get();
            isForRent = false;
            isForBuy = false;
        }
    }

    UUID ownerId = null;
    boolean isForBuy;
    int buyPrice;
    boolean isForRent;
    int rentPrice;

    public void buyEstate(Player buyer) {
        if(ownerId != null){
            buyer.sendMessage(Chat.errorFade("Dieses Grundstück ist von " + Bukkit.getOfflinePlayer(ownerId).getName() + " belegt."));
        }
        if(!isForBuy) {
            buyer.sendMessage(Chat.errorFade("Dieses Grundstück steht nicht zum verkauf erhältlich."));
        }

        int transfer = ItemTransfer.charge(buyer, "terranova_silver",buyPrice,true);
        if(transfer == -1){
            buyer.sendMessage(Chat.errorFade("Du hast leider nicht genug Silver in deinem Inventory"));
        } else {
            parentBank.getBank().cashTransfer(String.format("Property bought by %s(%s) for %s",buyer.getName(),buyer.getUniqueId(),transfer),transfer);
        }

        addOwner(region.getWorldguardRegion(),buyer.getUniqueId());
        isForBuy = false;
        isForRent = false;
        this.ownerId = buyer.getUniqueId();
        buyer.sendMessage(Chat.greenFade(String.format("Du hast soeben erfolgreich %s für %s Silber gekauft.",region.getName(),transfer)));
        parentTown.getAccess().broadcast(String.format("%s hat soeben erfolgreich für %s Silber %s gekauft.",buyer.getName(),transfer,region.getName()), AccessLevel.CITIZEN);

    }

    public void rentEstate(Player buyer) {
        if(ownerId != null){
            buyer.sendMessage(Chat.errorFade("Dieses Grundstück ist von " + Bukkit.getOfflinePlayer(ownerId).getName() + " belegt."));
        }
        if(!isForRent) {
            buyer.sendMessage(Chat.errorFade("Dieses Grundstück ist nicht zur miete erhältlich."));
        }
    }

    public void addOwner(ProtectedRegion region, UUID ownerUuid) {
        DefaultDomain owners = region.getOwners();
        owners.addPlayer(ownerUuid);
        region.setOwners(owners); // optional, da `getOwners()` nicht kopiert
    }

}
