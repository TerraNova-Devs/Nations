package de.terranova.nations;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.SessionManager;
import de.terranova.nations.citizens.SettleTrait;
import de.terranova.nations.command.NationCommands;
import de.terranova.nations.command.TownCommands;
import de.terranova.nations.command.realEstate.RealEstateCommand;
import de.terranova.nations.database.HikariCP;
import de.terranova.nations.database.dao.BoundaryRegionDAO;
import de.terranova.nations.database.dao.GridRegionDAO;
import de.terranova.nations.database.dao.RealEstateDAO;
import de.terranova.nations.discord.WebhookHandler;
import de.terranova.nations.logging.FileLogger;
import de.terranova.nations.nations.NationManager;
import de.terranova.nations.pl3xmap.InfoLayer;
import de.terranova.nations.pl3xmap.RegionLayer;
import de.terranova.nations.professions.ProfessionManager;
import de.terranova.nations.professions.pojo.ProfessionConfig;
import de.terranova.nations.professions.pojo.ProfessionConfigLoader;
import de.terranova.nations.professions.pojo.ProfessionsYaml;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.RegionRegistry;
import de.terranova.nations.regions.boundary.PropertyRegion;
import de.terranova.nations.regions.boundary.PropertyRegionFactory;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.grid.SettleRegionFactory;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.regions.modules.rank.RankObjective;
import de.terranova.nations.regions.rule.RuleSet;
import de.terranova.nations.regions.rule.rules.AccessLevelCheck;
import de.terranova.nations.regions.rule.rules.SelfOverlap;
import de.terranova.nations.regions.rule.rules.RegionNameValidationRule;
import de.terranova.nations.regions.rule.rules.WithinParent;
import de.terranova.nations.utils.SecretsReader;
import de.terranova.nations.utils.terraRenderer.refactor.Listener.BreezeToolListener;
import de.terranova.nations.worldguard.NationsRegionFlag.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.registry.Registry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public final class NationsPlugin extends JavaPlugin implements Listener {

  public static boolean debug = false;
  public static HikariCP hikari;
  public static Map<Integer, RankObjective> levelObjectives;
  public static List<ProfessionConfig> professionConfigs;
  public static Plugin plugin;
  public static FileLogger nationsLogger;
  public static FileLogger nationsDebugger;
  public static NationManager nationManager;
  public static WebhookHandler professionsHook;


  @Override
  public void onLoad() {
    nationsDebugger =
        new FileLogger(getDataFolder().getAbsolutePath() + "/logs/debug", "Nations_Debug");
    nationsLogger = new FileLogger(getDataFolder().getAbsolutePath() + "/logs", "Nations");
    worldguardFlagRegistry();
  }

  @Override
  public void onEnable() {
    plugin = this;
    saveDefaultConfig();
    saveResource("secrets.env", false);
    SecretsReader.init();
    initDatabase();
    professionsHook = new WebhookHandler(SecretsReader.DISCORD_WEBHOOK_URL);
    citizensTraitRegistry();
    worldguardHandlerRegistry();
    pl3xmapMarkerRegistry();
    nationManager = new NationManager();
    RealEstateDAO.loadAllHoldings();
    commandRegistry();
    listenerRegistry();
    serilizationRegistry();
    nationsRegionTypeRegistry();
    loadConfigs();
    ProfessionManager.loadAll();
    getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
  }

  @EventHandler
  public void onCitizensEnable(CitizensEnableEvent event) {
    // Citizens muss vor den Regionen geladen sein
    RegionManager.cacheRegions("settle", GridRegionDAO.fetchRegionsByType("settle"));
    RegionManager.cacheRegions("property", BoundaryRegionDAO.fetchRegionsByType("property"));
  }

  private void nationsRegionTypeRegistry() {
    RegionRegistry.register(
        new SettleRegionFactory(),
        RuleSet.defaultRules()
            .addRule(
                new RegionNameValidationRule(
                    "^(?!.*__)(?!_)(?!.*_$)(?!.*(.)\\1{3,})[a-zA-Z0-9_]{3,20}$"))
            .addRule(new SelfOverlap(true))
            .addRule(new AccessLevelCheck(null)));

    RegionRegistry.register(
        new PropertyRegionFactory(), 
        RuleSet.defaultRules()
            .addRule(new SelfOverlap(false))
            .addRule(new AccessLevelCheck(AccessLevel.VICE))
            .addRule(new WithinParent<>(PropertyRegion.class, SettleRegion.class)));
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
    Registry<@NotNull Layer> layerRegistry;
    layerRegistry = Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world")).getLayerRegistry();
    layerRegistry.register(
            "settlement-layer",
            new RegionLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world"))));
    layerRegistry.register(
            "settlement-info",
            new InfoLayer(Objects.requireNonNull(Pl3xMap.api().getWorldRegistry().get("world"))));
  }

  private void initDatabase() {
    try {
      hikari = new HikariCP(this);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void citizensTraitRegistry() {
    net.citizensnpcs.api.CitizensAPI.getTraitFactory()
        .registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(SettleTrait.class));
  }

  private void worldguardFlagRegistry() {
    RegionFlag.registerRegionFlag(this);
    TypeFlag.registerRegionFlag(this);
  }

  private void worldguardHandlerRegistry() {
    SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
    sessionManager.registerHandler(RegionHandler.FACTORY, null);
    sessionManager.registerHandler(TypeHandler.FACTORY, null);
  }

  public void commandRegistry() {
    RealEstateCommand realEstateCommand = new RealEstateCommand();
    Objects.requireNonNull(this.getCommand("realEstate")).setExecutor(realEstateCommand);
    Objects.requireNonNull(this.getCommand("realEstate")).setTabCompleter(realEstateCommand);
    TownCommands townCommand = new TownCommands();
    Objects.requireNonNull(this.getCommand("town")).setExecutor(townCommand);
    Objects.requireNonNull(this.getCommand("town")).setTabCompleter(townCommand);
    NationCommands nationCommand = new NationCommands();
    Objects.requireNonNull(this.getCommand("nation")).setExecutor(nationCommand);
    Objects.requireNonNull(this.getCommand("nation")).setTabCompleter(nationCommand);
  }

  public void listenerRegistry() {
    Bukkit.getPluginManager().registerEvents(this, this);
    Bukkit.getPluginManager().registerEvents(new BreezeToolListener(), this);
  }

  public void serilizationRegistry() {}

  public void loadConfigs() {
    File professionsFile = new File(this.getDataFolder(), "professions.yml");
    if (!professionsFile.exists()) {
      saveResource("professions.yml", false);
    }
    try {
      ProfessionsYaml data = ProfessionConfigLoader.load(professionsFile);
      professionConfigs = data.professions;

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
      constructor.addTypeDescription(
          new TypeDescription(RankObjective.class, new Tag("Stadtlevel")));

      Yaml yaml = new Yaml(constructor, representer, dumperOptions, loaderOptions);

      if (file.createNewFile()) {
        HashMap<Integer, RankObjective> exampleObj = new HashMap<>();
        exampleObj.put(1, new RankObjective(1, 1));
        exampleObj.put(2, new RankObjective(2, 2));
        FileWriter writer = new FileWriter(file);
        yaml.dump(exampleObj, writer);
      }

      levelObjectives = yaml.load(new FileInputStream(file));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
