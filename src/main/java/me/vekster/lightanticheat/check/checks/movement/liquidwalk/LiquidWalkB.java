package me.vekster.lightanticheat.check.checks.movement.liquidwalk;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

public class LiquidWalkB extends MovementCheck implements Listener {
    public LiquidWalkB() {
        super(CheckName.LIQUIDWALK_B);
    }

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -5 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -4)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 250 && time - cache.lastKnockbackNotVanilla > 1000 &&
                time - cache.lastWasFished > 1000 && time - cache.lastTeleport > 500 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 200 &&
                time - cache.lastBlockExplosion > 2000 && time - cache.lastEntityExplosion > 1000 &&
                time - cache.lastSlimeBlockVertical > 3000 && time - cache.lastSlimeBlockHorizontal > 2500 &&
                time - cache.lastHoneyBlockVertical > 3000 && time - cache.lastHoneyBlockHorizontal > 3000 &&
                time - cache.lastWasHit > 150 && time - cache.lastWasDamaged > 50 &&
                time - cache.lastFlight > 750;
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("flags", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("flags", 0);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("flags", 0);
            return;
        }

        if (getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) != 0) {
            buffer.put("flags", 0);
            return;
        }

        boolean blockHeight = false;
        for (HistoryElement element : HistoryElement.values()) {
            if (cache.history.onEvent.onGround.get(element).towardsTrue ||
                    cache.history.onPacket.onGround.get(element).towardsTrue) {
                buffer.put("flags", 0);
                return;
            }
            if (!blockHeight && isBlockHeight((float) getBlockY(cache.history.onEvent.location.get(element).getY())))
                blockHeight = true;
        }

        if (!blockHeight) {
            buffer.put("flags", 0);
            return;
        }

        boolean downLiquid = true;
        for (Block block : getDownBlocks(player, event.getTo(), 0.22)) {
            if (downLiquid && !block.isLiquid())
                downLiquid = false;
            if (!block.isLiquid() && !block.getRelative(BlockFace.DOWN).isLiquid() &&
                    !block.getRelative(0, -2, 0).isLiquid()) {
                buffer.put("flags", 0);
                return;
            }
        }

        for (Block block : getInteractiveBlocks(player, event.getTo())) {
            if (!isActuallyPassable(block)) {
                buffer.put("flags", 0);
                return;
            }
        }

        buffer.put("flags", buffer.getInt("flags") + 1);
        if (buffer.getInt("flags") <= 1 || System.currentTimeMillis() - buffer.getLong("lastUpdate") > 3000) {
            buffer.put("up", false);
            buffer.put("down", false);
            buffer.put("lastUpdate", System.currentTimeMillis());
            return;
        }


        double verticalSpeed = distanceVertical(event.getFrom(), event.getTo());
        if (!buffer.getBoolean("up")) {
            buffer.put("up", verticalSpeed > 0.07);
            return;
        }
        if (!buffer.getBoolean("down")) {
            buffer.put("down", verticalSpeed < -0.07);
            return;
        }

        if (distanceHorizontal(event.getFrom(), event.getTo()) <= 0.08)
            return;
        if (!downLiquid) {
            double subtract = event.getTo().getY() % 1;
            if (!(subtract < -0.3 || subtract > 0 && subtract < 0.7))
                return;
            if (event.getTo().getY() < 0) subtract = 1 + subtract;
            for (Block block : getDownBlocks(player, event.getTo().subtract(0, subtract, 0), 0.25)) {
                if (!block.isLiquid())
                    return;
            }
        }

        updateDownBlocks(player, lacPlayer, event.getToDownBlocks());
        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, event, buffer, 5000L);
        });
    }

}
