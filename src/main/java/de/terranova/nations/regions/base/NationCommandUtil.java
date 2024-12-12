package de.terranova.nations.regions.base;

import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.access.AccessLevel;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public class NationCommandUtil {

    public static boolean hasAccess(AccessLevel access, AccessLevel neededAcess) {
        return access.getWeight() >= neededAcess.getWeight();
    }

    public static TerraSelectCache hasSelect(Player p) {
        if(TerraSelectCache.selectCache.containsKey(p.getUniqueId())) return TerraSelectCache.selectCache.get(p.getUniqueId());
        p.sendMessage(Chat.errorFade("Bitte nutze für die Aktion erst ./t select <Stadtname> umd die zu betreffende Stadt auszuwählen."));
        return null;
    }

}
