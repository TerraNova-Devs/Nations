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
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

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

            Collection<String> vices = settle.getEveryMemberNameWithCertainAccessLevel(AccessLevelEnum.VICE);
            Collection<String> councils = settle.getEveryMemberNameWithCertainAccessLevel(AccessLevelEnum.COUNCIL);

            String owner = Bukkit.getOfflinePlayer(settle.getEveryUUIDWithCertainAccessLevel(AccessLevelEnum.MAJOR).stream().findFirst().get()).getName();
            String tooltip = String.format(
                    "<style> @font-face { font-family: minecraft; src: url('images/font/Minecrafter.Reg.ttf'); } p { font-family: minecraft; text-align: center; margin-top: 0; margin-bottom: 0; color:#D9D9D9; } p.mid { text-align: left; } p.color{ color: #68D9B0; font-size: 30px;} </style>" +
                            "<center><p class='color'>%s</p></center>" +
                            "<br><center><img src='images/banner/Mitglieder.png' height='50' width='200' ></center> <br><p class='mid'>Owner: %s<br>Vize%s<br>Council%s</p>" +
                            "<br><center><img src='images/banner/Statistiken.png' height='50' width='200' ></center> <br><p class='mi'>Level: %s<br>Claims: %s/9</p>"
                    , settle.name.replaceAll("_"," "), owner,": " + StringUtils.join(vices,", "), ": " + StringUtils.join(councils,", "), settle.level, settle.claims);

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
