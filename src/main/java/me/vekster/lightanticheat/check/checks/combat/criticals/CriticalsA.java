package me.vekster.lightanticheat.check.checks.combat.criticals;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.check.checks.combat.CombatCheck;
import me.vekster.lightanticheat.event.playerattack.LACAsyncPlayerAttackEvent;
import me.vekster.lightanticheat.event.playerattack.LACPlayerAttackEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.ValhallaMMOHook;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

/**
 * Packet/Bypass mode
 */
public class CriticalsA extends CombatCheck implements Listener {
    public CriticalsA() {
        super(CheckName.CRITICALS_A);
    }

    @EventHandler
    public void onHit(LACPlayerAttackEvent event) {
        if (!event.isEntityAttackCause())
            return;
        if (ValhallaMMOHook.isPluginInstalled())
            return;
        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer))
            return;

        if (player.isFlying() || player.isInsideVehicle() || lacPlayer.isGliding() || lacPlayer.isRiptiding() ||
                lacPlayer.isClimbing() || lacPlayer.isInWater())
            return;
        if (cache.flyingTicks >= -3 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -3)
            return;
        long time = System.currentTimeMillis();
        if (time - cache.lastInsideVehicle <= 150 || time - cache.lastInWater <= 150 ||
                time - cache.lastWasFished <= 4000 || time - cache.lastTeleport <= 700 ||
                time - cache.lastRespawn <= 500 || time - cache.lastEntityVeryNearby <= 500 ||
                time - cache.lastSlimeBlock <= 500 || time - cache.lastHoneyBlock <= 500 ||
                time - cache.lastWasHit <= 350 || time - cache.lastWasDamaged <= 150 ||
                time - cache.lastKbVelocity <= 500)
            return;
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        if (getEffectAmplifier(cache, PotionEffectType.BLINDNESS) != 0 ||
                getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) != 0)
            return;

        for (Block block : getWithinBlocks(player)) {
            if (!isActuallyPassable(block))
                return;
        }

        boolean ground = false;
        for (Block block : getDownBlocks(player, 0.1)) {
            if (!isActuallyPassable(block)) {
                ground = true;
                break;
            }
        }
        if (!ground) return;

        if (((Entity) player).isOnGround() || player.getFallDistance() == 0)
            return;
        if (!isBlockHeight((float) getBlockY(player.getLocation().getY())))
            return;

        Buffer buffer = getBuffer(player, true);
        if (getItemStackAttributes(player, "PLAYER_SWEEPING_DAMAGE_RATIO") != 0 ||
                getPlayerAttributes(player).getOrDefault("PLAYER_SWEEPING_DAMAGE_RATIO", 0.0) > 0.01)
            buffer.put("attribute", System.currentTimeMillis());
        if (System.currentTimeMillis() - buffer.getLong("attribute") < 2500)
            return;

        callViolationEvent(player, lacPlayer, event.getEvent());
    }

    @EventHandler
    public void onAsyncHit(LACAsyncPlayerAttackEvent event) {
        if (event.getEntityId() == 0)
            return;
        if (ValhallaMMOHook.isPluginInstalled())
            return;

        LACPlayer lacPlayer = event.getLacPlayer();
        PlayerCache cache = lacPlayer.cache;
        Player player = event.getPlayer();

        if (!isCheckAllowed(player, lacPlayer))
            return;

        if (FloodgateHook.isBedrockPlayer(player, true))
            return;

        if (player.getFallDistance() == 0 || ((Entity) player).isOnGround())
            return;

        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        if (getEffectAmplifier(cache, PotionEffectType.BLINDNESS) != 0 ||
                getEffectAmplifier(cache, VerUtil.potions.get("LEVITATION")) != 0)
            return;

        if (player.isFlying() || player.isInsideVehicle() || lacPlayer.isGliding() || lacPlayer.isRiptiding() ||
                lacPlayer.isClimbing() || lacPlayer.isInWater())
            return;
        if (cache.flyingTicks >= -3 || cache.climbingTicks >= -2 ||
                cache.glidingTicks >= -3 || cache.riptidingTicks >= -3)
            return;
        long time = System.currentTimeMillis();
        if (time - cache.lastInsideVehicle <= 150 || time - cache.lastInWater <= 150 ||
                time - cache.lastWasFished <= 4000 || time - cache.lastTeleport <= 700 ||
                time - cache.lastRespawn <= 500 || time - cache.lastEntityVeryNearby <= 500 ||
                time - cache.lastSlimeBlock <= 500 || time - cache.lastHoneyBlock <= 500 ||
                time - cache.lastWasHit <= 350 || time - cache.lastWasDamaged <= 150 ||
                time - cache.lastKbVelocity <= 500)
            return;

        for (Block block : getWithinBlocks(player)) {
            if (!isActuallyPassable(block) || !isActuallyPassable(block.getRelative(BlockFace.UP)))
                return;
        }

        boolean ground = false;
        for (Block block : getDownBlocks(player, 0.1)) {
            if (!isActuallyPassable(block) || !isActuallyPassable(block.getRelative(BlockFace.DOWN))) {
                ground = true;
                break;
            }
        }
        if (!ground) return;

        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double previousY = Double.MIN_VALUE;
        boolean bounce = false;
        for (HistoryElement historyElement : HistoryElement.values()) {
            double y = cache.history.onPacket.location.get(historyElement).getY();
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            if (previousY == Double.MIN_VALUE) {
                previousY = y;
                continue;
            }
            if (!bounce && Math.abs(y - previousY) < LOWEST_BLOCK_HEIGHT)
                bounce = true;
            previousY = y;
        }
        if (!bounce || maxY - minY >= 0.3)
            return;

        Buffer buffer = getBuffer(player, true);
        if (getItemStackAttributes(player, "PLAYER_SWEEPING_DAMAGE_RATIO") != 0 ||
                getPlayerAttributes(player).getOrDefault("PLAYER_SWEEPING_DAMAGE_RATIO", 0.0) > 0.01)
            buffer.put("attribute", System.currentTimeMillis());
        if (System.currentTimeMillis() - buffer.getLong("attribute") < 2500)
            return;

        Scheduler.runTask(true, () -> {
            callViolationEventIfRepeat(player, lacPlayer, null, buffer, Main.getBufferDurationMils() - 1000);
        });
    }

}
