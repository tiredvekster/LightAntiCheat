package me.vekster.lightanticheat.util.hook.simplehook;

import me.vekster.lightanticheat.util.hook.HookUtil;

public class EliteMobsHook extends HookUtil {

    private static final String PLUGIN_NAME = "EliteMobs";

    public static boolean isPluginInstalled() {
        return isPlugin(PLUGIN_NAME);
    }

}
