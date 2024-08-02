package de.terranova.nations.pl3xmap;

import de.terranova.nations.settlements.AccessLevelEnum;
import de.terranova.nations.settlements.settlement;
import de.terranova.nations.worldguard.math.Vectore2;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Polygon;
import net.pl3x.map.core.markers.marker.Polyline;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Tooltip;
import net.pl3x.map.core.world.World;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class createPl3xMapSettlementLayer extends WorldLayer {

    Polygon polygonMarker;

    public createPl3xMapSettlementLayer(@NotNull World world, @NotNull Collection<@NotNull Vectore2> marker, @NotNull settlement settle) {
        super(settle.name.toLowerCase()+"-smarker", world, () -> "Settlements");

        Collection<Point> markerPoints = new ArrayList<>();
        for (Vectore2 v: marker) markerPoints.add(v.asPoint());
        polygonMarker = new Polygon("polygon",new Polyline("line", markerPoints));
        setUpdateInterval(0);
        setLiveUpdate(true);
        setShowControls(true);
        setDefaultHidden(false);
        setPriority(100);
        setZIndex(999);

        String owner = Bukkit.getOfflinePlayer(settle.getEveryMemberWithCertainAccessLevel(AccessLevelEnum.MAJOR).stream().findFirst().get()).getName();

        String tooltip = String.format(
                "<b><center>%s</center></b>  " +
                "<br> <p style='color:#8640E6;'>Owner: %s</p>",getLabel(),owner
        );

        if (!tooltip.isBlank()) {
            setOptions(Options.builder()
                    .tooltipContent(tooltip)
                    .tooltipDirection(Tooltip.Direction.TOP)
                    .fillColor(0x668640E6)
                    .strokeColor(0xDD40E53F)
                    .build()
            );
        }

    }

    @Override
    public @NotNull Collection<@NotNull Marker<?>> getMarkers() {
        return Collections.singletonList(polygonMarker.setOptions(getOptions()));
    }
}
