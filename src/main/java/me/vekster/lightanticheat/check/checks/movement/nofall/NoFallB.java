package me.vekster.lightanticheat.check.checks.movement.nofall;


import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

/**
 * Spoof of Entity.isOnGround()
 */
public class NoFallB extends MovementCheck implements Listener {
    public NoFallB() {
        super(CheckName.NOFALL_B);
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
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 900 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 700 &&
                time - cache.lastBlockExplosion > 4000 && time - cache.lastEntityExplosion > 2000 &&
                time - cache.lastSlimeBlockVertical > 2500 && time - cache.lastSlimeBlockHorizontal > 2500 &&
                time - cache.lastHoneyBlockVertical > 2500 && time - cache.lastHoneyBlockHorizontal > 2500 &&
                time - cache.lastWasHit > 350 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastFlight > 750;
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("fallEvents", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("fallEvents", 0);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("fallEvents", 0);
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - cache.lastEntityNearby <= 3000) {
            buffer.put("fallEvents", 0);
            return;
        }

        if (currentTime - buffer.getLong("effectTime") < 1000) {
            buffer.put("fallEvents", 0);
            return;
        }

        if (!event.isToDownBlocksPassable()) {
            buffer.put("fallEvents", 0);
            return;
        }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && getBlockY(event.getTo().getY()) == 0) {
            for (Block block : getDownBlocks(player, event.getTo(), Double.MIN_VALUE * 100)) {
                if (!isActuallyPassable(block.getRelative(BlockFace.DOWN))) {
                    buffer.put("fallEvents", 0);
                    return;
                }
            }
        }

        for (int i = 0; i < 3 && i < HistoryElement.values().length; i++)
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsTrue ||
                    cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsTrue) {
                buffer.put("fallEvents", 0);
                return;
            }

        Location newerLocation = event.getTo();
        for (int i = 0; i < 3 && i < HistoryElement.values().length; i++) {
            Location location = cache.history.onEvent.location.get(HistoryElement.values()[i]);
            double vSpeed = distanceVertical(location, newerLocation);
            newerLocation = location;
            if (vSpeed > -0.00001) {
                buffer.put("fallEvents", 0);
                return;
            }
        }

        if (event.getTo().getY() % 0.5 == 0) {
            buffer.put("fallEvents", 0);
            return;
        }

        buffer.put("fallEvents", buffer.getInt("fallEvents") + 1);

        if (buffer.getInt("fallEvents") <= 3)
            return;

        if (!((LivingEntity) player).isOnGround())
            return;

        updateDownBlocks(player, lacPlayer, event.getToDownBlocks());
        int jumpEffectAmplifier = getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP);
        Scheduler.runTask(true, () -> {
            if (jumpEffectAmplifier <= 2) callViolationEventIfRepeat(player, lacPlayer, null, buffer, 3000);
            else callViolationEventIfRepeat(player, lacPlayer, null, buffer, 600);
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
