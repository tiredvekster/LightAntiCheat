package me.vekster.lightanticheat.util.hook.plugin;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class HookUtil {

    private static final Map<String, HPlugin> PLUGINS = new HashMap<>();

    static class HPlugin {
        public HPlugin(long lastCheck, boolean isInstalled) {
            this.lastCheck = lastCheck;
            this.isInstalled = isInstalled;
        }

        long lastCheck;
        boolean isInstalled;
    }

    protected static boolean isPlugin(String pluginName) {
        HPlugin hPlugin = PLUGINS.getOrDefault(pluginName, null);
        if (hPlugin == null || System.currentTimeMillis() - hPlugin.lastCheck > 1111) {
            hPlugin = new HPlugin(System.currentTimeMillis(),
                    Bukkit.getPluginManager().getPlugin(pluginName) != null);
            PLUGINS.put(pluginName, hPlugin);
        }
        return hPlugin.isInstalled;
    }

}
