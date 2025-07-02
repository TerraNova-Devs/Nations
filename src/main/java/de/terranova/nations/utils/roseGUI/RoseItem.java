package de.terranova.nations.utils.roseGUI;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.nexomc.nexo.api.NexoItems;
import de.terranova.nations.utils.persistentData.PersistentNationsDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class RoseItem {

    private static Method metaSetProfileMethod;
    public ItemStack stack;
    private Consumer<InventoryClickEvent> clickAction;
    private Consumer<InventoryDragEvent> dragAction;
    private UUID uuid;

    private RoseItem(Builder builder) {
        ItemStack stack = new ItemStack(builder.builderStack);
        ItemMeta meta = stack.getItemMeta();
        if (builder.displayname != null) meta.displayName(builder.displayname);
        if (builder.lore != null) meta.lore(builder.lore);
        if (builder.isEnchanted) meta.setEnchantmentGlintOverride(true);
        if (builder.plugin != null) {
            NamespacedKey key = new NamespacedKey(builder.plugin, "uuid");
            this.uuid = UUID.randomUUID();
            meta.getPersistentDataContainer().set(key, PersistentNationsDataType.UUID, uuid);
        }
        stack.setItemMeta(meta);
        this.stack = stack;
        this.dragAction = event -> {
        };
        this.clickAction = event -> {
        };

    }

    private static void mutateSkullMetaSkinBy64(String b64, SkullMeta skullMeta) {
        try {
            metaSetProfileMethod = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            metaSetProfileMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        UUID id = new UUID(b64.substring(b64.length() - 20).hashCode(), b64.substring(b64.length() - 10).hashCode());
        GameProfile profile = new GameProfile(id, "Player");
        profile.getProperties().put("textures", new Property("textures", b64));
        try {
            metaSetProfileMethod.invoke(skullMeta, profile);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public Consumer<InventoryClickEvent> getClickAction() {
        return clickAction;
    }

    @Nonnull
    public RoseItem onClick(Consumer<InventoryClickEvent> clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    @Nonnull
    public Consumer<InventoryDragEvent> getDragAction() {
        return dragAction;

    }

    @Nonnull
    public RoseItem onDrag(Consumer<InventoryDragEvent> dragAction) {
        this.dragAction = dragAction;
        return this;
    }

    public UUID getUUID() {
        return uuid;
    }

    public static class Builder {

        ItemStack builderStack;
        Component displayname;
        List<Component> lore = new ArrayList<>();
        boolean isEnchanted;
        JavaPlugin plugin;

        public Builder material(String material) {
            if (NexoItems.exists(material)) {
                this.builderStack = Objects.requireNonNull(NexoItems.itemFromId(material)).build();
            } else {
                this.builderStack = new ItemStack(Material.valueOf(material));
            }
            return this;
        }

        public Builder material(Material material) {
            this.builderStack = new ItemStack(material);
            return this;
        }

        public Builder copyStack(ItemStack stack) {
            this.builderStack = new ItemStack(stack);
            return this;
        }

        public Builder displayName(String displayname) {
            this.displayname = MiniMessage.miniMessage().deserialize(displayname).decoration(TextDecoration.ITALIC, false);
            return this;
        }

        public Builder displayName(Component displayname) {
            this.displayname = displayname.decoration(TextDecoration.ITALIC, false);
            return this;
        }

        public Builder addLore(String lore) {
            if(lore == null) return this;
            this.lore.add(MiniMessage.miniMessage().deserialize(lore).decoration(TextDecoration.ITALIC, false));
            return this;
        }

        public Builder addLore(Component lore) {
            if(lore == null) return this;
            this.lore.add(lore.decoration(TextDecoration.ITALIC, false));
            return this;
        }

        public Builder addLore(Component... lore) {
            for (Component text : lore) this.lore.add(text.decoration(TextDecoration.ITALIC, false));
            return this;
        }

        public Builder addLore(String... lore) {
            for (String text : lore)
                this.lore.add(MiniMessage.miniMessage().deserialize(text).decoration(TextDecoration.ITALIC, false));
            return this;
        }

        public Builder isEnchanted(boolean isenchanted) {
            this.isEnchanted = isenchanted;
            return this;
        }

        public Builder setSkull(String texture) {
            this.builderStack = new ItemStack(Material.PLAYER_HEAD);
            mutateSkullMetaSkinBy64(texture, (SkullMeta) this.builderStack.getItemMeta());
            return this;
        }

        public Builder setCompass(Location location) {
            this.builderStack = new ItemStack(Material.COMPASS);
            CompassMeta meta = (CompassMeta) this.builderStack.getItemMeta();
            meta.setLodestoneTracked(false);
            meta.setLodestone(location);
            this.builderStack.setItemMeta(meta);
            return this;
        }

        public Builder generateUUID(JavaPlugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder isCraftable(JavaPlugin plugin, boolean isCraft) {
            NamespacedKey key = new NamespacedKey(plugin, "craft");
            ItemMeta meta = this.builderStack.getItemMeta();
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, isCraft);
            this.builderStack.setItemMeta(meta);
            return this;
        }

        public RoseItem build() {
            return new RoseItem(this);
        }

    }


}
