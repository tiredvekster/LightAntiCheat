package me.vekster.lightanticheat.check.checks.movement.speed;

import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.movement.MovementCheck;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.precise.AccuracyUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Horizontal speed
 */
public class SpeedA extends MovementCheck implements Listener {
    public SpeedA() {
        super(CheckName.SPEED_A);
    }

    @Override
    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache, boolean isClimbing, boolean isInWater,
                                      boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding) {
        if (isFlying || isInsideVehicle || isClimbing || isGliding || isRiptiding || isInWater)
            return false;
        if (cache.flyingTicks >= -6 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -5 || cache.riptidingTicks >= -6)
            return false;
        long time = System.currentTimeMillis();
        return time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 1500 && time - cache.lastKnockbackNotVanilla > 5000 &&
                time - cache.lastWasFished > 3000 && time - cache.lastTeleport > 600 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 400 &&
                time - cache.lastBlockExplosion > 4000 && time - cache.lastEntityExplosion > 3000 &&
                time - cache.lastSlimeBlockVertical > 3000 && time - cache.lastSlimeBlockHorizontal > 3500 &&
                time - cache.lastHoneyBlockVertical > 3000 && time - cache.lastHoneyBlockHorizontal > 3000 &&
                time - cache.lastWasHit > 700 && time - cache.lastWasDamaged > 300 &&
                time - cache.lastKbVelocity > 500 && time - cache.lastAirKbVelocity > 1000 &&
                time - cache.lastStrongKbVelocity > 2500 && time - cache.lastStrongAirKbVelocity > 5000 &&
                time - cache.lastFlight > 1000 &&
                time - cache.lastGliding > 750 && time - cache.lastRiptiding > 1500 &&
                time - cache.lastWindCharge > 500 && time - cache.lastWindChargeReceive > 750 &&
                time - cache.lastWindBurst > 500 && time - cache.lastWindBurstNotVanilla > 1000;
    }

    @EventHandler
    public void totalHorizontal(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("speedTicks", 0);
            return;
        }

        if (!isConditionAllowed(player, lacPlayer, event)) {
            buffer.put("speedTicks", 0);
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong("effectTime") <= 1250) {
            buffer.put("speedTicks", 0);
            return;
        }

        for (Block block : event.getToWithinBlocks()) {
            if (isActuallyPassable(block))
                continue;
            if (block.getType().name().endsWith("_SLAB") && !VerUtil.isWatterLoggedSlab(block))
                continue;
            buffer.put("speedTicks", 0);
            return;
        }

        Set<Material> downMaterials = new HashSet<>();
        downMaterials.addAll(event.getToDownMaterials());
        downMaterials.addAll(event.getFromDownMaterials());

        if (downMaterials.contains(Material.ICE) || downMaterials.contains(Material.PACKED_ICE) ||
                downMaterials.contains(VerUtil.material.get("BLUE_ICE"))) {
            buffer.put("speedTicks", 0);
            return;
        }

        if (downMaterials.contains(Material.SOUL_SAND) || downMaterials.contains(VerUtil.material.get("SOUL_SOIL"))) {
            ItemStack boots = lacPlayer.getArmorPiece(EquipmentSlot.FEET);
            if (boots != null && boots.getEnchantmentLevel(VerUtil.enchantment.get("SOUL_SPEED")) != 0) {
                buffer.put("speedTicks", 0);
                return;
            }
        }

        buffer.put("speedTicks", buffer.getInt("speedTicks") + 1);
        if (buffer.getInt("speedTicks") <= 2)
            return;

        double hSpeed1 = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0;
        hSpeed1 -= distanceAbsVertical(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0 * 2.0;
        double hSpeed2 = distanceHorizontal(event.getFrom(), event.getTo());
        hSpeed2 -= distanceAbsVertical(event.getFrom(), event.getTo()) * 2.0;

        double hSpeed = Math.min(hSpeed1, hSpeed2);
        hSpeed /= player.getWalkSpeed() / 0.2;

        double maxSpeed = 0.8418;
        int speedEffectAmplifier = getEffectAmplifier(lacPlayer.cache, PotionEffectType.SPEED);
        if (speedEffectAmplifier > 0) {
            maxSpeed *= speedEffectAmplifier * 0.35 + 1;
            if (speedEffectAmplifier > 2)
                maxSpeed *= 1.35;
        }
        if (getEffectAmplifier(cache, PotionEffectType.JUMP) > 0) {
            maxSpeed *= getEffectAmplifier(cache, PotionEffectType.JUMP) * 0.25 + 1;
        }
        if (getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) > 0) {
            maxSpeed *= getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) * 0.20 + 1;
        }

        Set<Block> interactiveBlocks = new HashSet<>();
        getInteractiveBlocks(player, event.getFrom()).forEach(block -> {
            interactiveBlocks.add(block);
            interactiveBlocks.add(block.getRelative(BlockFace.UP));
        });
        getInteractiveBlocks(player, event.getTo()).forEach(block -> {
            interactiveBlocks.add(block);
            interactiveBlocks.add(block.getRelative(BlockFace.UP));
        });
        maxSpeed /= 1.15;
        for (Block block : interactiveBlocks)
            if (!isActuallyPassable(block)) {
                maxSpeed *= 1.15;
                break;
            }

        Map<String, Double> attributes = getPlayerAttributes(player);
        double attributeAmount = Math.max(
                getItemStackAttributes(player, "GENERIC_MOVEMENT_SPEED", "PLAYER_SNEAKING_SPEED"),
                Math.max(attributes.getOrDefault("GENERIC_MOVEMENT_SPEED", 0.13) - 0.13, attributes.getOrDefault("PLAYER_SNEAKING_SPEED", 0.0))
        );
        if (attributeAmount != 0) {
            maxSpeed = (maxSpeed * 1.05 + 0.11) * (1 + Math.max(0, attributeAmount));
            buffer.put("attribute", System.currentTimeMillis());
        } else if (System.currentTimeMillis() - buffer.getLong("attribute") < 3000) {
            return;
        }

        if (hSpeed < maxSpeed)
            return;

        buffer.put("flags", buffer.getInt("flags") + 1);
        if (buffer.getInt("flags") <= 2)
            return;

        Set<Player> players = getPlayersForEnchantsSquared(lacPlayer, player);
        double finalHSpeed = hSpeed;
        double finalMaxSpeed = maxSpeed;
        Scheduler.runTask(true, () -> {
            if (lacPlayer.isGliding() || lacPlayer.isRiptiding()) {
                buffer.put("speedTicks", 0);
                return;
            }

            if (isEnchantsSquaredImpact(players) && finalHSpeed / 2.5 < finalMaxSpeed)
                return;

            if (AccuracyUtil.isViolationCancel(getCheckSetting(), buffer))
                return;
            callViolationEventIfRepeat(player, lacPlayer, event, buffer, 5000);
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void beforeMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (getEffectAmplifier(lacPlayer.cache, PotionEffectType.SPEED) > 5) {
            Buffer buffer = getBuffer(player, true);
            buffer.put("effectTime", System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTeleport(PlayerTeleportEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("flags", 0);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("flags", 0);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRespawn(PlayerRespawnEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("flags", 0);
    }

    @EventHandler
    public void airHorizontal(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();
        Buffer buffer = getBuffer(player, true);

        if (lacPlayer.violations.getViolations(getCheckSetting(this).name) == 0 &&
                buffer.getInt("airSpeedTicks") == 0)
            if (CooldownUtil.isSkip(160, lacPlayer.cooldown, this))
                return;

        if (!isCheckAllowed(player, lacPlayer, true)) {
            buffer.put("airSpeedTicks", 0);
            return;
        }

        if (event.isPlayerFlying() || event.isPlayerInsideVehicle() || event.isPlayerClimbing() ||
                event.isPlayerGliding() || event.isPlayerRiptiding() || event.isPlayerInWater()) {
            buffer.put("airSpeedTicks", 0);
            return;
        }
        if (cache.flyingTicks >= -6 || cache.climbingTicks >= -2 || cache.glidingTicks >= -5 || cache.riptidingTicks >= -6) {
            buffer.put("airSpeedTicks", 0);
            return;
        }
        long time = System.currentTimeMillis();
        boolean conditionAllowed = time - cache.lastInsideVehicle > 150 && time - cache.lastInWater > 150 &&
                time - cache.lastKnockback > 1500 && time - cache.lastKnockbackNotVanilla > 5000 &&
                time - cache.lastWasFished > 4000 && time - cache.lastTeleport > 700 &&
                time - cache.lastRespawn > 500 && time - cache.lastEntityVeryNearby > 700 &&
                time - cache.lastBlockExplosion > 5000 && time - cache.lastEntityExplosion > 3000 &&
                time - cache.lastSlimeBlockVertical > 3000 && time - cache.lastSlimeBlockHorizontal > 3500 &&
                time - cache.lastHoneyBlockVertical > 1500 && time - cache.lastHoneyBlockHorizontal > 2500 &&
                time - cache.lastPowderSnowWalk > 750 &&
                time - cache.lastWasHit > 700 && time - cache.lastWasDamaged > 300 &&
                time - cache.lastKbVelocity > 1000 && time - cache.lastAirKbVelocity > 2000 &&
                time - cache.lastStrongKbVelocity > 5000 && time - cache.lastStrongAirKbVelocity > 15 * 1000 &&
                time - cache.lastFlight > 1000 &&
                time - cache.lastGliding > 2000 && time - cache.lastRiptiding > 3500;
        if (!conditionAllowed) {
            buffer.put("airSpeedTicks", 0);
            return;
        }

        if (!event.isToWithinBlocksPassable() || !event.isFromWithinBlocksPassable()) {
            buffer.put("airSpeedTicks", 0);
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - buffer.getLong("airEffectTime") <= 2000) {
            buffer.put("airSpeedTicks", 0);
            return;
        }

        if (currentTime - cache.lastEntityNearby <= 1000) {
            buffer.put("airSpeedTicks", 0);
            return;
        }

        if (event.getFrom().getBlockY() > event.getTo().getBlockY() ||
                event.getFrom().getY() > event.getTo().getY() && getBlockY(event.getTo().getY()) == 0) {
            if (!event.isToDownBlocksPassable()) {
                buffer.put("airSpeedTicks", 0);
                return;
            }
            for (Block block : event.getToDownBlocks()) {
                if (!isActuallyPassable(block.getRelative(BlockFace.DOWN))) {
                    buffer.put("airSpeedTicks", 0);
                    return;
                }
            }
        }

        Set<Material> downMaterials = new HashSet<>();
        downMaterials.addAll(event.getToWithinMaterials());
        downMaterials.addAll(event.getFromDownMaterials());

        if (downMaterials.contains(Material.ICE) || downMaterials.contains(Material.PACKED_ICE) ||
                downMaterials.contains(VerUtil.material.get("BLUE_ICE"))) {
            buffer.put("airSpeedTicks", 0);
            return;
        }

        if (downMaterials.contains(Material.SOUL_SAND) || downMaterials.contains(VerUtil.material.get("SOUL_SOIL"))) {
            ItemStack boots = lacPlayer.getArmorPiece(EquipmentSlot.FEET);
            if (boots != null && boots.getEnchantmentLevel(VerUtil.enchantment.get("SOUL_SPEED")) != 0) {
                buffer.put("airSpeedTicks", 0);
                return;
            }
        }

        buffer.put("airSpeedTicks", buffer.getInt("airSpeedTicks") + 1);
        if (buffer.getInt("airSpeedTicks") <= 2)
            return;

        for (int i = 0; i < 4 && i < HistoryElement.values().length; i++)
            if (cache.history.onEvent.onGround.get(HistoryElement.values()[i]).towardsTrue ||
                    cache.history.onPacket.onGround.get(HistoryElement.values()[i]).towardsTrue)
                return;

        Location to = event.getTo();
        Location from = event.getFrom();
        Location first = cache.history.onEvent.location.get(HistoryElement.FIRST);
        Location second = cache.history.onEvent.location.get(HistoryElement.SECOND);

        if (distanceHorizontal(to, from) < 0.091 &&
                distanceHorizontal(from, first) < 0.091 &&
                distanceHorizontal(first, second) < 0.091)
            return;

        if ((to.getX() - from.getX()) - (from.getX() - first.getX()) > 0.000071 &&
                (to.getZ() - from.getZ()) - (from.getZ() - first.getZ()) > 0.000071 &&
                (from.getX() - first.getX()) - (first.getX() - second.getX()) > 0.000071 &&
                (from.getZ() - first.getZ()) - (first.getZ() - second.getZ()) > 0.000071)
            return;

        double hSpeed1 = distanceHorizontal(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0;
        hSpeed1 -= distanceAbsVertical(cache.history.onEvent.location.get(HistoryElement.FIRST), event.getTo()) / 2.0 / 7.5;
        double hSpeed2 = distanceHorizontal(event.getFrom(), event.getTo());
        hSpeed2 -= distanceAbsVertical(event.getFrom(), event.getTo()) / 7.5;

        double hSpeed = Math.min(hSpeed1, hSpeed2);
        hSpeed /= player.getWalkSpeed() / 0.2;

        double maxSpeed = 1.15;
        int speedEffectAmplifier = getEffectAmplifier(lacPlayer.cache, PotionEffectType.SPEED);
        if (speedEffectAmplifier > 0) {
            maxSpeed *= speedEffectAmplifier * 0.35 + 1;
            if (speedEffectAmplifier > 2)
                maxSpeed *= 1.3;
        }
        int justBoostEffectAmplifier = getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP);
        if (justBoostEffectAmplifier > 0) {
            maxSpeed *= justBoostEffectAmplifier * 0.35 + 1;
            if (justBoostEffectAmplifier > 2)
                maxSpeed *= 1.3;
        }
        if (getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) > 0) {
            maxSpeed *= getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) * 0.5 + 1;
        }

        Set<Block> interactiveBlocks = new HashSet<>();
        getInteractiveBlocks(player, event.getFrom()).forEach(block -> {
            interactiveBlocks.add(block);
            interactiveBlocks.add(block.getRelative(BlockFace.UP));
        });
        getInteractiveBlocks(player, event.getTo()).forEach(block -> {
            interactiveBlocks.add(block);
            interactiveBlocks.add(block.getRelative(BlockFace.UP));
        });
        for (Block block : interactiveBlocks)
            if (!isActuallyPassable(block)) {
                maxSpeed *= 1.15;
                break;
            }

        if (hSpeed < maxSpeed)
            return;

        buffer.put("airFlags", buffer.getInt("airFlags") + 1);
        if (buffer.getInt("airFlags") <= 2)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, event, buffer, 5000);
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void beforeAirMovement(LACAsyncPlayerMoveEvent event) {
        LACPlayer lacPlayer = event.getLacPlayer();
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer, true))
            return;

        if (getEffectAmplifier(lacPlayer.cache, PotionEffectType.SPEED) > 5 ||
                getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("LEVITATION")) > 0 ||
                getEffectAmplifier(lacPlayer.cache, VerUtil.potions.get("SLOW_FALLING")) > 1 ||
                getEffectAmplifier(lacPlayer.cache, PotionEffectType.JUMP) > 5) {
            Buffer buffer = getBuffer(player, true);
            long currentTime = System.currentTimeMillis();
            buffer.put("airEffectTime", currentTime);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAirTeleport(PlayerTeleportEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("airFlags", 0);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAirWorldChange(PlayerChangedWorldEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("airFlags", 0);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAirRespawn(PlayerRespawnEvent event) {
        if (isExternalNPC(event)) return;
        Buffer buffer = getBuffer(event.getPlayer(), true);
        buffer.put("airFlags", 0);
    }

}
