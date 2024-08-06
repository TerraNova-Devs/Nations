package de.terranova.nations.pl3xmap;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.settlement;
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
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class createPl3xMapSettlementLayer extends WorldLayer {

    Collection<Marker<?>> markers = new ArrayList<>();

    public createPl3xMapSettlementLayer(@NotNull World world) {
        super("settlement-layer", world, () -> "Städte");

        Path icon = FileUtil.getWebDir().resolve("images/icon/Castle.png");
        try {
            IconImage image = new IconImage("castle", ImageIO.read(icon.toFile()), "png");
            Pl3xMap.api().getIconRegistry().register(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (settlement settle : NationsPlugin.settlementManager.settlements.values()) {

            Collection<Point> markerPoints = new ArrayList<>();
            for (Vectore2 v : Vectore2.fromBlockVectorList(settle.getWorldguardRegion().getPoints()))
                markerPoints.add(v.asPoint());
            Polygon polygonMarker = new Polygon("polygon" + settle.id, new Polyline("line" + settle.id, markerPoints));

            setUpdateInterval(0);
            setLiveUpdate(true);
            setShowControls(true);
            setDefaultHidden(false);
            setPriority(100);
            setZIndex(999);

            String owner = Bukkit.getOfflinePlayer(settle.getEveryMemberWithCertainAccessLevel(AccessLevelEnum.MAJOR).stream().findFirst().get()).getName();
            String tooltip = String.format(
                    "<style> @font-face { font-family: minecraft; src: url('images/font/Minecrafter.Reg.ttf'); } p { font-family: minecraft; text-align: center; margin-top: 0; margin-bottom: 0; } p.major { color:#8640E6 } </style>" +
                            "<center><p class='minecraft'>%s</p></center>" +
                            "<br><center><img src='images/banner/Mitglieder.png' height='50' width='200' ></center> <br><p class='color'>Owner: %s<br>Vize: coming soon...<br>Council: coming soon...</p>" +
                            "<br><center><img src='images/banner/Statistiken.png' height='50' width='200' ></center> <br><p class='color'>Level: %s<br>Claims: %s/9</p>"
                    , settle.name, owner, settle.level, settle.claims);

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

            markers.add(Marker.icon("icon" + settle.id, settle.location.x, settle.location.z, "castle", 32).setOptions(optionsicon));
            markers.add(polygonMarker.setOptions(optionspoly));
        }
    }

    @Override
    public @NotNull Collection<@NotNull Marker<?>> getMarkers() {
        return markers;
    }

}
