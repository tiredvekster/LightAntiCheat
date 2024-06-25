package me.vekster.lightanticheat.check.checks.movement;

import me.vekster.lightanticheat.check.Check;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.buffer.Buffer;
import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.event.playermove.LACPlayerMoveEvent;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.hook.simplehook.EnchantsSquaredHook;
import me.vekster.lightanticheat.version.VerPlayer;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MovementCheck extends Check {
    public MovementCheck(CheckName name) {
        super(name);
    }

    public abstract boolean isConditionAllowed(Player player, LACPlayer lacPlayer, PlayerCache cache,
                                               boolean isClimbing, boolean isInWater,
                                               boolean isFlying, boolean isInsideVehicle, boolean isGliding, boolean isRiptiding);

    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, LACPlayerMoveEvent event,
                                      boolean isClimbing, boolean isInWater) {
        return isConditionAllowed(player, lacPlayer, lacPlayer.cache,
                isClimbing, isInWater,
                player.isFlying(), player.isInsideVehicle(), lacPlayer.isGliding(), lacPlayer.isRiptiding());
    }

    public boolean isConditionAllowed(Player player, LACPlayer lacPlayer, LACAsyncPlayerMoveEvent event) {
        return isConditionAllowed(player, lacPlayer, lacPlayer.cache,
                event.isPlayerClimbing(), event.isPlayerInWater(),
                event.isPlayerFlying(), event.isPlayerInsideVehicle(), event.isPlayerGliding(), event.isPlayerRiptiding());
    }


    public boolean isPingGlidingPossible(Player player, PlayerCache cache) {
        long currentTime = System.currentTimeMillis();
        if (VerPlayer.getPing(player) > 350 && (currentTime - cache.lastGliding < 2000 || currentTime - cache.lastRiptiding < 3000))
            return true;
        if (VerPlayer.getPing(player) > 500 && (currentTime - cache.lastGliding < 3000 || currentTime - cache.lastRiptiding < 4000))
            return true;
        return false;
    }

    public boolean isLagGlidingPossible(Player player, Buffer buffer, int requiredAccuracy) {
        if (buffer.getInt("methodAccuracy") >= requiredAccuracy)
            return false;

        LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);
        PlayerInventory inventory = player.getInventory();

        boolean elytra = lacPlayer.getArmorPiece(EquipmentSlot.CHEST).getType() == VerUtil.material.get("ELYTRA");
        boolean trident = false;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && itemStack.getType() == VerUtil.material.get("TRIDENT") &&
                    itemStack.getEnchantmentLevel(VerUtil.enchantment.get("RIPTIDE")) != 0) {
                trident = true;
                break;
            }
        }

        int increase = 1;
        if (!elytra) increase = 2;
        if (!elytra && !trident) increase = requiredAccuracy;

        buffer.put("methodAccuracy", buffer.getInt("methodAccuracy") + increase);
        return buffer.getInt("methodAccuracy") < requiredAccuracy;
    }

    public boolean isLagGlidingPossible(Player player, Buffer buffer) {
        return isLagGlidingPossible(player, buffer, 9);
    }

    public void updateDownBlocks(Player player, LACPlayer lacPlayer, Set<Block> downBlocks) {
        if (lacPlayer.violations.getViolations(getCheckSetting(this).name) % 3 != 0)
            return;
        for (Block block : downBlocks)
            lacPlayer.sendBlockDate(block.getLocation(), block);
    }

    public Set<Player> getPlayersForEnchantsSquared(LACPlayer lacPlayer, Player player) {
        if (!EnchantsSquaredHook.isPluginInstalled())
            return Collections.emptySet();
        Set<Player> players = ConcurrentHashMap.newKeySet();
        for (Entity entity : CooldownUtil.getAllEntitiesAsync(lacPlayer.cooldown, player)) {
            if (entity.getType() == EntityType.PLAYER)
                players.add((Player) entity);
        }
        return players;
    }

    public boolean isEnchantsSquaredImpact(Set<Player> players) {
        if (!EnchantsSquaredHook.isPluginInstalled())
            return false;
        for (Player player : players)
            if (EnchantsSquaredHook.hasEnchantment(player, "Rope Dart", "Shockwave"))
                return true;
        return false;
    }

}
