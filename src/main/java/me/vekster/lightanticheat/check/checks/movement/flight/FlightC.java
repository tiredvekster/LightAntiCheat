package me.vekster.lightanticheat.check.checks.movement.flight;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.event.playerplaceblock.LACAsyncPlayerPlaceBlockEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.player.cache.history.PlayerCacheHistory;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.precise.AccuracyUtil;
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

import java.util.Set;

/**
 * Horizontal speed while flying
 */
public class FlightC extends MovementCheck implements Listener {

    public FlightC() {
        super(CheckName.FLIGHT_C);
    }

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -15 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -5)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 750 && time - cache.lastKnockbackNotVanilla > 3000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 700 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 700 &&
                time - cache.lastBlockExplosion > 5000 && time - cache.lastEntityExplosion > 3000 &&
                time - cache.lastSlimeBlockVertical > 4000 && time - cache.lastSlimeBlockHorizontal > 2500 &&
                time - cache.lastHoneyBlockVertical > 2500 && time - cache.lastHoneyBlockHorizontal > 1500 &&
                time - cache.lastPowderSnowWalk > 750 &&
                time - cache.lastWasHit > 350 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastKbVelocity > 1000 && time - cache.lastAirKbVelocity > 2000 &&
                time - cache.lastStrongKbVelocity > 5000 && time - cache.lastStrongAirKbVelocity > 15 * 1000 &&
                time - cache.lastFlight > 750 &&
                time - cache.lastGliding > 2000 && time - cache.lastRiptiding > 3500 &&
                time - cache.lastWindCharge > 1000 && time - cache.lastWindChargeReceive > 500 &&
                time - cache.lastWindBurst > 1500 && time - cache.lastWindBurstNotVanilla > 4000;
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("flightTicks", 0);
            buffer.put("airJump", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("flightTicks", 0);
            if (System.currentTimeMillis() - cache.lastTeleport > 700)
                buffer.put("airJump", 0);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("flightTicks", 0);
            buffer.put("airJump", 0);
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - cache.lastEntityNearby <= 1000) {
            buffer.put("flightTicks", 0);
            buffer.put("airJump", 0);
            return;
        }

        if (currentTime - buffer.getLong("effectTime") <= 2000) {
            buffer.put("flightTicks", 0);
            buffer.put("airJump", 0);
            return;
        }

        for (int i = 0; i < 3 && i < HistoryElement.values().length; i++)
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsTrue ||
                    cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsTrue) {
                buffer.put("flightTicks", 0);
                buffer.put("airJump", 0);
                return;
            }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && getBlockY(event.getTo().getY()) == 0) {
            if (!event.isToDownBlocksPassable()) {
                buffer.put("flightTicks", 0);
                buffer.put("airJump", 0);
                return;
            }
            for (Block block : event.getToDownBlocks()) {
                if (!isActuallyPassable(block.getRelative(BlockFace.DOWN))) {
                    buffer.put("flightTicks", 0);
                    buffer.put("airJump", 0);
                    return;
                }
            }
        }

        if (currentTime - buffer.getLong("lastScaffoldPlace") <= 400L) {
            buffer.put("flightTicks", 0);
            buffer.put("airJump", 0);
            return;
        }

        buffer.put("flightTicks", buffer.getInt("flightTicks") + 1);
        if (buffer.getInt("flightTicks") <= 2)
            return;
        boolean isBedrockPlayer = FloodgateHook.isBedrockPlayer(player, true);

        if (buffer.getInt("airJump") == 0) {
            if (distanceVertical(event.getFrom(), event.getTo()) > 0.05) {
                buffer.put("airJump", buffer.getInt("airJump") + 1);
            }
        } else if (buffer.getInt("airJump") == 1) {
            if (distanceVertical(event.getFrom(), event.getTo()) < -0.125) {
                buffer.put("airJump", buffer.getInt("airJump") + 1);
            }
        } else if (buffer.getInt("airJump") == 2) {
            if (distanceVertical(event.getFrom(), event.getTo()) > 0.125) {
                buffer.put("airJump", 1);
                Set<Player> players = getPlayersForEnchantsSquared(lacPlayer, player);
                Scheduler.runTask(true, () -> {
                    if (isEnchantsSquaredImpact(players))
                        return;
                    if (getItemStackAttributes(player, "GENERIC_JUMP_STRENGTH") > 0.15 ||
                            getPlayerAttributes(player).getOrDefault("GENERIC_JUMP_STRENGTH", 0.42) > 0.43)
                        return;
                    if (!isBedrockPlayer)
                        callViolationEventIfRepeat(player, lacPlayer, event, buffer, Main.getBufferDurationMils() - 1000L);
                    else
                        callViolationEventIfRepeat(player, lacPlayer, event, buffer, 2000L);
                });
                return;
            }
        }

        if (isSpeedDecreasing(cache.history.onEvent.location))
            return;
        if (isSpeedDecreasing(cache.history.onPacket.location))
            return;

        Set<Player> players = getPlayersForEnchantsSquared(lacPlayer, player);
        Scheduler.runTask(true, () -> {
            if (currentTime - buffer.getLong("lastScaffoldPlace") <= 400L ||
                    lacPlayer.isGliding() || lacPlayer.isRiptiding()) {
                buffer.put("flightTicks", 0);
                buffer.put("airJump", 0);
                return;
            }

            if (isLagGlidingPossible(player, buffer)) {
                buffer.put("lastGlidingLagPossibleTime", System.currentTimeMillis());
                return;
            }
            if (isPingGlidingPossible(player, cache))
                return;

            if (isEnchantsSquaredImpact(players))
                return;

            if (AccuracyUtil.isViolationCancel(getCheckSetting(), buffer))
                return;
            if (!isBedrockPlayer) {
                if (System.currentTimeMillis() - buffer.getLong("lastGlidingLagPossibleTime") < 5 * 1000)
                    callViolationEventIfRepeat(player, lacPlayer, event, buffer, 300);
                else
                    callViolationEventIfRepeat(player, lacPlayer, event, buffer, 750);
            } else {
                if (System.currentTimeMillis() - buffer.getLong("lastGlidingLagPossibleTime") < 5 * 1000)
                    callViolationEventIfRepeat(player, lacPlayer, event, buffer, 250);
                else
                    callViolationEventIfRepeat(player, lacPlayer, event, buffer, 400);
            }
        });
    }

    private static boolean isSpeedDecreasing(PlayerCacheHistory<Location> history) {
        Location previousLocation = null;
        Location prePreviousLocation = null;
        for (int i = 0; i <= 7 && i < HistoryElement.values().length; i++) {
            HistoryElement element = HistoryElement.values()[i];
            Location location = history.get(element);
            if (previousLocation == null) {
                previousLocation = location;
                continue;
            }
            if (prePreviousLocation == null) {
                prePreviousLocation = previousLocation;
                previousLocation = location;
                continue;
            }

            if (distanceVertical(previousLocation, location) + 0.0000005 < distanceVertical(prePreviousLocation, previousLocation))
                return true;

            prePreviousLocation = previousLocation;
            previousLocation = location;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void beforeMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("LEVITATION")) > 0 ||
                getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("SLOW_FALLING")) > 1 ||
                getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP) > 6) {
            Buffer buffer = getBuffer(player, true);
            long currentTime = System.currentTimeMillis();
            buffer.put("effectTime", currentTime);
        }
    }

    @EventHandler
    public void scaffoldAsyncBlockPlace(LACAsyncPlayerPlaceBlockEvent event) {
        if (isActuallyPassable(event.getBlock()))
            return;
        Block placedBlock = event.getBlock();
        boolean within = false;
        for (Block block : getWithinBlocks(event.getPlayer())) {
            if (!equals(placedBlock, block) &&
                    !equals(placedBlock, block.getRelative(BlockFace.DOWN)))
                continue;
            within = true;
            break;
        }
        if (!within)
            return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("lastScaffoldPlace", System.currentTimeMillis());
    }

    private static boolean equals(Block block1, Block block2) {
        return block1.getX() == block2.getX() &&
                block1.getY() == block2.getY() &&
                block1.getZ() == block2.getZ();
    }

}
