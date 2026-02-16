package me.vekster.lightanticheat.check.checks.combat.criticals;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.combat.CombatCheck;
import me.vekster.lightanticheat.event.playerattack.LACAsyncPlayerAttackEvent;
import me.vekster.lightanticheat.event.playerattack.LACPlayerAttackEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.player.cache.history.PlayerCacheHistory;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.ValhallaMMOHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

/**
 * Packet/Bypass mode
 */
public class CriticalsA extends CombatCheck implements Listener {
    private static final long REPEAT_WINDOW_MS = 1600L;
    private static final long SWEEPING_ATTRIBUTE_WINDOW_MS = 2500L;

    private static final String KEY_ATTRIBUTE = "attribute";
    private static final String KEY_CONTEXT_LAST_TELEPORT = "contextLastTeleport";
    private static final String KEY_CONTEXT_LAST_WATER = "contextLastWater";
    private static final String KEY_CONTEXT_LAST_VEHICLE = "contextLastVehicle";
    private static final String KEY_MOVEMENT_CONTEXT = "movementContext";
    private static final String KEY_LAST_METHOD_FLAG = "lastMethodFlag";
    private static final String KEY_MISSED_METHOD_FLAG = "missedMethodFlag";

    private static final HistoryElement[] HISTORY = HistoryElement.values();

    public CriticalsA() {
        super(CheckName.CRITICALS_A);
    }

    @EventHandler
    public void onHit(LACPlayerAttackEvent event) {
        if (!event.isEntityAttackCause())
            return;
        if (ValhallaMMOHook.isPluginInstalled())
            return;

        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer))
            return;

        long now = System.currentTimeMillis();
        Buffer buffer = getBuffer(player, true);
        refreshContext(buffer, player, lacPlayer, cache);

        if (!passesCommonChecks(player, lacPlayer, cache, true, now))
            return;
        if (!passesGroundShapeChecks(player, false))
            return;
        if (!hasMicroJumpPattern(cache))
            return;
        if (isSweepingAttributeRecentlyUsed(player, buffer, now))
            return;

        callViolationEventIfRepeat(player, lacPlayer, event.getEvent(), buffer, REPEAT_WINDOW_MS);
    }

    @EventHandler
    public void onAsyncHit(LACAsyncPlayerAttackEvent event) {
        if (event.hasDamageCause() && !event.isEntityAttackCause())
            return;
        if (event.getEntityId() == 0)
            return;
        if (ValhallaMMOHook.isPluginInstalled())
            return;

        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        Scheduler.runTask(true, () -> {
            if (!isCheckAllowed(player, lacPlayer))
                return;

            long now = System.currentTimeMillis();
            Buffer buffer = getBuffer(player, true);
            refreshContext(buffer, player, lacPlayer, cache);

            if (!passesCommonChecks(player, lacPlayer, cache, true, now))
                return;
            if (!passesGroundShapeChecks(player, true))
                return;
            if (!hasMicroJumpPattern(cache))
                return;
            if (isSweepingAttributeRecentlyUsed(player, buffer, now))
                return;

            callViolationEventIfRepeat(player, lacPlayer, null, buffer, REPEAT_WINDOW_MS);
        });
    }

    private boolean passesCommonChecks(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean checkBedrock, long now) {
        if (checkBedrock && FloodgateHook.isBedrockPlayer(player, true))
            return false;

        if (player.getFallDistance() == 0 || ((Entity) player).isOnGround())
            return false;

        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return false;

        if (getEffectAmplifier(cache, PotionEffectType.BLINDNESS) != 0 ||
                getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) != 0)
            return false;

        if (player.isFlying() || player.isInsideVehicle() || lacPlayer.isGliding() || lacPlayer.isRiptiding() ||
                lacPlayer.isClimbing() || lacPlayer.isInWater())
            return false;
        if (cache.flyingTicks >= -3 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -3)
            return false;

        if (hasRecentMobilityContext(cache, lacPlayer, now, 800, 1300, 1000, 1700, 1400, 2400))
            return false;
        if (now - cache.lastInsideVehicle <= 150 || now - cache.lastInWater <= 150 ||
                now - cache.lastWasFished <= 4000 || now - cache.lastTeleport <= 700 ||
                now - cache.lastRespawn <= 500 || now - cache.lastEntityVeryNearby <= 500 ||
                now - cache.lastSlimeBlock <= 500 || now - cache.lastHoneyBlock <= 500 ||
                now - cache.lastWasHit <= 350 || now - cache.lastWasDamaged <= 150 ||
                now - cache.lastKbVelocity <= 500)
            return false;

        return true;
    }

    private boolean passesGroundShapeChecks(Player player, boolean strict) {
        for (Block block : getWithinBlocks(player)) {
            if (!isActuallyPassable(block))
                return false;
            if (strict && !isActuallyPassable(block.getRelative(BlockFace.UP)))
                return false;
        }

        for (Block block : getDownBlocks(player, 0.1)) {
            if (!isActuallyPassable(block))
                return true;
            if (strict && !isActuallyPassable(block.getRelative(BlockFace.DOWN)))
                return true;
        }
        return false;
    }

    private boolean isSweepingAttributeRecentlyUsed(Player player, Buffer buffer, long now) {
        if (getItemStackAttributes(player, "PLAYER_SWEEPING_DAMAGE_RATIO") != 0 ||
                getPlayerAttributes(player).getOrDefault("PLAYER_SWEEPING_DAMAGE_RATIO", 0.0) > 0.01)
            buffer.put(KEY_ATTRIBUTE, now);
        return now - buffer.getLong(KEY_ATTRIBUTE) < SWEEPING_ATTRIBUTE_WINDOW_MS;
    }

    private void refreshContext(Buffer buffer, Player player, LACPlayer lacPlayer, PlayerCache cache) {
        long teleportState = cache.lastTeleport;
        long waterState = cache.lastInWater;
        long vehicleState = cache.lastInsideVehicle;

        if (buffer.getLong(KEY_CONTEXT_LAST_TELEPORT) != teleportState ||
                buffer.getLong(KEY_CONTEXT_LAST_WATER) != waterState ||
                buffer.getLong(KEY_CONTEXT_LAST_VEHICLE) != vehicleState) {
            resetRepeatBuffer(buffer);
            buffer.put(KEY_CONTEXT_LAST_TELEPORT, teleportState);
            buffer.put(KEY_CONTEXT_LAST_WATER, waterState);
            buffer.put(KEY_CONTEXT_LAST_VEHICLE, vehicleState);
        }

        String context = (((Entity) player).isOnGround() ? "G" : "A") +
                (lacPlayer.isInWater() ? "W" : "D") +
                (player.isInsideVehicle() ? "V" : "N") +
                (lacPlayer.isGliding() ? "E" : "-") +
                (lacPlayer.isRiptiding() ? "R" : "-");
        String previousContext = buffer.getString(KEY_MOVEMENT_CONTEXT);
        if (previousContext != null && !previousContext.equals(context))
            resetRepeatBuffer(buffer);

        buffer.put(KEY_MOVEMENT_CONTEXT, context);
    }

    private void resetRepeatBuffer(Buffer buffer) {
        // Keys below must match Check#callViolationEventIfRepeat implementation.
        buffer.put(KEY_LAST_METHOD_FLAG, 0L);
        buffer.put(KEY_MISSED_METHOD_FLAG, false);
    }

    private boolean hasMicroJumpPattern(PlayerCache cache) {
        return isMicroJumpHistory(cache.history.onEvent.location) && isMicroJumpHistory(cache.history.onPacket.location);
    }

    private boolean isMicroJumpHistory(PlayerCacheHistory<Location> history) {
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double previousY = Double.NaN;

        int samples = 0;
        int tinyDeltaCount = 0;
        int directionChanges = 0;
        int steadyLargeRise = 0;
        int steadyLargeDrop = 0;
        int currentDirection = 0;

        for (HistoryElement element : HISTORY) {
            Location location = history.get(element);
            if (location == null)
                return false;

            samples++;
            double y = location.getY();
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);

            if (Double.isNaN(previousY)) {
                previousY = y;
                continue;
            }

            double delta = y - previousY;
            double epsilon = Math.max(LOWEST_BLOCK_HEIGHT, 0.003);
            if (Math.abs(delta) <= epsilon)
                tinyDeltaCount++;

            if (Math.abs(delta) >= 0.06) {
                if (delta > 0)
                    steadyLargeRise++;
                else
                    steadyLargeDrop++;
            }

            int direction = delta > epsilon ? 1 : delta < -epsilon ? -1 : 0;
            if (direction != 0 && currentDirection != 0 && direction != currentDirection)
                directionChanges++;
            if (direction != 0)
                currentDirection = direction;

            previousY = y;
        }

        if (samples < 4)
            return false;

        double span = maxY - minY;
        if (span >= 0.3)
            return false;

        int requiredTinyDeltas = Math.max(2, samples / 5);
        if (tinyDeltaCount < requiredTinyDeltas)
            return false;

        if (directionChanges == 0)
            return false;

        int maxLargeMoves = Math.max(1, samples / 4);
        return steadyLargeRise < maxLargeMoves && steadyLargeDrop < maxLargeMoves;
    }

}
