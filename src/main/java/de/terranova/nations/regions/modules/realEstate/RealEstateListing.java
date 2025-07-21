package de.terranova.nations.regions.modules.realEstate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RealEstateListing {

    public static Map<UUID, Integer> holdings = new HashMap<>();

    public UUID landlord;
    public boolean isForBuy;
    public int buyPrice;
    public boolean isForRent;
    public int rentPrice;
    public Instant timestamp;

    public RealEstateListing(UUID landlord, boolean isForBuy, int buyPrice, boolean isForRent, int rentPrice, Instant timestamp) {
        this.landlord = landlord;
        this.isForBuy = isForBuy;
        this.buyPrice = buyPrice;
        this.isForRent = isForRent;
        this.rentPrice = rentPrice;
        this.timestamp = timestamp;
    }

}
