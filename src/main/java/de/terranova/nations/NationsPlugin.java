package de.terranova.nations;

import de.mcterranova.bona.lib.YMLHandler;
import de.terranova.nations.commands.settle;
import de.terranova.nations.database.HikariCP;
import de.terranova.nations.listener.NpcInteractListener;
import de.terranova.nations.settlements.SettlementTrait;
import de.terranova.nations.settlements.settlementManager;
import de.terranova.nations.worldguard.settlementFlag;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import mc.obliviate.inventory.InventoryAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public final class NationsPlugin extends JavaPlugin {

    public settlementManager settlementManager = new settlementManager();

    public YMLHandler skinsYML;
    public Logger logger;
    //public YMLHandler levelYML;
    HikariCP hikari;

    @Override
    public void onLoad() {
        worldguardFlagRegistry();
    }

    // version savedata
    @Override
    public void onEnable() {
        logger = getLogger();
        saveDefaultConfig();

        new InventoryAPI(this).init();
        commandRegistry();
        listenerRegistry();
        serilizationRegistry();
        citizensTraitRegiystry();
        try {
            loadConfigs();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void citizensTraitRegiystry() {
        net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(SettlementTrait.class));
    }

    private void worldguardFlagRegistry() {
        settlementFlag.registerSettlementFlag();
    }

    @Override
    public void onDisable() {
        //unloaden wenn ausgelesen nicht erst am Ende
        skinsYML.unloadYAML();
        //levelYML.unloadYAML();
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
        Bukkit.getPluginManager().registerEvents(new NpcInteractListener(), this);
    }

    public void serilizationRegistry() {
        //  ConfigurationSerialization.registerClass(Objective.class, "objective");
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






