package de.terranova.nations;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.SessionManager;
import de.mcterranova.bona.lib.YMLHandler;
import de.terranova.nations.commands.settle;
import de.terranova.nations.database.HikariCP;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.SettlementTrait;
import de.terranova.nations.settlements.settlementManager;
import de.terranova.nations.worldguard.settlementFlag;
import de.terranova.nations.worldguard.settlementHandler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import mc.obliviate.inventory.InventoryAPI;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.registry.Registry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class NationsPlugin extends JavaPlugin {

    public static settlementManager settlementManager;
    //public YMLHandler levelYML;
    public static HikariCP hikari;
    public YMLHandler skinsYML;
    public Logger logger;
    private Registry<@NotNull Layer> layerRegistry;

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
        new InventoryAPI(this).init();
        commandRegistry();
        listenerRegistry();
        serilizationRegistry();
        citizensTraitRegistry();
        try {
            loadConfigs();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        settlementManager = new settlementManager();
        try {
            SettleDBstuff.getInitialSettlementData();
            settlementManager.addSettlementsToPl3xmap();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        //layerRegistry.register("test",new testLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world"))));
    }


    private void initDatabase() {
        try {
            hikari = new HikariCP(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void citizensTraitRegistry() {
        net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(SettlementTrait.class));
    }

    private void worldguardFlagRegistry() {
        settlementFlag.registerSettlementFlag();
    }

    private void worldguardHandlerRegistry() {
        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(settlementHandler.FACTORY, null);
    }


    public void commandRegistry() {
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("settle", "Command facilitates settlements creation.", List.of("s"), new settle(this));
        });
    }
    //Objects.requireNonNull(getCommand("settle")).setExecutor(new settle(this));

    public void listenerRegistry() {
    }

    public void serilizationRegistry() {
    }

    public void loadConfigs() throws FileNotFoundException {
        try {
            skinsYML = new YMLHandler("skins.yml", this.getDataFolder());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //LinkedList<Objective> d = yaml.load(new FileInputStream(new File(this.getDataFolder(),"level.yml")));


        /*
        try {
            levelYML = new YMLHandler("level.yml", this.getDataFolder());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

         */
    }

}






