package com.kireiko.utils.world;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.block.Block;

@UtilityClass
public final class BlockUtil {

    public Block getBlockAsync(final Location location) {
        if (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return location.getWorld().getBlockAt(location);
        } else {
            return null;
        }
    }
}
