package me.vekster.lightanticheat.check.checks.movement.jump;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.detection.CheckUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

/**
 * HighJump
 */
public class JumpA extends MovementCheck implements Listener {
    public JumpA() {
        super(CheckName.JUMP_A);
    }

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -5 || cache.climbingTicks >= -4 ||
                cache.glidingTicks >= -6 || cache.riptidingTicks >= -10)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastInsideVehicle > 300 && time - cache.lastInWater > 300 &&
                time - cache.lastKnockback > 750 && time - cache.lastKnockbackNotVanilla > 3000 &&
                time - cache.lastWasFished > 5000 && time - cache.lastTeleport > 700 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 700 &&
                time - cache.lastBlockExplosion > 5000 && time - cache.lastEntityExplosion > 3000 &&
                time - cache.lastSlimeBlockVertical > 4000 && time - cache.lastSlimeBlockHorizontal > 2500 &&
                time - cache.lastHoneyBlockVertical > 2500 && time - cache.lastHoneyBlockHorizontal > 1500 &&
                time - cache.lastPowderSnowWalk > 750 &&
                time - cache.lastWasHit > 350 && time - cache.lastWasDamaged > 150 &&
                time - cache.lastStrongKbVelocity > 5000 && time - cache.lastStrongAirKbVelocity > 10 * 1000 &&
                time - cache.lastFlight > 1200 &&
                time - cache.lastWindCharge > 1000 && time - cache.lastWindChargeReceive > 500 &&
                time - cache.lastWindBurst > 1500 && time - cache.lastWindBurstNotVanilla > 4000;
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (!isConditionAllowed(player, lacPlayer, event))
            return;

        Buffer buffer = getBuffer(player, true);
        if (System.currentTimeMillis() - buffer.getLong("lastVelocity") < 1750)
            return;

        if (isBlockHeight((float) getBlockY(event.getTo().getY())) ||
                cache.history.onEvent.onGround.get(HistoryElement.FROM).towardsTrue ||
                cache.history.onPacket.onGround.get(HistoryElement.FROM).towardsTrue)
            return;

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable())
            return;

        if (!event.isToDownBlocksPassable() || !event.isFromDownBlocksPassable())
            return;

        if (!secondFlag(player, cache, event.getFrom(), event.getTo()))
            return;

        Set<Material> interactiveMaterials = new HashSet<>();
        getInteractiveBlocks(player, event.getFrom()).forEach(block -> {
            interactiveMaterials.add(block.getType());
            interactiveMaterials.add(block.getRelative(BlockFace.UP).getType());
        });
        getInteractiveBlocks(player, event.getTo()).forEach(block -> {
            interactiveMaterials.add(block.getType());
            interactiveMaterials.add(block.getRelative(BlockFace.UP).getType());
        });
        if (interactiveMaterials.contains(Material.SLIME_BLOCK) || interactiveMaterials.contains(VerUtil.material.get("HONEY_BLOCK")))
            return;

        Set<Material> downMaterials = new HashSet<>();
        getDownBlocks(player, event.getTo(), 0.45).forEach(block -> downMaterials.add(block.getType()));
        getDownBlocks(player, event.getFrom(), 0.45).forEach(block -> {
            downMaterials.add(block.getType());
            downMaterials.add(block.getRelative(BlockFace.DOWN).getType());
        });
        getDownBlocks(player, cache.history.onEvent.location.get(HistoryElement.FIRST), 0.40)
                .forEach(block -> downMaterials.add(block.getType()));
        if (downMaterials.contains(Material.SLIME_BLOCK) || downMaterials.contains(VerUtil.material.get("HONEY_BLOCK")))
            return;

        if (System.currentTimeMillis() - buffer.getLong("lastFlag") > 100) {
            buffer.put("lastFlag", System.currentTimeMillis());
            return;
        }

        if (getAttribute(player, "GENERIC_JUMP_STRENGTH") != 0)
            buffer.put("attribute", System.currentTimeMillis());
        if (System.currentTimeMillis() - buffer.getLong("attribute") < 2000)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, event, buffer, Main.getBufferDurationMils() - 1000L);
        });
    }

/*    private boolean firstFlag(Player player, PlayerCache cache, Location from, Location to) {
        boolean eventGround = false;
        boolean packetGround = false;
        for (int i = 0; i < 3 && i < HistoryElement.values().length; i++) {
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsFalse)
                eventGround = true;
            if (cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsFalse)
                packetGround = true;
            if (eventGround && packetGround)
                break;
        }
        if (!eventGround || !packetGround)
            return false;

        if (getEffectAmplifier(cache, PotionEffectType.LEVITATION) > 0 ||
                getEffectAmplifier(cache, PotionEffectType.JUMP) > 2)
            return false;

        double velocity = player.getVelocity().getY();
        double vSpeed = distanceVertical(from, to);
        if (getEffectAmplifier(cache, PotionEffectType.JUMP) == 0) {
            if (!(velocity < 0.42 * 1.1 && vSpeed > 0.42 * 1.35 ||
                    vSpeed > 0.42 * 2.55))
                return false;
        } else if (getEffectAmplifier(cache, PotionEffectType.JUMP) == 1) {
            if (!(velocity < 0.52 * 1.1 && vSpeed > 0.52 * 1.35 ||
                    vSpeed > 0.52 * 2.55))
                return false;
        } else if (getEffectAmplifier(cache, PotionEffectType.JUMP) == 2) {
            if (!(velocity < 0.621 * 1.1 && vSpeed > 0.621 * 1.35 ||
                    vSpeed > 0.621 * 2.55))
                return false;
        }
        return true;
    }*/

    private boolean secondFlag(Player player, PlayerCache cache, Location from, Location to) {
        int eventGround = 0;
        int maxEventGround = 0;
        int packetGround = 0;
        int maxPacketGround = 0;
        for (int i = 0; i < 6 && i < HistoryElement.values().length; i++) {
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsFalse) eventGround++;
            else eventGround = 0;
            maxEventGround = Math.max(maxEventGround, eventGround);
            if (cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsFalse) packetGround++;
            else packetGround = 0;
            maxPacketGround = Math.max(maxPacketGround, packetGround);
        }
        if (maxEventGround < 2 || maxPacketGround < 2)
            return false;

        if (getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) > 0 ||
                getEffectAmplifier(cache, PotionEffectType.JUMP) > 2)
            return false;

        double velocity = player.getVelocity().getY();
        double vSpeed = distanceVertical(from, to);
        if (getEffectAmplifier(cache, PotionEffectType.JUMP) == 0) {
            if (!(velocity < 0.42 * 1.05 && vSpeed > 0.42 * 1.9))
                return false;
        } else if (getEffectAmplifier(cache, PotionEffectType.JUMP) == 1) {
            if (!(velocity < 0.52 * 1.05 && vSpeed > 0.52 * 1.9))
                return false;
        } else if (getEffectAmplifier(cache, PotionEffectType.JUMP) == 2) {
            if (!(velocity < 0.621 * 1.05 && vSpeed > 0.621 * 1.9))
                return false;
        }
        return true;
    }

    @EventHandler
    public void onVelocity(PlayerVelocityEvent event) {
        if (CheckUtil.isExternalNPC(event)) return;
        double yVelocity = event.getVelocity().getY();
        if (yVelocity < -0.0784000015258789 + 0.005 &&
                yVelocity > -0.0784000015258789 - 0.005)
            return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("lastVelocity", System.currentTimeMillis());
    }

}
