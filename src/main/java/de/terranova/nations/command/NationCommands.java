package de.terranova.nations.command;

import static de.terranova.nations.NationsPlugin.nationManager;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.command.commands.AbstractCommand;
import de.terranova.nations.command.commands.CommandAnnotation;
import de.terranova.nations.command.commands.PlayerAwarePlaceholder;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.nations.NationPlayerRank;
import de.terranova.nations.nations.SettlementRank;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.access.Access;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.utils.Chat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.StringUtils;

public class NationCommands extends AbstractCommand {

  static Map<UUID, UUID> pendingInvites = new HashMap<>();

  public NationCommands() {

    addPlaceholder(
        "$PENDING_INVITES",
        PlayerAwarePlaceholder.ofCachedPlayerFunction(
            (UUID uuid) -> {
              return pendingInvites.entrySet().stream()
                  .filter(
                      entry ->
                          RegionManager.retrievePlayersSettlement(uuid).get().getId()
                              == entry.getValue())
                  .map(entry -> nationManager.getNation(entry.getKey()).getName())
                  .collect(Collectors.toList());
            },
            10000));
    addPlaceholder(
        "$NATION_NAMES",
        () ->
            nationManager.getNations().values().stream()
                .map(Nation::getName)
                .collect(Collectors.toList()));
    addPlaceholder(
        "$SETTLEMENTS",
        () ->
            RegionManager.retrieveAllCachedRegions("settle").values().stream()
                .map(Region::getName)
                .collect(Collectors.toList()));

    registerSubCommand(this, "create");
    registerSubCommand(this, "delete");
    registerSubCommand(this, "leave");
    registerSubCommand(this, "kick");
    registerSubCommand(this, "delete.confirm");
    registerSubCommand(this, "invite");
    registerSubCommand(this, "accept");
    registerSubCommand(this, "rename");

    setupHelpCommand();
    initialize();
  }

  @CommandAnnotation(
      domain = "rename.$0",
      permission = "nations.nation.rename",
      description = "Renames a nation",
      usage = "/nation rename <name>")
  public boolean renameNation(Player p, String[] args) {
    if (args.length < 2) {
      p.sendMessage(Chat.errorFade("Bitte gebe den Namen der Nation an."));
      return false;
    }

    String nationName = args[1];
    Nation nation = nationManager.getNationByMember(p.getUniqueId());
    if (nation == null) {
      p.sendMessage(Chat.errorFade("Du bist in keiner Nation."));
      return false;
    }

    if (nation.getPlayerRank(p.getUniqueId()).getWeight()
        < NationPlayerRank.VICE_LEADER.getWeight()) {
      p.sendMessage(
          Chat.errorFade("Du musst mindestens Vize-Anführer der Nation sein um sie umzubenennen."));
      return false;
    }

    if (nationManager.getNationByName(nationName) != null) {
      p.sendMessage(Chat.errorFade("Der Name ist bereits vergeben."));
      return false;
    }

    nation.setName(nationName);
    nationManager.saveNation(nation);
    p.sendMessage(
        Chat.greenFade(
            "Die Nation wurde erfolgreich in "
                + StringUtils.capitalise(nationName)
                + " umbenannt."));
    return true;
  }

  @CommandAnnotation(
      domain = "create.$0",
      permission = "nations.nation.create",
      description = "Creates a new nation",
      usage = "/nation create <name>")
  public boolean createNation(Player p, String[] args) {
    if (args.length < 2) {
      p.sendMessage(Chat.errorFade("Bitte gebe den Namen der Nation an."));
      return false;
    }

    String nationName = args[1].toLowerCase();
    if (nationManager.getNationByName(nationName) != null) {
      p.sendMessage(Chat.errorFade("Der Name ist bereits vergeben."));
      return false;
    }

    Optional<SettleRegion> osettle = RegionManager.retrievePlayersSettlement(p.getUniqueId());
    if (osettle.isEmpty()) {
      p.sendMessage(Chat.errorFade("Du musst erst eine Stadt besitzen."));
      return false;
    }

    SettleRegion settle = osettle.get();

    if (!Access.hasAccess(settle.getAccess().getAccessLevel(p.getUniqueId()), AccessLevel.MAJOR)) {
      p.sendMessage(Chat.errorFade("Du musst Bürgermeister deiner Stadt sein."));
      return false;
    }

    // Check if settle is already in a nation
    for (Nation nation : nationManager.getNations().values()) {
      if (nation.getSettlements().containsKey(settle.getId())) {
        p.sendMessage(Chat.errorFade("Die Stadt ist bereits in einer Nation."));
        return false;
      }
    }

    if (settle.getBank().getCredit() < NationsPlugin.plugin.getConfig().getInt("nation.cost")) {
      p.sendMessage(
          Chat.errorFade(
              "Deine Stadt hat nicht genügend Geld um eine Nation zu gründen. ("
                  + NationsPlugin.plugin.getConfig().getInt("nation.cost")
                  + " Silber)"));
      return false;
    }

    Nation nation = new Nation(nationName, p.getUniqueId(), settle.getId());
    nationManager.addNation(nation);
    settle
        .getBank()
        .cashTransfer("Nationsgründung", -NationsPlugin.plugin.getConfig().getInt("nation.cost"));

    p.sendMessage(
        Chat.greenFade(
            "Die Nation " + StringUtils.capitalise(nationName) + " wurde erfolgreich gegründet."));
    return true;
  }

  @CommandAnnotation(
      domain = "delete.$NATION_NAMES",
      permission = "nations.nation.delete",
      description = "Deletes a nation",
      usage = "/nation delete <name>")
  public boolean deleteNation(Player p, String[] args) {
    if (args.length < 2) {
      p.sendMessage(Chat.errorFade("Bitte gebe den Namen der Nation an."));
      return false;
    }

    String nationName = args[1].toLowerCase();
    Nation nation = nationManager.getNationByName(nationName);
    if (nation == null) {
      p.sendMessage(
          Chat.errorFade("Die Nation " + StringUtils.capitalise(nationName) + " existiert nicht."));
      return false;
    }

    if (!nation.getLeader().equals(p.getUniqueId())) {
      p.sendMessage(Chat.errorFade("Du bist nicht der Anführer der Nation."));
      return false;
    }

    p.sendMessage(
        Chat.greenFade(
            "Bist du sicher, dass du die Nation "
                + StringUtils.capitalise(nationName)
                + " löschen möchtest? Dann gib /nation delete confirm ein."));
    return true;
  }

  @CommandAnnotation(
      domain = "delete.confirm.$NATION_NAMES",
      permission = "nations.nation.delete.confirm",
      description = "Deletes your nation",
      usage = "/nation delete confirm <name>")
  public boolean deleteNationConfirm(Player p, String[] args) {
    if (args.length < 3) {
      p.sendMessage(Chat.errorFade("Bitte gebe den Namen der Nation an."));
      return false;
    }

    String nationName = args[2].toLowerCase();
    Nation nation = nationManager.getNationByName(nationName);
    if (nation == null) {
      p.sendMessage(Chat.errorFade("Diese Nation existiert nicht."));
      return false;
    }

    if (!nation.getLeader().equals(p.getUniqueId())) {
      p.sendMessage(Chat.errorFade("Du bist nicht der Anführer der Nation."));
      return false;
    }

    nationManager.removeNation(nation.getId());
    p.sendMessage(
        Chat.greenFade(
            "Die Nation "
                + StringUtils.capitalise(nation.getName())
                + " wurde erfolgreich gelöscht."));
    return true;
  }

  @CommandAnnotation(
      domain = "invite.$SETTLEMENTS",
      permission = "nations.nation.invite",
      description = "Invite a settlement to the nation",
      usage = "/nation invite <settle-name>")
  public boolean inviteSettlement(Player p, String[] args) {
    if (args.length < 2) {
      p.sendMessage(Chat.errorFade("Bitte gebe den Namen der Stadt an."));
      return false;
    }

    String settleName = args[1].toLowerCase();
    Optional<SettleRegion> settleOpt = RegionManager.retrieveRegion("settle", settleName);
    if (settleOpt.isEmpty()) {
      p.sendMessage(
          Chat.errorFade("Die Stadt " + StringUtils.capitalise(settleName) + " existiert nicht."));
      return false;
    }
    SettleRegion settle = settleOpt.get();

    Nation nation = nationManager.getNationByMember(p.getUniqueId());
    if (nation == null) {
      p.sendMessage(Chat.errorFade("Du bist in keiner Nation."));
      return false;
    }

    if (nation.getPlayerRank(p.getUniqueId()).getWeight()
        < NationPlayerRank.VICE_LEADER.getWeight()) {
      p.sendMessage(
          Chat.errorFade(
              "Du musst mindestens Vize-Anführer sein um eine Stadt in die Nation einzuladen."));
      return false;
    }

    if (nation.hasSettlement(settle.getId())) {
      p.sendMessage(
          Chat.errorFade(
              "Die Stadt "
                  + StringUtils.capitalise(settleName)
                  + " gehört bereits zu deiner Nation."));
      return false;
    }

    boolean isSettlementInNation = nationManager.isSettlementInNation(settle.getId());
    if (isSettlementInNation) {
      p.sendMessage(
          Chat.errorFade(
              "Die Stadt "
                  + StringUtils.capitalise(settleName)
                  + " gehört bereits zu einer Nation."));
      return false;
    }

    if (pendingInvites.containsKey(nation.getId())) {
      p.sendMessage(
          Chat.errorFade(
              "Die Stadt " + StringUtils.capitalise(settleName) + " hat bereits eine Einladung."));
      return false;
    }

    pendingInvites.put(nation.getId(), settle.getId());
    settle
        .getAccess()
        .broadcast(
            "Die Stadt "
                + StringUtils.capitalise(settleName)
                + " wurde in die Nation "
                + StringUtils.capitalise(nation.getName())
                + " eingeladen.",
            AccessLevel.VICE);
    nation.broadcast(
        "Die Stadt "
            + StringUtils.capitalise(settleName)
            + " wurde erfolgreich in die Nation eingeladen.");
    return true;
  }

  @CommandAnnotation(
      domain = "accept.$PENDING_INVITES",
      permission = "nations.nation.accept",
      description = "Accept an invitation to a nation",
      usage = "/nation accept <nation-name>")
  public boolean acceptInvite(Player p, String[] args) {
    if (args.length < 2) {
      p.sendMessage(Chat.errorFade("Bitte gebe den Namen der Nation an."));
      return false;
    }

    String nationName = args[1].toLowerCase();
    Nation nation = nationManager.getNationByName(nationName);
    if (nation == null) {
      p.sendMessage(
          Chat.errorFade("Die Nation " + StringUtils.capitalise(nationName) + " existiert nicht."));
      return false;
    }

    if (!pendingInvites.containsKey(nation.getId())) {
      p.sendMessage(
          Chat.errorFade(
              "Du hast keine Einladung von der Nation "
                  + StringUtils.capitalise(nationName)
                  + "."));
      return false;
    }

    Optional<SettleRegion> settleOpt =
        RegionManager.retrieveRegion("settle", pendingInvites.get(nation.getId()));

    if (settleOpt.isEmpty()) {
      p.sendMessage(Chat.errorFade("Fehler: Die Stadt existiert nicht."));
      return false;
    }

    SettleRegion settle = settleOpt.get();

    if (!Access.hasAccess(settle.getAccess().getAccessLevel(p.getUniqueId()), AccessLevel.VICE)) {
      p.sendMessage(
          Chat.errorFade(
              "Du musst mindestens Vize sein um mit deiner Stadt einer Nation beizutreten."));
      return false;
    }

    nationManager.addSettlementToNation(nation.getId(), settle.getId());
    pendingInvites.remove(nation.getId());
    nation.broadcast(
        "Die Stadt "
            + StringUtils.capitalise(settle.getName())
            + " ist der Nation "
            + StringUtils.capitalise(nation.getName())
            + " beigetreten.");
    return true;
  }

  @CommandAnnotation(
      domain = "leave",
      permission = "nations.nation.leave",
      description = "Leave your current nation",
      usage = "/nation leave")
  public boolean leaveNation(Player p, String[] args) {
    Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
    if (settleOpt.isEmpty()) {
      p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
      return false;
    }

    SettleRegion settle = settleOpt.get();
    if (!Access.hasAccess(settle.getAccess().getAccessLevel(p.getUniqueId()), AccessLevel.VICE)) {
      p.sendMessage(
          Chat.errorFade(
              "Du musst mindestens Vize sein um deine Stadt aus der Nation zu entfernen."));
      return false;
    }

    Nation nation = nationManager.getNationByMember(p.getUniqueId());
    if (nation == null) {
      p.sendMessage(Chat.errorFade("Du bist in keiner Nation."));
      return false;
    }

    if (nation.getSettlements().get(settle.getId()) == SettlementRank.CAPITAL) {
      p.sendMessage(
          Chat.errorFade("Du kannst die Nation nicht verlassen, da du in der Hauptstadt bist."));
      return false;
    }

    nationManager.removeSettlementFromNation(
        nation.getId(), RegionManager.retrievePlayersSettlement(p.getUniqueId()).get().getId());
    RegionManager.retrievePlayersSettlement(p.getUniqueId())
        .get()
        .getAccess()
        .broadcast(
            "Die Stadt "
                + StringUtils.capitalise(
                    RegionManager.retrievePlayersSettlement(p.getUniqueId()).get().getName())
                + " hat die Nation "
                + StringUtils.capitalise(nation.getName())
                + " verlassen.",
            AccessLevel.CITIZEN);
    nation.broadcast(
        "Die Stadt "
            + StringUtils.capitalise(
                RegionManager.retrievePlayersSettlement(p.getUniqueId()).get().getName())
            + " hat die Nation verlassen.");
    return true;
  }

  @CommandAnnotation(
      domain = "kick.$SETTLEMENTS",
      permission = "nations.nation.kick",
      description = "Kick a settlement from the nation",
      usage = "/nation kick <settle-name>")
  public boolean kickSettlement(Player p, String[] args) {
    if (args.length < 2) {
      p.sendMessage(Chat.errorFade("Bitte gebe den Namen der Stadt an."));
      return false;
    }

    String settleName = args[1].toLowerCase();
    Optional<SettleRegion> settleOpt = RegionManager.retrieveRegion("settle", settleName);
    if (settleOpt.isEmpty()) {
      p.sendMessage(
          Chat.errorFade("Die Stadt " + StringUtils.capitalise(settleName) + " existiert nicht."));
      return false;
    }
    SettleRegion settle = settleOpt.get();

    Nation nation = nationManager.getNationByMember(p.getUniqueId());
    if (nation == null) {
      p.sendMessage(Chat.errorFade("Du bist in keiner Nation."));
      return false;
    }

    if (nation.getPlayerRank(p.getUniqueId()).getWeight()
        < NationPlayerRank.VICE_LEADER.getWeight()) {
      p.sendMessage(
          Chat.errorFade(
              "Du musst mindestens Vize-Anführer sein um eine Stadt aus der Nation zu entfernen."));
      return false;
    }

    if (!nation.hasSettlement(settle.getId())) {
      p.sendMessage(
          Chat.errorFade(
              "Die Stadt "
                  + StringUtils.capitalise(settleName)
                  + " gehört nicht zu deiner Nation."));
      return false;
    }

    if (nation.getSettlements().get(settle.getId()) == SettlementRank.CAPITAL) {
      p.sendMessage(Chat.errorFade("Du kannst die Hauptstadt nicht entfernen."));
      return false;
    }

    nationManager.removeSettlementFromNation(nation.getId(), settle.getId());
    settle
        .getAccess()
        .broadcast(
            "Die Stadt "
                + StringUtils.capitalise(settle.getName())
                + " wurde aus der Nation "
                + StringUtils.capitalise(nation.getName())
                + " entfernt.",
            AccessLevel.CITIZEN);
    nation.broadcast(
        "Die Stadt "
            + StringUtils.capitalise(settle.getName())
            + " wurde aus der Nation entfernt.");
    return true;
  }
}
