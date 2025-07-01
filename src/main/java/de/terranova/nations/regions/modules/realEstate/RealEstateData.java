package de.terranova.nations.regions.modules.realEstate;

import java.time.Instant;
import java.util.UUID;

public class RealEstateData {

    UUID ownerId;
    boolean isForBuy;
    int buyPrice;
    boolean isForRent;
    int rentPrice;
    Instant timestamp;
    public RealEstateData(UUID ownerId, boolean isForBuy, int buyPrice, boolean isForRent, int rentPrice, Instant timestamp) {
        this.ownerId = ownerId;
        this.isForBuy = isForBuy;
        this.buyPrice = buyPrice;
        this.isForRent = isForRent;
        this.rentPrice = rentPrice;
        this.timestamp = timestamp;
    }

}
