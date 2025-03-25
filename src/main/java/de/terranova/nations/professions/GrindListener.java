package de.terranova.nations.professions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class GrindListener implements Listener {

    // ------------------------------------------------------------------------
    // 1) BLOCK BREAK & HARVEST
    // ------------------------------------------------------------------------
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material mat = block.getType();

        // Falls eine Feldfrucht nicht ausgewachsen ist -> keine Ernte
        if (isCrop(mat) && !isFullyGrown(block)) {
            return;
        }

        // Beispiel: immer "DESTROY" auslösen
        ObjectiveManager.handleEvent(player, "DESTROY", mat.name(), 1);

        // Falls es eine Erntepflanze ist, zusätzlich "HARVEST"
        if (isCrop(mat)) {
            ObjectiveManager.handleEvent(player, "HARVEST", mat.name(), 1);
        }
    }

    private boolean isCrop(Material mat) {
        return switch (mat) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART -> true;
            default -> false;
        };
    }

    private boolean isFullyGrown(Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Ageable ageable) {
            return ageable.getAge() == ageable.getMaximumAge();
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // 2) CRAFTING
    // ------------------------------------------------------------------------
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Material mat = event.getRecipe().getResult().getType();
        int amountCrafted = event.getRecipe().getResult().getAmount();

        if (event.isShiftClick()) {
            amountCrafted = computeShiftClickAmount(event);
        }

        ObjectiveManager.handleEvent(player, "CRAFT", mat.name(), amountCrafted);
    }

    private int computeShiftClickAmount(CraftItemEvent event) {
        int perCraft = event.getRecipe().getResult().getAmount();
        ItemStack[] matrix = event.getInventory().getMatrix().clone();
        int maxCrafts = Integer.MAX_VALUE;

        for (ItemStack slot : matrix) {
            if (slot == null || slot.getType().isAir()) continue;
            int slotCount = slot.getAmount();
            // Hier ggf. genauer berechnen, wie viel vom Slot pro Craft verbraucht wird
            int craftsFromSlot = slotCount / 1;
            if (craftsFromSlot < maxCrafts) {
                maxCrafts = craftsFromSlot;
            }
        }
        int totalItems = maxCrafts * perCraft;
        return (totalItems > 0) ? totalItems : perCraft;
    }

    // ------------------------------------------------------------------------
    // 3) FURNACE EXTRACTION => SMELT
    // ------------------------------------------------------------------------
    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getItemType();
        int amount = event.getItemAmount();

        ObjectiveManager.handleEvent(player, "SMELT", mat.name(), amount);
    }

    // ------------------------------------------------------------------------
    // 4) FISHING
    // ------------------------------------------------------------------------
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        switch (event.getState()) {
            case CAUGHT_FISH, CAUGHT_ENTITY -> {
                Entity caught = event.getCaught();
                if (caught instanceof Item itemEntity) {
                    ItemStack caughtStack = itemEntity.getItemStack();
                    Material caughtMat = caughtStack.getType();
                    int amount = caughtStack.getAmount();
                    ObjectiveManager.handleEvent(player, "FISH", caughtMat.name(), amount);
                }
            }
            default -> {}
        }
    }

    // ------------------------------------------------------------------------
    // 5) ENTITY DEATH => KILL
    // ------------------------------------------------------------------------
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();

        EntityType type = event.getEntityType();

        // "KILL" => in config: z. B. "action: KILL" und "object: ZOMBIE"
        // oder "object: HOSTILE_MOB"
        ObjectiveManager.handleEvent(player, "KILL", type.name(), 1);
    }
}
