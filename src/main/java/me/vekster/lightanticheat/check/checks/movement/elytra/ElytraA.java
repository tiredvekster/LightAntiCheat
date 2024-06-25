package me.vekster.lightanticheat.check.checks.movement.elytra;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

/**
 * 1. The same speed
 * 2. Too low speed
 */
public class ElytraA extends MovementCheck implements Listener {
    public ElytraA() {
        super(CheckName.ELYTRA_A);
    }

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || !isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -5 || cache.climbingTicks >= -2 || cache.glidingTicks <= 3)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 750 && time - cache.lastKnockbackNotVanilla > 3000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 500 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 700 &&
                time - cache.lastBlockExplosion > 8000 && time - cache.lastEntityExplosion > 3000 &&
                time - cache.lastSlimeBlockVertical > 6000 && time - cache.lastSlimeBlockHorizontal > 6000 &&
                time - cache.lastHoneyBlockVertical > 2500 && time - cache.lastHoneyBlockHorizontal > 2500 &&
                time - cache.lastFireworkBoost > 4500 && time - cache.lastFireworkBoostNotVanilla > 7000 &&
                time - cache.lastRiptiding > 15 * 1000 &&
                time - cache.lastWasHit > 350 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastKbVelocity > 500 && time - cache.lastAirKbVelocity > 1000 &&
                time - cache.lastStrongKbVelocity > 2500 && time - cache.lastStrongAirKbVelocity > 5000 &&
                time - cache.lastFlight > 750;
    }

    @EventHandler
    public void theSameSpeed(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("theSameSpeedEvents", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("theSameSpeedEvents", 0);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("theSameSpeedEvents", 0);
            return;
        }
        if (!event.isToDownBlocksPassable() || !event.isFromDownBlocksPassable()) {
            buffer.put("theSameSpeedEvents", 0);
            return;
        }

        for (int i = 0; i < HistoryElement.values().length; i++)
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsTrue ||
                    cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsTrue) {
                buffer.put("theSameSpeedEvents", 0);
                return;
            }

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong("effectTime") < 1000) {
            buffer.put("theSameSpeedEvents", 0);
            return;
        }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && getBlockY(event.getTo().getY()) == 0) {
            if (!event.isToDownBlocksPassable()) {
                buffer.put("theSameSpeedEvents", 0);
                return;
            }
            for (Block block : event.getToDownBlocks()) {
                if (!isActuallyPassable(block.getRelative(BlockFace.DOWN))) {
                    buffer.put("theSameSpeedEvents", 0);
                    return;
                }
            }
        }

        buffer.put("theSameSpeedEvents", buffer.getInt("theSameSpeedEvents") + 1);
        if (buffer.getInt("theSameSpeedEvents") <= 5)
            return;

        double speed = distance(event.getFrom(), event.getTo());
        double hSpeed = distanceHorizontal(event.getFrom(), event.getTo());
        float pitchDifference = 0;
        Location newerLocation = event.getTo();
        for (int i = 0; i < HistoryElement.values().length; i++) {
            Location location = cache.history.onEvent.location.get(HistoryElement.values()[i]);
            if (Math.abs(speed - distance(location, newerLocation)) >= 0.00005 &&
                    Math.abs(hSpeed - distanceHorizontal(location, newerLocation)) >= 0.00005)
                return;
            if (speed < 0.25 || hSpeed < 0.15)
                return;
            pitchDifference = Math.max(Math.abs(newerLocation.getPitch() - location.getPitch()), pitchDifference);
            newerLocation = location;
        }
        if (pitchDifference < 1.2)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, event);
        });
    }

    @EventHandler
    public void tooLowSpeed(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (lacPlayer.violations.getViolations(getCheckSetting(this).name) == 0 &&
                buffer.getInt("tooLowSpeedEvents") == 0)
            if (CooldownUtil.isSkip(lacPlayer.cooldown, this))
                return;

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("tooLowSpeedEvents", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("tooLowSpeedEvents", 0);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("tooLowSpeedEvents", 0);
            return;
        }
        if (!event.isToDownBlocksPassable() || !event.isFromDownBlocksPassable()) {
            buffer.put("tooLowSpeedEvents", 0);
            return;
        }

        for (int i = 0; i < HistoryElement.values().length; i++)
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsTrue ||
                    cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsTrue) {
                buffer.put("tooLowSpeedEvents", 0);
                return;
            }

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong("effectTime") < 1000) {
            buffer.put("tooLowSpeedEvents", 0);
            return;
        }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && getBlockY(event.getTo().getY()) == 0) {
            if (!event.isToDownBlocksPassable()) {
                buffer.put("tooLowSpeedEvents", 0);
                return;
            }
            for (Block block : event.getToDownBlocks()) {
                if (!isActuallyPassable(block.getRelative(BlockFace.DOWN))) {
                    buffer.put("tooLowSpeedEvents", 0);
                    return;
                }
            }
        }

        buffer.put("tooLowSpeedEvents", buffer.getInt("tooLowSpeedEvents") + 1);
        if (buffer.getInt("tooLowSpeedEvents") <= 10)
            return;

        float yawDifference = 0;
        float pitchDifference = 0;
        Location newerLocation = event.getTo();
        for (int i = 0; i < HistoryElement.values().length; i++) {
            Location location = cache.history.onEvent.location.get(HistoryElement.values()[i]);
            if (distance(location, newerLocation) > 0.025)
                return;
            pitchDifference = Math.max(Math.abs(newerLocation.getPitch() - location.getPitch()), pitchDifference);
            yawDifference = Math.max(Math.abs(newerLocation.getYaw() - location.getYaw()), yawDifference);
            newerLocation = location;
        }
        if (pitchDifference < 0.25 && yawDifference < 0.25)
            return;

        updateDownBlocks(player, lacPlayer, event.getToDownBlocks());
        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, event);
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void beforeMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("LEVITATION")) > 0 ||
                getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("SLOW_FALLING")) > 0) {
            Buffer buffer = getBuffer(player, true);
            long currentTime = System.currentTimeMillis();
            buffer.put("effectTime", currentTime);
        }
    }

}
