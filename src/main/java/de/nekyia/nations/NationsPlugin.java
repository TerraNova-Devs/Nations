package de.nekyia.nations;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.SessionManager;
import de.nekyia.nations.citizens.SettleTrait;
import de.nekyia.nations.command.TerraCommand;
import de.nekyia.nations.database.HikariCP;
import de.nekyia.nations.logging.FileLogger;
import de.nekyia.nations.regions.RegionManager;
import de.nekyia.nations.regions.base.RegionType;
import de.nekyia.nations.regions.base.RegionTypeDatabase;
import de.nekyia.nations.regions.grid.SettleRegionType;
import de.nekyia.nations.regions.grid.SettleRegionTypeFactory;
import de.nekyia.nations.pl3xmap.RegionLayer;
import de.nekyia.nations.regions.rank.RankObjective;
import de.nekyia.nations.utils.roseGUI.RoseGUIListener;
import de.nekyia.nations.worldguard.NationsRegionFlag.RegionFlag;
import de.nekyia.nations.worldguard.NationsRegionFlag.RegionHandler;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.registry.Registry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class NationsPlugin extends JavaPlugin implements Listener {

    public static boolean debug = false;
    public static HikariCP hikari;
    public static Map<Integer, RankObjective> levelObjectives;
    static public Plugin plugin;
    public static FileLogger nationsLogger;
    public static FileLogger nationsDebugger;
    private Registry<Layer> layerRegistry;

    @Override
    public void onLoad() {
        nationsDebugger = new FileLogger(getDataFolder().getAbsolutePath() + "/logs/debug", "Nations_Debug");
        nationsLogger = new FileLogger(getDataFolder().getAbsolutePath() + "/logs", "Nations");
        worldguardFlagRegistry();
        initDatabase();
    }

    @Override
    public void onEnable() {
        plugin = this;
        citizensTraitRegistry();
        worldguardHandlerRegistry();
        pl3xmapMarkerRegistry();
        layerRegistry.register("settlement-layer",new RegionLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world"))));
        saveDefaultConfig();
        commandRegistry();
        listenerRegistry();
        serilizationRegistry();
        nationsRegionTypeRegistry();
        loadConfigs();
    }

    @EventHandler
    public void onCitizensEnable(CitizensEnableEvent event) {
        RegionManager.cacheRegions("settle", RegionTypeDatabase.fetchRegions("settle"));
    }

    private void nationsRegionTypeRegistry() {
        RegionType.registerRegionType(SettleRegionType.REGION_TYPE, new SettleRegionTypeFactory());
    }

    @Override
    public void onDisable() {
        try {
            hikari.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        nationsLogger.close();
    }

    private void pl3xmapMarkerRegistry() {
        this.layerRegistry = Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world")).getLayerRegistry();
    }


    private void initDatabase() {
        try {
            hikari = new HikariCP(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void citizensTraitRegistry() {
        net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(SettleTrait.class));
    }

    private void worldguardFlagRegistry() {
        RegionFlag.registerRegionFlag(this);
    }

    private void worldguardHandlerRegistry() {
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(RegionHandler.FACTORY, null);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void commandRegistry() {
        TerraCommand terraCommand = new TerraCommand();
        if (this.getCommand("terra") == null) {
            getLogger().severe("Failed to get command 'terra' from plugin.yml. Please check your plugin.yml!");
            return;
        }
        Objects.requireNonNull(this.getCommand("terra")).setExecutor(terraCommand);
        Objects.requireNonNull(this.getCommand("terra")).setTabCompleter(terraCommand);
    }

    public void listenerRegistry() {
        Bukkit.getPluginManager().registerEvents(new RoseGUIListener(), this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void serilizationRegistry() {
    }

    public void loadConfigs() {
        try {
            File file = new File(this.getDataFolder(), "level.yml");

            LoaderOptions loaderOptions = new LoaderOptions();

            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setIndent(2);
            dumperOptions.setSplitLines(false);
            dumperOptions.setPrettyFlow(true);
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Representer representer = new Representer(dumperOptions);
            representer.addClassTag(RankObjective.class, new Tag("Stadtlevel"));

            Constructor constructor = new Constructor(loaderOptions);
            constructor.addTypeDescription(new TypeDescription(RankObjective.class, new Tag("Stadtlevel")));

            Yaml yaml = new Yaml(constructor, representer, dumperOptions, loaderOptions);

            if (file.createNewFile()) {
                HashMap<Integer, RankObjective> exampleObj = new HashMap<>();
                exampleObj.put(1, new RankObjective(1, 1, 1, 1, 1, "Test", "Test", "Test"));
                exampleObj.put(2, new RankObjective(2, 2, 1, 2, 2, "Test2", "Test2", "Test2"));
                FileWriter writer = new FileWriter(file);
                yaml.dump(exampleObj, writer);
            }

            levelObjectives = yaml.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}






