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
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

  /** A nation's banner as drawn on the main thread; png and design null where that failed. */
  private record Drawn(String name, String base64, byte[] png, Object design) {}

  /** A town as read on the main thread, with the id of its nation, null for none. */
  private record Entry(Town town, UUID nation) {}

  /** What the main thread read for one sync; nations only with a banner. */
  private record Snapshot(List<Entry> towns, Map<UUID, Drawn> nations) {}

  private final Plugin plugin;
  // whether HeroicMap takes banner designs (0.6 on); without, banners have images only
  private final boolean designs;
  private final Map<UUID, Professions> professions = new ConcurrentHashMap<>();

  // The main thread reads Bukkit, WorldGuard and Citizens into a Snapshot; apply takes it to
  // HeroicMap on an async task, one at a time. Each state below belongs to one of the two.

  // main thread only
  private final Map<UUID, Name> names = new HashMap<>();
  // nation id -> its banner as last drawn
  private final Map<UUID, Drawn> drawn = new HashMap<>();

  // apply only
  private final Synced towns;
  private final Synced areas;
  private final Synced circles;
  // nation id -> banner (Base64 of the item) last uploaded as its image
  private final Map<UUID, String> banners = new HashMap<>();
  // nation id -> banner that could not be drawn or uploaded; tried again only when it changes
  private final Map<UUID, String> failed = new HashMap<>();
  // nation id -> banner (Base64 of the item) last set as its design, and those that failed
  private final Map<UUID, String> designed = new HashMap<>();
  private final Map<UUID, String> failedDesigns = new HashMap<>();

  private final Latest<Snapshot> latest = new Latest<>();
  private volatile boolean stopped;
  private BukkitTask task;

  private TownMap(Plugin plugin, HeroicMapApi api, boolean designs) {
    this.plugin = plugin;
    this.designs = designs;
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
    Plugin heroicMap = Bukkit.getPluginManager().getPlugin("HeroicMap");
    boolean designs =
        heroicMap != null && TownObjects.hasDesigns(heroicMap.getPluginMeta().getVersion());
    TownMap map = new TownMap(plugin, api, designs);
    Layer images = map.towns.layer; // all layers of Nations share their images
    images.image(TownObjects.MEMBERS_IMAGE, resource(plugin, "heroicmap/mitglieder.png"));
    images.image(TownObjects.STATS_IMAGE, resource(plugin, "heroicmap/statistiken.png"));
    images.image(TownObjects.WHITE_BANNER, png(white));
    if (designs) {
      TownDesigns.putWhite(map.towns.layer);
    }
    // Professions come from the database and the layers take objects on any thread, so both off
    // the main thread; only reading Bukkit, WorldGuard and Citizens on it.
    map.task =
        Bukkit.getScheduler()
            .runTaskTimerAsynchronously(
                plugin,
                () -> {
                  map.refreshProfessions();
                  if (!plugin.isEnabled() || map.stopped) {
                    return;
                  }
                  Bukkit.getScheduler().runTask(plugin, map::snapshot);
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

  /** Main thread: reads the towns, draws changed banners, and hands it all to apply. */
  private void snapshot() {
    if (stopped) {
      return;
    }
    long now = System.currentTimeMillis();
    Map<UUID, ProtectedRegion> regions = regionsByTown();
    List<Entry> entries = new ArrayList<>();
    Map<UUID, Drawn> nations = new HashMap<>();
    for (SettleRegion s : RegionManager.retrieveAllCachedRegions(SettleRegion.class).values()) {
      try {
        Nation nation = NationsPlugin.nationManager.getNationBySettlement(s.getId());
        if (nation != null && nation.getBannerBase64() != null) {
          nations.computeIfAbsent(nation.getId(), id -> draw(nation));
        }
        Town town = town(s, nation, regions.get(s.getId()), now);
        entries.add(new Entry(town, nation == null ? null : nation.getId()));
      } catch (RuntimeException e) {
        plugin.getLogger().warning("HeroicMap: Stadt " + s.getId() + " fehlt auf der Karte: " + e);
      }
    }
    drawn.keySet().retainAll(nations.keySet());
    latest.offer(new Snapshot(List.copyOf(entries), Map.copyOf(nations)));
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> latest.applyNewest(this::apply));
  }

  /** Async, one at a time: uploads changed banners, then brings the layers to the snapshot. */
  private void apply(Snapshot s) {
    if (stopped || !plugin.isEnabled()) {
      return;
    }
    try {
      Set<UUID> used = new HashSet<>();
      List<Town> all = new ArrayList<>();
      for (Entry e : s.towns()) {
        Drawn d = e.nation() == null ? null : s.nations().get(e.nation());
        String banner = d == null ? null : upload(e.nation(), d);
        String design = designs ? design(e.nation(), d) : null;
        if (banner != null || (design != null && !design.equals(TownDesigns.WHITE))) {
          used.add(e.nation());
        }
        all.add(e.town().withBanner(banner, design));
      }
      towns.sync(all.stream().map(TownObjects::banner).toList());
      areas.sync(all.stream().flatMap(t -> TownObjects.area(t).stream()).toList());
      circles.sync(all.stream().flatMap(t -> TownObjects.circles(t).stream()).toList());
      removeBannersExcept(used);
      removeDesignsExcept(used);
    } catch (IllegalStateException e) {
      // The layers are gone, as while Nations or HeroicMap is being disabled: stop with one line.
      stopped = true;
      task.cancel();
      plugin.getLogger().warning("HeroicMap: Ebenen von Nations sind weg: " + e.getMessage());
    }
  }

  /** What the map shows of the town; banner and design are added by apply. */
  private Town town(SettleRegion s, Nation nation, ProtectedRegion wg, long now) {
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
    // The banner stands at the town NPC and follows it within one sync when it is moved; the
    // stored location of the town is the anchor of the grid and stays the fallback.
    NPC npc = s.getNPC() == null ? null : s.getNPC().getNPC();
    Location at = npc == null ? null : npc.getStoredLocation();
    TownObjects.Place place =
        at == null || at.getWorld() == null
            ? TownObjects.place(null, 0, 0, 0, s.getLocation().x, s.getLocation().z)
            : TownObjects.place(
                at.getWorld().getName(), at.getX(), at.getY(), at.getZ(),
                s.getLocation().x, s.getLocation().z);
    return new Town(
        s.getId(),
        s.getName().replace("_", " "),
        place.x(),
        place.z(),
        place.y(),
        corners,
        nation == null ? null : nation.getName(),
        nation != null && s.getId().equals(nation.getCapital()),
        color,
        null,
        null,
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

  /**
   * Main thread: the nation's banner as image and design, drawn again only when it changed, since
   * both read the item and the design also the registry of the server.
   */
  private Drawn draw(Nation nation) {
    String base64 = nation.getBannerBase64();
    Drawn d = drawn.get(nation.getId());
    if (d != null && d.base64().equals(base64)) {
      return new Drawn(nation.getName(), base64, d.png(), d.design());
    }
    ItemStack item = nation.getBanner();
    String uri = BannerRenderer.renderBannerToDataURI(item);
    Object design = null;
    if (designs) {
      try {
        design = TownDesigns.design(item);
      } catch (RuntimeException e) {
        plugin.getLogger().warning("HeroicMap: Entwurf des Banners " + nation.getName() + ": " + e);
      }
    }
    d = new Drawn(nation.getName(), base64, uri == null ? null : png(uri), design);
    drawn.put(nation.getId(), d);
    return d;
  }

  /** Apply: the image of the nation's banner, uploaded when it changed; null if it fails. */
  private String upload(UUID id, Drawn d) {
    String path = bannerPath(id);
    if (d.base64().equals(banners.get(id))) {
      return path;
    }
    if (d.base64().equals(failed.get(id))) {
      return null;
    }
    // HeroicMap keeps 200 images per owner: 2 headings, the white banner and banners of 197
    // nations; more nations get the white one.
    if (!banners.containsKey(id) && banners.size() >= 197) {
      return null;
    }
    if (d.png() == null) {
      failed.put(id, d.base64());
      return null;
    }
    try {
      towns.layer.image(path, d.png());
    } catch (IllegalArgumentException e) {
      plugin.getLogger().warning("HeroicMap: Banner von " + d.name() + ": " + e);
      failed.put(id, d.base64());
      return null;
    }
    failed.remove(id);
    banners.put(id, d.base64());
    return path;
  }

  /** The PNG in a data URI of the BannerRenderer: 20 × 40 pixels, the front of the banner. */
  private static byte[] png(String dataUri) {
    return Base64.getDecoder().decode(dataUri.substring(dataUri.indexOf(',') + 1));
  }

  private static String bannerPath(UUID nationId) {
    return "images/banner-" + nationId + ".png";
  }

  /**
   * Apply: the design of the nation's banner, set on the layer when the banner changed, under the
   * UUID of the nation; the white one without a nation, without a banner ({@code d} null), when it
   * cannot be read, or when the layer holds 200 designs already. Only when HeroicMap takes designs.
   */
  private String design(UUID id, Drawn d) {
    if (d == null) {
      return TownDesigns.WHITE;
    }
    if (d.base64().equals(designed.get(id))) {
      return id.toString();
    }
    // 200 designs per layer: the white one and those of 197 nations, as with the images
    if (d.base64().equals(failedDesigns.get(id))
        || (!designed.containsKey(id) && designed.size() >= 197)) {
      return TownDesigns.WHITE;
    }
    if (d.design() == null) {
      failedDesigns.put(id, d.base64());
      return TownDesigns.WHITE;
    }
    try {
      TownDesigns.put(towns.layer, id.toString(), d.design());
    } catch (IllegalArgumentException e) {
      plugin.getLogger().warning("HeroicMap: Entwurf des Banners " + d.name() + ": " + e);
      failedDesigns.put(id, d.base64());
      return TownDesigns.WHITE;
    }
    failedDesigns.remove(id);
    designed.put(id, d.base64());
    return id.toString();
  }

  /** Removes the designs no town names any more, after the sync took them off. */
  private void removeDesignsExcept(Set<UUID> used) {
    for (UUID id : new ArrayList<>(designed.keySet())) {
      if (used.contains(id)) {
        continue;
      }
      try {
        TownDesigns.remove(towns.layer, id.toString());
        designed.remove(id);
      } catch (IllegalArgumentException e) {
        // still named by a banner the layer refused to replace; try again next time
      }
    }
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
