package me.vekster.lightanticheat.version.identifier;

import org.bukkit.Bukkit;

public class VerIdentifier {

    private static LACVersion serverVersion = null;

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
            serverVersion = LACVersion.V1_17;
        else if (version.startsWith("v1_19"))
            serverVersion = LACVersion.V1_19;
        else serverVersion = LACVersion.V1_20;
        return serverVersion;
    }

}
