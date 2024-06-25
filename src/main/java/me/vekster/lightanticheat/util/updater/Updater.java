package me.vekster.lightanticheat.util.updater;

import me.vekster.lightanticheat.Main;
import me.vekster.lightanticheat.util.config.ConfigManager;
import me.vekster.lightanticheat.util.config.placeholder.Placeholder;
import me.vekster.lightanticheat.util.config.placeholder.PlaceholderConvertor;
import me.vekster.lightanticheat.util.logger.LogType;
import me.vekster.lightanticheat.util.logger.Logger;
import me.vekster.lightanticheat.util.metrics.Metrics;
import me.vekster.lightanticheat.util.permission.ACPermission;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class Updater implements Listener {

    private static String latestVersion;

    public static void loadUpdateChecker() {
        new Metrics(Main.getInstance(), Main.getStatsId());

        Scheduler.runTaskTimer(() -> {
            updateLatestVersion();
        }, 0, 30 * 60 * 20);

        Scheduler.runTaskLater(() -> {
            if (!ConfigManager.Config.enabled || !ConfigManager.Config.UpdateChecker.enabled)
                return;
            if (!ConfigManager.Config.UpdateChecker.Notification.Console.enabled)
                return;

            if (latestVersion == null)
                return;
            if (latestVersion.equals(Main.getInstance().getDescription().getVersion()))
                return;
            try {
                if (Long.parseLong(Main.getInstance().getDescription().getVersion().replaceAll("\\.", "")) >
                        Long.parseLong(latestVersion.replaceAll("\\.", "")))
                    return;
            } catch (NumberFormatException ignored) {
            }

            String message = ConfigManager.Config.UpdateChecker.Notification.Console.message;
            message = message.replaceAll("%latest-version%", latestVersion);
            message = PlaceholderConvertor.colorize(PlaceholderConvertor.swapSome(message,
                    Placeholder.PREFIX, Placeholder.VERSION, Placeholder.TPS), false);
            Bukkit.getConsoleSender().sendMessage(message);
        }, 5 * 20);
    }

    private static void updateLatestVersion() {
        if (!ConfigManager.Config.enabled || !ConfigManager.Config.UpdateChecker.enabled)
            return;

        Scheduler.runTaskAsynchronously(false, () -> {
            InputStream inputStream;
            try {
                inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + Main.getPluginId() + "/~")
                        .openStream();
            } catch (IOException e) {
                Logger.logConsole(LogType.WARN, "(" + Main.getInstance().getName() + ") " +
                        "An error occurred while checking for the plugin updates.");
                return;
            }
            Scheduler.runTask(false, () -> {
                Scanner scanner = new Scanner(inputStream);
                if (!scanner.hasNext())
                    return;
                latestVersion = scanner.next();
            });
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!ConfigManager.Config.enabled || !ConfigManager.Config.UpdateChecker.enabled)
            return;
        if (!ConfigManager.Config.UpdateChecker.Notification.OnJoin.enabled)
            return;

        if (latestVersion == null ||
                latestVersion.equals(Main.getInstance().getDescription().getVersion()))
            return;
        Player player = event.getPlayer();
        if (ConfigManager.Config.UpdateChecker.Notification.OnJoin.requirePermission &&
                !player.hasPermission(ACPermission.ALERTS_NOTIFY) &&
                !player.hasPermission(ACPermission.ALERTS))
            return;

        String message = ConfigManager.Config.UpdateChecker.Notification.OnJoin.message;
        message = message.replaceAll("%latest-version%", latestVersion);
        message = PlaceholderConvertor.colorize(PlaceholderConvertor.swapSome(message,
                Placeholder.PREFIX, Placeholder.VERSION, Placeholder.TPS), true);
        player.sendMessage(message);
    }

}
