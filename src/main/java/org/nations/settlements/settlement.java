package org.nations.settlements;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class settlement {

    private final playerdata owner;
    private final String name;
    private final Location location;
    private final int level;

    private int npcid;
    private Hologram hologram;

    public settlement(UUID uuid, Location location, String name) {
        this.owner = new playerdata(uuid);
        this.name = name;
        this.location = location;
        this.level = 4;
        createNPC();
    }

    public boolean canSettle() {
        return owner.canSettle;
    }

    public void updateHolo() {

        if (hologram == null) {
            this.hologram = DHAPI.createHologram(this.name, location.add(0, 2.6, 0));
            updateHolo();
        }
        List<String> lines = new ArrayList<>();
        lines.add("<#e3173c> &lHeilige Mizellarium Staette </#9f17e3>");
        lines.add(String.format("<#5EE118>Level: [%s]", this.level));
        DHAPI.setHologramLines(hologram, lines);
    }

    public void resetHolo() {
        hologram.delete();
        updateHolo();
    }

    private void createNPC() {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "hi");

        //https://mineskin.org/ für signature & texture values
        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinPersistent("beggar", "pIy+sAOaqt70OtSP5DgR0KjBL/PvY9f/+ofULATfjGraa6JVWj5sZ1PTU8L+0xSz07saePgfHPehmV6YnpSvKS+ot7MmZZcUFMq/1PXthwusk7VcjUjwPJCFZgMGG4Fd3v5xQAZG7+xhavWap94lZpvm6fO/IyIkrG+UYQRdDt2GvGSeIxJXCE0OYr3kptxQmKRyWGc7Kpwfx8kcwRW19TIJ94GD51xmWM5jQ8Drro1ycJROtETG9rDuuyy7axdLJFN/WZ4c+JiWDE4F2oRNNfX19tawyIDIYLTqisjWPms0tp0j/TT3DPgW2EZceMq6SVuSLvinT+qO+xkCPKqq9VTGT65QFXSESKSYPsojTKKjsGcFaFUF826VYA+cZJfr8jbGfOvyX3aobOq4vaRomqojlR+GI7/UOxkWpjTAbEjkxRXH0fhcSv5fFbPNqFEb/Sx9/YBn09dPh0d1KfzCfe01zRfrzfqrlBWJdB5uvQHXSVcJt56ArPxb1gWTQInAVeQKQeiwmyr7fTkc+2JL/86reF1OhUVLlG8q7iIi38S5R02oQaQQrqEpLgysJ32q6iwuHqqxZgwga3cBmPCn4DDG4mR1EPoeEOHv9TWlFkopT7ha187JO6YthlBZis9u/Fa/crdIqyic7ej/GwopW+YsYwbw102QpDSc+ZwT1Do=",
                "ewogICJ0aW1lc3RhbXAiIDogMTY3MzIxODMyOTczMCwKICAicHJvZmlsZUlkIiA6ICJmMjc0YzRkNjI1MDQ0ZTQxOGVmYmYwNmM3NWIyMDIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJIeXBpZ3NlbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kOTFlMmQ2ODk0NTcyZjA5ZDY0N2I5MmVhOWNmMjE2MDFjZThmNzgzNGMzYzhkZTM0NmI3NGM4ZjMxM2E2MDUwIgogICAgfQogIH0KfQ==");
        LookClose lookTrait = npc.getOrAddTrait(LookClose.class);
        lookTrait.toggle();

        npc.setAlwaysUseNameHologram(true);
        npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);

        updateHolo();
        npc.spawn(location);

        this.npcid = npc.getId();
    }

    public void tpNPC(Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcid);
        npc.teleport(location, PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);
        tpHolo(location);
    }

    private void tpHolo(Location location) {
        DHAPI.moveHologram(this.name, location.add(0, 2.6, 0));
    }

}

