package me.vekster.lightanticheat.player.cache.entity;

import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class CachedEntity {

    public CachedEntity(Entity entity) {
        uuid = entity.getUniqueId();
        entityType = entity.getType();

        double cachedWidth = -1;
        double cachedHeight = -1;
        if (!FoliaUtil.isFolia() || FoliaUtil.isOwnedByCurrentRegion(entity)) {
            cachedWidth = VerUtil.getWidth(entity);
            cachedHeight = VerUtil.getHeight(entity);
        }

        width = cachedWidth;
        height = cachedHeight;
    }

    public final UUID uuid;
    public final EntityType entityType;
    public final double width;
    public final double height;

}
