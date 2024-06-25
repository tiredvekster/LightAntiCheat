package me.vekster.lightanticheat.util.detection.specific;

import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.entity.CachedEntity;
import me.vekster.lightanticheat.util.annotation.SecureAsync;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.detection.LeanTowards;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.*;

public class GroundUtil extends BlockUtil {

    private static final Set<Float> BLOCK_HEIGHTS = ConcurrentHashMap.newKeySet();
    public static final float LOWEST_BLOCK_HEIGHT;
    public static final float HIGHEST_BLOCK_HEIGHT;


    static {
        Set<Double> blockHeights = new HashSet<>(Arrays.asList(
                0.0, 0.0625, 0.09375, 0.125,
                0.1875, 0.25, 0.3125, 0.375,
                0.4375, 0.5, 0.5625, 0.59375,
                0.625, 0.6875, 0.75, 0.8125,
                0.875, 0.9375
        ));
        blockHeights.forEach(aDouble -> BLOCK_HEIGHTS.add(aDouble.floatValue()));
        blockHeights.remove(0.0);
        LOWEST_BLOCK_HEIGHT = Collections.min(blockHeights).floatValue();
        HIGHEST_BLOCK_HEIGHT = Collections.max(blockHeights).floatValue();
    }

    public static double getBlockY(double y) {
        y %= 1.0;
        return (y >= 0.0) ? (Math.abs(y)) : (Math.abs(y + 1.0));
    }

    public static boolean isBlockHeight(float height) {
        return BLOCK_HEIGHTS.contains(height);
    }

    @SecureAsync
    public static boolean isOnGround(Player player, Set<Block> downBlocks, PlayerCache cache, LeanTowards leanTowards) {
        if (leanTowards == LeanTowards.TRUE && isOnEntity(player, cache))
            return true;
        return isOnBlock(player, downBlocks);
    }

    @SecureAsync
    public static boolean isOnGround(Entity entity, Set<Block> downBlocks, LeanTowards leanTowards, boolean async) {
        if (leanTowards == LeanTowards.TRUE && isOnEntity(entity, async))
            return true;
        return isOnBlock(entity, downBlocks);
    }

    @SecureAsync
    public static boolean isOnGround(Player player, double padding, PlayerCache cache, LeanTowards leanTowards) {
        if (leanTowards == LeanTowards.TRUE && isOnEntity(player, cache))
            return true;
        return isOnBlock(player, BlockUtil.getDownBlocks(player, padding));
    }

    @SecureAsync
    public static boolean isOnGround(Entity entity, double padding, LeanTowards leanTowards, boolean async) {
        if (leanTowards == LeanTowards.TRUE && isOnEntity(entity, async))
            return true;
        return isOnBlock(entity, BlockUtil.getDownBlocks(entity, padding));
    }

    private static boolean isOnEntity(Player player, PlayerCache cache) {
        Set<CachedEntity> nearbyEntities = cache.entitiesVeryNearby;
        if (nearbyEntities.isEmpty())
            return false;
        double y = player.getLocation().getY();
        for (CachedEntity nearbyEntity : nearbyEntities) {
            if (nearbyEntity.entityType == EntityType.PLAYER || nearbyEntity.width < 0.55 || nearbyEntity.height < 0.5)
                continue;
            if (BLOCK_HEIGHTS.contains((float) (getBlockY(y - nearbyEntity.width))))
                return true;
        }
        return false;
    }

    private static boolean isOnEntity(Entity entity, boolean async) {
        List<Entity> nearbyEntities;
        if (!async) {
            nearbyEntities = AsyncUtil.getNearbyEntities(entity, 0, 1, 0);
        } else {
            CompletableFuture<List<Entity>> futureNearbyEntities = new CompletableFuture<>();
            Scheduler.runTask(true, () -> {
                futureNearbyEntities.complete(AsyncUtil.getNearbyEntities(entity, 0, 1, 0));
            });
            try {
                nearbyEntities = futureNearbyEntities.get(250, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return false;
            }
        }
        if (nearbyEntities.isEmpty())
            return false;
        double y = entity.getLocation().getY();
        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity.getType() == EntityType.PLAYER || VerUtil.getWidth(nearbyEntity) < 0.55 || VerUtil.getHeight(nearbyEntity) < 0.5)
                continue;
            if (BLOCK_HEIGHTS.contains((float) (getBlockY(y - VerUtil.getHeight(nearbyEntity)))))
                return true;
        }
        return false;
    }

    private static boolean isOnBlock(Entity entity, Set<Block> downBlocks) {
        boolean notPassable = false;
        boolean occluding = true;
        for (Block block : downBlocks) {
            String downBlockName = block.getRelative(BlockFace.DOWN).getType().name().toLowerCase();
            if (!VerUtil.isPassable(block) || downBlockName.endsWith("_wall") || downBlockName.endsWith("_fence") ||
                    downBlockName.endsWith("_fence_gate") || downBlockName.endsWith("shulker_box") ||
                    downBlockName.endsWith("_door")) {
                notPassable = true;
                if (!occluding) break;
            }
            if (!block.getType().isOccluding()) {
                occluding = false;
                if (notPassable) break;
            }
        }
        if (notPassable) {
            if (occluding) {
                return getBlockY(entity.getLocation().getY()) == 0;
            } else {
                return BLOCK_HEIGHTS.contains((float) getBlockY(entity.getLocation().getY()));
            }
        }
        return false;
    }

}
