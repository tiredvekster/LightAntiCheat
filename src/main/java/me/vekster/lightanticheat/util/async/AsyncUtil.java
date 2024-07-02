package me.vekster.lightanticheat.util.async;

import me.vekster.lightanticheat.util.annotation.SecureAsync;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.identifier.LACVersion;
import me.vekster.lightanticheat.version.identifier.VerIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncUtil {

    private static boolean isUnstableLegacy() {
        return VerIdentifier.getVersion().isOlderOrEqualsTo(LACVersion.V1_8);
    }

    @Nullable
    @SecureAsync
    public static World getWorld(Entity entity) throws RuntimeException {
        if (!isUnstableLegacy() || FoliaUtil.isFolia() || Bukkit.isPrimaryThread())
            return entity.getWorld();

        CompletableFuture<World> future = new CompletableFuture<>();
        Scheduler.runTask(true, () -> future.complete(entity.getWorld()));
        try {
            return future.get(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }

    @Nullable
    @SecureAsync
    public static World getWorld(Location location) {
        if (!isUnstableLegacy() || FoliaUtil.isFolia() || Bukkit.isPrimaryThread())
            return location.getWorld();

        CompletableFuture<World> future = new CompletableFuture<>();
        Scheduler.runTask(true, () -> future.complete(location.getWorld()));
        try {
            return future.get(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return null;
        }
    }

    @Nullable
    @SecureAsync
    public static Block getBlock(Location location) {
        if (Bukkit.isPrimaryThread() || FoliaUtil.isFolia())
            return location.getBlock();

        if (isUnstableLegacy()) {
            CompletableFuture<Block> future = new CompletableFuture<>();
            Scheduler.runTask(true, () -> future.complete(location.getBlock()));
            try {
                return future.get(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return null;
            }
        }

        World world = AsyncUtil.getWorld(location);
        if (world == null) return null;
        if (world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return world.getBlockAt(location);
        } else {
            return null;
        }
    }

    @Nullable
    @SecureAsync
    public static Block getBlockAt(World world, int x, int y, int z) {
        if (Bukkit.isPrimaryThread() || FoliaUtil.isFolia())
            return world.getBlockAt(x, y, z);

        if (isUnstableLegacy()) {
            CompletableFuture<Block> future = new CompletableFuture<>();
            Scheduler.runTask(true, () -> future.complete(world.getBlockAt(x, y, z)));
            try {
                return future.get(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return null;
            }
        }

        if (world.isChunkLoaded(x >> 4, z >> 4)) {
            return world.getBlockAt(x, y, z);
        } else {
            return null;
        }
    }

    @Nullable
    @SecureAsync
    public static Block getBlockAt(World world, Location location) {
        if (Bukkit.isPrimaryThread() || FoliaUtil.isFolia())
            return world.getBlockAt(location);

        if (isUnstableLegacy()) {
            CompletableFuture<Block> future = new CompletableFuture<>();
            Scheduler.runTask(true, () -> future.complete(world.getBlockAt(location)));
            try {
                return future.get(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return null;
            }
        }

        if (world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return world.getBlockAt(location);
        } else {
            return null;
        }
    }

    @SecureAsync
    public static List<Entity> getNearbyEntities(Entity entity, double x, int y, int z) {
        if (Bukkit.isPrimaryThread() || FoliaUtil.isFolia()) {
            if (VerIdentifier.getVersion().isNewerThan(LACVersion.V1_8)) {
                return entity.getNearbyEntities(x, y, z);
            } else {
                try {
                    return entity.getNearbyEntities(x, y, z);
                } catch (IllegalStateException exception) {
                    return new ArrayList<>();
                }
            }
        }

        CompletableFuture<List<Entity>> future = new CompletableFuture<>();
        Scheduler.runTask(true, () -> {
            if (VerIdentifier.getVersion().isNewerThan(LACVersion.V1_8)) {
                future.complete(entity.getNearbyEntities(x, y, z));
            } else {
                try {
                    future.complete(entity.getNearbyEntities(x, y, z));
                } catch (IllegalStateException exception) {
                    future.complete(new ArrayList<>());
                }
            }
        });
        try {
            return future.get(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new ArrayList<>();
        }
    }


}
