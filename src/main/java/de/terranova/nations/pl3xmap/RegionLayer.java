package de.terranova.nations.pl3xmap;

import de.terranova.nations.professions.ProfessionManager;
import de.terranova.nations.professions.ProfessionProgressManager;
import de.terranova.nations.professions.ProfessionStatus;
import de.terranova.nations.professions.pojo.ProfessionConfig;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.base.RegionListener;
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

public class RegionLayer extends WorldLayer implements RegionListener {

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

        // -- NEW: Build the professions HTML (4 circles per level, for each type) --
        String professionsHtml = buildProfessionsHtml(region.getId());

        // 4) Build your tooltip text with the new <div> for professions at the bottom
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
                    color: %s; /* nation color */
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
                  .professions-block {
                    margin-top: 10px; /* put some space above */
                  }
                  /* Each profession line: name + 4 circles */
                  .profession-line {
                    display: flex;
                    align-items: center;
                    margin: 4px 0;
                  }
                  .profession-name {
                    min-width: 100px;
                    margin-right: 8px;
                    color: #D9D9D9; /* or something light */
                  }
                  .circle {
                    display: inline-block;
                    width: 14px;
                    height: 14px;
                    border-radius: 50%%;
                    margin-right: 4px;
                    background-color: #888; /* fallback if not overridden */
                  }
                </style>
                
                <!-- Top row: settlement name, banner, etc. -->
                <div class="row">
                  <div>
                    <h2 class="title">%s</h2>
                    <p>Nation: %s<br>Level: %s<br>Claims: %s/%s</p>
                  </div>
                  <div>
                    %s
                  </div>
                </div>
                
                <!-- Middle row with the “Mitglieder” image, owner, vize, council -->
                <div class="row-bottom">
                  <div>
                    <img src="images/banner/Mitglieder.png" height="50" width="200" />
                    <p>Owner: %s<br>Vize: %s<br>Council: %s</p>
                  </div>
                </div>
                
                <!-- Profession Dots -->
                <div class="professions-block">
                  <img src="images/banner/Statistiken.png" height="50" width="200" />
                  %s
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
                StringUtils.join(vices, ", "),
                StringUtils.join(councils, ", "),
                professionsHtml  // your dynamic HTML with the circles
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

    private static String buildProfessionsHtml(UUID settlementId) {
        ProfessionProgressManager manager = ProfessionProgressManager.loadForSettlement(settlementId);
        List<String> allTypes = ProfessionManager.getProfessionTypes();

        StringBuilder sb = new StringBuilder();
        // Removed the “Berufe:” <p> here so it won’t appear twice!

        sb.append("<div class=\"profession-list\">");

        for (String type : allTypes) {
            boolean[] completed = new boolean[4];
            boolean atLeastOneComplete = false;

            List<ProfessionConfig> confs = ProfessionManager.getProfessionsByType(type);
            if (confs.isEmpty()) continue;

            for (ProfessionConfig p : confs) {
                ProfessionStatus status = manager.getProfessionStatus(p.professionId);
                if (status == ProfessionStatus.COMPLETED && p.getLevel() >= 1 && p.getLevel() <= 4) {
                    completed[p.getLevel() - 1] = true;
                    atLeastOneComplete = true;
                }
            }

            // Skip profession if no levels completed
            if (!atLeastOneComplete) {
                continue;
            }

            // Use the first config's prettyName
            String prettyName = replaceGermanLetters(confs.get(0).prettyName);

            // A row for this profession
            sb.append("<div class=\"profession-line\">");

            // Profession name (styled so it’s visible)
            sb.append("<span class=\"profession-name\" style=\"color:#DDD; font-family: minecraft; margin-right:8px;\">")
                    .append(prettyName)
                    .append("</span>");

            // 4 circles
            String color = getColorFor(type);
            sb.append("<div>");
            for (int lvl = 0; lvl < 4; lvl++) {
                if (completed[lvl]) {
                    // Completed => fill with profession color
                    sb.append("<span class=\"circle\" style=\"background-color:")
                            .append(color)
                            .append(";\"></span>");
                } else {
                    // Not completed => gray/dim
                    sb.append("<span class=\"circle\" style=\"background-color:#222;opacity:0.4;\"></span>");
                }
            }
            sb.append("</div>");

            sb.append("</div>"); // end .profession-line
        }

        sb.append("</div>"); // end .profession-list
        return sb.toString();
    }

    private static String replaceGermanLetters(String s) {
        return s.replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("Ä", "Ae")
                .replace("Ö", "Oe")
                .replace("Ü", "Ue")
                .replace("ß", "ss");
    }

    /**
     * Returns a hex color (e.g. "#E91E63") to use for completed circles
     * of the given profession type. Adjust as desired!
     */
    private static String getColorFor(String type) {
        switch (type.toUpperCase()) {
            case "FISHERY":      return "#2980B9"; // a nice blue
            case "MINING":       return "#7f8c8d"; // gray
            case "FARMING":      return "#27ae60"; // green
            case "RANCHING":     return "#9C6B3B"; // brownish
            case "BREWING":      return "#9b59b6"; // purple
            case "SMITHING":     return "#424242"; // dark gray
            case "MAGIC":        return "#E91E63"; // pinkish
            case "WOODCUTTING":  return "#8D6E63"; // wood-brown
            case "MILITARY":     return "#c0392b"; // red
            case "STONEWORK":    return "#BCAAA4"; // pale stone
            case "TRADE":        return "#FFD54F"; // gold
            case "FAITH":        return "#9575CD"; // lavender
            default:             return "#888888"; // fallback gray
        }
    }


    public static void removeRegion(UUID regionID){
        markers.remove(regionID.toString() + "icon");
        markers.remove(regionID + "area");
    }

    @Override
    public @NotNull Collection<Marker<?>> getMarkers() {
        return markers.values();
    }

    @Override
    public void onRegionRenamed(String newRegionName) {
        Optional<SettleRegion> settleOpt = RegionManager.retrieveRegion("settle", newRegionName);

        if(settleOpt.isEmpty()){
            return;
        }

        updateRegion(settleOpt.get());
    }

    @Override
    public void onRegionRemoved() {

    }
}
