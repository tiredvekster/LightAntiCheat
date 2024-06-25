package me.vekster.lightanticheat.check.checks.movement.elytra;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.player.cache.history.PlayerCacheHistory;
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
 * Acceleration based on pitch
 */
public class ElytraB extends MovementCheck implements Listener {
    public ElytraB() {
        super(CheckName.ELYTRA_B);
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
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("glidingEvents", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("glidingEvents", 0);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("glidingEvents", 0);
            return;
        }
        if (!event.isToDownBlocksPassable() || !event.isFromDownBlocksPassable()) {
            buffer.put("glidingEvents", 0);
            return;
        }

        for (int i = 0; i < HistoryElement.values().length; i++)
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsTrue ||
                    cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsTrue) {
                buffer.put("glidingEvents", 0);
                return;
            }

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong("effectTime") < 1000) {
            buffer.put("glidingEvents", 0);
            return;
        }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && getBlockY(event.getTo().getY()) == 0) {
            if (!event.isToDownBlocksPassable()) {
                buffer.put("glidingEvents", 0);
                return;
            }
            for (Block block : event.getToDownBlocks()) {
                if (!isActuallyPassable(block.getRelative(BlockFace.DOWN))) {
                    buffer.put("glidingEvents", 0);
                    return;
                }
            }
        }

        for (Block block : getInteractiveBlocks(player)) {
            if (!isActuallyPassable(block)) {
                buffer.put("glidingEvents", 0);
                return;
            }
        }

        buffer.put("glidingEvents", buffer.getInt("glidingEvents") + 1);
        if (buffer.getInt("glidingEvents") <= 8)
            return;

        PlayerCacheHistory<Location> history = cache.history.onEvent.location;
        double verticalSpeed = distanceVertical(event.getFrom(), event.getTo());
        double previousVerticalSpeed = distanceVertical(history.get(HistoryElement.FIRST), event.getFrom());
        if (verticalSpeed <= 0.05 || previousVerticalSpeed <= 0.05)
            return;
        double speed = distance(event.getFrom(), event.getTo());
        double previousSpeed = distance(history.get(HistoryElement.FIRST), event.getFrom());
        double prePreviousSpeed = distance(history.get(HistoryElement.SECOND), history.get(HistoryElement.FIRST));
        double offset = speed > 1.0 ? 0 : (speed > 0.5 ? 0.00003 : (speed > 0.25 ? 0.00006 : 0.00012));
        if (speed + 0.00005 - offset <= previousSpeed || previousSpeed + 0.00005 - offset <= prePreviousSpeed)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, event, buffer, 900);
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
