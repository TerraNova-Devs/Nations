package de.terranova.nations.regions.boundary;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.base.RegionContext;
import de.terranova.nations.regions.base.RegionFactoryBase;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.worldguard.BoundaryClaimFunctions;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyRegionFactory implements RegionFactoryBase {

    @Override
    public String getType() {
        return PropertyRegion.REGION_TYPE;
    }

    @Override
    public Region createWithContext(RegionContext ctx) {

        Player p = ctx.player;
        String name = ctx.name;

        name = buildRegionName(name, p);

        if(name == null){
            p.sendMessage(Chat.errorFade("Error bei der Bennenung!"));
            return null;
        }

        Optional<SettleRegion> settleOpt = RegionManager.retrievePlayersSettlement(p.getUniqueId());
        if (settleOpt.isEmpty()) {
            p.sendMessage(Chat.errorFade("Du bist in keiner Stadt."));
            return null;
        }

        SettleRegion settle = settleOpt.get();

        com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(p);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        RegionSelector selector = session.getRegionSelector(BukkitAdapter.adapt(p.getWorld()));
        try {
            com.sk89q.worldedit.regions.Region region = selector.getRegion();
            ProtectedRegion tempRegion = BoundaryClaimFunctions.asProtectedRegion(region, UUID.randomUUID().toString());

            if (!RegionClaimFunctions.checkRegionSize(tempRegion, 2, 24)) {
                p.sendMessage(Chat.errorFade("Die Region muss mindestens 2 Blöcke hoch und 24 blöcke Inhalt haben."));
                return null;
            }

            if (!validate(ctx, name, tempRegion, settle)) {
                return null;
            }
        } catch (IncompleteRegionException e) {
            p.sendMessage(Chat.errorFade("Deine Worldeditauswahl ist unvollständig."));
            return null;
        }


        return new PropertyRegion(
                name,
                UUID.randomUUID(),
                settle
        );
    }

    @Override
    public Region createFromArgs(List<String> args) {
        return new PropertyRegion(
                args.getFirst(),
                UUID.fromString(args.get(1)),
                (SettleRegion) RegionManager.retrieveRegion("settle", UUID.fromString(args.get(2))).get()
        );
    }
    public static String buildRegionName(String name, Player player) {
        if (name == null) {
            player.sendMessage(Chat.errorFade("Bitte gib einen gültigen Straßennamen ein."));
            return null;
        }

        if (!name.matches("[A-Za-z0-9_]+")) {
            player.sendMessage(Chat.errorFade("Bitte nutze im Regionsnamen nur Großbuchstaben, Kleinbuchstaben, Zahlen oder (_) für Leerzeichen."));
            return null;
        }

        Pattern trailingNumberPattern = Pattern.compile("^(.*?)(_\\d{1,3})?$");
        Matcher matcher = trailingNumberPattern.matcher(name);

        if (!matcher.matches()) {
            player.sendMessage(Chat.errorFade("Am Ende darf optional ein Unterstrich mit bis zu 3 Ziffern stehen (z. B. Badergasse_131)."));
            return null;
        }

        String base = matcher.group(1);
        String trailingNumber = matcher.group(2);

        if (base.length() > 30) {
            player.sendMessage(Chat.errorFade("Der Straßenname (ohne Nummer) darf höchstens 30 Zeichen lang sein."));
            return null;
        }

        if (base.startsWith("_") || base.endsWith("_")) {
            player.sendMessage(Chat.errorFade("Der Name der Region darf nicht mit (_) beginnen oder enden, da (_) Leerzeichen repräsentiert."));
            return null;
        }

        if (base.contains("__")) {
            player.sendMessage(Chat.errorFade("Du hast in deinem Regionsnamen mehrere aufeinanderfolgende Unterstriche (_). Das ist nicht erlaubt."));
            return null;
        }

        long underscoreCount = base.chars().filter(c -> c == '_').count();
        if (underscoreCount > 3) {
            player.sendMessage(Chat.errorFade("Der Regionsname darf maximal 3 Unterstriche (_) enthalten."));
            return null;
        }

        // Validate or determine region number
        int regionNumber;
        if (trailingNumber != null) {
            regionNumber = Integer.parseInt(trailingNumber.substring(1)); // Entferne führenden "_"
            if (regionNumber > 999) {
                player.sendMessage(Chat.errorFade("Die Grundstücksnummer darf 999 nicht überschreiten."));
                return null;
            }

            if (!BoundaryClaimFunctions.isRegionNumberFree(base, regionNumber)) {
                int suggestion = BoundaryClaimFunctions.getNextFreeRegionNumber(base);
                player.sendMessage(Chat.errorFade(String.format("Die Grundstücksnummer %s existiert bereits im Bereich %s.", regionNumber, base)));
                player.sendMessage(Chat.errorFade(String.format("Noch frei wäre: %s_%s", base, suggestion)));
                return null;
            }
        } else {
            regionNumber = BoundaryClaimFunctions.getNextFreeRegionNumber(base);
        }

        return base + "_" + regionNumber;
    }

}
