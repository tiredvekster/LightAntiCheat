package me.vekster.lightanticheat.util.violation;

import me.vekster.lightanticheat.api.event.LACPunishmentEvent;
import me.vekster.lightanticheat.api.event.LACViolationEvent;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.CheckSetting;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.player.cache.history.HistoryElement;
import me.vekster.lightanticheat.player.violation.PlayerViolations;
import me.vekster.lightanticheat.util.async.AsyncUtil;
import me.vekster.lightanticheat.util.config.ConfigManager;
import me.vekster.lightanticheat.util.config.placeholder.PlaceholderConvertor;
import me.vekster.lightanticheat.util.detection.CheckUtil;
import me.vekster.lightanticheat.util.detection.LeanTowards;
import me.vekster.lightanticheat.util.folia.FoliaUtil;
import me.vekster.lightanticheat.util.logger.Logger;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Async;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ViolationHandler implements Listener {

    private static boolean isVerticalSetback(Player player, LACPlayer lacPlayer, CheckSetting checkSetting) {
        if (checkSetting.name.type != CheckName.CheckType.MOVEMENT)
            return false;
        if (CheckUtil.isOnGround(player, 0.2, lacPlayer.cache, LeanTowards.TRUE))
            return false;
        if (lacPlayer.cache.history.onEvent.onGround.get(HistoryElement.FIRST).towardsTrue)
            return false;

        Set<CheckName> checks = new HashSet<>(Arrays.asList(
                CheckName.FLIGHT_A, CheckName.FLIGHT_B, CheckName.FLIGHT_C
        ));
        Set<CheckName> additionalChecks = new HashSet<>(Arrays.asList(
                CheckName.SPEED_A, CheckName.SPEED_B, CheckName.SPEED_C, CheckName.JUMP_A, CheckName.JUMP_B
        ));

        boolean vSetback = checks.contains(checkSetting.name);
        boolean afterVSetback = false;
        if (additionalChecks.contains(checkSetting.name)) {
            for (CheckName checkName : checks) {
                if (lacPlayer.violations.getViolations(checkName) < Math.min(5, checkSetting.punishmentVio / 2))
                    continue;
                afterVSetback = true;
                break;
            }
        }

        return vSetback || afterVSetback;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFlag(LACViolationEvent event) {
        if (!event.getPlayer().isOnline() || event.getAcPlayer().leaveTime != 0L)
            return;
        if (ConfigManager.Config.Api.enabled && event.isCancelled())
            return;

        LACPlayer lacPlayer = event.getAcPlayer();
        CheckSetting checkSetting = event.getCheckSettings();

        if (checkSetting.punishmentVio == lacPlayer.violations.getViolations(checkSetting.name)) {
            Bukkit.getServer().getPluginManager().callEvent(new LACPunishmentEvent(event));
            return;
        }

        if (lacPlayer.violations.getViolations(checkSetting.name) < checkSetting.punishmentVio)
            lacPlayer.violations.increaseViolations(checkSetting.name, 1);

        long currentTime = System.currentTimeMillis();
        if (ConfigManager.Config.Log.enabled) {
            if (ConfigManager.Config.Log.LogViolations.enabled &&
                    currentTime - lacPlayer.violations.violationLogTime > ConfigManager.Config.Log.LogViolations.cooldown) {
                lacPlayer.violations.violationLogTime = currentTime;
                Logger.logFile(PlaceholderConvertor.swapAll(ConfigManager.Config.Log.LogViolations.message,
                        checkSetting, event.getPlayer(), lacPlayer));
            }
        }

        if (ConfigManager.Config.Alerts.BroadcastViolations.enabled &&
                currentTime - lacPlayer.violations.violationDebugTime > ConfigManager.Config.Alerts.BroadcastViolations.cooldown) {
            lacPlayer.violations.violationDebugTime = currentTime;
            Logger.logAlert(ConfigManager.Config.Alerts.BroadcastViolations.message,
                    checkSetting, event.getPlayer(), lacPlayer);
        }

        if (ConfigManager.Config.DiscordWebhook.enabled) {
            if (ConfigManager.Config.DiscordWebhook.SendViolations.enabled &&
                    currentTime - lacPlayer.violations.violationDiscordTime > ConfigManager.Config.DiscordWebhook.SendViolations.cooldown) {
                lacPlayer.violations.violationDiscordTime = currentTime;
                Logger.logDiscord(PlaceholderConvertor.swapAll(ConfigManager.Config.DiscordWebhook.SendViolations.message,
                        checkSetting, event.getPlayer(), lacPlayer), false);
            }
        }

        if (checkSetting.setback && lacPlayer.violations.getViolations(checkSetting.name) >= checkSetting.setbackVio &&
                event.getCancellable() != null) {
            if (!isVerticalSetback(event.getPlayer(), lacPlayer, checkSetting)) {
                event.getCancellable().setCancelled(true);
            } else {
                Location location = event.getPlayer().getLocation();
                boolean isDownBlocks = true;
                for (int i = 1; i <= 25; i++) {
                    boolean cancel = false;
                    Set<Block> blocks = new HashSet<>();
                    if (isDownBlocks || i == 25) {
                        blocks.addAll(new HashSet<>(CheckUtil.getDownBlocks(event.getPlayer(), location, 0.05)));
                    } else {
                        Block block = AsyncUtil.getBlock(event.getPlayer().getLocation());
                        if (block != null) {
                            blocks.add(block);
                            blocks.add(block.getRelative(BlockFace.DOWN));
                        }
                    }
                    isDownBlocks = !isDownBlocks;
                    for (Block block : blocks)
                        if (!CheckUtil.isActuallyPassable(block) || i == 25) {
                            cancel = true;
                            break;
                        }
                    if (cancel) {
                        for (Block block : CheckUtil.getWithinBlocks(event.getPlayer())) {
                            if (!CheckUtil.isActuallyPassable(block)) {
                                FoliaUtil.teleportPlayer(event.getPlayer(), location.add(0, 1 - (location.getY() % 1), 0));
                                break;
                            }
                        }
                        boolean slab = true;
                        for (Block block : CheckUtil.getDownBlocks(event.getPlayer(), 0.1)) {
                            if (!block.getType().name().endsWith("_SLAB")) {
                                slab = false;
                                break;
                            }
                        }
                        if (slab) FoliaUtil.teleportPlayer(event.getPlayer(), location.subtract(0, 0.5, 0));
                        break;
                    }
                    FoliaUtil.teleportPlayer(event.getPlayer(), location.subtract(0, 1, 0));
                }
            }
        }

        if (checkSetting.punishmentVio == lacPlayer.violations.getViolations(checkSetting.name))
            Bukkit.getServer().getPluginManager().callEvent(new LACPunishmentEvent(event));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPunishment(LACPunishmentEvent event) {
        if (!event.getPlayer().isOnline() || event.getAcPlayer().leaveTime != 0L)
            return;
        if (ConfigManager.Config.Api.enabled && event.isCancelled())
            return;

        LACPlayer lacPlayer = event.getAcPlayer();
        CheckSetting checkSetting = event.getCheckSettings();
        long currentTime = System.currentTimeMillis();
        if (ConfigManager.Config.Log.enabled) {
            if (ConfigManager.Config.Log.LogPunishments.enabled &&
                    currentTime - lacPlayer.violations.punishmentLogTime > ConfigManager.Config.Log.LogPunishments.cooldown) {
                lacPlayer.violations.punishmentLogTime = currentTime;
                Logger.logFile(PlaceholderConvertor.swapAll(ConfigManager.Config.Log.LogPunishments.message,
                        checkSetting, event.getPlayer(), lacPlayer));
            }
        }

        if (ConfigManager.Config.Alerts.BroadcastPunishments.enabled &&
                currentTime - lacPlayer.violations.punishmentDebugTime > ConfigManager.Config.Alerts.BroadcastPunishments.cooldown) {
            lacPlayer.violations.punishmentDebugTime = currentTime;
            Logger.logAlert(ConfigManager.Config.Alerts.BroadcastPunishments.message,
                    checkSetting, event.getPlayer(), lacPlayer);
        }

        if (ConfigManager.Config.DiscordWebhook.enabled) {
            if (ConfigManager.Config.DiscordWebhook.SendPunishments.enabled &&
                    currentTime - lacPlayer.violations.punishmentDiscordTime > ConfigManager.Config.DiscordWebhook.SendPunishments.cooldown) {
                lacPlayer.violations.punishmentDiscordTime = currentTime;
                Logger.logDiscord(PlaceholderConvertor.swapAll(ConfigManager.Config.DiscordWebhook.SendPunishments.message,
                        checkSetting, event.getPlayer(), lacPlayer), true);
            }
        }

        if (checkSetting.punishable &&
                checkSetting.punishmentCommands != null && !checkSetting.punishmentCommands.isEmpty()) {
            Scheduler.runTask(false, () -> {
                for (String command : checkSetting.punishmentCommands)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            PlaceholderConvertor.colorize(PlaceholderConvertor.swapAll(command, checkSetting, event.getPlayer(), lacPlayer), true));
            });
        }

        lacPlayer.violations = new PlayerViolations();
    }

}
