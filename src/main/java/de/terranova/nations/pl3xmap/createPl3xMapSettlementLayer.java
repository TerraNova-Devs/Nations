package de.terranova.nations.pl3xmap;

import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Polygon;
import net.pl3x.map.core.markers.marker.Polyline;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Tooltip;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class createPl3xMapSettlementLayer extends WorldLayer {
    public createPl3xMapSettlementLayer(@NotNull World world) {
        super("settlement", world, () -> "Settlement");

        setUpdateInterval(0);
        setLiveUpdate(true);
        setShowControls(true);
        setDefaultHidden(false);
        setPriority(100);
        setZIndex(999);

        String tooltip = getLabel();
        if (!tooltip.isBlank()) {
            setOptions(Options.builder()
                    .tooltipContent(tooltip)
                    .tooltipDirection(Tooltip.Direction.TOP)
                    .build()
            );
        }
    }

    @Override
    public @NotNull Collection<@NotNull Marker<?>> getMarkers() {
        Collection<Point> points = new ArrayList<>();
        points.add(new Point(100, 100));
        points.add(new Point(-100, 100));
        points.add(new Point(-100, -100));
        points.add(new Point(100, -100));
        Polyline polyline = new Polyline("polyline", points);
        Polygon polygon = new Polygon("polygon", polyline);
        return Collections.singletonList(polygon.setOptions(getOptions()));
    }
}
