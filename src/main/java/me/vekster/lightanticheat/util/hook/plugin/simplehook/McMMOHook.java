package me.vekster.lightanticheat.util.hook.plugin.simplehook;

import me.vekster.lightanticheat.util.hook.plugin.HookUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class McMMOHook extends HookUtil {

    private static final String PLUGIN_NAME = "mcMMO";
    private static final String ABILITY_API_CLASS = "com.gmail.nossr50.api.AbilityAPI";
    private static final String[] BLOCK_BREAK_ABILITY_METHODS = {
            "treeFellerEnabled",
            "superBreakerEnabled",
            "gigaDrillBreakerEnabled",
            "greenTerraEnabled"
    };
    private static final long ABILITY_API_RETRY_DELAY_MS = 5000;

    private static volatile boolean abilityApiInitialized = false;
    private static volatile Method[] blockBreakAbilityMethods = null;
    private static volatile long lastAbilityApiInitAttempt = 0;

    /**
     * Prefer mcMMO runtime API for active abilities when available.
     * Falls back to the legacy material-based check when the API is unavailable or cannot be queried.
     */
    public static boolean isPrevented(Player player, Block block) {
        if (!isPlugin(PLUGIN_NAME))
            return false;

        Boolean activeAbility = isBlockBreakAbilityActive(player);
        if (activeAbility != null)
            return activeAbility;

        // API unavailable / unknown -> legacy fallback
        return isPrevented(block.getType());
    }

    /**
     * Legacy fallback behavior (kept for compatibility).
     */
    public static boolean isPrevented(Material material) {
        if (!isPlugin(PLUGIN_NAME))
            return false;

        String name = material.name();
        return name.endsWith("_LOG") || name.endsWith("_LEAVES");
    }

    /**
     * @return TRUE if an mcMMO block-break ability is active;
     * FALSE if API is available and reports no active ability;
     * NULL if API is unavailable or could not be queried reliably.
     */
    private static Boolean isBlockBreakAbilityActive(Player player) {
        Method[] methods = getBlockBreakAbilityMethods();
        if (methods == null || methods.length == 0)
            return null;

        boolean hadUnknown = false;
        for (Method method : methods) {
            Boolean active = invokeAbilityMethod(method, player);
            if (Boolean.TRUE.equals(active))
                return true;
            if (active == null)
                hadUnknown = true;
        }

        // If any call failed/was unknown, be safe and fall back to legacy checks.
        return hadUnknown ? null : false;
    }

    private static Method[] getBlockBreakAbilityMethods() {
        if (abilityApiInitialized)
            return blockBreakAbilityMethods;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAbilityApiInitAttempt < ABILITY_API_RETRY_DELAY_MS)
            return null;

        synchronized (McMMOHook.class) {
            if (abilityApiInitialized)
                return blockBreakAbilityMethods;

            currentTime = System.currentTimeMillis();
            if (currentTime - lastAbilityApiInitAttempt < ABILITY_API_RETRY_DELAY_MS)
                return null;
            lastAbilityApiInitAttempt = currentTime;

            Class<?> abilityApiClass = loadAbilityApiClass();
            if (abilityApiClass == null)
                return null;

            List<Method> methods = new ArrayList<>();
            for (String methodName : BLOCK_BREAK_ABILITY_METHODS) {
                try {
                    Method method = abilityApiClass.getMethod(methodName, Player.class);
                    methods.add(method);
                } catch (NoSuchMethodException ignored) {
                    // method not present in this mcMMO version
                }
            }

            blockBreakAbilityMethods = methods.isEmpty() ? null : methods.toArray(new Method[0]);
            abilityApiInitialized = true;
            return blockBreakAbilityMethods;
        }
    }

    private static Class<?> loadAbilityApiClass() {
        // Attempt 1: current classloader
        try {
            return Class.forName(ABILITY_API_CLASS);
        } catch (ClassNotFoundException ignored) {
        } catch (LinkageError ignored) {
            return null;
        }

        // Attempt 2: mcMMO plugin classloader (more robust on some servers)
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
            if (plugin == null)
                return null;
            ClassLoader classLoader = plugin.getClass().getClassLoader();
            return classLoader.loadClass(ABILITY_API_CLASS);
        } catch (ClassNotFoundException ignored) {
            return null;
        } catch (LinkageError ignored) {
            return null;
        }
    }

    private static Boolean invokeAbilityMethod(Method method, Player player) {
        try {
            Object result = method.invoke(null, player);
            if (result instanceof Boolean)
                return (Boolean) result;
            return null;
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException ignored) {
            return null;
        } catch (LinkageError ignored) {
            return null;
        }
    }

}
