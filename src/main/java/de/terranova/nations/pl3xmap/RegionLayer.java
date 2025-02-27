package de.terranova.nations.pl3xmap;

import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.utils.ColorUtils;
import de.terranova.nations.worldguard.math.Vectore2;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Polygon;
import net.pl3x.map.core.markers.marker.Polyline;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Tooltip;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.world.World;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class RegionLayer extends WorldLayer {

    public RegionLayer(@NotNull World world) {
        super("settlement-layer", world, () -> "St\u00E4dte");

        Path icon = FileUtil.getWebDir().resolve("images/icon/Castle.png");
        try {
            IconImage image = new IconImage("castle", ImageIO.read(icon.toFile()), "png");
            Pl3xMap.api().getIconRegistry().register(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setUpdateInterval(200);
        setLiveUpdate(true);
        setShowControls(true);
        setDefaultHidden(false);
        setPriority(100);
        setZIndex(999);
    }

    static Map<String,Marker<?>> markers = new HashMap<>();

    public static void updateRegion(SettleRegion region) {
        if (markers.containsKey(region.getId() + "icon")) {
            removeRegion(region.getId());
        }

        if (region.getWorldguardRegion() == null) {
            return;
        }

        // 1) Build the polygon from region points
        Collection<Point> markerPoints = new ArrayList<>();
        for (Vectore2 v : Vectore2.fromBlockVectorList(region.getWorldguardRegion().getPoints())) {
            markerPoints.add(v.asPoint());
        }
        Polygon polygonMarker = new Polygon("polygon" + region.getId(), new Polyline("line" + region.getId(), markerPoints));

        // 2) Build the tooltip (already done in your code)
        Collection<String> vices = region.getAccess().getEveryMemberNameWithCertainAccessLevel(TownAccessLevel.VICE);
        Collection<String> councils = region.getAccess().getEveryMemberNameWithCertainAccessLevel(TownAccessLevel.COUNCIL);
        String owner = "error";
        if (!region.getAccess().getEveryUUIDWithCertainAccessLevel(TownAccessLevel.MAJOR).isEmpty()) {
            UUID ownerId = region.getAccess().getEveryUUIDWithCertainAccessLevel(TownAccessLevel.MAJOR).iterator().next();
            owner = Bukkit.getOfflinePlayer(ownerId).getName();
        }

        String tooltip = String.format(
                "<style> @font-face { font-family: minecraft; src: url('images/font/Minecrafter.Reg.ttf'); } p { font-family: minecraft; text-align: center; margin-top: 0; margin-bottom: 0; color:#D9D9D9; } p.mid { text-align: left; } p.color{ color: #68D9B0; font-size: 30px;} </style>"
                        + "<center><p class='color'>%s</p></center>"
                        + "<br><center><img src='images/banner/Mitglieder.png' height='50' width='200' ></center>"
                        + "<br><p class='mid'>Owner: %s<br>Vize%s<br>Council%s</p>"
                        + "<br><center><img src='images/banner/Statistiken.png' height='50' width='200' ></center>"
                        + "<br><p class='mi'>Level: %s<br>Claims: %s/%s</p>",
                region.getName().replace("_", " "),
                owner,
                ": " + StringUtils.join(vices, ", "),
                ": " + StringUtils.join(councils, ", "),
                region.getRank().getLevel(),
                region.getClaims(),
                region.getMaxClaims()
        );

        // 3) Determine fill/stroke color based on the settlement's nation
        //    (Replace 'YourNationManagerClass' with your actual manager)
        int fillColor = 0x5540E53F;  // default if no nation
        int strokeColor = 0xDD8640E6; // default stroke

        // Suppose you have something like:
        // Nation nation = NationsPlugin.nationManager.getNationBySettlement(region.getId());
        // If it exists, use a color derived from the nation's name
        var nation = de.terranova.nations.NationsPlugin.nationManager.getNationBySettlement(region.getId());
        if (nation != null) {
            fillColor = ColorUtils.getColorFromName(nation.getName());    // from step 1
            // For stroke, you might lighten/darken it:
            strokeColor = (fillColor & 0x00FFFFFF) | 0xDD000000; // same hue, a bit more opaque, etc.
        }

        // 4) Build Options for icon’s tooltip
        Options optionsicon = Options.builder()
                .tooltipContent(tooltip)
                .tooltipDirection(Tooltip.Direction.TOP)
                .build();

        // 5) Build Options for polygon fill/stroke using the derived color
        Options optionspoly = Options.builder()
                .fillColor(fillColor)
                .strokeColor(strokeColor)
                .build();

        // 6) Create & store markers
        markers.put(region.getId().toString() + "icon",
                Marker.icon("icon" + region.getId(), region.getLocation().x, region.getLocation().z, "castle", 32)
                        .setOptions(optionsicon));
        markers.put(region.getId().toString() + "area",
                polygonMarker.setOptions(optionspoly));
    }

    public static void removeRegion(UUID regionID){
        markers.remove(regionID.toString() + "icon");
        markers.remove(regionID + "area");
    }

    @Override
    public @NotNull Collection<Marker<?>> getMarkers() {
        return markers.values();
    }

}
