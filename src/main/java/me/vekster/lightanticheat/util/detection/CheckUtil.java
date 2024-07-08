package me.vekster.lightanticheat.util.detection;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.api.DetectionStatus;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.CheckSetting;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.PlayerCache;
import me.vekster.lightanticheat.util.annotation.SecureAsync;
import me.vekster.lightanticheat.util.api.ApiUtil;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.config.ConfigManager;
import me.vekster.lightanticheat.util.cooldown.CooldownUtil;
import me.vekster.lightanticheat.util.detection.specific.PassableUtil;
import me.vekster.lightanticheat.util.hook.plugin.FloodgateHook;
import me.vekster.lightanticheat.util.hook.plugin.simplehook.ExecutableItemsHook;
import me.vekster.lightanticheat.util.logger.LogType;
import me.vekster.lightanticheat.util.logger.Logger;
import me.vekster.lightanticheat.util.npc.ExternalNPCUtil;
import me.vekster.lightanticheat.util.permission.ACPermission;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import me.vekster.lightanticheat.util.tps.TPSCalculator;
import me.vekster.lightanticheat.version.VerPlayer;
import me.vekster.lightanticheat.version.VerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CheckUtil extends PassableUtil {

    @SecureAsync
    public static boolean isCheckAllowed(CheckSetting checkSetting, Player player, LACPlayer lacPlayer, boolean async) {
        if (checkSetting == null || player == null || lacPlayer == null)
            return false;

        if (!checkSetting.enabled) {
            Scheduler.runTask(true, () -> {
                Logger.logConsole(LogType.ERROR, "(" + Main.getInstance().getName() + ") Something went wrong! " +
                        checkSetting.name.title + " check is supposed to be disabled!");
            });
            return false;
        }

        if (!ConfigManager.Config.Permission.disableAllBypassPermissions) {
            if (CooldownUtil.hasPermission(lacPlayer.cooldown, player, ACPermission.BYPASS, async))
                return false;

            if (ConfigManager.Config.Permission.perCheckBypassPermission &&
                    CooldownUtil.hasPermission(lacPlayer.cooldown, player,
                            ACPermission.BYPASS + "." + checkSetting.name.name().toLowerCase(), async))
                return false;
        }

        DetectionStatus detectionStatus = ApiUtil.getCheckStatus(player, checkSetting.name.name().toLowerCase());
        if (ConfigManager.Config.Api.enabled && detectionStatus != DetectionStatus.ENABLED)
            return false;

        if (TPSCalculator.getTickDurationInMs() >= ConfigManager.Config.LagProtection.tickThreshold)
            return false;
        if (TPSCalculator.getTPS() < checkSetting.minTps)
            return false;

        boolean bedrock = FloodgateHook.isBedrockPlayer(player, async);
        if (!bedrock && !checkSetting.detectJava || bedrock && !checkSetting.detectBedrock)
            return false;
        if (FloodgateHook.isCancelledCombat(checkSetting.name, player, async))
            return false;

        CheckName checkName = checkSetting.name;
        if (!async) {
            if (ExecutableItemsHook.isPrevented(checkName, player))
                return false;
        }

        if (VerPlayer.getPing(player, async) > checkSetting.maxPing)
            return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lacPlayer.joinTime < ConfigManager.Config.LagProtection.ignoreTimeOnJoin ||
                currentTime - lacPlayer.cache.lastWorldChange < ConfigManager.Config.LagProtection.ignoreTimeOnTeleport)
            return false;
        return true;
    }

    public static boolean isCheckAllowed(CheckSetting checkSetting, Player player, LACPlayer lacPlayer) {
        return isCheckAllowed(checkSetting, player, lacPlayer, false);
    }

    public static double distance(Location from, Location to) {
        if (from == null || to == null || AsyncUtil.getWorld(from) != AsyncUtil.getWorld(to))
            return 0;
        return from.distance(to);
    }

    public static double distanceAbsVertical(Location from, Location to) {
        if (from == null || to == null || AsyncUtil.getWorld(from) != AsyncUtil.getWorld(to))
            return 0;
        return Math.abs(to.getY() - from.getY());
    }

    public static double distanceVertical(Location from, Location to) {
        if (from == null || to == null || AsyncUtil.getWorld(from) != AsyncUtil.getWorld(to))
            return 0;
        return to.getY() - from.getY();
    }

    public static double distanceHorizontal(Location from, Location to) {
        if (from == null || to == null || AsyncUtil.getWorld(from) != AsyncUtil.getWorld(to))
            return 0;
        return Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
    }

    @SecureAsync
    public static int getEffectAmplifier(Player player, PotionEffectType type) {
        return VerUtil.getPotionLevel(player, type);
    }

    @SecureAsync
    public static int getEffectAmplifier(PlayerCache cache, PotionEffectType type) {
        PotionEffect effect = cache.potionEffects.getOrDefault(type, null);
        if (effect == null) return 0;
        return effect.getAmplifier() + 1;
    }

    @SecureAsync
    public static double getItemStackAttributes(Player player, String... names) {
        Set<ItemStack> itemStacks = new HashSet<>();
        ItemStack itemInMainHand = VerPlayer.getItemInMainHand(player);
        if (itemInMainHand != null)
            itemStacks.add(itemInMainHand);
        ItemStack itemInOffHand = VerPlayer.getItemInOffHand(player);
        if (itemInOffHand != null)
            itemStacks.add(itemInOffHand);
        for (ItemStack itemStack : player.getInventory().getArmorContents())
            if (itemStack != null)
                itemStacks.add(itemStack);
        double result = 0;
        for (ItemStack itemStack : itemStacks) {
            Map<String, Double> attributes = VerUtil.getAttributes(itemStack);
            for (String name : names)
                if (attributes.containsKey(name))
                    result = Math.max(result, attributes.get(name));
        }
        return result;
    }

    @SecureAsync
    public static Map<String, Double> getPlayerAttributes(Player player) {
        return VerUtil.getAttributes(player);
    }

    @SecureAsync
    public static boolean isExternalNPC(Player player, boolean async) {
        return ExternalNPCUtil.isExternalNPC(player, async);
    }

    public static boolean isExternalNPC(Player player) {
        return isExternalNPC(player, false);
    }

    public static boolean isExternalNPC(PlayerEvent playerEvent) {
        return isExternalNPC(playerEvent.getPlayer(), false);
    }

    @SecureAsync
    public static boolean isExternalNPC(Entity entity, boolean async) {
        return ExternalNPCUtil.isExternalNPC(entity, async);
    }

    public static boolean isExternalNPC(Entity entity) {
        return isExternalNPC(entity, false);
    }

}
