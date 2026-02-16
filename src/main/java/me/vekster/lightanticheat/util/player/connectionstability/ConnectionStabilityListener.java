package me.vekster.lightanticheat.util.player.connectionstability;

import me.vekster.lightanticheat.event.playermove.LACAsyncPlayerMoveEvent;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionStabilityListener implements Listener {

    private static final ConcurrentHashMap<UUID, List<Integer>> PLAYERS = new ConcurrentHashMap<>();
    private static final long TARGET_WINDOW_MILLIS = 2000L;
    private static final long MIN_ELAPSED_MILLIS = 500L;
    private static final long MAX_ELAPSED_MILLIS = 15_000L;
    private static volatile long lastRotationTime = System.currentTimeMillis();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PLAYERS.put(event.getPlayer().getUniqueId(), createHistoryWindow());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PLAYERS.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onAsyncMovement(LACAsyncPlayerMoveEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        List<Integer> list = PLAYERS.get(uuid);
        if (list == null)
            return;
        synchronized (list) {
            if (list.isEmpty())
                list.addAll(Arrays.asList(0, 0, 0, 0));
            int lastIndex = list.size() - 1;
            list.set(lastIndex, list.get(lastIndex) + 1);
        }
    }

    public static void loadConnectionCalculator() {
        lastRotationTime = System.currentTimeMillis();
        Scheduler.runTaskTimer(() -> {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = clampElapsedTime(currentTime - lastRotationTime);
            lastRotationTime = currentTime;

            Set<UUID> onlinePlayers = new HashSet<>();
            for (Player player : Bukkit.getOnlinePlayers())
                onlinePlayers.add(player.getUniqueId());

            for (UUID onlinePlayer : onlinePlayers)
                PLAYERS.putIfAbsent(onlinePlayer, createHistoryWindow());

            PLAYERS.entrySet().removeIf(entry -> {
                if (!onlinePlayers.contains(entry.getKey()))
                    return true;
                List<Integer> list = entry.getValue();
                synchronized (list) {
                    if (list.isEmpty())
                        list.addAll(Arrays.asList(0, 0, 0, 0));

                    int lastIndex = list.size() - 1;
                    list.set(lastIndex, normalizeToTargetWindow(list.get(lastIndex), elapsedTime));
                    list.add(0);
                    list.remove(0);
                }
                return false;
            });
        }, 40, 40);
    }

    public static void loadConnectionCalculatorOnReload() {
        Set<UUID> onlinePlayers = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers())
            onlinePlayers.add(player.getUniqueId());

        lastRotationTime = System.currentTimeMillis();
        PLAYERS.keySet().removeIf(uuid -> !onlinePlayers.contains(uuid));
        for (UUID onlinePlayer : onlinePlayers)
            PLAYERS.putIfAbsent(onlinePlayer, createHistoryWindow());
    }


    private static List<Integer> createHistoryWindow() {
        return new ArrayList<>(Arrays.asList(0, 0, 0, 0));
    }


    private static long clampElapsedTime(long elapsedTime) {
        if (elapsedTime <= 0)
            return MIN_ELAPSED_MILLIS;
        return Math.min(MAX_ELAPSED_MILLIS, Math.max(MIN_ELAPSED_MILLIS, elapsedTime));
    }

    private static int normalizeToTargetWindow(int value, long elapsedTime) {
        if (elapsedTime <= 0)
            return value;
        double normalized = value * (TARGET_WINDOW_MILLIS / (double) elapsedTime);
        return (int) Math.max(0, Math.round(normalized));
    }

    public static ConnectionStability getConnectionStability(Player player) {
        return getConnectionStability(player.getUniqueId());
    }

    public static ConnectionStability getConnectionStability(UUID uuid) {
        List<Integer> list = PLAYERS.get(uuid);
        if (list == null)
            return ConnectionStability.HIGH;

        int maxVal = 0;
        synchronized (list) {
            for (int value : list)
                if (value > maxVal)
                    maxVal = value;
        }

        int max = (int) Math.floor(maxVal / 2.0);
        if (max <= 25)
            return ConnectionStability.HIGH;
        else if (max <= 50)
            return ConnectionStability.MEDIUM;
        else
            return ConnectionStability.LOW;
    }

}
