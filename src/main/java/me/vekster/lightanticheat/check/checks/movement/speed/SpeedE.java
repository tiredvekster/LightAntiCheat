package me.vekster.lightanticheat.check.checks.movement.speed;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Set;

/**
 * The absolute horizontal, vertical and absolute speed limiter
 */
public class SpeedE extends MovementCheck implements Listener {
    private static final double MICRO_BASE_SPRINT_PER_TICK = 0.32D;
    private static final double SPEED_EFFECT_BONUS_PER_LEVEL = 0.20D;

    private static final long SETBACK_COOLDOWN_MS = 150L;
    private static final long MICRO_TELEPORT_BASE_WINDOW_MS = 600L;
    private static final long TELEPORT_EXEMPT_BASE_WINDOW_MS = 1000L;

    private static final String KEY_LAST_MOVEMENT = "lastMovement";
    private static final String KEY_FLAGS = "flags";
    private static final String KEY_LAST_TELEPORT = "lastTeleport";
    private static final String KEY_LAST_SETBACK = "lastSetback";
    private static final String KEY_ATTRIBUTE = "attribute";

    private static final String KEY_MICRO_WINDOW_START = "microTeleportWindowStart";
    private static final String KEY_MICRO_SUM = "microTeleportHorizontalSum";
    private static final String KEY_MICRO_SAMPLES = "microTeleportSamples";
    private static final String KEY_MICRO_GAPS = "microTeleportPhysicsGaps";
    private static final String KEY_MICRO_PREV_JUMP = "microTeleportPrevJump";

    public SpeedE() {
        super(CheckName.SPEED_E);
    }

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -5 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -5)
            return false;
        long time = System.currentTimeMillis();
        long instabilityGrace = getDynamicGraceWindow(lacPlayer, 450);
        return time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 750 && time - cache.lastKnockbackNotVanilla > 3000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 600 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 400 &&
                time - cache.lastBlockExplosion > 5000 && time - cache.lastEntityExplosion > 3000 &&
                time - cache.lastSlimeBlockVertical > 4000 && time - cache.lastSlimeBlockHorizontal > 3500 &&
                time - cache.lastHoneyBlockVertical > 2500 && time - cache.lastHoneyBlockHorizontal > 2500 &&
                time - cache.lastWasHit > 350 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastKbVelocity > 250 && time - cache.lastAirKbVelocity > 500 &&
                time - cache.lastStrongKbVelocity > 1250 && time - cache.lastStrongAirKbVelocity > 2500 &&
                time - cache.lastFlight > 750 &&
                !hasRecent121MobilityBoost(cache, time, false) &&
                !hasInstabilityCooldown(cache, time, instabilityGrace);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void afterMovement(LACAsyncPlayerMoveEvent event) {
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put(KEY_LAST_MOVEMENT, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTeleport(PlayerTeleportEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put(KEY_FLAGS, 0);
        buffer.put(KEY_LAST_TELEPORT, System.currentTimeMillis());
        resetMicroTeleportWindow(buffer);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put(KEY_FLAGS, 0);
        buffer.put(KEY_LAST_TELEPORT, System.currentTimeMillis());
        resetMicroTeleportWindow(buffer);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRespawn(PlayerRespawnEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put(KEY_FLAGS, 0);
        buffer.put(KEY_LAST_TELEPORT, System.currentTimeMillis());
        resetMicroTeleportWindow(buffer);
    }

    @EventHandler
    public void onTeleportHorizontal(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;
        if (FloodgateHook.isBedrockPlayer(player, true))
            return;

        if (!isConditionAllowed(player, lacPlayer, event))
            return;

        Buffer buffer = getBuffer(player, true);

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong(KEY_LAST_SETBACK) < SETBACK_COOLDOWN_MS)
            return;

        if (isMicroTeleportContextExempt(lacPlayer, cache, buffer, TELEPORT_EXEMPT_BASE_WINDOW_MS, false)) {
            resetMicroTeleportWindow(buffer);
            return;
        }

        double horizontalDistance = distanceHorizontal(event.getFrom(), event.getTo());
        if (horizontalDistance > 6.0) {
            applySetback(player, event, buffer);
            Scheduler.runTaskLater(() -> callViolationEvent(player, lacPlayer, event), 1);
            return;
        }

        if (horizontalDistance <= 0.75)
            return;

        double maxSpeed = getMicroTeleportMaxSpeed(player, cache, buffer);
        if (maxSpeed < 0.0)
            return;

        double microCap = getMicroTeleportKinematicCap(event, cache, lacPlayer, buffer);
        if (horizontalDistance <= microCap * 1.10)
            return;

        if (!isMicroTeleportSuspicious(event, lacPlayer, cache, buffer, microCap, horizontalDistance))
            return;

        applySetback(player, event, buffer);
        Scheduler.runTaskLater(() -> {
            if (isMicroTeleportContextExempt(lacPlayer, cache, buffer, TELEPORT_EXEMPT_BASE_WINDOW_MS, true))
                return;
            callViolationEventIfRepeat(player, lacPlayer, event, buffer, 1200);
        }, 1);
    }

    @EventHandler
    public void onTeleportVertical(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;
        if (FloodgateHook.isBedrockPlayer(player, true))
            return;

        if (!isConditionAllowed(player, lacPlayer, event))
            return;

        Buffer buffer = getBuffer(player, true);

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong(KEY_LAST_SETBACK) < SETBACK_COOLDOWN_MS)
            return;

        if (isMicroTeleportContextExempt(lacPlayer, cache, buffer, TELEPORT_EXEMPT_BASE_WINDOW_MS, false)) {
            resetMicroTeleportWindow(buffer);
            return;
        }

        if (distanceVertical(event.getFrom(), event.getTo()) > 12) {
            applySetback(player, event, buffer);
            Scheduler.runTaskLater(() -> callViolationEvent(player, lacPlayer, event), 1);
            return;
        }

    }

    @EventHandler
    public void onHorizontal(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true))
            return;
        if (FloodgateHook.isBedrockPlayer(player, true))
            return;

        if (!isConditionAllowed(player, lacPlayer, event))
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong(KEY_LAST_SETBACK) < SETBACK_COOLDOWN_MS)
            return;

        if (isMicroTeleportContextExempt(lacPlayer, cache, buffer, MICRO_TELEPORT_BASE_WINDOW_MS, false)) {
            resetMicroTeleportWindow(buffer);
            return;
        }

        if (currentTime - lacPlayer.joinTime < 7500)
            return;
        if (currentTime - buffer.getLong(KEY_LAST_MOVEMENT) > 1000)
            return;

        if (!event.isFromWithinBlocksPassable() || !event.isToWithinBlocksPassable())
            return;

        double preHSpeed1 = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.SECOND), event.getTo()) / 3.0;
        double preHSpeed2 = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0;
        double preHSpeed3 = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.SECOND), event.getFrom()) / 2.0;

        double hSpeed = Math.min(preHSpeed1, Math.min(preHSpeed2, preHSpeed3));
        hSpeed /= player.getWalkSpeed() / 0.2;

        double maxSpeed = getMicroTeleportMaxSpeed(player, cache, buffer);
        if (maxSpeed < 0.0)
            return;

        double horizontalJump = distanceHorizontal(event.getFrom(), event.getTo());
        double microCap = getMicroTeleportKinematicCap(event, cache, lacPlayer, buffer);
        boolean microTeleportSuspicion = false;
        if (horizontalJump > microCap * 1.20)
            microTeleportSuspicion = isMicroTeleportSuspicious(event, lacPlayer, cache, buffer, microCap, horizontalJump);

        if (hSpeed < maxSpeed && !microTeleportSuspicion)
            return;

        if (microTeleportSuspicion) {
            applySetback(player, event, buffer);

            Scheduler.runTaskLater(() -> {
                if (isMicroTeleportContextExempt(lacPlayer, cache, buffer, TELEPORT_EXEMPT_BASE_WINDOW_MS, true))
                    return;
                callViolationEventIfRepeat(player, lacPlayer, event, buffer, 1200);
            }, 1);
            return;
        }

        buffer.put(KEY_FLAGS, buffer.getInt(KEY_FLAGS) + 1);
        if (buffer.getInt(KEY_FLAGS) <= 3)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, event);
        });
    }

    private boolean isMicroTeleportContextExempt(LACPlayer lacPlayer, PlayerCache cache, Buffer buffer,
                                                long baseTeleportWindow, boolean ignoreRecentTeleportWindows) {
        long now = System.currentTimeMillis();
        long dynamicTeleportWindow = Math.max(baseTeleportWindow, getDynamicGraceWindow(lacPlayer, baseTeleportWindow));

        if (!ignoreRecentTeleportWindows) {
            if (now - cache.lastTeleport < dynamicTeleportWindow)
                return true;
            if (now - buffer.getLong(KEY_LAST_TELEPORT) < dynamicTeleportWindow)
                return true;
        }

        if (now - cache.lastWorldChange < 800 ||
                now - cache.lastRespawn < 1000 ||
                now - cache.lastGamemodeChange < 600)
            return true;

        if (ignoreRecentTeleportWindows)
            return false;

        int ping = Math.max(lacPlayer.getPing(true), 0);
        long sinceMove = now - buffer.getLong(KEY_LAST_MOVEMENT);
        return ping > 450 || ping > 300 && sinceMove > 450;
    }

    private double getConnectionAllowance(LACPlayer lacPlayer, Buffer buffer) {
        int ping = Math.max(lacPlayer.getPing(true), 0);
        long sinceMove = System.currentTimeMillis() - buffer.getLong(KEY_LAST_MOVEMENT);

        double allowance = 1.0;
        if (ping > 220) allowance += 0.10;
        if (ping > 350) allowance += 0.20;
        if (sinceMove > 250) allowance += 0.10;

        return allowance;
    }

    private double getMicroTeleportKinematicCap(LACAsyncPlayerMoveEvent event, PlayerCache cache,
                                               LACPlayer lacPlayer, Buffer buffer) {
        double cap = MICRO_BASE_SPRINT_PER_TICK;

        double walkFactor = Math.max(0.35, event.getPlayer().getWalkSpeed() / 0.2);
        cap *= walkFactor;

        int speedLevel = Math.max(0, getEffectAmplifier(cache, PotionEffectType.SPEED));
        if (speedLevel > 0)
            cap *= 1.0 + SPEED_EFFECT_BONUS_PER_LEVEL * speedLevel;

        Map<String, Double> attributes = getPlayerAttributes(event.getPlayer());
        double movementBonus = Math.max(
                getItemStackAttributes(event.getPlayer(), "GENERIC_MOVEMENT_SPEED", "minecraft:movement_speed") +
                        getItemStackAttributes(event.getPlayer(), "generic.movement_speed"),
                Math.max(
                        attributes.getOrDefault("GENERIC_MOVEMENT_SPEED", 0.13) - 0.13,
                        Math.max(
                                attributes.getOrDefault("minecraft:movement_speed", 0.13) - 0.13,
                                attributes.getOrDefault("generic.movement_speed", 0.13) - 0.13
                        )
                )
        );
        if (movementBonus > 0)
            cap *= Math.max(1.0, 1.0 + movementBonus * 8.0);

        if (isOnIceSurface(event))
            cap *= 1.9;

        cap *= getConnectionAllowance(lacPlayer, buffer);
        cap += isHighPingPlayer(lacPlayer) ? 0.06 : 0.04;
        return cap;
    }

    private boolean isOnIceSurface(LACAsyncPlayerMoveEvent event) {
        Set<Block> toDownBlocks = event.getToDownBlocks();
        if (toDownBlocks != null) {
            for (Block block : toDownBlocks) {
                if (block != null && isIce(block.getType()))
                    return true;
            }
        }

        Set<Block> fromDownBlocks = event.getFromDownBlocks();
        if (fromDownBlocks != null) {
            for (Block block : fromDownBlocks) {
                if (block != null && isIce(block.getType()))
                    return true;
            }
        }

        return false;
    }

    private boolean isIce(Material material) {
        if (material == null)
            return false;
        return material == VerUtil.material.get("ICE") ||
                material == VerUtil.material.get("PACKED_ICE") ||
                material == VerUtil.material.get("BLUE_ICE") ||
                material == VerUtil.material.get("FROSTED_ICE");
    }

    private boolean isMicroTeleportSuspicious(LACAsyncPlayerMoveEvent event, LACPlayer lacPlayer, PlayerCache cache,
                                              Buffer buffer, double microCapPerTick, double currentJump) {
        long currentTime = System.currentTimeMillis();
        long window = isHighPingPlayer(lacPlayer) ? 350 : 220;
        if (currentTime - buffer.getLong(KEY_MICRO_WINDOW_START) > window) {
            buffer.put(KEY_MICRO_WINDOW_START, currentTime);
            buffer.put(KEY_MICRO_SUM, 0.0);
            buffer.put(KEY_MICRO_SAMPLES, 0);
            buffer.put(KEY_MICRO_GAPS, 0);
            buffer.put(KEY_MICRO_PREV_JUMP, 0.0);
        }

        if (currentJump <= microCapPerTick * 1.10)
            return false;

        buffer.put(KEY_MICRO_SUM, buffer.getDouble(KEY_MICRO_SUM) + currentJump);
        buffer.put(KEY_MICRO_SAMPLES, buffer.getInt(KEY_MICRO_SAMPLES) + 1);

        double prevJump = buffer.getDouble(KEY_MICRO_PREV_JUMP);
        buffer.put(KEY_MICRO_PREV_JUMP, currentJump);

        double eventPrevStep = safeDistanceHorizontal(
                cache.history.onEvent.location.get(HistoryElement.FIRST),
                cache.history.onEvent.location.get(HistoryElement.SECOND)
        );
        double packetPrevStep = safeDistanceHorizontal(
                cache.history.onPacket.location.get(HistoryElement.FIRST),
                cache.history.onPacket.location.get(HistoryElement.SECOND)
        );
        double expectedPrev = Math.max(prevJump, Math.max(eventPrevStep, packetPrevStep));

        boolean physicsGap = currentJump > microCapPerTick * 1.45 ||
                (currentJump > microCapPerTick * 1.30 && expectedPrev <= microCapPerTick * 1.05);
        if (physicsGap)
            buffer.put(KEY_MICRO_GAPS, buffer.getInt(KEY_MICRO_GAPS) + 1);

        int samples = buffer.getInt(KEY_MICRO_SAMPLES);
        int requiredSamples = isHighPingPlayer(lacPlayer) ? 5 : 4;
        if (samples < requiredSamples)
            return false;

        double horizontalSum = buffer.getDouble(KEY_MICRO_SUM);
        int dynamicBuffer = getConnectionBufferRequirement(lacPlayer, 0);
        double maxKinematicSum = microCapPerTick * samples + 0.18 + dynamicBuffer * 0.08;
        return horizontalSum > maxKinematicSum && buffer.getInt(KEY_MICRO_GAPS) >= 2;
    }

    private void applySetback(Player player, LACAsyncPlayerMoveEvent event, Buffer buffer) {
        event.setCancelled(true);
        FoliaUtil.teleportPlayer(player, event.getFrom());
        buffer.put(KEY_LAST_SETBACK, System.currentTimeMillis());
        resetMicroTeleportWindow(buffer);
    }

    private void resetMicroTeleportWindow(Buffer buffer) {
        buffer.put(KEY_MICRO_WINDOW_START, 0L);
        buffer.put(KEY_MICRO_SUM, 0.0);
        buffer.put(KEY_MICRO_SAMPLES, 0);
        buffer.put(KEY_MICRO_GAPS, 0);
        buffer.put(KEY_MICRO_PREV_JUMP, 0.0);
    }

    private double safeDistanceHorizontal(Location first, Location second) {
        if (first == null || second == null)
            return 0.0;
        return distanceHorizontal(first, second);
    }

    private double getMicroTeleportMaxSpeed(Player player, PlayerCache cache, Buffer buffer) {
        if (getEffectAmplifier(cache, PotionEffectType.SPEED) > 4 ||
                getEffectAmplifier(cache, VerUtil.potions.get("DOLPHINS_GRACE")) > 2)
            return -1.0;

        double maxSpeed = 3.0;

        if (getEffectAmplifier(cache, PotionEffectType.SPEED) > 3)
            maxSpeed *= 2.5;
        else if (getEffectAmplifier(cache, PotionEffectType.SPEED) > 2)
            maxSpeed *= 2;

        if (getEffectAmplifier(cache, VerUtil.potions.get("DOLPHINS_GRACE")) > 1)
            maxSpeed *= 2.5;

        Map<String, Double> attributes = getPlayerAttributes(player);
        double attributeAmount = Math.max(
                getItemStackAttributes(player,
                        "GENERIC_WATER_MOVEMENT_EFFICIENCY", "PLAYER_SNEAKING_SPEED",
                        "GENERIC_MOVEMENT_SPEED", "GENERIC_MOVEMENT_EFFICIENCY"
                ),
                Math.max(
                        Math.max(
                                attributes.getOrDefault("GENERIC_WATER_MOVEMENT_EFFICIENCY", 0.0),
                                attributes.getOrDefault("PLAYER_SNEAKING_SPEED", 0.0)
                        ),
                        Math.max(
                                attributes.getOrDefault("GENERIC_MOVEMENT_SPEED", 0.13) - 0.13,
                                attributes.getOrDefault("GENERIC_MOVEMENT_EFFICIENCY", 0.0)
                        )
                )
        );

        if (attributeAmount != 0) {
            maxSpeed = (maxSpeed * 1.05 + 0.11) * Math.max(1, attributeAmount * 13);
            buffer.put(KEY_ATTRIBUTE, System.currentTimeMillis());
        } else if (System.currentTimeMillis() - buffer.getLong(KEY_ATTRIBUTE) < 3000) {
            return -1.0;
        }

        return maxSpeed;
    }

    @EventHandler
    public void onVertical(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        if (lacPlayer.violations.getViolations(getCheckSetting(this).name) == 0)
            if (CooldownUtil.isSkip(150, lacPlayer.cooldown, this))
                return;

        if (!isCheckAllowed(player, lacPlayer, true))
            return;
        if (FloodgateHook.isBedrockPlayer(player, true))
            return;

        if (event.isPlayerFlying() || event.isPlayerInsideVehicle() || event.isPlayerClimbing() ||
                event.isPlayerGliding() || event.isPlayerRiptiding() || event.isPlayerInWater())
            return;
        if (cache.flyingTicks >= -5 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -5)
            return;
        long time = System.currentTimeMillis();
        long instabilityGrace = getDynamicGraceWindow(lacPlayer, 450);
        boolean isConditionAllowed = time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 750 && time - cache.lastKnockbackNotVanilla > 3000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 2500 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 400 &&
                time - cache.lastBlockExplosion > 5000 && time - cache.lastEntityExplosion > 3000 &&
                time - cache.lastSlimeBlockVertical > 4000 && time - cache.lastSlimeBlockHorizontal > 2500 &&
                time - cache.lastHoneyBlockVertical > 2000 && time - cache.lastHoneyBlockHorizontal > 2000 &&
                time - cache.lastWasHit > 350 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastKbVelocity > 500 && time - cache.lastAirKbVelocity > 1000 &&
                time - cache.lastStrongKbVelocity > 2500 && time - cache.lastStrongAirKbVelocity > 5000 &&
                time - cache.lastFlight > 750 &&
                !hasRecent121MobilityBoost(cache, time, false) &&
                !hasInstabilityCooldown(cache, time, instabilityGrace);
        if (!isConditionAllowed)
            return;

        if (System.currentTimeMillis() - lacPlayer.joinTime < 2000)
            return;

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable())
            return;

        if (getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) > 1 ||
                getEffectAmplifier(cache, VerUtil.potions.get("SLOW_FALLING")) > 1 ||
                getEffectAmplifier(cache, PotionEffectType.JUMP_BOOST) > 2)
            return;

        for (int i = 0; i < HistoryElement.values().length; i++) {
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsFalse)
                break;
            if (HistoryElement.values()[i] == HistoryElement.TENTH)
                return;
        }
        for (int i = 0; i < HistoryElement.values().length; i++) {
            if (cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsFalse)
                break;
            if (HistoryElement.values()[i] == HistoryElement.TENTH)
                return;
        }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && getBlockY(event.getTo().getY()) == 0) {
            if (!event.isToDownBlocksPassable())
                return;
            for (Block block : event.getToDownBlocks()) {
                if (!isActuallyPassable(block.getRelative(BlockFace.DOWN)))
                    return;
            }
        }

        double preVSpeed1 = distanceAbsVertical(event.getFrom(), event.getTo());
        double preVSpeed2 = distanceAbsVertical(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0;

        double vSpeed = Math.min(preVSpeed1, preVSpeed2);
        double maxSpeed = 0.72;
        maxSpeed *= 2.0;

        Buffer buffer = getBuffer(player, true);
        Map<String, Double> attributes = getPlayerAttributes(player);
        double attributeAmount = Math.max(
                getItemStackAttributes(player,
                        "GENERIC_WATER_MOVEMENT_EFFICIENCY", "GENERIC_MOVEMENT_SPEED",
                        "GENERIC_MOVEMENT_EFFICIENCY", "GENERIC_JUMP_STRENGTH"
                ),
                Math.max(
                        Math.max(
                                attributes.getOrDefault("GENERIC_WATER_MOVEMENT_EFFICIENCY", 0.0),
                                attributes.getOrDefault("GENERIC_MOVEMENT_SPEED", 0.13) - 0.13
                        ),
                        Math.max(
                                attributes.getOrDefault("GENERIC_MOVEMENT_EFFICIENCY", 0.0),
                                attributes.getOrDefault("GENERIC_JUMP_STRENGTH", 0.42) - 0.42
                        )
                )
        );
        if (attributeAmount != 0) {
            maxSpeed = (maxSpeed * 1.05 + 0.11) * Math.max(1, attributeAmount * 13);
            buffer.put(KEY_ATTRIBUTE, System.currentTimeMillis());
        } else if (System.currentTimeMillis() - buffer.getLong(KEY_ATTRIBUTE) < 3000) {
            return;
        }

        if (vSpeed < maxSpeed)
            return;

        Scheduler.runTask(true, () -> {
            if (isPingGlidingPossible(player, cache))
                return;

            callViolationEventIfRepeat(player, lacPlayer, event, buffer, 1500);
        });
    }

}
