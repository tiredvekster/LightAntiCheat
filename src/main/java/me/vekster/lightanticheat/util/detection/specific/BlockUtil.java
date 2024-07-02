package me.vekster.lightanticheat.util.detection.specific;

import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import me.vekster.lightanticheat.version.identifier.LACVersion;
import me.vekster.lightanticheat.version.identifier.VerIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BlockUtil {

    public static Set<Block> getWithinBlocksClearly(Entity player, Location location) {
        World world = AsyncUtil.getWorld(player);
        if (world == null) return new HashSet<>();
        Location startPoint = location.clone().subtract(VerUtil.getWidth(player) / 2.0, 0, VerUtil.getWidth(player) / 2.0);
        Location endPoint = location.clone().add(VerUtil.getWidth(player) / 2.0, VerUtil.getHeight(player), VerUtil.getWidth(player) / 2.0);
        Set<Block> blocks = new HashSet<>();
        for (int x = startPoint.getBlockX(); x <= (endPoint.getX() % 1.0 == 0 ? endPoint.getBlockX() - 1 : endPoint.getBlockX()); x++) {
            for (int z = startPoint.getBlockZ(); z <= (endPoint.getZ() % 1.0 == 0 ? endPoint.getBlockZ() - 1 : endPoint.getBlockZ()); z++) {
                Block block = AsyncUtil.getBlockAt(world, x, startPoint.getBlockY(), z);
                if (block != null) blocks.add(block);
            }
        }
        Set<Block> higherBlocks = new HashSet<>();
        for (int y = startPoint.getBlockY() + 1; y <= (endPoint.getY() % 1.0 == 0 ? endPoint.getBlockY() - 1 : endPoint.getBlockY()); y++)
            for (Block block : blocks)
                higherBlocks.add(block.getRelative(BlockFace.UP, y - startPoint.getBlockY()));
        blocks.addAll(higherBlocks);
        return blocks;
    }

    public static Set<Block> getWithinBlocks(Entity player, Location location) {
        if (VerIdentifier.getVersion().isNewerThan(LACVersion.V1_8) || FoliaUtil.isFolia() || Bukkit.isPrimaryThread())
            return getWithinBlocksClearly(player, location);
        CompletableFuture<Set<Block>> result = new CompletableFuture<>();
        Scheduler.runTask(true, () -> {
            result.complete(getWithinBlocksClearly(player, location));
        });
        try {
            return result.get(250, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return getWithinBlocksClearly(player, location);
        }
    }

    public static Set<Block> getWithinBlocks(Entity player) {
        return getWithinBlocks(player, player.getLocation());
    }

    private static Set<Block> getInteractiveBlocksClearly(Entity player, Location location, boolean removeCorners) {
        World world = AsyncUtil.getWorld(player);
        if (world == null) return new HashSet<>();
        Location startPoint = location.clone().subtract(VerUtil.getWidth(player) / 2.0, 0, VerUtil.getWidth(player) / 2.0);
        Location endPoint = location.clone().add(VerUtil.getWidth(player) / 2.0, VerUtil.getHeight(player), VerUtil.getWidth(player) / 2.0);
        Set<Block> blocks = new HashSet<>();
        int startX = startPoint.getBlockX() - 1;
        int endX = (endPoint.getX() % 1.0 == 0 ? endPoint.getBlockX() - 1 : endPoint.getBlockX()) + 1;
        int startZ = startPoint.getBlockZ() - 1;
        int endZ = (endPoint.getZ() % 1.0 == 0 ? endPoint.getBlockZ() - 1 : endPoint.getBlockZ()) + 1;
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                if (removeCorners && (z == startZ || z == endZ) && (x == startX || x == endX))
                    continue;
                Block block = AsyncUtil.getBlockAt(world, x, startPoint.getBlockY(), z);
                if (block != null) blocks.add(block);
                if (location.getY() % 1.0 != 0) {
                    block = AsyncUtil.getBlockAt(world, x, startPoint.getBlockY() + 1, z);
                    if (block != null) blocks.add(block);
                }
            }
        }
        return blocks;
    }

    private static Set<Block> getInteractiveBlocks(Entity player, Location location, boolean removeCorners) {
        if (VerIdentifier.getVersion().isNewerThan(LACVersion.V1_8) || FoliaUtil.isFolia() || Bukkit.isPrimaryThread())
            return getInteractiveBlocksClearly(player, location, removeCorners);
        CompletableFuture<Set<Block>> result = new CompletableFuture<>();
        Scheduler.runTask(true, () -> {
            result.complete(getInteractiveBlocksClearly(player, location, removeCorners));
        });
        try {
            return result.get(250, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return getInteractiveBlocksClearly(player, location, removeCorners);
        }
    }

    public static Set<Block> getInteractiveBlocks(Entity player, Location location) {
        return getInteractiveBlocks(player, location, true);
    }

    public static Set<Block> getInteractiveBlocks(Entity player) {
        return getInteractiveBlocks(player, player.getLocation());
    }

    public static Set<Block> getCollisionBlockLayer(Entity player, Location location) {
        return getInteractiveBlocks(player, location, false);
    }

    public static Set<Block> getCollisionBlockLayer(Entity player) {
        return getInteractiveBlocks(player, player.getLocation(), false);
    }

    public static Set<Block> getDownBlocksClearly(Entity player, Location location, double padding) {
        World world = AsyncUtil.getWorld(player);
        if (world == null) return new HashSet<>();
        Location startPoint = location.clone().subtract(VerUtil.getWidth(player) / 2.0 + padding, 0, VerUtil.getWidth(player) / 2.0 + padding);
        Location endPoint = location.clone().add(VerUtil.getWidth(player) / 2.0 + padding, VerUtil.getHeight(player), VerUtil.getWidth(player) / 2.0 + padding);
        Set<Block> blocks = new HashSet<>();
        for (int x = startPoint.getBlockX(); x <= (endPoint.getX() % 1.0 == 0 ? endPoint.getBlockX() - 1 : endPoint.getBlockX()); x++) {
            for (int z = startPoint.getBlockZ(); z <= (endPoint.getZ() % 1.0 == 0 ? endPoint.getBlockZ() - 1 : endPoint.getBlockZ()); z++) {
                Block block = AsyncUtil.getBlockAt(world, x, startPoint.getBlockY() - (location.getY() % 1 == 0 ? 1 : 0), z);
                if (block != null) blocks.add(block);
            }
        }
        return blocks;
    }

    public static Set<Block> getDownBlocks(Entity player, Location location, double padding) {
        if (VerIdentifier.getVersion().isNewerThan(LACVersion.V1_8) || FoliaUtil.isFolia() || Bukkit.isPrimaryThread())
            return getDownBlocksClearly(player, location, padding);
        CompletableFuture<Set<Block>> result = new CompletableFuture<>();
        Scheduler.runTask(true, () -> {
            result.complete(getDownBlocksClearly(player, location, padding));
        });
        try {
            return result.get(250, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return getDownBlocksClearly(player, location, padding);
        }
    }

    public static Set<Block> getDownBlocks(Entity player, double padding) {
        return getDownBlocks(player, player.getLocation(), padding);
    }

}
