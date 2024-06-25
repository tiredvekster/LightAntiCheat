package me.vekster.lightanticheat.check.checks.movement.fastclimb;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.hook.FloodgateHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import me.vekster.lightanticheat.version.identifier.LACVersion;
import me.vekster.lightanticheat.version.identifier.VerIdentifier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

/**
 * Vertical speed while climbing
 */
public class FastClimbA extends MovementCheck implements Listener {
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
        return time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 500 && time - cache.lastKnockbackNotVanilla > 2000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 500 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 200 &&
                time - cache.lastBlockExplosion > 6500 && time - cache.lastEntityExplosion > 2500 &&
                time - cache.lastSlimeBlockVertical > 1000 && time - cache.lastSlimeBlockHorizontal > 1000 &&
                time - cache.lastHoneyBlockVertical > 700 && time - cache.lastHoneyBlockHorizontal > 700 &&
                time - cache.lastWasHit > 250 && time - cache.lastWasDamaged > 100 &&
                time - cache.lastFlight > 750;
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("climbingEvents", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("climbingEvents", 0);
            return;
        }

        if (FloodgateHook.isBedrockPlayer(player, true)) {
            buffer.put("climbingEvents", 0);
            return;
        }

        Set<Material> withinMaterials = new HashSet<>();
        withinMaterials.addAll(event.getToWithinMaterials());
        withinMaterials.addAll(event.getFromWithinMaterials());
        if (withinMaterials.contains(VerUtil.material.get("SCAFFOLDING"))) {
            buffer.put("climbingEvents", 0);
            return;
        }

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
        }

        double maxUpSpeed = 0.1176001 * 1.5;
        double maxDownSpeed = -0.15001 * 1.65;
        if (VerIdentifier.getVersion().isOlderThan(LACVersion.V1_13)) {
            maxUpSpeed *= 2;
            maxDownSpeed *= 2;
        } else if (FloodgateHook.isBedrockPlayer(player)) {
            maxUpSpeed *= 1.2;
            maxDownSpeed *= 1.2;
        }

        if (!(verticalSpeed > maxUpSpeed || verticalSpeed < maxDownSpeed))
            return;

        if (buffer.getBoolean("annoyingBug")) {
            if (Math.abs(verticalSpeed - 0.5 - 0.1176001) < 0.025 || Math.abs(verticalSpeed - 0.5 + 0.15001) < 0.025 ||
                    Math.abs(distanceVertical - 0.5 - 0.1176001) < 0.025 || Math.abs(distanceVertical - 0.5 + 0.15001) < 0.025) {
                return;
            }
        }

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
            if (vertical && eventVerticalSpeed < -0.007 || !vertical && eventVerticalSpeed > 0.007 ||
                    vertical && packetVerticalSpeed < -0.007 || !vertical && packetVerticalSpeed > 0.007)
                return;
            eventPrevious = eventLocation;
            packetPrevious = packetLocation;
        }

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, event);
        });
    }

}
