package me.vekster.lightanticheat.check.checks.combat.criticals;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.checks.combat.CombatCheck;
import me.vekster.lightanticheat.event.playerattack.LACAsyncPlayerAttackEvent;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.detection.LeanTowards;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MiniJump mode
 */
public class CriticalsB extends CombatCheck implements Listener {
    public CriticalsB() {
        super(CheckName.CRITICALS_B);
    }

    private static Map<UUID, List<Integer>> HEIGHTS = new ConcurrentHashMap<>();

    @EventHandler
    public void onAsyncHit(LACAsyncPlayerAttackEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        Location location = player.getLocation();
        if (player.isFlying() || player.isInsideVehicle() || lacPlayer.isGliding() || lacPlayer.isRiptiding() ||
                lacPlayer.isClimbing() || lacPlayer.isInWater())
            return;
        if (cache.flyingTicks >= -3 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -3)
            return;
        long time = System.currentTimeMillis();
        if (time - cache.lastInsideVehicle <= 150 || time - cache.lastInWater <= 150 ||
                time - cache.lastWasFished <= 4000 || time - cache.lastTeleport <= 700 ||
                time - cache.lastRespawn <= 500 || time - cache.lastEntityVeryNearby <= 500 ||
                time - cache.lastSlimeBlock <= 500 || time - cache.lastHoneyBlock <= 500 ||
                time - cache.lastWasHit <= 350 || time - cache.lastWasDamaged <= 150 ||
                time - cache.lastKbVelocity <= 500)
            return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        if (isOnGround(player, 0.1, LeanTowards.TRUE, true))
            return;
        if (distanceVertical(cache.history.onEvent.location.get(HistoryElement.FROM), player.getLocation()) >= -0.001)
            return;

        if (player.hasPotionEffect(PotionEffectType.BLINDNESS) ||
                player.hasPotionEffect(VerUtil.potions.get("LEVITATION")))
            return;

        for (Block block : getWithinBlocks(player))
            if (!isActuallyPassable(block) || !isActuallyPassable(block.getRelative(BlockFace.UP)))
                return;

        for (Block block : getWithinBlocks(player, cache.history.onEvent.location.get(HistoryElement.FROM)))
            if (!isActuallyPassable(block) || !isActuallyPassable(block.getRelative(BlockFace.UP)))
                return;

        boolean downPassable = true;
        boolean downMinus1Passable = true;
        boolean downMinus2Passable = true;
        boolean downMinus3Passable = true;

        for (Block block : getDownBlocks(player, 0.1)) {
            if (downPassable && !isActuallyPassable(block))
                downPassable = false;
            if (downMinus1Passable && !isActuallyPassable(block.getRelative(BlockFace.DOWN)))
                downMinus1Passable = false;
            if (downMinus2Passable && !isActuallyPassable(block.getRelative(0, -2, 0)))
                downMinus2Passable = false;
            if (downMinus3Passable && !isActuallyPassable(block.getRelative(0, -3, 0)))
                downMinus3Passable = false;
        }
        if (!downPassable && downMinus1Passable == downMinus2Passable == downMinus3Passable)
            return;

        boolean ground = true;
        for (int i = 0; i < 5 && i < HistoryElement.values().length; i++) {
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsTrue ||
                    cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsTrue) {
                ground = false;
                break;
            }
        }
        if (ground)
            return;

        double maxY = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        for (int i = 0; i < 10 && i < HistoryElement.values().length; i++) {
            Location eventLocation = cache.history.onEvent.location.get(HistoryElement.values()[i]);
            Location packetLocation = cache.history.onPacket.location.get(HistoryElement.values()[i]);
            maxY = Math.max(maxY, Math.min(eventLocation.getY(), packetLocation.getY()));
            minY = Math.min(minY, Math.min(eventLocation.getY(), packetLocation.getY()));
        }
        double verticalDistance = Math.abs(maxY - minY);
        if (verticalDistance >= 0.75)
            return;

        Set<Integer> minHeights = ConcurrentHashMap.newKeySet();
        Scheduler.runTaskLater(() -> {
            if (!player.isOnline() || LACPlayer.getLacPlayer(player) == null)
                return;
            minHeights.add(getMinDownHeight(getDownBlocks(player, 0.1), true));

            Scheduler.runTaskLater(() -> {
                if (!player.isOnline() || LACPlayer.getLacPlayer(player) == null)
                    return;
                minHeights.add(getMinDownHeight(getDownBlocks(player, 0.1), true));

                Scheduler.runTaskLater(() -> {
                    if (!player.isOnline() || LACPlayer.getLacPlayer(player) == null)
                        return;
                    minHeights.add(getMinDownHeight(getDownBlocks(player, 0.1), true));

                    List<Integer> heights = HEIGHTS.getOrDefault(player.getUniqueId(), null);
                    if (heights == null) return;
                    if (Collections.max(heights) > Collections.min(minHeights))
                        return;

                    Scheduler.runTask(true, () -> {
                        callViolationEvent(player, lacPlayer, null);
                    });
                }, 1);
            }, 1);
        }, 1);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Integer[] heights = new Integer[40];
        Arrays.fill(heights, getMaxDownHeight(getDownBlocks(event.getPlayer(), 0.1), true));
        HEIGHTS.put(event.getPlayer().getUniqueId(), Collections.synchronizedList(new LinkedList<>(Arrays.asList(heights))));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        HEIGHTS.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        Player player = event.getPlayer();
        List<Integer> heights = HEIGHTS.getOrDefault(player.getUniqueId(), null);
        if (heights == null) return;
        heights.add(getMaxDownHeight(getDownBlocks(player, 0.1), false));
        heights.remove(0);
    }

    private static int getMaxDownHeight(Set<Block> blocks, boolean passable) {
        int height = Integer.MIN_VALUE;
        for (Block block : blocks) {
            if (!isActuallyPassable(block) || passable)
                height = Math.max(height, block.getY());
        }
        return height;
    }

    private static int getMinDownHeight(Set<Block> blocks, boolean passable) {
        int height = Integer.MAX_VALUE;
        for (Block block : blocks) {
            if (!isActuallyPassable(block) || passable)
                height = Math.min(height, block.getY());
        }
        return height;
    }

}
