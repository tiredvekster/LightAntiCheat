package me.vekster.lightanticheat.check.checks.movement.boat;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.detection.LeanTowards;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BoatFly (the vertical speed should decrease)
 */
public class BoatA extends MovementCheck implements Listener {
    public BoatA() {
        super(CheckName.BOAT_A);
    }

    private static final Map<Integer, Double> SPEEDS = new ConcurrentHashMap<>();

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || !isInsideVehicle || isGliding || isRiptiding)
            return false;
        if (cache.flyingTicks >= -5 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -5)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastKnockback > 500 && time - cache.lastKnockbackNotVanilla > 2000 &&
                time - cache.lastWasFished > 3000 && time - cache.lastTeleport > 500 &&
                time - cache.lastRespawn > 500 &&
                time - cache.lastBlockExplosion > 5500 && time - cache.lastEntityExplosion > 2000 &&
                time - cache.lastSlimeBlockVertical > 3500 && time - cache.lastSlimeBlockHorizontal > 3000 &&
                time - cache.lastHoneyBlockVertical > 2000 && time - cache.lastHoneyBlockHorizontal > 2000 &&
                time - cache.lastWasHit > 300 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastKbVelocity > 250 && time - cache.lastAirKbVelocity > 500 &&
                time - cache.lastStrongKbVelocity > 1250 && time - cache.lastStrongAirKbVelocity > 2500 &&
                time - cache.lastFlight > 750;
    }

    @EventHandler
    public void boatFlight(LACPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player);

        if (!isCheckAllowed(player, lacPlayer)) {
            buffer.put("boatFlightEvents", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event, false, false)) {
            buffer.put("boatFlightEvents", 0);
            return;
        }

        Entity boat = player.getVehicle();
        if (boat == null) {
            buffer.put("boatFlightEvents", 0);
            return;
        }

        if (boat.getType() != EntityType.BOAT &&
                !boat.getType().name().equalsIgnoreCase("CHEST_BOAT")) {
            buffer.put("boatFlightEvents", 0);
            return;
        }

        if (System.currentTimeMillis() - buffer.getLong("entityCollisionTime") <= 3000) {
            buffer.put("boatFlightEvents", 0);
            return;
        }
        if (AsyncUtil.getNearbyEntities(boat, 1, 2, 1).size() > 1) {
            buffer.put("boatFlightEvents", 0);
            buffer.put("entityCollisionTime", System.currentTimeMillis());
            return;
        }

        if (isOnGround(boat, 0.35, LeanTowards.TRUE, false) ||
                boat.isInsideVehicle()) {
            buffer.put("boatFlightEvents", 0);
            return;
        }

        for (Block block : getWithinBlocks(boat, event.getFrom())) {
            if (!isActuallyPassable(block)) {
                buffer.put("boatFlightEvents", 0);
                return;
            }
        }
        for (Block block : getWithinBlocks(boat, event.getTo())) {
            if (slimeOrHoney(block))
                buffer.put("slimeHoneyTime", System.currentTimeMillis());
            if (!isActuallyPassable(block)) {
                buffer.put("boatFlightEvents", 0);
                return;
            }
        }
        for (Block block : getDownBlocks(boat, event.getFrom(), 0.35)) {
            if (!isActuallyPassable(block)) {
                buffer.put("boatFlightEvents", 0);
                return;
            }
        }
        Set<Block> toDownBlocks = getDownBlocks(boat, event.getTo(), 0.35);
        for (Block block : toDownBlocks) {
            if (slimeOrHoney(block))
                buffer.put("slimeHoneyTime", System.currentTimeMillis());
            if (!isActuallyPassable(block)) {
                buffer.put("boatFlightEvents", 0);
                return;
            }
        }

        for (Block block : toDownBlocks) {
            if (slimeOrHoney(block.getRelative(0, -1, 0)) ||
                    slimeOrHoney(block.getRelative(0, -2, 0)) ||
                    slimeOrHoney(block.getRelative(0, -3, 0))) {
                buffer.put("boatFlightEvents", 0);
                buffer.put("slimeHoneyTime", System.currentTimeMillis());
                return;
            }
        }

        for (Block block : getInteractiveBlocks(boat)) {
            if (slimeOrHoney(block) ||
                    slimeOrHoney(block.getRelative(0, 1, 0)) ||
                    slimeOrHoney(block.getRelative(0, -1, 0))) {
                buffer.put("boatFlightEvents", 0);
                buffer.put("slimeHoneyTime", System.currentTimeMillis());
                return;
            }
        }

        if (System.currentTimeMillis() - buffer.getLong("slimeHoneyTime") < 7500)
            return;

        buffer.put("boatFlightEvents", buffer.getInt("boatFlightEvents") + 1);
        if (buffer.getInt("boatFlightEvents") <= 3)
            return;

        if (boat.isOnGround() && boat.getFallDistance() == 0)
            return;

        double verticalSpeed = distanceVertical(event.getFrom(), event.getTo());
        double calculatedVerticalSpeed = SPEEDS.getOrDefault(buffer.getInt("boatFlightEvents"), Collections.min(SPEEDS.values()));

        if (verticalSpeed > calculatedVerticalSpeed * (calculatedVerticalSpeed > 0 ? 1.4 : 0.7) + 0.1)
            buffer.put("flags", Math.min(buffer.getInt("flags") + 1, 4));
        else
            buffer.put("flags", Math.max(buffer.getInt("flags") - 1, 0));

        if (buffer.getInt("flags") <= 3)
            return;

        callViolationEventIfRepeat(player, lacPlayer, event.getEvent(), buffer, 2000);
    }

    private static boolean slimeOrHoney(Block block) {
        Material type = block.getType();
        return type == Material.SLIME_BLOCK || type == VerUtil.material.get("HONEY_BLOCK");
    }

    @EventHandler
    public void boatSpeed(LACPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player);

        if (!isCheckAllowed(player, lacPlayer)) {
            buffer.put("boatSpeedEvents", 0);
            buffer.put("previousLocation", event.getFrom());
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event, false, false)) {
            buffer.put("boatSpeedEvents", 0);
            buffer.put("previousLocation", event.getFrom());
            return;
        }

        Entity boat = player.getVehicle();
        if (boat == null) {
            buffer.put("boatSpeedEvents", 0);
            buffer.put("previousLocation", event.getFrom());
            return;
        }

        if (boat.getType() != EntityType.BOAT &&
                !boat.getType().name().equalsIgnoreCase("CHEST_BOAT")) {
            buffer.put("boatSpeedEvents", 0);
            buffer.put("previousLocation", event.getFrom());
            return;
        }

        boolean liquid = false;

        for (Block block : getWithinBlocks(boat, event.getFrom())) {
            if (block.getType() == Material.WATER) {
                liquid = true;
                continue;
            }
            if (!isActuallyPassable(block)) {
                buffer.put("boatSpeedEvents", 0);
                buffer.put("previousLocation", event.getFrom());
                return;
            }
        }
        for (Block block : getWithinBlocks(boat, event.getTo())) {
            if (block.getType() == Material.WATER) {
                liquid = true;
                continue;
            }
            if (!isActuallyPassable(block)) {
                buffer.put("boatSpeedEvents", 0);
                buffer.put("previousLocation", event.getFrom());
                return;
            }
        }

        if (liquid)
            buffer.put("liquidTime", System.currentTimeMillis());

        buffer.put("boatSpeedEvents", buffer.getInt("boatSpeedEvents") + 1);
        if (buffer.getInt("boatSpeedEvents") <= 2) {
            buffer.put("previousLocation", event.getFrom());
            return;
        }

        Location previousLocation = buffer.getLocation("previousLocation");
        if (previousLocation == null) {
            buffer.put("previousLocation", event.getFrom());
            return;
        }
        buffer.put("previousLocation", event.getFrom());
        double horizontalSpeed = Math.min(
                distanceHorizontal(event.getFrom(), event.getTo()),
                distanceHorizontal(previousLocation, event.getTo()) / 2.0
        );

        double maxSpeed = 3.65;
        maxSpeed *= 1.35;

        if (isIce(getDownBlocks(boat, event.getTo(), 0.4)) ||
                isIce(getDownBlocks(boat, previousLocation, 0.4)))
            buffer.put("iceTime", System.currentTimeMillis());

        if (System.currentTimeMillis() - buffer.getLong("liquidTime") > 5000) {
            if (System.currentTimeMillis() - buffer.getLong("iceTime") > Main.getBufferDurationMils() - 1000L)
                maxSpeed /= 3.0;
        } else {
            if (System.currentTimeMillis() - buffer.getLong("iceTime") > Main.getBufferDurationMils() - 1000L)
                maxSpeed /= 2.7;
        }

        if (horizontalSpeed < maxSpeed)
            return;

        callViolationEventIfRepeat(player, lacPlayer, event.getEvent(), buffer, 2000);
    }

    static {
        SPEEDS.put(4, -0.19999999552965164);
        SPEEDS.put(5, -0.23999999463558197);
        SPEEDS.put(6, -0.2799999937415123);
        SPEEDS.put(7, -0.3199999928474426);
        SPEEDS.put(8, -0.35999999195337296);
        SPEEDS.put(9, -0.3999999910593033);
        SPEEDS.put(10, -0.4399999901652336);
        SPEEDS.put(11, -0.47999998927116394);
        SPEEDS.put(12, -0.5199999883770943);
        SPEEDS.put(13, -0.5599999874830246);
        SPEEDS.put(14, -0.5999999865889549);
        SPEEDS.put(15, -0.6399999856948853);
        SPEEDS.put(16, -0.6799999848008156);
        SPEEDS.put(17, -0.7199999839067459);
        SPEEDS.put(18, -0.7599999830126762);
        SPEEDS.put(19, -0.7999999821186066);
        SPEEDS.put(20, -0.8399999812245369);
    }

    private static boolean isIce(Set<Block> blocks) {
        Material blueIce = VerUtil.material.get("BLUE_ICE");
        for (Block block : blocks) {
            Material type = block.getType();
            if (type == Material.ICE || type == Material.PACKED_ICE || type == blueIce)
                return true;
        }
        return false;
    }

}
