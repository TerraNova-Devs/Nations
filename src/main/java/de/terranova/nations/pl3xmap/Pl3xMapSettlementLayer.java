package de.terranova.nations.pl3xmap;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.base.RegionType;
import de.terranova.nations.regions.grid.SettleRegionType;
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
import java.util.ArrayList;
import java.util.Collection;

public class Pl3xMapSettlementLayer extends WorldLayer {

    Collection<Marker<?>> markers = new ArrayList<>();

    public Pl3xMapSettlementLayer(@NotNull World world) {
        super("settlement-layer", world, () -> "St\u00E4dte");

        Path icon = FileUtil.getWebDir().resolve("images/icon/Castle.png");
        try {
            IconImage image = new IconImage("castle", ImageIO.read(icon.toFile()), "png");
            Pl3xMap.api().getIconRegistry().register(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setUpdateInterval(0);
        setLiveUpdate(true);
        setShowControls(true);
        setDefaultHidden(false);
        setPriority(100);
        setZIndex(999);

        for (RegionType type : NationsPlugin.settleManager.settlements.values()) {

            if(!(type instanceof SettleRegionType settle)){
                continue;
            }

            if(settle.getWorldguardRegion() == null) {
                Bukkit.getLogger().severe(String.format("Wordguard Region wurde für %s nicht gefunden!",settle.getName()));
                continue;
            }

            Collection<Point> markerPoints = new ArrayList<>();

            for (Vectore2 v : Vectore2.fromBlockVectorList(settle.getWorldguardRegion().getPoints()))
                markerPoints.add(v.asPoint());
            Polygon polygonMarker = new Polygon("polygon" + settle.getId(), new Polyline("line" + settle.getId(), markerPoints));

            Collection<String> vices = settle.getAccess().getEveryMemberNameWithCertainAccessLevel(AccessLevel.VICE);
            Collection<String> councils = settle.getAccess().getEveryMemberNameWithCertainAccessLevel(AccessLevel.COUNCIL);
            String owner = Bukkit.getOfflinePlayer(settle.getAccess().getEveryUUIDWithCertainAccessLevel(AccessLevel.MAJOR).stream().findFirst().get()).getName();

            String tooltip = String.format(
                    "<style> @font-face { font-family: minecraft; src: url('images/font/Minecrafter.Reg.ttf'); } p { font-family: minecraft; text-align: center; margin-top: 0; margin-bottom: 0; color:#D9D9D9; } p.mid { text-align: left; } p.color{ color: #68D9B0; font-size: 30px;} </style>" +
                            "<center><p class='color'>%s</p></center>" +
                            "<br><center><img src='images/banner/Mitglieder.png' height='50' width='200' ></center> <br><p class='mid'>Owner: %s<br>Vize%s<br>Council%s</p>" +
                            "<br><center><img src='images/banner/Statistiken.png' height='50' width='200' ></center> <br><p class='mi'>Level: %s<br>Claims: %s/%s</p>"
                    , settle.getName().replaceAll("_", " "), owner, ": " + StringUtils.join(vices, ", "), ": " + StringUtils.join(councils, ", "), settle.getRank().getLevel(), settle.getClaims(),settle.getMaxClaims());

            Options optionsicon;
            optionsicon = Options.builder()
                    .tooltipContent(tooltip)
                    .tooltipDirection(Tooltip.Direction.TOP)
                    .build();
            
            Options optionspoly;
            optionspoly = Options.builder()
                    .fillColor(0x5540E53F)
                    .strokeColor(0xDD8640E6)
                    .build();

            markers.add(Marker.icon("icon" + settle.getId(), settle.getLocation().x, settle.getLocation().z, "castle", 32).setOptions(optionsicon));
            markers.add(polygonMarker.setOptions(optionspoly));
        }
    }

    @Override
    public @NotNull Collection<Marker<?>> getMarkers() {
        return markers;
    }

}
