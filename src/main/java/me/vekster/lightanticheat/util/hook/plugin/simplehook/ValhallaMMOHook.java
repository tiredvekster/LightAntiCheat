package me.vekster.lightanticheat.util.hook.plugin.simplehook;

import me.vekster.lightanticheat.util.hook.plugin.HookUtil;

public class ValhallaMMOHook extends HookUtil {

    private static final String PLUGIN_NAME = "ValhallaMMO";

    public static boolean isPluginInstalled() {
        return isPlugin(PLUGIN_NAME);
    }

}
