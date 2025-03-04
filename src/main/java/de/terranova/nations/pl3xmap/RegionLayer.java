package de.terranova.nations.pl3xmap;

import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.utils.BannerRenderer;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
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

        String regionName = region.getName().replace("_", " ");

        // 3) Determine fill/stroke color based on the settlement's nation
        int fillColor = 0x5540E53F;  // default
        int strokeColor = 0xDD8640E6;
        String nationColorHex = "#68D9B0"; // fallback if no nation

        var nation = de.terranova.nations.NationsPlugin.nationManager.getNationBySettlement(region.getId());
        String nationName = "No Nation";
        String bannerHtml = "";

        if (nation != null) {
            fillColor = ColorUtils.getColorFromName(nation.getName());
            strokeColor = (fillColor & 0x00FFFFFF) | 0xDD000000;
            nationName = nation.getName();

            // convert fillColor -> #RRGGBB (ignoring alpha)
            nationColorHex = String.format("#%06X", (0xFFFFFF & fillColor));

            // Render banner -> data URI
            ItemStack bannerItem = nation.getBanner();
            if (bannerItem != null && bannerItem.getType().name().contains("BANNER")) {
                String dataUri = BannerRenderer.renderBannerToDataURI(bannerItem);
                if (dataUri != null) {
                    // Put banner in a small <img> on the right
                    bannerHtml = "<img src=\"" + dataUri + "\" style=\"opacity:1.0;\" width=\"44\" height=\"80\" />";
                }
            }

            if(nation.getCapital().equals(region.getId())){
                regionName = "✪ " + regionName;
            }
        }

        String tooltip = String.format("""
                <style>
                  @font-face {
                    font-family: minecraft; 
                    src: url('images/font/Minecrafter.Reg.ttf'); 
                  }
                  p, h2 {
                    font-family: minecraft;
                    margin: 2px;
                    color: #D9D9D9;
                  }
                  .title {
                    font-size: 26px; 
                    color: %s; /* use the nation color if available */
                  }
                  .row {
                    display: flex; 
                    justify-content: space-between; 
                    align-items: flex-start;
                  }
                  .row-bottom {
                    display: flex; 
                    justify-content: space-around; 
                    margin-top: 10px;
                  }
                </style>
                
                <!-- Top row: Text on left, Banner on right -->
                <div class="row">
                  <div>
                    <!-- Town Name / Nation Info -->
                    <h2 class="title">%s</h2>
                    <p>Nation: %s<br>Level: %s<br>Claims: %s/%s</p>
                  </div>
                  <div>
                    %s
                  </div>
                </div>
                
                <!-- Bottom row: two side-by-side boxes -->
                <div class="row-bottom">
                  <div>
                    <img src="images/banner/Mitglieder.png" height="50" width="200" />
                    <p>Owner: %s<br>Vize: %s<br>Council: %s</p>
                  </div>
                </div>
                """,
                nationColorHex,
                regionName,
                nationName,
                region.getRank().getLevel(),
                region.getClaims(),
                region.getMaxClaims(),
                bannerHtml,
                owner,
                ": " + StringUtils.join(vices, ", "),
                ": " + StringUtils.join(councils, ", ")

        );

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
