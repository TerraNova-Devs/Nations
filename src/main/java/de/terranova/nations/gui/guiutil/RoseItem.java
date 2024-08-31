package de.terranova.nations.gui.guiutil;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.th0rgal.oraxen.api.OraxenItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class RoseItem {

    public ItemStack stack;
    private Consumer<InventoryClickEvent> clickAction;
    private Consumer<InventoryDragEvent> dragAction;

    private RoseItem(Builder builder) {
        ItemStack stack = new ItemStack(builder.material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(builder.displayname);
        if (builder.lore != null) meta.lore(builder.lore);
        if (builder.isEnchanted) meta.setEnchantmentGlintOverride(true);
        if(builder.skullTexture != null) mutateSkullMetaSkinBy64(builder.skullTexture,(SkullMeta) meta);
        stack.setItemMeta(meta);
        this.stack = stack;
        this.dragAction = event -> {
        };
        this.clickAction = event -> {
        };
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

    public static class Builder {

        ItemStack material;
        Component displayname;
        List<Component> lore = new ArrayList<>();
        boolean isEnchanted;
        String skullTexture;

        public Builder material(String material) {
            if (OraxenItems.exists(material)) {
                this.material = OraxenItems.getItemById(material).build();
            } else {
                this.material = new ItemStack(Material.valueOf(material));
            }
            return this;
        }

        public Builder material(Material material) {
            this.material = new ItemStack(material);
            return this;
        }

        public Builder displayName(String displayname) {
            this.displayname = Component.text(displayname).decoration(TextDecoration.ITALIC, false);
            return this;
        }

        public Builder displayName(Component displayname) {
            this.displayname = displayname.decoration(TextDecoration.ITALIC, false);
            return this;
        }

        public Builder addLore(String lore) {
            this.lore.add(Component.text(lore).decoration(TextDecoration.ITALIC, false));
            return this;
        }

        public Builder addLore(Component lore) {
            this.lore.add(lore.decoration(TextDecoration.ITALIC, false));
            return this;
        }

        public Builder addLore(Component... lore) {
            for (Component text : lore) this.lore.add(text.decoration(TextDecoration.ITALIC, false));
            return this;
        }

        public Builder addLore(String... lore) {
            for (String text : lore) this.lore.add(Component.text(text).decoration(TextDecoration.ITALIC, false));
            return this;
        }

        public Builder isEnchanted(boolean isenchanted) {
            this.isEnchanted = isenchanted;
            return this;
        }

        public Builder setSkull(String texture) {
            this.material = new ItemStack(Material.PLAYER_HEAD);
            this.skullTexture = texture;
            return this;
        }

        public RoseItem build() {
            return new RoseItem(this);
        }

    }

    private static Method metaSetProfileMethod;

    private void mutateSkullMetaSkinBy64(String b64, SkullMeta skullMeta) {
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

}
