package de.terranova.nations.command;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.mcterranova.terranovaLib.commands.AbstractCommand;
import de.mcterranova.terranovaLib.commands.CachedSupplier;
import de.mcterranova.terranovaLib.commands.CommandAnnotation;
import de.mcterranova.terranovaLib.commands.PlayerAwarePlaceholder;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.pl3xmap.RegionLayer;
import de.terranova.nations.regions.access.TownAccess;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.bank.Transaction;
import de.terranova.nations.regions.base.GridRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TownCommands extends AbstractCommand {

    static Map<UUID, UUID> pendingInvites = new HashMap<>();

    public TownCommands() {
        addPlaceholder("$ONLINEPLAYERS", new CachedSupplier<>(
                () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
                10000
        ));
        addPlaceholder("$PENDING_INVITES", PlayerAwarePlaceholder.ofCachedPlayerFunction(
                (UUID uuid) -> {
                    return pendingInvites.entrySet().stream()
                            .filter(entry -> entry.getKey().equals(uuid))
                            .map(entry -> RegionManager.retrieveRegion("settle", entry.getValue()))
                            .filter(Optional::isPresent)
                            .map(optionalRegion -> optionalRegion.get().getName())
                            .collect(Collectors.toList());
                },
                10000
        ));
        addPlaceholder("$REGION_NAMES", Region::getNameCache);
        addPlaceholder("$RANKS", () ->
                Arrays.stream(TownAccessLevel.values())
                        .filter(level -> level != TownAccessLevel.ADMIN)
                        .filter(level -> level != TownAccessLevel.MAJOR)
                        .map(Enum::name)
                        .collect(Collectors.toList()));
        addPlaceholder("$REGION_CITIZENS",
                PlayerAwarePlaceholder.ofCachedPlayerFunction(
                        (UUID uuid) -> {
                            return RegionManager.retrievePlayersSettlement(uuid).map(settle -> {
                                return settle.getAccess().getAccessLevels()
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> TownAccess.hasAccess(entry.getValue(), TownAccessLevel.CITIZEN))
                                        .map(Map.Entry::getKey)
                                        .map(Bukkit::getOfflinePlayer)
                                        .map(OfflinePlayer::getName)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                            }).orElseGet(Collections::emptyList);
                        },
                        3000
                )
        );
        addPlaceholder("$REGION_ACCESS_USERS",
                PlayerAwarePlaceholder.ofCachedPlayerFunction(
                        (UUID uuid) -> {
                            return RegionManager.retrievePlayersSettlement(uuid).map(settle -> {
                                return settle.getAccess().getAccessLevels()
                                        .entrySet()
                                        .stream()
                                        .filter(entry -> TownAccess.hasAccess(entry.getValue(), TownAccessLevel.TRUSTED))
                                        .map(Map.Entry::getKey)
                                        .map(Bukkit::getOfflinePlayer)
                                        .map(OfflinePlayer::getName)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                            }).orElseGet(Collections::emptyList);
                        },
                        3000
                )
        );

        registerSubCommand(this,"create");
        registerSubCommand(this,"claim");
        registerSubCommand(this,"rename");
        registerSubCommand(this,"invite");
        registerSubCommand(this,"accept");
        registerSubCommand(this,"kick");
        registerSubCommand(this,"rank");
        registerSubCommand(this,"deposit");
        registerSubCommand(this,"withdraw");
        registerSubCommand(this,"balance");
        registerSubCommand(this,"history");
        registerSubCommand(this,"leave");
        registerSubCommand(this,"npc");
        registerSubCommand(this, "trust");

        setupHelpCommand();
        initialize();
    }

    @CommandAnnotation(
            domain = "invite.$ONLINEPLAYERS",
            permission = "nations.town.invite",
            description = "Invites a player to your town",
            usage = "/town invite <player>"
    )
    public boolean invitePlayer(Player p, String[] args){
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();
        if (!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.COUNCIL)) {
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung, um jemanden in diese Stadt einzuladen."));
            return false;
        }

        UUID target = getTargetPlayerUUID(p, args, 1);
        if (target == null) return false;

        if(RegionManager.retrievePlayersSettlement(target).isPresent()){
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist bereits Mitglied einer Stadt.", args[1])));
            return false;
        }

        if(pendingInvites.containsKey(target)){
            if(pendingInvites.get(target).equals(settle.getId())){
                p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist bereits eingeladen.", args[1])));
                return false;
            }
        }

        pendingInvites.put(target, settle.getId());
        p.sendMessage(Chat.greenFade("Du hast " + args[1] + " erfolgreich in die Stadt " + settle.getName() + " eigeladen."));

        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            Component message = Chat.cottonCandy("Du wurdest von " + p.getName() + " in die Stadt " + settle.getName() + " eingeladen.")
                    .hoverEvent(HoverEvent.showText(Chat.cottonCandy("Klicke, um die Einladung anzunehmen!")))
                    .clickEvent(ClickEvent.runCommand("/town accept " + settle.getName()));

            targetPlayer.sendMessage(message);
        }
        return true;
    }

    @CommandAnnotation(
            domain = "accept.$PENDING_INVITES",
            permission = "nations.town.accept",
            description = "Accepts an invite to a town",
            usage = "/town accept <town>"
    )
    public boolean acceptInvite(Player p, String[] args){
        if (args.length < 2) {
            p.sendMessage(Chat.errorFade("Bitte gib den Namen der Stadt an."));
            return false;
        }

        String townName = args[1].toLowerCase();
        Optional<SettleRegion> settleOpt = RegionManager.retrieveRegion("settle", townName);
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Die angegebene Stadt existiert nicht."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();

        if (access == null) return false;

        if(!pendingInvites.containsKey(p.getUniqueId())){
            p.sendMessage(Chat.errorFade("Es wurden keine Einladungen für dich gefunden!"));
            return false;
        }
        if(!pendingInvites.get(p.getUniqueId()).equals(settle.getId())){
            p.sendMessage(Chat.errorFade("Für die von dir ausgewählte Region wurden keine Einladungen für dich gefunden!"));
            return false;
        }

        if(RegionManager.retrievePlayersSettlement(p.getUniqueId()).isPresent()){
            p.sendMessage(Chat.errorFade(String.format("Du bist bereits Mitglied der Stadt %s.", settle.getName())));
            return false;
        }

        access.broadcast(p.getName() + " ist erfolgreich der Stadt " + settle.getName() + " beigetreten.");
        settle.addMember(p.getUniqueId());
        access.setAccessLevel(p.getUniqueId(), TownAccessLevel.CITIZEN);
        p.sendMessage(Chat.greenFade("Du bist erfolgreich der Stadt " + settle.getName() + " beigetreten."));
        return true;
    }

    @CommandAnnotation(
            domain = "kick.$REGION_ACCESS_USERS",
            permission = "nations.town.kick",
            description = "Kicks a player from your town",
            usage = "/town kick <player>"
    )
    public boolean kickPlayer(Player p, String[] args){
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();

        if (access == null) return false;

        if(!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.VICE)){
            p.sendMessage(Chat.errorFade("Du musst mindestens Vize sein um einen Spieler von der Stadt zu entfernen."));
            return false;
        }

        UUID target = getTargetPlayerUUID(p, args, 1);
        if (target == null) return false;

        if (!TownAccess.hasAccess(access.getAccessLevel(target), TownAccessLevel.TRUSTED)) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist kein Mitglied deiner Stadt.", args[1])));
            return false;
        }

        if(access.getAccessLevel(target).getWeight() >= access.getAccessLevel(p.getUniqueId()).getWeight()){
            p.sendMessage(Chat.errorFade("Du kannst keinen Spieler entfernen der einen höheren oder den gleichen Rang hat."));
            return false;
        }

        settle.removeMember(target);
        access.removeAccess(target);
        access.broadcast(args[1] + " wurde von " + p.getName() + " der Stadt " + settle.getName() + " verwiesen.");
        return true;
    }

    @CommandAnnotation(
            domain = "rank.$REGION_CITIZENS.$RANKS",
            permission = "nations.town.rank",
            description = "Sets the rank of a player in your town",
            usage = "/town rank <player> <rank>"
    )
    public boolean setRank(Player p, String[] args){
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();
        if (access == null) return false;

        if (!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.VICE)) {
            p.sendMessage(Chat.errorFade("Um die Ränge innerhalb der Stadt zu ändern musst du mindestens Vizeanführer sein."));
            return false;
        }

        UUID target = getTargetPlayerUUID(p, args, 1);
        if (target == null) return false;

        TownAccessLevel newAccess = getAccessLevelFromArgs(p, args, 2);
        if (newAccess == null) return false;

        if(target == p.getUniqueId() && TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.MAJOR)){
            p.sendMessage(Chat.errorFade("Du kannst deinen eigenen Rang nicht ändern!"));
            return false;
        }

        if(!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.VICE)){
            p.sendMessage(Chat.errorFade("Du musst mindestens Vize sein um Ränge ändern zu können."));
            return false;
        }

        if(newAccess.equals(TownAccessLevel.MAJOR) || newAccess.equals(TownAccessLevel.ADMIN)) {
            p.sendMessage(Chat.errorFade("Du kannst den Stadtbesitzer nicht ändern."));
            return false;
        }

        if (access.getAccessLevel(target) == null) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist kein Mitglied deiner Stadt.", args[1])));
            return false;
        }

        if (!TownAccess.hasAccess(access.getAccessLevel(target), TownAccessLevel.CITIZEN)) {
            p.sendMessage(Chat.errorFade("Der Spieler ist getrusted. Lade ihn ein um ihn zum Bewohner zu machen."));
            return false;
        }

        if(access.getAccessLevel(target) == newAccess) {
            p.sendMessage(Chat.errorFade("Der Spieler " + args[1] + " ist bereits auf dem Rang " + newAccess.name() +"."));
            return false;
        }

        if(access.getAccessLevel(target).getWeight() >= access.getAccessLevel(p.getUniqueId()).getWeight()){
            p.sendMessage(Chat.errorFade("Du kannst nicht den Rang eines Spielers ändern der höher oder gleich ist als deiner selbst."));
            return false;
        }

        access.setAccessLevel(target, newAccess);
        p.sendMessage(Chat.greenFade(String.format("Du hast %s erfolgreich auf den Rang %s gestuft.", args[1], newAccess.name())));
        return true;
    }

    @CommandAnnotation(
            domain = "deposit.$0",
            permission = "nations.town.deposit",
            description = "Deposits money to the town bank",
            usage = "/town deposit <amount>"
    )
    public boolean deposit(Player p, String[] args){
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();
        if (access == null) return false;

        if(!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.CITIZEN)) {
            p.sendMessage(Chat.errorFade("Du musst ein Einwohner dieser Stadt sein um in die Stadtkasse einzahlen zu können"));
            return false;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if(amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            p.sendMessage(Chat.errorFade("Bitte nutze /t (<withdraw|deposit>) (<value>)"));
            p.sendMessage(Chat.errorFade("Bitte gib als value eine Zahl zwischen 1 und 2304!"));
            return false;
        }
        Integer deposit = settle.getBank().cashInFromInv(p, amount);
        if(deposit == null) {
            p.sendMessage("Error während Zahlung");
            return false;
        }
        p.sendMessage(Chat.cottonCandy("Du hast erfolgreich: " + deposit + " Silber eingezahlt."));
        return true;
    }

    @CommandAnnotation(
            domain = "withdraw.$0",
            permission = "nations.town.withdraw",
            description = "Withdraws money from the town bank",
            usage = "/town withdraw <amount>"
    )
    public boolean withdraw(Player p, String[] args){
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();
        if (access == null) return false;

        if(!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.COUNCIL)) {
            p.sendMessage(Chat.errorFade("Du musst mindestens Council sein um von der Stadtkasse abheben zu können"));
            return false;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if(amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            p.sendMessage(Chat.errorFade("Bitte nutze /t bank (<withdraw|deposit>) (<value>)"));
            p.sendMessage(Chat.errorFade("Bitte gib als value eine Zahl zwischen 1 und 2304!"));
            return false;
        }
        Integer withdraw = settle.getBank().cashOutFromInv(p, amount);
        if(withdraw == null) {
            p.sendMessage("Error während Zahlung");
            return false;
        }
        p.sendMessage(Chat.cottonCandy("Du hast erfolgreich: " + withdraw + " Silber abgehoben."));
        return true;
    }

    @CommandAnnotation(
            domain = "balance",
            permission = "nations.town.balance",
            description = "Shows the balance of the town bank",
            usage = "/town balance"
    )
    public boolean balance(Player p, String[] args){
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();

        p.sendMessage(Chat.greenFade(String.format("In der Bank der Stadt liegt: %s Silber",settle.getBank().getCredit())));
        return true;
    }

    @CommandAnnotation(
            domain = "history",
            permission = "nations.town.history",
            description = "Shows the bank history of the town",
            usage = "/town history"
    )
    public boolean bankHistory(Player p, String[] args){
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();
        if(!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.CITIZEN)) {
            p.sendMessage(Chat.errorFade("Du musst ein Einwohner sein um die Historie sehen zu können."));
            return false;
        }
        if(settle.getBank().getTransactions().isEmpty()) {
            p.sendMessage("No Transactions found");
        } else {
            for (Transaction t : settle.getBank().getTransactions()) {
                p.sendMessage(Chat.cottonCandy(String.format("Transaktion: %s -> %s am %s (%s)",t.user,t.amount,Chat.prettyInstant(t.instant),t.total)));
            }
        }
        return true;
    }

    @CommandAnnotation(
            domain = "leave",
            permission = "nations.town.leave",
            description = "Leaves your current town",
            usage = "/town leave"
    )
    public boolean leaveTown(Player p, String[] args){
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();
        if (TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.MAJOR)) {
            p.sendMessage(Chat.errorFade("Du kannst die Stadt nicht verlassen, da du der Bürgermeister bist."));
            return false;
        }

        access.broadcast(p.getName() + " hat die Stadt verlassen.");
        settle.removeMember(p.getUniqueId());
        access.removeAccess(p.getUniqueId());
        p.sendMessage(Chat.greenFade("Du hast die Stadt erfolgreich verlassen."));
        return true;
    }

    @CommandAnnotation(
            domain = "rename.%<name>",
            permission = "nations.town.rename",
            description = "Renames a settlement",
            usage = "/town rename <name>"
    )
    public boolean renameTown(Player p, String[] args){
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();
        if (!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.VICE)) {
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung, um diese Stadt zu erweitern."));
            return false;
        }

        if (args.length < 2) {
            p.sendMessage(Chat.errorFade("Bitte gebe den neuen Namen der Stadt an."));
            return false;
        }

        String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 1, args.length))).toLowerCase();
        if (!name.matches("^(?!.*__)(?!_)(?!.*_$)(?!.*(.)\\1{3,})[a-zA-Z0-9_]{3,20}$")) {
            p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
            return false;
        }

        if (Region.getNameCache().contains(name)) {
            p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
            return false;
        }

        settle.rename(name);
        return true;
    }

    @CommandAnnotation(
            domain = "create.$0",
            permission = "nations.town.create",
            description = "Creates a new settlement",
            usage = "/town create <name>"
    )
    public boolean createTown(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(Chat.errorFade("Bitte gebe den Namen der Stadt an."));
            return false;
        }

        String name = args[1].toLowerCase();
        if (Region.isNameCached(name)) {
            p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
            return false;
        }

        Optional<Region> osettle = Region.createRegion("settle", name ,p);
        if (osettle.isPresent()) {
            p.sendMessage(Chat.greenFade("Stadt " + name + " wurde erfolgreich gegründet."));
            return true;
        } else {
            p.sendMessage(Chat.errorFade("Die Erstellung der Stadt wurde abgebrochen."));
            return false;
        }
    }

    @CommandAnnotation(
            domain = "claim",
            permission = "nations.town.claim",
            description = "Claims a region for your town",
            usage = "/town claim"
    )
    public boolean claimRegion(Player p, String[] args) {
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();
        if (!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.VICE)) {
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung, um diese Stadt zu erweitern."));
            return false;
        }

        double abstand = Integer.MAX_VALUE;
        for (Vectore2 location : GridRegion.locationCache) {
            if (settle.getLocation().equals(location)) continue;
            double abstandneu = claimCalc.abstand(location, new Vectore2(p.getLocation()));
            if (abstand == Integer.MAX_VALUE || abstand > abstandneu) {
                abstand = abstandneu;
            }
        }
        if (abstand < 750) {
            p.sendMessage(Chat.errorFade("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>750<#FFD7FE> Bl\u00F6cke Abstand zum Stadtzentrum muss eingehalten werden."));
            p.sendMessage(Chat.errorFade(String.format("Die n\u00E4chste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(abstand))));
            return false;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(p.getLocation()));
        if (set.size() != 0) {
            p.sendMessage(Chat.errorFade("Du kannst nicht auf der Region eines anderen Spielers claimen!."));
            p.sendMessage(Chat.errorFade("Überlappende Regionen: " + set));
            return false;
        }

        if (settle.getClaims() >= settle.getMaxClaims()) {
            p.sendMessage(Chat.errorFade("Du hast bereits die maximale Anzahl an Claims für dein Stadtlevel erreicht."));
            return false;
        }

        RegionClaimFunctions.addToExistingClaim(p, settle.getWorldguardRegion());

        settle.setClaims(RegionClaimFunctions.getClaimAnzahl(settle.getId()));
        RegionLayer.updateRegion(settle);
        p.sendMessage(Chat.greenFade("Deine Stadt wurde erfolgreich erweitert. (" + settle.getClaims() + "/" + settle.getMaxClaims() + ")"));

        return true;
    }

    @CommandAnnotation(
            domain = "npc",
            permission = "nations.npc.movehere",
            description = "Moves the npc to your location",
            usage = "/town npc"
    )
    public boolean moveNPC(Player p, String[] args) {
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if(settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        Optional<SettleRegion> settle = RegionManager.retrieveRegion("settle", p.getLocation());
        if(settle.isEmpty()) {
            p.sendMessage(Chat.errorFade("Bitte gehe sicher dass du innerhalb von deiner Stadt geclaimten bereich stehst."));
            return false;
        }
        if(settle.get().getId() != settleOpt.get().getId()) {
            p.sendMessage(Chat.errorFade("Du bist nicht in deiner Stadt."));
            return false;
        }

        if(!TownAccess.hasAccess(settleOpt.get().getAccess().getAccessLevel(p.getUniqueId()), TownAccessLevel.VICE)){
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung den NPC zu verschieben."));
            return false;
        }

        settleOpt.get().getNPC().tpNPC(p.getLocation());
        return true;
    }

    @CommandAnnotation(
            domain = "trust.$0",
            permission = "nations.town.trust",
            description = "Trusts a player to your town",
            usage = "/town trust <player>"
    )
    public boolean trustPlayer(Player p, String[] args){
        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return false;
        }
        SettleRegion settle = settleOpt.get();
        TownAccess access = settle.getAccess();
        if (!TownAccess.hasAccess(access.getAccessLevel(p.getUniqueId()), TownAccessLevel.VICE)) {
            p.sendMessage(Chat.errorFade("Du hast nicht die Berechtigung, um jemanden in dieser Stadt zu trusten."));
            return false;
        }

        UUID target = getTargetPlayerUUID(p, args, 1);
        if (target == null) return false;

        if(TownAccess.hasAccess(access.getAccessLevel(target), TownAccessLevel.TRUSTED)){
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist bereits getrusted in dieser Stadt.", args[1])));
            return false;
        }

        settle.addMember(p.getUniqueId());
        access.setAccessLevel(target, TownAccessLevel.TRUSTED);
        access.broadcast(args[1] + " wurde von " + p.getName() + " in die Stadt getrusted.");
        return true;
    }


    private UUID getTargetPlayerUUID(Player p, String[] args, int index) {
        if (args.length <= index) {
            p.sendMessage(Chat.errorFade("Bitte gib den Spielernamen an."));
            return null;
        }
        Player target = Bukkit.getPlayer(args[index]);

        if (target == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[index]);
            return offlinePlayer.getUniqueId();
        }
        return target.getUniqueId();
    }

    private TownAccessLevel getAccessLevelFromArgs(Player p, String[] args, int index) {
        if (args.length <= index) {
            p.sendMessage(Chat.errorFade("Bitte gib ein gültiges AccessLevel an."));
            return null;
        }
        for (TownAccessLevel level : TownAccessLevel.values()) {
            if (level.name().equalsIgnoreCase(args[index])) {
                return level;
            }
        }
        p.sendMessage(Chat.errorFade(String.format("Das AccessLevel %s konnte nicht gefunden werden", args[index])));
        return null;
    }
}