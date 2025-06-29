package de.terranova.nations.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * A placeholder can provide a list of possible tab completions,
 * given a CommandSender (which may or may not be a Player).
 */
@FunctionalInterface
public interface PlayerAwarePlaceholder {
    static PlayerAwarePlaceholder ofStatic(java.util.function.Supplier<List<String>> supplier) {
        return sender -> supplier.get();
    }

    static PlayerAwarePlaceholder ofPlayerFunction(java.util.function.Function<java.util.UUID, List<String>> function) {
        return sender -> {
            if (sender instanceof Player player) {
                return function.apply(player.getUniqueId());
            }
            return Collections.emptyList();
        };
    }

    /**
     * New convenience factory method to return a cached placeholder
     * that delegates to a Function<UUID, List<String>>, with a specified TTL.
     */
    static PlayerAwarePlaceholder ofCachedPlayerFunction(Function<UUID, List<String>> function, long cacheDurationMillis) {
        return new CachedUUIDPlaceholder(function, cacheDurationMillis);
    }

    List<String> getSuggestions(CommandSender sender);
}
