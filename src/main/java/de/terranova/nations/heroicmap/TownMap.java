package de.terranova.nations.heroicmap;

import com.nekyia.heroicmap.api.HeroicMapApi;
import com.nekyia.heroicmap.api.Layer;
import com.nekyia.heroicmap.api.MapObject;
import com.nekyia.heroicmap.api.Panel;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.dao.SettlementProfessionRelationDAO;
import de.terranova.nations.heroicmap.TownObjects.Synced;
import de.terranova.nations.heroicmap.TownObjects.Town;
import de.terranova.nations.nations.Nation;
import de.terranova.nations.professions.ProfessionManager;
import de.terranova.nations.professions.ProfessionStatus;
import de.terranova.nations.professions.pojo.ProfessionConfig;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.grid.SettleRegion;
import de.terranova.nations.regions.modules.access.Access;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.utils.BannerRenderer;
import de.terranova.nations.utils.ColorUtils;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Shows the towns on HeroicMap: banners, areas and circles; a nation shows by its banner only.
 * Names the API of HeroicMap, so NationsPlugin loads it only when HeroicMap is enabled. If
 * HeroicMap is disabled while Nations runs, the calls go nowhere without an error; a restarted
 * HeroicMap gets the layers only with the next start of Nations.
 */
public final class TownMap {

  // Professions from the database and names of players are fetched again after this.
  private static final long MAX_AGE_MS = 5 * 60 * 1000;
  private static final String NO_NATION_COLOR = "#40E53F";

  private record Professions(long fetchedAt, List<Panel.Row> rows) {}

  private record Name(String name, long fetchedAt) {}

  private final Plugin plugin;
  private final Synced towns;
  private final Synced areas;
  private final Synced circles;
  private final Map<UUID, Professions> professions = new ConcurrentHashMap<>();
  // nation id -> banner (Base64 of the item) last uploaded as its image
  private final Map<UUID, String> banners = new HashMap<>();
  // nation id -> banner that could not be drawn; tried again only when the banner changes
  private final Map<UUID, String> failed = new HashMap<>();
  private final Map<UUID, Name> names = new HashMap<>();
  private BukkitTask task;

  private TownMap(Plugin plugin, HeroicMapApi api) {
    this.plugin = plugin;
    towns = synced(api, "staedte", "Städte", "Towns", true, 102);
    areas = synced(api, "regionen", "Regionen", "Regions", true, 101);
    circles = synced(api, "kreise", "Kreise", "Circles", false, 100);
  }

  /** Creates the layers and syncs them every 30 seconds. */
  public static void start(Plugin plugin) throws IOException {
    HeroicMapApi api = Bukkit.getServicesManager().load(HeroicMapApi.class);
    if (api == null) {
      throw new IllegalStateException("HeroicMap bietet keinen HeroicMapApi-Service an");
    }
    // drawn before any layer exists, so that a failure leaves no empty layers behind
    String white = BannerRenderer.renderBannerToDataURI(new ItemStack(Material.WHITE_BANNER));
    if (white == null) {
      throw new IOException("Das weisse Banner liess sich nicht zeichnen");
    }
    TownMap map = new TownMap(plugin, api);
    Layer images = map.towns.layer; // all layers of Nations share their images
    images.image(TownObjects.MEMBERS_IMAGE, resource(plugin, "heroicmap/mitglieder.png"));
    images.image(TownObjects.STATS_IMAGE, resource(plugin, "heroicmap/statistiken.png"));
    images.image(TownObjects.WHITE_BANNER, png(white));
    // Professions come from the database, so off the main thread; everything else on it.
    map.task =
        Bukkit.getScheduler()
            .runTaskTimerAsynchronously(
                plugin,
                () -> {
                  map.refreshProfessions();
                  if (!plugin.isEnabled()) {
                    return;
                  }
                  Bukkit.getScheduler().runTask(plugin, map::update);
                },
                20L,
                20L * 30);
  }

  private Synced synced(
      HeroicMapApi api, String name, String de, String en, boolean visible, int order) {
    Layer layer = api.layer(plugin, name);
    layer.name(de, en);
    layer.visible(visible);
    layer.order(order);
    return new Synced(layer, plugin.getLogger());
  }

  private static byte[] resource(Plugin plugin, String path) throws IOException {
    try (InputStream in = plugin.getResource(path)) {
      if (in == null) {
        throw new IOException(path + " fehlt im Jar");
      }
      return in.readAllBytes();
    }
  }

  private void update() {
    try {
      Set<UUID> usedBanners = new HashSet<>();
      List<Town> all = snapshot(usedBanners, regionsByTown(), System.currentTimeMillis());
      towns.sync(all.stream().map(TownObjects::banner).toList());
      areas.sync(all.stream().flatMap(t -> TownObjects.area(t).stream()).toList());
      circles.sync(all.stream().flatMap(t -> TownObjects.circles(t).stream()).toList());
      removeBannersExcept(usedBanners);
    } catch (IllegalStateException e) {
      // Only after the layers are gone, as while Nations itself is being disabled.
      plugin.getLogger().warning("HeroicMap: Ebenen von Nations sind weg: " + e);
      task.cancel();
    }
  }

  private List<Town> snapshot(
      Set<UUID> usedBanners, Map<UUID, ProtectedRegion> regions, long now) {
    List<Town> all = new ArrayList<>();
    for (SettleRegion s : RegionManager.retrieveAllCachedRegions(SettleRegion.class).values()) {
      try {
        all.add(town(s, usedBanners, regions.get(s.getId()), now));
      } catch (RuntimeException e) {
        plugin.getLogger().warning("HeroicMap: Stadt " + s.getId() + " fehlt auf der Karte: " + e);
      }
    }
    return all;
  }

  private Town town(SettleRegion s, Set<UUID> usedBanners, ProtectedRegion wg, long now) {
    Nation nation = NationsPlugin.nationManager.getNationBySettlement(s.getId());
    String banner = nation == null ? null : banner(nation);
    if (banner != null) {
      usedBanners.add(nation.getId());
    }
    List<MapObject.Point> corners =
        wg == null
            ? List.of()
            : wg.getPoints().stream().map(v -> new MapObject.Point(v.x(), v.z())).toList();
    String color =
        nation == null
            ? NO_NATION_COLOR
            : String.format("#%06X", ColorUtils.getColorFromName(nation.getName()) & 0xFFFFFF);
    Access access = s.getAccess();
    UUID major = access.getMajor();
    Professions p = professions.get(s.getId());
    return new Town(
        s.getId(),
        s.getName().replace("_", " "),
        s.getLocation().x,
        s.getLocation().z,
        corners,
        nation == null ? null : nation.getName(),
        nation != null && s.getId().equals(nation.getCapital()),
        color,
        banner,
        s.getRank().getLevel(),
        s.getClaims(),
        s.getMaxClaims(),
        major == null ? null : name(major, now),
        names(access, AccessLevel.VICE, now),
        names(access, AccessLevel.COUNCIL, now),
        p == null ? List.of() : p.rows());
  }

  private List<String> names(Access access, AccessLevel level, long now) {
    return access.getEveryUUIDWithCertainAccessLevel(level).stream()
        .map(player -> name(player, now))
        .filter(Objects::nonNull)
        .sorted()
        .toList();
  }

  /** The name of the player, looked up again after five minutes; null if unknown. */
  private String name(UUID player, long now) {
    Name n = names.get(player);
    if (n == null || now - n.fetchedAt() > MAX_AGE_MS) {
      n = new Name(Bukkit.getOfflinePlayer(player).getName(), now);
      names.put(player, n);
    }
    return n.name();
  }

  /** The WorldGuard region of every town by its id, in one pass over the regions. */
  private static Map<UUID, ProtectedRegion> regionsByTown() {
    Map<UUID, ProtectedRegion> byTown = new HashMap<>();
    World world = Bukkit.getWorld("world");
    if (world == null) {
      return byTown;
    }
    com.sk89q.worldguard.protection.managers.RegionManager regions =
        WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    if (regions == null) {
      return byTown;
    }
    for (ProtectedRegion region : regions.getRegions().values()) {
      String id = region.getFlag(RegionFlag.REGION_UUID_FLAG);
      if (id == null) {
        continue;
      }
      try {
        byTown.put(UUID.fromString(id), region);
      } catch (IllegalArgumentException e) {
        // not the id of a town
      }
    }
    return byTown;
  }

  /** The image of the nation's banner, uploaded when it changed; null without one. */
  private String banner(Nation nation) {
    String base64 = nation.getBannerBase64();
    if (base64 == null) {
      return null;
    }
    String path = bannerPath(nation.getId());
    if (base64.equals(banners.get(nation.getId()))) {
      return path;
    }
    if (base64.equals(failed.get(nation.getId()))) {
      return null;
    }
    // HeroicMap keeps 200 images per owner: 2 headings, the white banner and banners of 197
    // nations; more nations get the white one.
    if (!banners.containsKey(nation.getId()) && banners.size() >= 197) {
      return null;
    }
    String uri = BannerRenderer.renderBannerToDataURI(nation.getBanner());
    if (uri == null) {
      failed.put(nation.getId(), base64);
      return null;
    }
    try {
      towns.layer.image(path, png(uri));
    } catch (IllegalArgumentException e) {
      plugin.getLogger().warning("HeroicMap: Banner von " + nation.getName() + ": " + e);
      failed.put(nation.getId(), base64);
      return null;
    }
    failed.remove(nation.getId());
    banners.put(nation.getId(), base64);
    return path;
  }

  /** The PNG in a data URI of the BannerRenderer: 20 × 40 pixels, the front of the banner. */
  private static byte[] png(String dataUri) {
    return Base64.getDecoder().decode(dataUri.substring(dataUri.indexOf(',') + 1));
  }

  private static String bannerPath(UUID nationId) {
    return "images/banner-" + nationId + ".png";
  }

  /** Removes the banner images no town shows any more, after the sync took them off. */
  private void removeBannersExcept(Set<UUID> used) {
    for (UUID id : new ArrayList<>(banners.keySet())) {
      if (used.contains(id)) {
        continue;
      }
      try {
        towns.layer.removeImage(bannerPath(id));
        banners.remove(id);
      } catch (IllegalArgumentException e) {
        // still named by an object the layer refused to replace; try again next time
      }
    }
  }

  /** Fetches the professions of every town not fetched in the last five minutes. */
  private void refreshProfessions() {
    long now = System.currentTimeMillis();
    Set<UUID> ids = RegionManager.retrieveAllCachedRegions(SettleRegion.class).keySet();
    professions.keySet().retainAll(ids);
    for (UUID id : ids) {
      Professions p = professions.get(id);
      if (p != null && now - p.fetchedAt() <= MAX_AGE_MS) {
        continue;
      }
      try {
        Map<String, ProfessionStatus> statuses =
            SettlementProfessionRelationDAO.getAllStatuses(id.toString());
        professions.put(id, new Professions(now, rows(statuses)));
      } catch (RuntimeException e) {
        // e.g. an unknown status in the database; keep the old rows, try again in five minutes
        plugin.getLogger().warning("HeroicMap: Berufe der Stadt " + id + ": " + e);
        professions.put(id, new Professions(now, p == null ? List.of() : p.rows()));
      }
    }
  }

  /** One row per profession type with a completed level, its levels 1 to 4 as points. */
  private static List<Panel.Row> rows(Map<String, ProfessionStatus> statuses) {
    List<Panel.Row> rows = new ArrayList<>();
    for (String type : ProfessionManager.getProfessionTypes().stream().sorted().toList()) {
      List<ProfessionConfig> confs = ProfessionManager.getProfessionsByType(type);
      int done =
          (int)
              confs.stream()
                  .filter(c -> c.getLevel() >= 1 && c.getLevel() <= 4)
                  .filter(c -> statuses.get(c.professionId) == ProfessionStatus.COMPLETED)
                  .map(ProfessionConfig::getLevel)
                  .distinct()
                  .count();
      if (done > 0) {
        rows.add(new Panel.Row(confs.getFirst().prettyName, done, 4, colorFor(type)));
      }
    }
    return List.copyOf(rows);
  }

  private static String colorFor(String type) {
    return switch (type.toUpperCase()) {
      case "FISHERY" -> "#2980B9";
      case "MINING" -> "#7F8C8D";
      case "FARMING" -> "#27AE60";
      case "RANCHING" -> "#9C6B3B";
      case "BREWING" -> "#9B59B6";
      case "SMITHING" -> "#424242";
      case "MAGIC" -> "#E91E63";
      case "WOODCUTTING" -> "#8D6E63";
      case "MILITARY" -> "#C0392B";
      case "STONEWORK" -> "#BCAAA4";
      case "TRADE" -> "#FFD54F";
      case "FAITH" -> "#9575CD";
      default -> "#888888";
    };
  }
}
