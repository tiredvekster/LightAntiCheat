package me.vekster.lightanticheat.util.logger.text;

import me.vekster.lightanticheat.util.config.ConfigManager;
import org.bukkit.ChatColor;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static boolean legacyColor = false;
    private static final Map<Color, ChatColor> BUKKIT_COLORS = new HashMap<>();

    static {
        BUKKIT_COLORS.put(Color.decode("#000000"), org.bukkit.ChatColor.BLACK);
        BUKKIT_COLORS.put(Color.decode("#0000AA"), org.bukkit.ChatColor.DARK_BLUE);
        BUKKIT_COLORS.put(Color.decode("#00AA00"), org.bukkit.ChatColor.DARK_GRAY);
        BUKKIT_COLORS.put(Color.decode("#00AAAA"), org.bukkit.ChatColor.DARK_AQUA);
        BUKKIT_COLORS.put(Color.decode("#AA0000"), org.bukkit.ChatColor.DARK_RED);
        BUKKIT_COLORS.put(Color.decode("#AA00AA"), org.bukkit.ChatColor.DARK_PURPLE);
        BUKKIT_COLORS.put(Color.decode("#FFAA00"), org.bukkit.ChatColor.GOLD);
        BUKKIT_COLORS.put(Color.decode("#AAAAAA"), org.bukkit.ChatColor.GRAY);
        BUKKIT_COLORS.put(Color.decode("#555555"), org.bukkit.ChatColor.DARK_GRAY);
        BUKKIT_COLORS.put(Color.decode("#5555FF"), org.bukkit.ChatColor.BLUE);
        BUKKIT_COLORS.put(Color.decode("#55FF55"), org.bukkit.ChatColor.GREEN);
        BUKKIT_COLORS.put(Color.decode("#55FFFF"), org.bukkit.ChatColor.AQUA);
        BUKKIT_COLORS.put(Color.decode("#FF5555"), org.bukkit.ChatColor.RED);
        BUKKIT_COLORS.put(Color.decode("#FF55FF"), org.bukkit.ChatColor.LIGHT_PURPLE);
        BUKKIT_COLORS.put(Color.decode("#FFFF55"), org.bukkit.ChatColor.YELLOW);
        BUKKIT_COLORS.put(Color.decode("#FFFFFF"), org.bukkit.ChatColor.WHITE);
    }

    public static String colorize(String text, boolean customColor) {
        if (!text.contains("&"))
            return text;
        if (!ConfigManager.Config.Messages.hexColorCodes)
            customColor = false;

        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String color = text.substring(matcher.start(), matcher.end());
            text = customColor ? text.replace(color, String.valueOf(ColorUtil.chatColorOf(color.substring(1)))) :
                    text.replace(color, String.valueOf(ColorUtil.getBukkitColor(color.substring(1))));
            matcher = pattern.matcher(text);
        }

        text = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', text);
        return text;
    }

    public static net.md_5.bungee.api.ChatColor chatColorOf(String hex) {
        if (legacyColor)
            return ColorUtil.getBukkitColor(hex);
        for (Method method : net.md_5.bungee.api.ChatColor.class.getDeclaredMethods()) {
            if (!method.getName().equals("of"))
                continue;
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1 || !parameterTypes[0].getTypeName().equals("java.lang.String"))
                continue;
            try {
                Object result = method.invoke(net.md_5.bungee.api.ChatColor.class, hex);
                if (result instanceof net.md_5.bungee.api.ChatColor)
                    return (net.md_5.bungee.api.ChatColor) result;
                return ColorUtil.getBukkitColor(hex);
            } catch (IllegalAccessException | InvocationTargetException e) {
                return ColorUtil.getBukkitColor(hex);
            }
        }
        legacyColor = true;
        return ColorUtil.getBukkitColor(hex);
    }

    public static net.md_5.bungee.api.ChatColor getBukkitColor(String hex) {
        Color hexColor;
        try {
            hexColor = Color.decode(hex);
        } catch (IllegalArgumentException e) {
            return net.md_5.bungee.api.ChatColor.BLACK;
        }
        ChatColor closestColor = ChatColor.BLACK;
        double closesDistance = 1000.0;
        for (Color color : BUKKIT_COLORS.keySet()) {
            ChatColor chatColor = BUKKIT_COLORS.get(color);
            double distance = colorDistance(hexColor, color);
            if (distance >= closesDistance)
                continue;
            closestColor = chatColor;
            closesDistance = distance;
        }
        return closestColor.asBungee();
    }

    private static double colorDistance(Color c1, Color c2) {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int rmean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return Math.sqrt((((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8));
    }

    public static String removeColors(String message) {
        Pattern pattern = Pattern.compile("[&§][a-zA-Z0-9]");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, "");
            matcher = pattern.matcher(message);
        }
        pattern = Pattern.compile("[&§]#[a-zA-Z0-9]{6}");
        matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexColor = message.substring(matcher.start(), matcher.end());
            message = message.replace(hexColor, "");
            matcher = pattern.matcher(message);
        }
        return message;
    }

}
