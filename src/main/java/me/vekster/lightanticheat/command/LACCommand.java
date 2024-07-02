package me.vekster.lightanticheat.command;

import me.vekster.lightanticheat.check.Check;
import me.vekster.lightanticheat.check.CheckName;
import me.vekster.lightanticheat.check.CheckSetting;
import me.vekster.lightanticheat.util.hook.server.folia.FoliaUtil;
import me.vekster.lightanticheat.util.player.brand.ClientBrandRecognizer;
import me.vekster.lightanticheat.util.player.cps.CPSListener;
import me.vekster.lightanticheat.player.LACPlayer;
import me.vekster.lightanticheat.util.config.ConfigManager;
import me.vekster.lightanticheat.util.config.placeholder.Placeholder;
import me.vekster.lightanticheat.util.config.placeholder.PlaceholderConvertor;
import me.vekster.lightanticheat.util.permission.ACPermission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class LACCommand implements TabExecutor {

    private static final Placeholder[] SERVER_PLACEHOLDERS = new Placeholder[]{Placeholder.PREFIX, Placeholder.VERSION,
            Placeholder.TPS};

    private static String getNoPermissionError(boolean hex) {
        String noPermissionError = ConfigManager.Config.Messages.ErrorMessages.noPermission;
        noPermissionError = PlaceholderConvertor.swapSome(noPermissionError, SERVER_PLACEHOLDERS);
        noPermissionError = PlaceholderConvertor.colorize(noPermissionError, hex);
        return noPermissionError;
    }

    private static String getInvalidFormatError(String usage, boolean hex) {
        String invalidFormatError = ConfigManager.Config.Messages.ErrorMessages.invalidFormat;
        invalidFormatError = invalidFormatError.replaceAll("%usage%", usage);
        invalidFormatError = PlaceholderConvertor.swapSome(invalidFormatError, SERVER_PLACEHOLDERS);
        invalidFormatError = PlaceholderConvertor.colorize(invalidFormatError, hex);
        return invalidFormatError;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!ConfigManager.Config.enabled) {
            sender.sendMessage("§8This plugin is disabled!");
            return true;
        }

        boolean hex = !(sender instanceof ConsoleCommandSender);
        if (args.length > 0) {
            //client
            if (args[0].equalsIgnoreCase("client")) {
                if (!sender.hasPermission(ACPermission.CLIENT) && !sender.hasPermission(ACPermission.ALL)) {
                    sender.sendMessage(getNoPermissionError(hex));
                    return true;
                }
                if (args.length == 1) {
                    sender.sendMessage(getInvalidFormatError("/" + label + " client <player>", hex));
                    return true;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(getInvalidFormatError("/" + label + " client <player>", hex));
                    return true;
                }
                String message = ConfigManager.Config.Messages.CommandMessages.Client.message;
                message = message.replaceAll("%client%", ClientBrandRecognizer.getClientBrand(player));
                message = PlaceholderConvertor.swapSome(message, SERVER_PLACEHOLDERS);
                message = PlaceholderConvertor.swapPlayer(message, player);
                message = PlaceholderConvertor.colorize(message, hex);
                sender.sendMessage(message);
                return true;
            }
            //cps
            if (args[0].equalsIgnoreCase("cps")) {
                if (!sender.hasPermission(ACPermission.CPS) && !sender.hasPermission(ACPermission.ALL)) {
                    sender.sendMessage(getNoPermissionError(hex));
                    return true;
                }
                if (args.length == 1) {
                    sender.sendMessage(getInvalidFormatError("/" + label + " cps <player>", hex));
                    return true;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(getInvalidFormatError("/" + label + " cps <player>", hex));
                    return true;
                }
                String message = ConfigManager.Config.Messages.CommandMessages.Cps.message;
                message = message.replaceAll("%cps%", String.valueOf(CPSListener.getCps(player)));
                message = PlaceholderConvertor.swapSome(message, SERVER_PLACEHOLDERS);
                message = PlaceholderConvertor.swapPlayer(message, player);
                message = PlaceholderConvertor.colorize(message, hex);
                sender.sendMessage(message);
                return true;
            }
            //reload
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission(ACPermission.RELOAD) && !sender.hasPermission(ACPermission.ALL)) {
                    sender.sendMessage(getNoPermissionError(hex));
                    return true;
                }
                long startTime = System.currentTimeMillis();
                ConfigManager.reloadConfig();
                String message = ConfigManager.Config.Messages.CommandMessages.Reload.message;
                message = message.replaceAll("%time%", String.valueOf(System.currentTimeMillis() - startTime));
                message = PlaceholderConvertor.swapSome(message, SERVER_PLACEHOLDERS);
                message = PlaceholderConvertor.colorize(message, hex);
                sender.sendMessage(message);
                return true;
            }
            //checks
            if (args[0].equalsIgnoreCase("checks")) {
                if (!sender.hasPermission(ACPermission.CHECKS) && !sender.hasPermission(ACPermission.ALL)) {
                    sender.sendMessage(getNoPermissionError(hex));
                    return true;
                }

                StringBuilder all = new StringBuilder();
                Map<CheckName.CheckType, StringBuilder> CHECKS = new HashMap<>();
                for (CheckName checkName : CheckName.values()) {
                    CheckSetting checkSetting = Check.getCheckSetting(checkName);
                    all.append(checkSetting.enabled ? "§a" : "§c")
                            .append(checkSetting.name.title)
                            .append("§7, ");
                    StringBuilder stringBuilder = CHECKS.getOrDefault(checkName.type, new StringBuilder())
                            .append(checkSetting.enabled ? "§a" : "§c")
                            .append(checkSetting.name.title).append("§7, ");
                    CHECKS.put(checkName.type, stringBuilder);
                }
                String message = ConfigManager.Config.Messages.CommandMessages.Checks.message;
                message = message.replaceAll("%checks%", all.substring(0, all.length() - 2) + "§r");
                for (CheckName.CheckType checkType : CHECKS.keySet()) {
                    message = message.replaceAll("%" + checkType.toString().toLowerCase() + "_checks%",
                            CHECKS.get(checkType).substring(0, CHECKS.get(checkType).length() - 2) + "§r");
                }

                message = PlaceholderConvertor.swapSome(message, SERVER_PLACEHOLDERS);
                message = PlaceholderConvertor.colorize(message, hex);
                sender.sendMessage(message);
                return true;
            }
            //tps
            if (args[0].equalsIgnoreCase("tps")) {
                if (!sender.hasPermission(ACPermission.TPS) && !sender.hasPermission(ACPermission.ALL)) {
                    sender.sendMessage(getNoPermissionError(hex));
                    return true;
                }
                String message = ConfigManager.Config.Messages.CommandMessages.Tps.message;
                message = PlaceholderConvertor.swapSome(message, SERVER_PLACEHOLDERS);
                message = PlaceholderConvertor.colorize(message, hex);
                sender.sendMessage(message);
                return true;
            }
            //ping
            if (args[0].equalsIgnoreCase("ping")) {
                if (!sender.hasPermission(ACPermission.PING) && !sender.hasPermission(ACPermission.ALL)) {
                    sender.sendMessage(getNoPermissionError(hex));
                    return true;
                }
                if (args.length == 1) {
                    sender.sendMessage(getInvalidFormatError("/" + label + " ping <player>", hex));
                    return true;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(getInvalidFormatError("/" + label + " ping <player>", hex));
                    return true;
                }

                String message = ConfigManager.Config.Messages.CommandMessages.Ping.message;
                message = PlaceholderConvertor.swapSome(message, SERVER_PLACEHOLDERS);
                message = PlaceholderConvertor.swapPlayer(message, player);
                message = PlaceholderConvertor.colorize(message, hex);
                sender.sendMessage(message);
                return true;
            }
            //alerts
            if (args[0].equalsIgnoreCase("alerts")) {
                if (!sender.hasPermission(ACPermission.ALERTS_TOGGLE) && !sender.hasPermission(ACPermission.ALERTS) &&
                        !sender.hasPermission(ACPermission.ALL)) {
                    sender.sendMessage(getNoPermissionError(hex));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§8This command is only available for players");
                    return true;
                }
                Player player = (Player) sender;
                LACPlayer lacPlayer = LACPlayer.getLacPlayer(player);
                lacPlayer.cache.alerts = !lacPlayer.cache.alerts;

                String message = lacPlayer.cache.alerts ?
                        ConfigManager.Config.Messages.CommandMessages.Alerts.toggledOnMessage :
                        ConfigManager.Config.Messages.CommandMessages.Alerts.toggledOffMessage;
                message = PlaceholderConvertor.swapSome(message, SERVER_PLACEHOLDERS);
                message = PlaceholderConvertor.colorize(message, hex);
                sender.sendMessage(message);
                return true;
            }
            //teleport
            if (args[0].equalsIgnoreCase("teleport")) {
                if (!sender.hasPermission(ACPermission.ALERTS_TELEPORT) && !sender.hasPermission(ACPermission.ALERTS) &&
                        !sender.hasPermission(ACPermission.ALL)) {
                    sender.sendMessage(getNoPermissionError(hex));
                    return true;
                }
                if (args.length != 5 && args.length != 7) {
                    sender.sendMessage(getInvalidFormatError("/" + label + " teleport <world> <x> <y> <z>", hex));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§8This command is only available for players");
                    return true;
                }
                Player player = (Player) sender;
                World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    sender.sendMessage(getInvalidFormatError("/" + label + " teleport <world> <x> <y> <z>", hex));
                    return true;
                }
                double x;
                double y;
                double z;
                float yaw = 0;
                float pitch = 0;
                try {
                    x = Double.parseDouble(args[2]);
                    y = Double.parseDouble(args[3]);
                    z = Double.parseDouble(args[4]);
                    if (args.length == 7) {
                        yaw = Float.parseFloat(args[5]);
                        pitch = Float.parseFloat(args[6]);
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(getInvalidFormatError("/" + label + " teleport <world> <x> <y> <z>", hex));
                    return true;
                }
                if (args.length == 5)
                    FoliaUtil.teleportPlayer(player, new Location(world, x, y, z));
                else
                    FoliaUtil.teleportPlayer(player, new Location(world, x, y, z, yaw, pitch));
                return true;
            }
        }

        String message = ConfigManager.Config.Messages.CommandMessages.Help.message;
        message = PlaceholderConvertor.swapSome(message, SERVER_PLACEHOLDERS);
        message = PlaceholderConvertor.colorize(message, hex);
        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!ConfigManager.Config.enabled)
            return Collections.emptyList();

        if (args.length == 1)
            return getTabComplete(args, "checks", "reload", "client", "cps", "tps", "ping", "alerts", "teleport");
        if (args.length == 2 &&
                (args[0].equalsIgnoreCase("client") ||
                        args[0].equalsIgnoreCase("cps") ||
                        args[0].equalsIgnoreCase("ping")))
            return null;
        if (args.length >= 2 && args[0].equalsIgnoreCase("teleport")) {
            if (args.length == 2)
                return getTabComplete(args, "world", "world_nether", "world_the_end");
            if (args.length == 3)
                return getTabComplete(args, "x");
            if (args.length == 4)
                return getTabComplete(args, "y");
            if (args.length == 5)
                return getTabComplete(args, "z");
            if (args.length == 6)
                return getTabComplete(args, "yaw");
            if (args.length == 7)
                return getTabComplete(args, "pitch");
        }
        return Collections.emptyList();
    }

    private static List<String> getTabComplete(String[] args, String... completes) {
        List<String> strings = Arrays.asList(completes);
        if (args.length == 0)
            return strings;
        return strings.stream().filter(s -> s.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }

}
