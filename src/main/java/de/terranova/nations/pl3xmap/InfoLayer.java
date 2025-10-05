package de.terranova.nations.pl3xmap;

import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class InfoLayer extends WorldLayer {
    static Map<String, Marker<?>> markers = new HashMap<>();

    public InfoLayer(@NotNull World world) {
        super("settlement-info", world, () -> "Stadtinfos");
        setUpdateInterval(200);
        setLiveUpdate(true);
        setShowControls(true);
        setDefaultHidden(true);
        setPriority(99);
        setZIndex(999);
    }

    @Override
    public @NotNull Collection<Marker<?>> getMarkers() {
        return markers.values();
    }

}
