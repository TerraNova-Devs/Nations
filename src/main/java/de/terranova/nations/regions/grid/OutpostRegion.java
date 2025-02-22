package de.terranova.nations.regions.grid;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.base.GridRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class OutpostRegion extends Region {

    public static final String type = "outpost";

    //Beim neu erstellen
    public OutpostRegion(Player p, String name) {
        super(name, UUID.randomUUID(), type);

    }

    public static OutpostRegion conditionCheck(Player p, String[] args) {

        String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 1, args.length)));
        if (!Region.isNameCached(name)) {
            p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
            return null;
        }
        if (RegionClaimFunctions.checkAreaForSettles(p)) {
            p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
            return null;
        }

        double abstand = Integer.MAX_VALUE;
        for (Vectore2 location : GridRegion.locationCache) {
            double abstandneu = claimCalc.abstand(location, new Vectore2(p.getLocation()));
            if (abstand == Integer.MAX_VALUE || abstand > abstandneu) {
                abstand = abstandneu;
            }
        }
        if (abstand < 750) {
            p.sendMessage(Chat.errorFade("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>750<#FFD7FE> Bl\u00F6cke Abstand muss eingehalten werden."));
            p.sendMessage(Chat.errorFade(String.format("Die n\u00E4chste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(abstand))));
            return null;
        }

        double abstand2 = Integer.MAX_VALUE;
        for (Vectore2 location : GridRegion.locationCache) {
            double abstandneu = claimCalc.abstand(location, new Vectore2(p.getLocation()));
            if (abstand2 == Integer.MAX_VALUE || abstand2 > abstandneu) {
                abstand2 = abstandneu;
            }
        }
        if (abstand2 < 300) {
            p.sendMessage(Chat.errorFade("Du bist zu nah an einem anderen Outpost, mindestens <#8769FF>300<#FFD7FE> Bl\u00F6cke Abstand muss eingehalten werden."));
            p.sendMessage(Chat.errorFade(String.format("Die n\u00E4chste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(abstand))));
            return null;
        }

        return new OutpostRegion(p, name);
    }

    @Override
    public void dataBaseCall() {

    }
}
