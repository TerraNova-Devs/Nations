package de.terranova.nations;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.SessionManager;
import de.mcterranova.terranovaLib.roseGUI.RoseGUIListener;
import de.mcterranova.terranovaLib.utils.YMLHandler;
import de.terranova.nations.commands.SettleCommand;
import de.terranova.nations.database.HikariCP;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.SettleManager;
import de.terranova.nations.settlements.SettleTrait;
import de.terranova.nations.settlements.level.Objective;
import de.terranova.nations.worldguard.SettleFlag;
import de.terranova.nations.worldguard.SettleHandler;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public final class NationsPlugin extends JavaPlugin {

    public static boolean debug = true;
    public static SettleManager settleManager;
    //public YMLHandler levelYML;
    public static HikariCP hikari;
    public static Map<Integer, Objective> levelObjectives;
    public YMLHandler skinsYML;
    public static Logger logger;
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
        worldguardHandlerRegistry();
        pl3xmapMarkerRegistry();
        logger = getLogger();
        saveDefaultConfig();
        commandRegistry();
        listenerRegistry();
        serilizationRegistry();
        citizensTraitRegistry();
        try {
            loadConfigs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        settleManager = new SettleManager();

        SettleDBstuff.getInitialSettlementData();
        settleManager.addSettlementsToPl3xmap();
    }

    @Override
    public void onDisable() {
        //unloaden wenn ausgelesen nicht erst am Ende
        skinsYML.unloadYAML();
        //levelYML.unloadYAML();
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
        SettleFlag.registerSettlementFlag();
    }

    private void worldguardHandlerRegistry() {
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(SettleHandler.FACTORY, null);
    }


    @SuppressWarnings("UnstableApiUsage")
    public void commandRegistry() {
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("settle", "Command facilitates settlements creation.", List.of("s"), new SettleCommand(this));
        });
        Objects.requireNonNull(getCommand("settle")).setTabCompleter(new SettleCommand(this));
    }
    //Objects.requireNonNull(getCommand("settle")).setExecutor(new settle(this));

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
        representer.addClassTag(Objective.class, new Tag("Stadtlevel"));

        Constructor constructor = new Constructor(loaderOptions);
        constructor.addTypeDescription(new TypeDescription(Objective.class, new Tag("Stadtlevel")));

        Yaml yaml = new Yaml(constructor, representer, dumperOptions, loaderOptions);

        if (file.createNewFile()) {
            HashMap<Integer, Objective> exampleObj = new HashMap<>();
            exampleObj.put(1, new Objective(1, 1, 1, 1, 1, "Test", "Test", "Test", "Test"));
            exampleObj.put(2, new Objective(2, 2, 1, 2, 2, "Test2", "Test2", "Test2", "Test2"));
            FileWriter writer = new FileWriter(file);
            yaml.dump(exampleObj, writer);
        }

        levelObjectives = yaml.load(new FileInputStream(file));

    }

}






