package me.vekster.lightanticheat.check.checks.movement.speed;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.hook.FloodgateHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
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

/**
 * The absolute horizontal, vertical and absolute speed limiter
 */
public class SpeedE extends MovementCheck implements Listener {
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
                time - cache.lastFlight > 750;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void afterMovement(LACAsyncPlayerMoveEvent event) {
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("lastMovement", System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTeleport(PlayerTeleportEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("flags", 0);
        buffer.put("lastTeleport", System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("flags", 0);
        buffer.put("lastTeleport", System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRespawn(PlayerRespawnEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("flags", 0);
        buffer.put("lastTeleport", System.currentTimeMillis());
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
        if (System.currentTimeMillis() - buffer.getLong("lastTeleport") < 1000)
            return;

        if (getEffectAmplifier(cache, PotionEffectType.SPEED) > 6 ||
                getEffectAmplifier(cache, VerUtil.potions.get("DOLPHINS_GRACE")) > 3)
            return;

        if (distanceHorizontal(event.getFrom(), event.getTo()) <= 6)
            return;

        event.setCancelled(true);
        player.teleport(event.getFrom());

        Scheduler.runTaskLater(() -> {
            if (System.currentTimeMillis() - buffer.getLong("lastTeleport") < 1000)
                return;
            callViolationEvent(player, lacPlayer, event);
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
        if (System.currentTimeMillis() - buffer.getLong("lastTeleport") < 1000)
            return;

        if (getEffectAmplifier(cache, PotionEffectType.SPEED) > 6 ||
                getEffectAmplifier(cache, VerUtil.potions.get("DOLPHINS_GRACE")) > 3)
            return;

        if (distanceVertical(event.getFrom(), event.getTo()) <= 12)
            return;

        event.setCancelled(true);
        player.teleport(event.getFrom());

        Scheduler.runTaskLater(() -> {
            if (System.currentTimeMillis() - buffer.getLong("lastTeleport") < 1000)
                return;
            callViolationEvent(player, lacPlayer, event);
        }, 1);
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
        if (currentTime - lacPlayer.joinTime < 7500)
            return;
        if (currentTime - buffer.getLong("lastMovement") > 1000)
            return;

        if (!event.isFromWithinBlocksPassable() || !event.isToWithinBlocksPassable())
            return;

        if (getEffectAmplifier(cache, PotionEffectType.SPEED) > 4 ||
                getEffectAmplifier(cache, VerUtil.potions.get("DOLPHINS_GRACE")) > 2)
            return;

        double preHSpeed1 = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.SECOND), event.getTo()) / 3.0;
        double preHSpeed2 = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0;
        double preHSpeed3 = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.SECOND), event.getFrom()) / 2.0;

        double hSpeed = Math.min(preHSpeed1, Math.min(preHSpeed2, preHSpeed3));
        hSpeed /= player.getWalkSpeed() / 0.2;

        double maxSpeed = 3.0;

        if (getEffectAmplifier(cache, PotionEffectType.SPEED) > 3)
            maxSpeed *= 2.5;
        else if (getEffectAmplifier(cache, PotionEffectType.SPEED) > 2)
            maxSpeed *= 2;

        if (getEffectAmplifier(cache, VerUtil.potions.get("DOLPHINS_GRACE")) > 1)
            maxSpeed *= 2.5;

        if (hSpeed < maxSpeed)
            return;

        buffer.put("flags", buffer.getInt("flags") + 1);
        if (buffer.getInt("flags") <= 3)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEvent(player, lacPlayer, event);
        });
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
                time - cache.lastFlight > 750;
        if (!isConditionAllowed)
            return;

        if (System.currentTimeMillis() - lacPlayer.joinTime < 2000)
            return;

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable())
            return;

        if (getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) > 1 ||
                getEffectAmplifier(cache, VerUtil.potions.get("SLOW_FALLING")) > 1 ||
                getEffectAmplifier(cache, PotionEffectType.JUMP) > 2)
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

        if (vSpeed < maxSpeed)
            return;

        Scheduler.runTask(true, () -> {
            if (isPingGlidingPossible(player, cache))
                return;

            Buffer buffer = getBuffer(player);
            callViolationEventIfRepeat(player, lacPlayer, event, buffer, 1500);
        });
    }

}
