package de.terranova.nations.pl3xmap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import javax.imageio.ImageIO;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.configuration.Lang;
import net.pl3x.map.core.configuration.SpawnLayerConfig;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Tooltip;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Manages world spawn marker.
 */
public class testLayer extends WorldLayer {
    public static final String KEY = "pl3xmap_spawn";

    private final String icon;

    /**
     * Create a new spawn layer.
     *
     * @param world world
     */
    public testLayer(@NotNull World world) {
        super("test", world, () -> "Lang.UI_LAYER_SPAWN");

        this.icon = SpawnLayerConfig.ICON;

        Path icon = FileUtil.getWebDir().resolve("images/icon/" + this.icon + ".png");
        try {
            IconImage image = new IconImage(this.icon, ImageIO.read(icon.toFile()), "png");
            Pl3xMap.api().getIconRegistry().register(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setUpdateInterval(SpawnLayerConfig.UPDATE_INTERVAL);
        setLiveUpdate(SpawnLayerConfig.LIVE_UPDATE);
        setShowControls(SpawnLayerConfig.SHOW_CONTROLS);
        setDefaultHidden(SpawnLayerConfig.DEFAULT_HIDDEN);
        setPriority(SpawnLayerConfig.PRIORITY);
        setZIndex(SpawnLayerConfig.Z_INDEX);

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
        return Collections.singletonList(Marker.icon("test", new Point(100,100), this.icon, 16).setOptions(getOptions()));
    }
}