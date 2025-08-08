package de.terranova.nations.command;

import de.terranova.nations.command.commands.CommandAnnotation;
import de.terranova.nations.professions.ProfessionProgressManager;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.utils.Chat;
import java.util.Optional;
import org.bukkit.entity.Player;

public class BuildingCommands {

  public BuildingCommands() {}

  @CommandAnnotation(
      domain = "building.confirm.$REGION_NAMES.$BUILDINGS",
      permission = "nations.town.building.confirm",
      description = "Bestätigt den Bau eines Gebäudes.",
      usage = "/town building confirm <settlement> <buildingId>")
  public boolean confirmBuilding(Player p, String[] args) {
    if (args.length < 4) {
      p.sendMessage("Bitte /town building confirm <settlement> <buildingId>");
      return false;
    }

    String buildingId;
    try {
      buildingId = args[3];
    } catch (NumberFormatException e) {
      p.sendMessage("Ungültige BuildingID!");
      return false;
    }

    // 1) Finde Stadt
    Optional<SettleRegion> settleOpt = RegionManager.retrieveRegion("settle", args[2]);
    if (settleOpt.isEmpty()) {
      p.sendMessage("Die Stadt existiert nicht.");
      return false;
    }
    SettleRegion settle = settleOpt.get();
    String ruuid = settle.getId().toString();

    // 2) Markiere in DB:
    ProfessionProgressManager mgr = ProfessionProgressManager.loadForSettlement(settle.getId());
    mgr.setBuildingBuilt(buildingId, true);

    // 3) Ggfs. Meldung ausgeben
    p.sendMessage(
        Chat.greenFade("Gebäude " + buildingId + " wurde erfolgreich als gebaut markiert!"));
    return true;
  }

  @CommandAnnotation(
      domain = "building.cancel.$REGION_NAMES.$BUILDINGS",
      permission = "nations.town.building.cancel",
      description = "Cancels the construction of a building.",
      usage = "/town building cancel <settlement> <buildingId>")
  public boolean cancelBuilding(Player p, String[] args) {
    if (args.length < 4) {
      p.sendMessage(Chat.yellowFade("Bitte /town building cancel <settlement> <buildingId>"));
      return false;
    }

    String buildingId;
    try {
      buildingId = args[3];
    } catch (NumberFormatException e) {
      p.sendMessage(Chat.errorFade("Ungültige BuildingID!"));
      return false;
    }

    // 1) Finde Stadt
    Optional<SettleRegion> settleOpt = RegionManager.retrieveRegion("settle", args[2]);
    if (settleOpt.isEmpty()) {
      p.sendMessage(Chat.errorFade("Die Stadt existiert nicht."));
      return false;
    }
    SettleRegion settle = settleOpt.get();

    // 2) Markiere in DB:
    ProfessionProgressManager mgr = ProfessionProgressManager.loadForSettlement(settle.getId());
    mgr.setBuildingBuilt(buildingId, false);

    // 3) Ggfs. Meldung ausgeben
    p.sendMessage(
        Chat.greenFade("Gebäude " + buildingId + " wurde erfolgreich als nicht gebaut markiert!"));
    return true;
  }
}
