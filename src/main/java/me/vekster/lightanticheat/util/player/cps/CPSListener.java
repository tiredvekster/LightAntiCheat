package me.vekster.lightanticheat.util.player.cps;

import me.vekster.lightanticheat.util.detection.CheckUtil;
import me.vekster.lightanticheat.util.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CPSListener implements Listener {

    private static final Map<UUID, List<Integer>> PLAYERS = new ConcurrentHashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PLAYERS.put(event.getPlayer().getUniqueId(), new LinkedList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (CheckUtil.isExternalNPC(event)) return;
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.LEFT_CLICK_AIR)
            return;
        UUID uuid = event.getPlayer().getUniqueId();
        if (!PLAYERS.containsKey(uuid))
            return;
        List<Integer> list = PLAYERS.get(uuid);
        list.set(list.size() - 1, list.get(list.size() - 1) + 1);
    }

    public static void loadCpsCalculator() {
        Scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                PLAYERS.entrySet().removeIf(entry -> {
                    if (Bukkit.getPlayer(entry.getKey()) == null)
                        return true;
                    List<Integer> list = entry.getValue();
                    list.add(0);
                    list.remove(0);
                    return false;
                });
            }
        }, 1000, 1000);
    }

    public static void loadCpsCalculatorOnReload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PLAYERS.put(player.getUniqueId(), new LinkedList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        }
    }

    public static int getCps(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PLAYERS.containsKey(uuid))
            return 0;
        List<Integer> list = PLAYERS.get(uuid);
        return list.stream()
                .mapToInt(v -> v)
                .max().orElseThrow(NoSuchElementException::new);
    }

    public static int getCurrentCps(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PLAYERS.containsKey(uuid))
            return 0;
        List<Integer> list = PLAYERS.get(uuid);
        return list.get(list.size() - 1);
    }

}
