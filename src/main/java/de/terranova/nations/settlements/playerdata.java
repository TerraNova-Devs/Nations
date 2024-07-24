package de.terranova.nations.settlements;

import java.util.UUID;

//kann gegen einen check auf level direkt in ther funktion ersetzt werden, die can settle checkt
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
