package me.vekster.lightanticheat.version.identifier;

import org.bukkit.Bukkit;

public class VerIdentifier {

    private static LACVersion serverVersion = null;
    private static int[] minecraftVersion = null;

    public static LACVersion getVersion() {
        if (serverVersion != null)
            return serverVersion;

        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1);
        if (version.startsWith("v1_8"))
            serverVersion = LACVersion.V1_8;
        else if (version.startsWith("v1_9"))
            serverVersion = LACVersion.V1_9;
        else if (version.startsWith("v1_10"))
            serverVersion = LACVersion.V1_10;
        else if (version.startsWith("v1_11"))
            serverVersion = LACVersion.V1_11;
        else if (version.startsWith("v1_12"))
            serverVersion = LACVersion.V1_12;
        else if (version.startsWith("v1_13"))
            serverVersion = LACVersion.V1_13;
        else if (version.startsWith("v1_14"))
            serverVersion = LACVersion.V1_14;
        else if (version.startsWith("v1_15"))
            serverVersion = LACVersion.V1_15;
        else if (version.startsWith("v1_16"))
            serverVersion = LACVersion.V1_16;
        else if (version.startsWith("v1_17"))
            serverVersion = LACVersion.V1_17;
        else if (version.startsWith("v1_18"))
            serverVersion = LACVersion.V1_18;
        else if (version.startsWith("v1_19"))
            serverVersion = LACVersion.V1_19;
        else if (version.startsWith("v1_20"))
            serverVersion = LACVersion.V1_20;
        else serverVersion = LACVersion.V1_21;
        return serverVersion;
    }

    public static int[] getMinecraftVersion() {
        if (minecraftVersion != null)
            return minecraftVersion.clone();

        String bukkitVersion = Bukkit.getBukkitVersion();
        String numeric = bukkitVersion.split("-")[0];
        String[] parts = numeric.split("\\.");

        int major = parts.length > 0 ? parseVersionPart(parts[0]) : 0;
        int minor = parts.length > 1 ? parseVersionPart(parts[1]) : 0;
        int patch = parts.length > 2 ? parseVersionPart(parts[2]) : 0;

        minecraftVersion = new int[] { major, minor, patch };
        return minecraftVersion.clone();
    }

    public static boolean isAtLeastMinecraft(int major, int minor, int patch) {
        int[] current = getMinecraftVersion();
        if (current[0] != major)
            return current[0] > major;
        if (current[1] != minor)
            return current[1] > minor;
        return current[2] >= patch;
    }

    private static int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

}
