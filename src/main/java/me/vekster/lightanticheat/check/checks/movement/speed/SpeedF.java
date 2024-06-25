package me.vekster.lightanticheat.check.checks.movement.speed;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Speed while flying legally
 */
public class SpeedF extends MovementCheck implements Listener {
    public SpeedF() {
        super(CheckName.SPEED_F);
    }

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || isGliding || isRiptiding)
            return false;
        if (cache.flyingTicks <= 5 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -5)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastInsideVehicle > 150 &&
                time - cache.lastKnockback > 750 && time - cache.lastKnockbackNotVanilla > 3000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 600 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 400 &&
                time - cache.lastBlockExplosion > 5000 && time - cache.lastEntityExplosion > 3000 &&
                time - cache.lastSlimeBlockVertical > 4000 && time - cache.lastSlimeBlockHorizontal > 3500 &&
                time - cache.lastHoneyBlockVertical > 2000 && time - cache.lastHoneyBlockHorizontal > 1750 &&
                time - cache.lastWasHit > 350 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastFlight > 1500;
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (lacPlayer.violations.getViolations(getCheckSetting(this).name) == 0 &&
                buffer.getInt("speedTicks") == 0)
            if (CooldownUtil.isSkip(190, lacPlayer.cooldown, this))
                return;

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("speedTicks", Math.max(buffer.getInt("speedTicks") - 1, 0));
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("speedTicks", Math.max(buffer.getInt("speedTicks") - 1, 0));
            return;
        }

        double preSpeed1 = distance(event.getFrom(), event.getTo());
        double preSpeed2 = distance(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0;

        double speed = Math.min(preSpeed1, preSpeed2);
        speed /= player.getFlySpeed() / 0.1;

        double maxSpeed = 1.17;
        maxSpeed *= 1.4;

        if (speed < maxSpeed) {
            buffer.put("speedTicks", Math.max(buffer.getInt("speedTicks") - 1, 0));
            return;
        }
        buffer.put("speedTicks", Math.min(buffer.getInt("speedTicks") + 1, 16));

        if (buffer.getInt("speedTicks") <= 15)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, event, buffer, 3000);
        });
    }

}
