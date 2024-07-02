package me.vekster.lightanticheat.check.checks.player.autobot;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.player.PlayerCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Head rotation, pathing
 */
public class AutoBotA extends PlayerCheck implements Listener {
    public AutoBotA() {
        super(CheckName.AUTOBOT_A);
    }

    @EventHandler
    public void onHeadRotation(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (FloodgateHook.isBedrockPlayer(player, true))
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lacPlayer.joinTime <= 1000 ||
                currentTime - cache.lastTeleport <= 1000 ||
                currentTime - cache.lastWorldChange <= 1000 ||
                currentTime - cache.lastRespawn <= 1000)
            return;

        boolean flag = false;
        Location from = event.getFrom();
        Location to = event.getTo();

        if (integerHeadRotation(from, to, true))
            flag = true;

        for (HistoryElement element : HistoryElement.values()) {
            Location location = cache.history.onEvent.location.get(element);
            if (integerHeadRotation(location, to, false))
                flag = true;
        }

        if (!flag)
            return;

        Buffer buffer = getBuffer(player, true);
        if (currentTime - buffer.getLong("lastRotationFlagTime") < 1000)
            return;
        buffer.put("lastRotationFlagTime", currentTime);

        if (currentTime - buffer.getLong("firstRotationFlagTime") > 12 * 1000) {
            buffer.put("firstRotationFlagTime", currentTime);
            return;
        }
        if (currentTime - buffer.getLong("secondRotationFlagTime") > 10 * 1000) {
            buffer.put("secondRotationFlagTime", currentTime);
            return;
        }

        if (currentTime - buffer.getLong("thirdRotationFlagTime") > 8 * 1000) {
            buffer.put("thirdRotationFlagTime", currentTime);
            return;
        }

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, null);
        });
    }

    private static boolean integerHeadRotation(Location location1, Location location2, boolean horizontal) {
        double change;
        if (!horizontal)
            change = Math.sqrt(Math.pow(location2.getYaw() - location1.getYaw(), 2) + Math.pow(location2.getPitch() - location1.getPitch(), 2));
        else
            change = Math.abs(location2.getYaw() - location1.getYaw());
        if (change % 360 == 0)
            return false;
        return change % (float) 90 == 0;
    }

    @EventHandler
    public void onMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        if (lacPlayer.violations.getViolations(getCheckSetting(this).name) == 0)
            if (CooldownUtil.isSkip(215, lacPlayer.cooldown, this))
                return;

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (FloodgateHook.isBedrockPlayer(player, true))
            return;

        if (!event.isFromWithinBlocksPassable() || !event.isToWithinBlocksPassable())
            return;

        if (player.isFlying() || player.isInsideVehicle() || lacPlayer.isGliding() || lacPlayer.isRiptiding())
            return;
        if (cache.flyingTicks >= -30 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -40 || cache.riptidingTicks >= -50)
            return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - cache.lastInsideVehicle <= 150 || currentTime - cache.lastInWater <= 150 ||
                currentTime - cache.lastKnockback <= 300 || currentTime - cache.lastKnockbackNotVanilla <= 1000 ||
                currentTime - cache.lastWasFished <= 400 || currentTime - cache.lastTeleport <= 500 ||
                currentTime - cache.lastRespawn <= 500 || currentTime - cache.lastEntityVeryNearby <= 500 ||
                currentTime - cache.lastBlockExplosion <= 1000 || currentTime - cache.lastEntityExplosion <= 1000 ||
                currentTime - cache.lastSlimeBlockVertical > 3000 && currentTime - cache.lastSlimeBlockHorizontal > 3000 ||
                currentTime - cache.lastHoneyBlockVertical > 1500 && currentTime - cache.lastHoneyBlockHorizontal > 1500 ||
                currentTime - cache.lastWasHit <= 150 || currentTime - cache.lastWasDamaged <= 50 ||
                currentTime - cache.lastKbVelocity <= 350 || currentTime - cache.lastAirKbVelocity <= 700 ||
                currentTime - cache.lastStrongKbVelocity <= 1500 || currentTime - cache.lastStrongAirKbVelocity <= 15 * 1000)
            return;

        for (int i = 0; i < 3 && i < HistoryElement.values().length; i++) {
            if (!cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsFalse ||
                    !cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsFalse)
                return;
        }

        for (Block block : getCollisionBlockLayer(player, player.getLocation()))
            if (!isActuallyPassable(block) || !isActuallyPassable(block.getRelative(BlockFace.UP)))
                return;

        if (event.getFrom().getYaw() % 5 == 0 &&
                event.getTo().getYaw() % 5 == 0)
            return;

        double ratio = getRatio(event.getFrom(), event.getTo());
        double longRation = getRatio(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo());
        if (ratio == 0 || longRation == 0)
            return;

        double[] diagonalRatios = new double[]{0.25, 0.333333, 0.50, 0.666666, 0.75, 1.0};
        for (double dRatio : diagonalRatios) {
            if (Math.abs(ratio - dRatio) < 0.000001)
                ratio = 0;
            if (Math.abs(longRation - dRatio) < 0.000001)
                longRation = 0;
        }

        if (ratio != 0 || longRation != 0)
            return;

        Buffer buffer = getBuffer(player, true);
        if (currentTime - buffer.getLong("lastDiagonalFlagTime") < 2000)
            return;
        buffer.put("lastDiagonalFlagTime", currentTime);

        if (currentTime - buffer.getLong("lastDiagonalViolationTime") >= 8 * 1000) {
            buffer.put("lastDiagonalViolationTime", currentTime);
            return;
        }
        buffer.put("lastDiagonalViolationTime", currentTime);

        Scheduler.runTaskLater(() -> {
            if (!cache.history.onEvent.onGround.get(HistoryElement.FROM).towardsFalse ||
                    !cache.history.onPacket.onGround.get(HistoryElement.FROM).towardsFalse)
                return;
            callViolationEvent(player, lacPlayer, null);
        }, 1);
    }

    private double getRatio(Location from, Location to) {
        if (AsyncUtil.getWorld(from) != AsyncUtil.getWorld(to) ||
                from.distance(to) < 0.11)
            return 0;
        if (from.getY() != to.getY())
            return 0;
        double xDistance = Math.abs(from.getX() - to.getX());
        double zDistance = Math.abs(from.getZ() - to.getZ());
        if (xDistance == 0 || zDistance == 0)
            return 0;
        return Math.max(xDistance, zDistance) / Math.min(xDistance, zDistance);
    }

}
