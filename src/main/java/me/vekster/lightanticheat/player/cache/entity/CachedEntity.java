package me.vekster.lightanticheat.player.cache.entity;

import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class CachedEntity {

    public CachedEntity(Entity entity) {
        uuid = entity.getUniqueId();
        entityType = entity.getType();
        width = VerUtil.getWidth(entity);
        height = VerUtil.getHeight(entity);
    }

    public final UUID uuid;
    public final EntityType entityType;
    public final double width;
    public final double height;

}
