package de.terranova.nations.gui.guiutil;

import de.mcterranova.bona.lib.chat.Chat;
import io.th0rgal.oraxen.api.OraxenItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class roseItem {

    public ItemStack stack;

    private roseItem(Builder builder) {
        ItemStack stack = new ItemStack(builder.material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(builder.displayname);
        if(builder.lore != null) meta.lore(builder.lore);
        if(builder.isenchanted) meta.setEnchantmentGlintOverride(true);
        stack.setItemMeta(meta);
        this.stack = stack;
    }

    public static class Builder {

        ItemStack material;
        Component displayname;
        List<Component> lore = new ArrayList<>();
        boolean isenchanted;

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

        public Builder isEnchanted(boolean isenchanted) {
            this.isenchanted = isenchanted;
            return this;
        }

        public roseItem build(){
            return new roseItem(this);
        }
    }

}
