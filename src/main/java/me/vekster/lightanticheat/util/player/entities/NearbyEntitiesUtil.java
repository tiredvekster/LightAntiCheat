package me.vekster.lightanticheat.util.player.entities;

import me.vekster.lightanticheat.player.cache.entity.CachedEntity;
import me.vekster.lightanticheat.player.cooldown.element.EntityDistance;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.util.hook.server.paper.PaperUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.identifier.LACVersion;
import me.vekster.lightanticheat.version.identifier.VerIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.*;

public class NearbyEntitiesUtil {

    public static Set<Entity> getAllEntitiesAsyncWithoutCache(Player player) {
        if (FoliaUtil.isFolia() && !FoliaUtil.isOwnedByCurrentRegion(player))
            return ConcurrentHashMap.newKeySet();

        if ((VerIdentifier.getVersion().isOlderOrEqualsTo(LACVersion.V1_8) || !PaperUtil.isPaper()) && !FoliaUtil.isFolia()) {
            if (Bukkit.isPrimaryThread()) {
                try {
                    Set<Entity> result = ConcurrentHashMap.newKeySet();
                    result.addAll(player.getNearbyEntities(16, 24, 16));
                    return result;
                } catch (IllegalStateException exception) {
                    return ConcurrentHashMap.newKeySet();
                }
            }

            CompletableFuture<List<Entity>> future = new CompletableFuture<>();
            Scheduler.runTask(true, () -> {
                try {
                    future.complete(player.getNearbyEntities(16, 24, 16));
                } catch (IllegalStateException exception) {
                    future.complete(new ArrayList<>());
                }
            });
            try {
                Set<Entity> result = ConcurrentHashMap.newKeySet();
                result.addAll(future.get(250, TimeUnit.MILLISECONDS));
                return result;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return ConcurrentHashMap.newKeySet();
            }
        }

        Location baseLocation = player.getLocation();
        Set<Entity> result = ConcurrentHashMap.newKeySet();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                try {
                    Location chunkLocation = baseLocation.clone().add(x * 16, 0, z * 16);
                    Entity[] entities;
                    World world = AsyncUtil.getWorld(chunkLocation);
                    if (world == null) world = chunkLocation.getWorld();
                    int chunkX = chunkLocation.getBlockX() >> 4;
                    int chunkZ = chunkLocation.getBlockZ() >> 4;
                    if (FoliaUtil.isFolia() && !FoliaUtil.isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
                        continue;
                    }
                    if (world.isChunkLoaded(chunkX, chunkZ)) {
                        entities = world.getChunkAt(chunkX, chunkZ).getEntities();
                    } else {
                        continue;
                    }
                    if (entities == null || entities.length == 0) continue;
                    for (Entity entity : entities.clone()) {
                        if (entity == null) continue;
                        result.add(entity);
                    }
                } catch (NoSuchElementException | IllegalStateException | NullPointerException ignored) {
                }
            }
        }
        result.remove(player);
        return result;
    }

    public static Set<CachedEntity> selectNearbyEntities(Player player, Set<Entity> entities, EntityDistance type) {
        Set<CachedEntity> result = ConcurrentHashMap.newKeySet();
        if (FoliaUtil.isFolia() && !FoliaUtil.isOwnedByCurrentRegion(player))
            return result;

        Location location = player.getLocation();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        for (Entity entity : entities) {
            if (entity == null)
                continue;
            if (FoliaUtil.isFolia() && !FoliaUtil.isOwnedByCurrentRegion(entity))
                continue;

            Location entityLocation = entity.getLocation();
            double entityX = entityLocation.getX();
            double entityY = entityLocation.getY();
            double entityZ = entityLocation.getZ();
            if (type == EntityDistance.VERY_NEARBY &&
                    Math.abs(x - entityX) <= 1.25 && Math.abs(y - entityY) <= 2.25 && Math.abs(z - entityZ) <= 1.25)
                result.add(new CachedEntity(entity));
            if (type == EntityDistance.NEARBY &&
                    Math.abs(x - entityX) <= 3.25 && Math.abs(y - entityY) <= 4.25 && Math.abs(z - entityZ) <= 3.25)
                result.add(new CachedEntity(entity));
        }
        return result;
    }

}
