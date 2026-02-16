package me.vekster.lightanticheat.version.identifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * String-key registry for content introduced in Minecraft 1.21+.
 * Keys are lowercase Bukkit enum-style names and are used as compatibility hints
 * for future checks and integrations.
 */
public final class MinecraftFeatureRegistry {

    private MinecraftFeatureRegistry() {
    }

    public static final Set<String> MC_121_MOBS = unmodifiable(
            "breeze", "bogged", "creaking", "copper_golem", "nautilus",
            "zombie_nautilus", "coral_zombie_nautilus", "camel_husk", "parched",
            "happy_ghast", "ghastling"
    );

    public static final Set<String> MC_121_MACE_AND_SPEAR_ENCHANTS = unmodifiable(
            "density", "breach", "wind_burst", "lunge"
    );

    public static final Set<String> MC_121_STATUS_EFFECTS = unmodifiable(
            "infested", "oozing", "weaving", "wind_charged", "raid_omen", "trial_omen",
            "breath_of_the_nautilus"
    );

    public static final Set<String> MC_121_WORLD_FEATURES = unmodifiable(
            "trial_chambers", "pale_garden", "fallen_trees"
    );

    private static Set<String> unmodifiable(String... values) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(values)));
    }
}
