package org.nations.settlements;

import java.util.UUID;

public class playerdata {

    public UUID uuid;
    public boolean canSettle;

    public playerdata(UUID uuid) {
        canSettle = true;
        this.uuid = uuid;
    }

    public boolean canSettle() {
        return canSettle;
    }

    public void setCanSettle(boolean canSettle) {
        this.canSettle = canSettle;
    }

}
