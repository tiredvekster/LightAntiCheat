package me.vekster.lightanticheat.check.checks.movement.fastclimb;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import me.vekster.lightanticheat.version.identifier.LACVersion;
import me.vekster.lightanticheat.version.identifier.VerIdentifier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

/**
 * Vertical speed while climbing
 */
public class FastClimbA extends MovementCheck implements Listener {
    private static final int ANNOYING_BUG_IDLE_RESET_TICKS = 12;

    public FastClimbA() {
        super(CheckName.FASTCLIMB_A);
    }

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || !isClimbing || isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -5 || cache.climbingTicks <= 2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -3)
            return false;
        long time = System.currentTimeMillis();
        long instabilityGrace = getDynamicGraceWindow(lacPlayer, 350);
        return time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 500 && time - cache.lastKnockbackNotVanilla > 2000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 500 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 200 &&
                time - cache.lastBlockExplosion > 6500 && time - cache.lastEntityExplosion > 2500 &&
                time - cache.lastSlimeBlockVertical > 1000 && time - cache.lastSlimeBlockHorizontal > 1000 &&
                time - cache.lastHoneyBlockVertical > 700 && time - cache.lastHoneyBlockHorizontal > 700 &&
                time - cache.lastWasHit > 250 && time - cache.lastWasDamaged > 100 &&
                time - cache.lastFlight > 750 &&
                !hasRecent121MobilityBoost(cache, time, false) &&
                !hasInstabilityCooldown(cache, time, instabilityGrace);
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);
        if (hasTeleportOrRespawnTransition(cache, buffer)) {
            resetTracking(buffer);
            return;
        }

        if (!isCheckAllowed(player, lacPlayer, true)) {
            resetTracking(buffer);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            resetTracking(buffer);
            return;
        }

        if (FloodgateHook.isBedrockPlayer(player, true)) {
            resetTracking(buffer);
            return;
        }

        if (cache.sneakingTicks > -15)
            return;

        Block block = AsyncUtil.getBlock(event.getTo());
        if (block == null) {
            resetTracking(buffer);
            return;
        }

        Block downBlock = AsyncUtil.getBlock(event.getTo().clone().subtract(0, 1, 0));
        if (downBlock == null) {
            resetTracking(buffer);
            return;
        }

        Set<Material> toWithin = event.getToWithinMaterials();
        Set<Material> fromWithin = event.getFromWithinMaterials();


        if (!isClimbableContext(block, downBlock, toWithin, event.getTo())) {
            resetTracking(buffer);
            return;
        }

        boolean scaffoldingContext = toWithin.contains(VerUtil.material.get("SCAFFOLDING")) ||
                isScaffolding(block.getType()) || isScaffolding(downBlock.getType());

        buffer.put("climbingEvents", buffer.getInt("climbingEvents") + 1);
        if (buffer.getInt("climbingEvents") <= 2)
            return;

        double verticalSpeed = distanceVertical(event.getFrom(), event.getTo());

        double verticalSpeed1 = distanceVertical(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0;
        if (Math.abs(verticalSpeed1) < Math.abs(verticalSpeed)) verticalSpeed = verticalSpeed1;

        double verticalSpeed2 = distanceVertical(cache.history.onEvent.location.get(HistoryElement.SECOND), event.getTo()) / 3.0;
        if (Math.abs(verticalSpeed2) < Math.abs(verticalSpeed)) verticalSpeed = verticalSpeed2;

        double verticalSpeed3 = distanceVertical(cache.history.onPacket.location.get(HistoryElement.FIRST), event.getTo());
        if (Math.abs(verticalSpeed3) < Math.abs(verticalSpeed)) verticalSpeed = verticalSpeed3;

        double distanceVertical = distanceVertical(event.getFrom(), event.getTo());
        if (Math.abs(verticalSpeed - 0.5 - 0.1176001) < 0.0175 || Math.abs(verticalSpeed - 0.5 + 0.15001) < 0.0175 ||
                Math.abs(distanceVertical - 0.5 - 0.1176001) < 0.0175 || Math.abs(distanceVertical - 0.5 + 0.15001) < 0.0175) {
            buffer.put("annoyingBug", true);
            buffer.put("annoyingBugIdleTicks", 0);
        } else if (buffer.getBoolean("annoyingBug")) {
            int annoyingBugIdleTicks = buffer.getInt("annoyingBugIdleTicks") + 1;
            if (annoyingBugIdleTicks >= ANNOYING_BUG_IDLE_RESET_TICKS) {
                buffer.put("annoyingBug", false);
                buffer.put("annoyingBugIdleTicks", 0);
            } else {
                buffer.put("annoyingBugIdleTicks", annoyingBugIdleTicks);
            }
        }

        double maxUpSpeed = 0.1176001 * 1.5;
        double maxDownSpeed = -0.15001 * 1.65;
        boolean vineContext = isVine(block.getType()) || isVine(downBlock.getType()) || toWithin.stream().anyMatch(this::isVine);
        boolean ladderContext = isLadder(block.getType()) || isLadder(downBlock.getType()) || toWithin.stream().anyMatch(this::isLadder);
        boolean edgeTransition = isClimbingEdgeTransition(event.getFrom(), event.getTo(), fromWithin, toWithin);

        if (VerIdentifier.getVersion().isOlderThan(LACVersion.V1_13)) {
            maxUpSpeed *= 2;
            maxDownSpeed *= 2;
        }
        if (ladderContext) {
            maxUpSpeed *= VerIdentifier.getVersion().isOlderThan(LACVersion.V1_13) ? 1.15 : 1.05;
        }
        if (vineContext) {
            maxUpSpeed *= 1.12;
            maxDownSpeed *= 1.08;
        }
        if (scaffoldingContext) {
            maxUpSpeed *= 1.18;
            maxDownSpeed *= 1.12;
        }
        if (edgeTransition) {
            maxUpSpeed *= 1.2;
            maxDownSpeed *= 1.15;
        }

        if (!(verticalSpeed > maxUpSpeed || verticalSpeed < maxDownSpeed)) {
            buffer.put("climbFlags", Math.max(0, buffer.getInt("climbFlags") - 1));
            return;
        }

        if (buffer.getBoolean("annoyingBug")) {
            if (Math.abs(verticalSpeed - 0.5 - 0.1176001) < 0.025 || Math.abs(verticalSpeed - 0.5 + 0.15001) < 0.025 ||
                    Math.abs(distanceVertical - 0.5 - 0.1176001) < 0.025 || Math.abs(distanceVertical - 0.5 + 0.15001) < 0.025) {
                return;
            }
        }

        double eps = Math.max(LOWEST_BLOCK_HEIGHT, 0.003D);
        boolean vertical = distanceVertical(event.getFrom(), event.getTo()) >= 0;
        Location eventPrevious = null;
        Location packetPrevious = null;
        for (int i = 0; i < 5 && i < HistoryElement.values().length; i++) {
            Location eventLocation = cache.history.onEvent.location.get(HistoryElement.values()[i]);
            Location packetLocation = cache.history.onPacket.location.get(HistoryElement.values()[i]);
            if (eventPrevious == null) {
                eventPrevious = eventLocation;
                packetPrevious = packetLocation;
                continue;
            }
            double eventVerticalSpeed = distanceVertical(eventLocation, eventPrevious);
            double packetVerticalSpeed = distanceVertical(packetLocation, packetPrevious);
            if (vertical && eventVerticalSpeed < -eps || !vertical && eventVerticalSpeed > eps ||
                    vertical && packetVerticalSpeed < -eps || !vertical && packetVerticalSpeed > eps)
                return;
            eventPrevious = eventLocation;
            packetPrevious = packetLocation;
        }

        Scheduler.runTask(true, () -> {
            buffer.put("climbFlags", buffer.getInt("climbFlags") + 1);
            int requiredClimbFlags = getConnectionBufferRequirement(lacPlayer, 1);
            if (buffer.getInt("climbFlags") <= requiredClimbFlags)
                return;
            callViolationEvent(player, lacPlayer, event);
        });
    }

    private boolean hasTeleportOrRespawnTransition(PlayerCache cache, Buffer buffer) {
        if (buffer.getLong("lastProcessedTeleport") != cache.lastTeleport ||
                buffer.getLong("lastProcessedRespawn") != cache.lastRespawn ||
                buffer.getLong("lastProcessedWorldChange") != cache.lastWorldChange) {
            buffer.put("lastProcessedTeleport", cache.lastTeleport);
            buffer.put("lastProcessedRespawn", cache.lastRespawn);
            buffer.put("lastProcessedWorldChange", cache.lastWorldChange);
            return true;
        }
        return false;
    }

    private void resetTracking(Buffer buffer) {
        buffer.put("climbingEvents", 0);
        buffer.put("climbFlags", 0);
        buffer.put("annoyingBug", false);
        buffer.put("annoyingBugIdleTicks", 0);
    }

    private boolean isClimbableContext(Block block, Block downBlock, Set<Material> toWithin, Location to) {
        boolean toHasClimbable = toWithin.stream().anyMatch(this::isClimbable);

        Block headBlock = AsyncUtil.getBlock(to.clone().add(0, 1, 0));
        boolean bodyInside =
                isClimbable(block.getType()) ||
                        isClimbable(downBlock.getType()) ||
                        (headBlock != null && isClimbable(headBlock.getType()));

        if (!toHasClimbable && !bodyInside)
            return false;

        return true;
    }

    private boolean isClimbingEdgeTransition(Location from, Location to, Set<Material> fromWithin, Set<Material> toWithin) {
        boolean fromClimb = fromWithin.stream().anyMatch(this::isClimbable);
        boolean toClimb = toWithin.stream().anyMatch(this::isClimbable);

        if (!fromClimb || toClimb)
            return false;

        double dy = to.getY() - from.getY();
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double horiz = Math.hypot(dx, dz);

        if (horiz > 0.25)
            return false;

        if (Math.abs(dy) < 0.01)
            return false;

        Block below = AsyncUtil.getBlock(to.clone().subtract(0, 0.2, 0));
        if (below == null || isActuallyPassable(below))
            return false;

        return Math.abs(to.getY() - Math.floor(to.getY())) < 0.08;
    }

    private boolean isClimbable(Material material) {
        return isLadder(material) || isVine(material) || isScaffolding(material);
    }

    private boolean isLadder(Material material) {
        return material != null && material == VerUtil.material.get("LADDER");
    }

    private boolean isVine(Material material) {
        return material != null && (
                material == VerUtil.material.get("VINE") ||
                        material == VerUtil.material.get("TWISTING_VINES") ||
                        material == VerUtil.material.get("WEEPING_VINES") ||
                        material == VerUtil.material.get("CAVE_VINES") ||
                        material == VerUtil.material.get("CAVE_VINES_PLANT") ||
                        material == VerUtil.material.get("TWISTING_VINES_PLANT") ||
                        material == VerUtil.material.get("WEEPING_VINES_PLANT")
        );
    }

    private boolean isScaffolding(Material material) {
        return material != null && material == VerUtil.material.get("SCAFFOLDING");
    }

}
