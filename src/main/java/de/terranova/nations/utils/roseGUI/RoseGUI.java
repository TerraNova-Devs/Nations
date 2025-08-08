package de.terranova.nations.utils.roseGUI;

import static org.bukkit.Bukkit.createInventory;

import java.sql.SQLException;
import java.util.*;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;

public abstract class RoseGUI implements InventoryHolder {

  public static final HashMap<UUID, RoseGUI> players = new HashMap<>();
  public final Player player;
  private final Map<Integer, RoseItem> registeredIcons;
  private final List<BukkitTask> taskList = new ArrayList<>();
  private final String id;
  private final InventoryType inventoryType;
  private final String title;
  private final int size;
  private Inventory inventory;
  private boolean isClosed = false;

  public RoseGUI(
      @Nonnull Player player, @Nonnull String id, Component title, @Nonnegative int rows) {
    this.size = rows * 9;
    this.registeredIcons = new HashMap<>(this.size);
    this.player = player;
    this.title =
        LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build()
            .serialize(title);
    this.id = id;
    this.inventoryType = InventoryType.CHEST;
  }

  public void open() {
    final RoseGUI currentGui = players.get(player.getUniqueId());
    if (currentGui != null) {
      // call Bukkit's inventory close event
      Bukkit.getPluginManager().callEvent(new InventoryCloseEvent(this.player.getOpenInventory()));
    }

    players.put(this.player.getUniqueId(), this);
    this.inventory = createInventory(null, this.size, this.title);
    this.player.openInventory(inventory);
  }

  public void fillGui(RoseItem icon) {
    for (int slot = 0; slot < size; slot++) {
      this.addItem(slot, icon);
    }
  }

  public void outlineGui(RoseItem icon) {
    this.addItem(
        icon, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51,
        52, 53);
  }

  public void inlineGui(RoseItem icon) {
    this.addItem(
        icon, 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34,
        38, 39, 40, 41, 42, 43);
  }

  public void addItem(@Nonnegative int slot, @Nullable RoseItem icon) {
    if (this.inventory.getSize() <= slot) {
      throw new IndexOutOfBoundsException(
          "Slot cannot be bigger than inventory size! [ "
              + slot
              + " >= "
              + this.inventory.getSize()
              + " ]");
    }

    this.registeredIcons.put(slot, icon);
    this.inventory.setItem(slot, (icon == null ? null : icon.stack));
  }

  public void addItem(@Nullable RoseItem item, @Nonnull Integer... slots) {
    for (int slot : slots) {
      this.addItem(slot, item);
    }
  }

  @Nonnull
  public Map<Integer, RoseItem> getItems() {
    return registeredIcons;
  }

  public void onOpen(InventoryOpenEvent event) throws SQLException {}

  public void onClose(InventoryCloseEvent event) {
    final RoseGUI gui = getGuiFromInventory(event.getPlayer().getOpenInventory().getTopInventory());
    if (gui == null) return;
    if (!gui.equals(this)) return;
    gui.stopAllTasks();
  }

  @Nullable
  public RoseGUI getGuiFromInventory(final Inventory inventory) {
    return players.values().stream()
        .filter(gui -> gui.getInventory().equals(inventory))
        .findFirst()
        .orElse(null);
  }

  public void setClosed(boolean closed) {
    this.isClosed = closed;
  }

  public void stopAllTasks() {
    taskList.forEach(BukkitTask::cancel);
    taskList.clear();
  }

  public boolean onClick(InventoryClickEvent event) {
    return false;
  }

  public boolean onDrag(InventoryDragEvent event) {
    return false;
  }

  @Override
  @Nonnull
  public Inventory getInventory() {
    return this.inventory;
  }
}
