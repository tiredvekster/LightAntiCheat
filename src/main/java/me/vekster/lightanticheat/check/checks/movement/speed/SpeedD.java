package me.vekster.lightanticheat.check.checks.movement.speed;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

/**
 * The underwater speed
 */
public class SpeedD extends MovementCheck implements Listener {
    public SpeedD() {
        super(CheckName.SPEED_D);
    }

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || isGliding || isRiptiding)
            return false;
        if (cache.flyingTicks >= -6 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -5 || cache.riptidingTicks >= -6)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastInsideVehicle > 250 &&
                time - cache.lastKnockback > 1500 && time - cache.lastKnockbackNotVanilla > 5000 &&
                time - cache.lastWasFished > 3000 && time - cache.lastTeleport > 600 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 400 &&
                time - cache.lastBlockExplosion > 4000 && time - cache.lastEntityExplosion > 2500 &&
                time - cache.lastSlimeBlockVertical > 2500 && time - cache.lastSlimeBlockHorizontal > 3500 &&
                time - cache.lastHoneyBlockVertical > 2000 && time - cache.lastHoneyBlockHorizontal > 2000 &&
                time - cache.lastWasHit > 700 && time - cache.lastWasDamaged > 300 &&
                time - cache.lastKbVelocity > 500 && time - cache.lastAirKbVelocity > 1000 &&
                time - cache.lastStrongKbVelocity > 2500 && time - cache.lastStrongAirKbVelocity > 5000 &&
                time - cache.lastFlight > 3000 &&
                time - cache.lastGliding > 1500 && time - cache.lastRiptiding > 3000;
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("liquidTicks", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("liquidTicks", 0);
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong("effectTime") < 4000) {
            buffer.put("liquidTicks", 0);
            return;
        }

        Set<Block> withinBlocks = getWithinBlocks(player, event.getFrom());
        Set<Material> withinMaterials = new HashSet<>();
        withinBlocks.forEach(block -> withinMaterials.add(block.getType()));
        if (!withinMaterials.contains(Material.WATER) && !withinMaterials.contains(Material.LAVA)) {
            buffer.put("liquidTicks", 0);
            return;
        }
        for (Block block : withinBlocks) {
            if (!block.isLiquid() && !isActuallyPassable(block)) {
                buffer.put("liquidTicks", 0);
                return;
            }
        }
        withinBlocks.clear();
        withinBlocks = event.getToWithinBlocks();
        withinMaterials.clear();
        withinBlocks.forEach(block -> withinMaterials.add(block.getType()));
        if (!withinMaterials.contains(Material.WATER) && !withinMaterials.contains(Material.LAVA)) {
            buffer.put("liquidTicks", 0);
            return;
        }
        for (Block block : withinBlocks) {
            if (!block.isLiquid() && !isActuallyPassable(block)) {
                buffer.put("liquidTicks", 0);
                return;
            }
        }

        for (int i = 0; i < 4 && i < HistoryElement.values().length; i++)
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsTrue ||
                    cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsTrue) {
                buffer.put("liquidTicks", 0);
                return;
            }

        Set<Material> downMaterials = new HashSet<>();
        downMaterials.addAll(event.getToDownMaterials());
        downMaterials.addAll(event.getFromDownMaterials());

        if (downMaterials.contains(Material.SOUL_SAND) || downMaterials.contains(VerUtil.material.get("SOUL_SOIL"))) {
            ItemStack boots = lacPlayer.getArmorPiece(EquipmentSlot.FEET);
            if (boots != null && boots.getEnchantmentLevel(VerUtil.enchantment.get("SOUL_SPEED")) != 0) {
                buffer.put("liquidTicks", 0);
                return;
            }
        }

        buffer.put("liquidTicks", buffer.getInt("liquidTicks") + 1);
        if (buffer.getInt("liquidTicks") <= 4)
            return;

        double preHSpeed1 = distanceHorizontal(event.getFrom(), event.getTo());
        preHSpeed1 -= distanceAbsVertical(event.getFrom(), event.getTo()) / 4.0;
        double preHSpeed2 = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0;
        preHSpeed2 -= distanceAbsVertical(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0 / 4.0;

        double hSpeed = Math.min(preHSpeed1, preHSpeed2);
        hSpeed /= player.getWalkSpeed() / 0.2;

        double maxSpeed = 0.2;

        if (currentTime - buffer.getLong("dolphin2EffectTime") < 4000)
            maxSpeed *= 5.0;
        else if (currentTime - buffer.getLong("dolphin1EffectTime") < 3500)
            maxSpeed *= 2.5;

        ItemStack boots = VerUtil.getArmorPiece(player.getInventory(), EquipmentSlot.FEET);
        if (boots != null && boots.getEnchantmentLevel(Enchantment.DEPTH_STRIDER) != 0) {
            if (getEffectAmplifier(cache, VerUtil.potions.get("DOLPHINS_GRACE")) > 0) {
                buffer.put("liquidTicks", 0);
                return;
            }
            maxSpeed *= 1.0 + boots.getEnchantmentLevel(Enchantment.DEPTH_STRIDER) / 2.0;
        }
        maxSpeed *= 1.3;

        double attributeAmount = getAttribute(player,
                "GENERIC_WATER_MOVEMENT_EFFICIENCY", "PLAYER_SNEAKING_SPEED",
                "GENERIC_MOVEMENT_SPEED", "GENERIC_MOVEMENT_EFFICIENCY"
        );
        if (attributeAmount != 0) {
            maxSpeed = (maxSpeed * 1.05 + 0.11) * (attributeAmount * 13);
            buffer.put("attribute", System.currentTimeMillis());
        } else if (System.currentTimeMillis() - buffer.getLong("attribute") < 3000) {
            return;
        }

        if (hSpeed < maxSpeed)
            return;

        Scheduler.runTask(true, () -> {
            if (isPingGlidingPossible(player, cache))
                return;

            if (lacPlayer.isGliding() || lacPlayer.isRiptiding()) {
                buffer.put("liquidTicks", 0);
                return;
            }

            callViolationEventIfRepeat(player, lacPlayer, event, buffer, 1000);
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void beforeMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("LEVITATION")) > 2 ||
                getEffectAmplifier(lacPlayer.cache, PotionEffectType.SPEED) > 5) {
            Buffer buffer = getBuffer(player, true);
            buffer.put("effectTime", System.currentTimeMillis());
        }

        int dolphinsGraceEffectAmplifier = getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("DOLPHINS_GRACE"));
        if (dolphinsGraceEffectAmplifier >= 1) {
            Buffer buffer = getBuffer(player, true);
            buffer.put("dolphin1EffectTime", System.currentTimeMillis());
            if (dolphinsGraceEffectAmplifier >= 2)
                buffer.put("dolphin2EffectTime", System.currentTimeMillis());
            if (dolphinsGraceEffectAmplifier > 2)
                buffer.put("effectTime", System.currentTimeMillis());
        }
    }

}
