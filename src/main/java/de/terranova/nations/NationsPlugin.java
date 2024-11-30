package de.terranova.nations;

import com.mojang.brigadier.Command;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.SessionManager;
import de.mcterranova.terranovaLib.roseGUI.RoseGUIListener;
import de.mcterranova.terranovaLib.utils.YMLHandler;
import de.terranova.nations.citizens.SettleTrait;
import de.terranova.nations.commands.*;
import de.terranova.nations.database.HikariCP;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.regions.SettleManager;
import de.terranova.nations.regions.base.RegionType;
import de.terranova.nations.regions.grid.SettleRegionFactory;
import de.terranova.nations.regions.grid.SettleRegionType;
import de.terranova.nations.regions.rank.RankObjective;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionHandler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.registry.Registry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public final class NationsPlugin extends JavaPlugin {

    public static boolean debug = true;
    public static SettleManager settleManager;
    //public YMLHandler levelYML;
    public static HikariCP hikari;
    public static Map<Integer, RankObjective> levelObjectives;
    public static Logger logger;
    static public Plugin plugin;
    public YMLHandler skinsYML;
    private Registry<Layer> layerRegistry;

    //NPC UND WORLDGUARDREGION IN SETTLEMENTS CACHEN
    //EIGENE KLASSE FÜR ACCESS

    @Override
    public void onLoad() {
        worldguardFlagRegistry();
        initDatabase();
    }

    // version savedata
    @Override
    public void onEnable() {
        //Stillbugs is used to send Action Bar to player later
        plugin = this;
        worldguardHandlerRegistry();
        pl3xmapMarkerRegistry();
        logger = getLogger();
        saveDefaultConfig();
        commandRegistry();
        listenerRegistry();
        serilizationRegistry();
        citizensTraitRegistry();
        nationsRegionTypeRegistry();

        try {
            loadConfigs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        settleManager = new SettleManager();

        SettleDBstuff.getInitialSettlementData();
        settleManager.addSettlementsToPl3xmap();

    }

    private void nationsRegionTypeRegistry() {
        RegionType.registerRegionType(SettleRegionType.REGION_TYPE, new SettleRegionFactory());
    }

    @Override
    public void onDisable() {
        try {
            hikari.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        //SettleFlag.registerSettlementFlag();
        RegionFlag.registerRegionFlag(this);
    }

    private void worldguardHandlerRegistry() {
        //SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        //sessionManager.registerHandler(SettleHandler.FACTORY, null);
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(RegionHandler.FACTORY, null);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void commandRegistry() {
        /*
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("terra", "Command facilitates settlements creation.", List.of("t"), new TerraCommand());
        });
         */
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
    }

    public void serilizationRegistry() {
    }

    public void loadConfigs() throws IOException {

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

    }

}






