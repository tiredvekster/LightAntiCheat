package me.vekster.lightanticheat.util.hook.plugin.simplehook;

import me.vekster.lightanticheat.util.hook.plugin.HookUtil;
import org.bukkit.Material;

public class McMMOHook extends HookUtil {

    private static final String PLUGIN_NAME = "mcMMO";

    public static boolean isPrevented(Material material) {
        if (!isPlugin(PLUGIN_NAME))
            return false;
        String name = material.name();
        if (name.endsWith("_LOG") || name.endsWith("_LEAVES"))
            return true;
        return false;
    }

}
